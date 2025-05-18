package com.ksptool.ql.commons.utils.mccq;

import com.ksptool.ql.commons.exception.BizException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * 内存对话控制队列
 */
@Slf4j
public class MemoryChatControlQueue {

    //对话分片池大小
    private int FRAGMENT_POOL_SIZE = 4096;

    //对话分片生存时间TTL(秒)
    private int FRAGMENT_TTL = 120;

    //Stream队列生存时间TTL(秒)
    private int STREAM_TTL = 32;

    //对话分片获取超时时间(MS)
    private int FRAGMENT_NEXT_TIMEOUT = 1000 * 60;

    //响应流MAP ThreadId -> StreamId
    private final ConcurrentHashMap<Long,String> threadStreamMap = new ConcurrentHashMap<>();

    //记录每个StreamQueue的TTL
    private final ConcurrentHashMap<String, Integer> streamTtlMap = new ConcurrentHashMap<>();

    //对话分片池
    private final ConcurrentHashMap<String, LinkedBlockingQueue<ChatFragment>> threadFragmentPool = new ConcurrentHashMap<>();

    // 定时任务线程池，用于 TTL 管理
    @Getter
    private final ScheduledExecutorService ttlScheduler;

    public MemoryChatControlQueue() {
        // 初始化定时任务线程池，单个线程每秒执行 TTL 清理任务
        ttlScheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r);
            t.setName("TTL-Cleaner-Thread");
            t.setDaemon(true);
            return t;
        });
        // 每秒执行一次 TTL 递减和清理任务
        ttlScheduler.scheduleAtFixedRate(this::cleanExpiredFragments, 0, 1, TimeUnit.SECONDS);
    }


    public String openStream(Long tid) throws BizException{
        if(threadStreamMap.containsKey(tid)){
            throw new BizException("该会话正在处理中，请等待完成 ThreadId:"+tid);
        }
        var sid = UUID.randomUUID().toString();
        threadStreamMap.put(tid,sid);
        return sid;
    }

    public void closeStream(Long tid){
        threadStreamMap.remove(tid);
    }

    public boolean isStreamOpen(Long tid){
        return threadStreamMap.containsKey(tid);
    }
    public boolean isStreamOpen(Long tid,String sid){
        String streamId = threadStreamMap.get(tid);

        if(streamId == null){
            return false;
        }

        if(streamId.equals(sid)){
            return true;
        }
        return false;
    }

    /**
     * 处理数据入栈
     */
    public void receive(ChatFragment cf) {

        if(cf.getType() < 0 || cf.getPlayerId() < 1 || cf.getThreadId() < 1 || cf.getStreamId() == null){
            log.error("入栈失败,分片数据不正确 T:{} PID:{} TID:{} SID:{}", cf.getType(), cf.getPlayerId(), cf.getThreadId(),cf.getStreamId());
            return;
        }
        //判断流ID是否为当前线程正在使用的
        //String streamId = threadStreamMap.get(cf.getThreadId());

        //if(streamId == null){
        //    log.error("丢弃分片,该Thread未创建Stream T:{} PID:{} TID:{} SID:{}", cf.getType(), cf.getPlayerId(), cf.getThreadId(),cf.getStreamId());
        //    return;
        //}
        //if(!streamId.equals(cf.getStreamId())){
        //    log.error("丢弃分片,分片StreamId与Thread当前活跃的StreamId不一致 T:{} PID:{} TID:{} SID:{}", cf.getType(), cf.getPlayerId(), cf.getThreadId(),cf.getStreamId());
        //    return;
        //}

        LinkedBlockingQueue<ChatFragment> queue = threadFragmentPool.get(cf.getStreamId());

        if (queue == null) {
            queue = new LinkedBlockingQueue<>(FRAGMENT_POOL_SIZE);
            log.info("为Stream:{}创建队列...", cf.getStreamId());
            threadFragmentPool.put(cf.getStreamId(), queue);
            streamTtlMap.put(cf.getStreamId(), STREAM_TTL);
        }

        cf.setSeq(queue.size() + 1);
        cf.setTtl(FRAGMENT_TTL);
        queue.offer(cf);
        log.info("MCCQ入栈 tid:{} pid:{} stream:{} 队列大小:{}", cf.getThreadId(), cf.getPlayerId() ,cf.getStreamId(),queue.size() );
    }

    /**
     * 获取下一个对话帧
     */
    public ChatFragment next(String streamId) throws BizException,TimeoutException {

        if(!threadFragmentPool.containsKey(streamId)) {
            throw new BizException("对话帧获取失败,该Thread未在池中注册:" + streamId);
        }

        try{

            var queue = threadFragmentPool.get(streamId);
            ChatFragment poll = queue.poll(FRAGMENT_NEXT_TIMEOUT, TimeUnit.MILLISECONDS);

            if(poll == null){
                log.error("获取对话帧超时 ThreadId:{} 池大小:{}",streamId,queue.size());
                throw new TimeoutException("获取对话帧超时");
            }

            return poll;
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            throw new BizException("获取对话帧时出现错误.");
        }
    }

    public boolean hasNext(String streamId){
        LinkedBlockingQueue<ChatFragment> queue = threadFragmentPool.get(streamId);
        return queue != null && !queue.isEmpty();
    }

    /**
     * 清理过期分片（TTL <= 0 的元素）
     */
    private void cleanExpiredFragments() {
        try {
            log.debug("开始执行 TTL 清理任务，池中Stream数量: {}", threadFragmentPool.size());
            Iterator<java.util.Map.Entry<String, LinkedBlockingQueue<ChatFragment>>> poolIterator = threadFragmentPool.entrySet().iterator();

            while (poolIterator.hasNext()) {
                java.util.Map.Entry<String, LinkedBlockingQueue<ChatFragment>> entry = poolIterator.next();
                String streamId = entry.getKey();
                LinkedBlockingQueue<ChatFragment> queue = entry.getValue();

                if (queue.isEmpty()) {
                    // 队列为空，处理 Stream的TTL
                    int currentStreamTtl = streamTtlMap.getOrDefault(streamId, STREAM_TTL);
                    currentStreamTtl--;

                    if (currentStreamTtl < 1) {
                        // Stream TTL耗尽，移除Stream
                        poolIterator.remove(); // 从 threadFragmentPool 移除
                        streamTtlMap.remove(streamId); // 从 streamTtlMap 移除
                        log.info("移除空队列且TTL耗尽，StreamId: {}", streamId);
                    }
                    // 如果TTL仍然有效 (currentStreamTtl >= 1)
                    if (currentStreamTtl >= 1) {
                        streamTtlMap.put(streamId, currentStreamTtl);
                        log.debug("空队列StreamId: {} TTL扣减为: {}", streamId, currentStreamTtl);
                    }
                    continue; // 处理下一个Stream
                }

                // 队列不为空，重置Stream的TTL为最大值，并处理分片TTL
                streamTtlMap.put(streamId, STREAM_TTL); // 回满Stream TTL
                log.debug("非空队列StreamId: {} TTL已重置为: {}", streamId, STREAM_TTL);

                // 遍历队列并更新分片 TTL
                Iterator<ChatFragment> fragmentIterator = queue.iterator();
                while (fragmentIterator.hasNext()) {
                    ChatFragment fragment = fragmentIterator.next();
                    int newFragmentTtl = fragment.getTtl() - 1;
                    fragment.setTtl(newFragmentTtl);
                    if (newFragmentTtl <= 0) {
                        // 如果分片 TTL <= 0，移除分片
                        fragmentIterator.remove();
                        log.debug("移除过期分片，StreamId: {}, Fragment Seq: {}", streamId, fragment.getSeq());
                    }
                }

                // 记录分片清理后的状态
                if (queue.isEmpty()) {
                    // 如果清理后队列变空，其Stream TTL已在本轮设置为最大值，将在下一轮开始递减
                    log.debug("队列在清理分片后变为空，StreamId: {}. 其Stream TTL已重置.", streamId);
                } else {
                    log.debug("分片TTL清理完成，StreamId: {}, 队列当前大小: {}", streamId, queue.size());
                }
            }
        } catch (Exception e) {
            log.error("TTL 清理任务执行出错: {}", e.getMessage(), e);
        }
    }

    public void setFragmentPoolSize(int size) {
        FRAGMENT_POOL_SIZE = size;
    }

    public void setFragmentTtl(int ttl) {
        FRAGMENT_TTL = ttl;
    }

    public void setFragmentNextTimeout(int nextTimeout) {
        FRAGMENT_NEXT_TIMEOUT = nextTimeout;
    }

}
