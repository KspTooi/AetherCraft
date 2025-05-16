package com.ksptool.ql.commons.utils.mccq;

import com.ksptool.ql.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
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

    //对话分片获取超时时间(MS)
    private int FRAGMENT_NEXT_TIMEOUT = 1000 * 60;

    //对话线程状态MAP
    private ConcurrentHashMap<Long, Integer> threadStatusMap = new ConcurrentHashMap<>();

    //对话分片池
    private ConcurrentHashMap<Long, LinkedBlockingQueue<ChatFragment>> threadFragmentPool = new ConcurrentHashMap<>();

    // 定时任务线程池，用于 TTL 管理
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


    /**
     * 处理数据入栈
     */
    public void receive(ChatFragment cf) {

        if(cf.getType() < 0 || cf.getPlayerId() < 1 || cf.getThreadId() < 1){
            log.error("入栈失败,分片数据不正确 T:{} P:{} TH:{}", cf.getType(), cf.getPlayerId(), cf.getThreadId());
            return;
        }

        LinkedBlockingQueue<ChatFragment> queue = threadFragmentPool.get(cf.getThreadId());

        if (queue == null) {
            queue = new LinkedBlockingQueue<>(FRAGMENT_POOL_SIZE);
            log.info("为Thread:{}创建队列...", cf.getThreadId());
            threadFragmentPool.put(cf.getThreadId(), queue);
        }

        cf.setSeq(queue.size() + 1);
        cf.setTtl(FRAGMENT_TTL);
        queue.offer(cf);
    }

    /**
     * 获取下一个对话帧
     */
    public ChatFragment next(long threadId) throws BizException,TimeoutException {

        if(!threadFragmentPool.containsKey(threadId)) {
            throw new BizException("对话帧获取失败,该Thread未在池中注册:" + threadId);
        }

        try{

            var queue = threadFragmentPool.get(threadId);
            ChatFragment poll = queue.poll(FRAGMENT_NEXT_TIMEOUT, TimeUnit.MILLISECONDS);

            if(poll == null){
                log.error("获取对话帧超时 ThreadId:{} 池大小:{}",threadId,queue.size());
                throw new TimeoutException("获取对话帧超时");
            }

            return poll;
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            throw new BizException("获取对话帧时出现错误.");
        }
    }



    /**
     * 清理过期分片（TTL <= 0 的元素）
     */
    private void cleanExpiredFragments() {
        try {
            log.debug("开始执行 TTL 清理任务，池中线程数: {}", threadFragmentPool.size());
            for (var entry : threadFragmentPool.entrySet()) {
                long threadId = entry.getKey();
                LinkedBlockingQueue<ChatFragment> queue = entry.getValue();
                if (queue.isEmpty()) {
                    // 如果队列为空，考虑移除线程池中的 key（可选）
                    //threadFragmentPool.remove(threadId);
                    //log.debug("移除空队列，ThreadId: {}", threadId);
                    //continue;
                }
                // 遍历队列并更新 TTL
                Iterator<ChatFragment> iterator = queue.iterator();
                while (iterator.hasNext()) {
                    ChatFragment fragment = iterator.next();
                    int newTtl = fragment.getTtl() - 1;
                    fragment.setTtl(newTtl);
                    if (newTtl <= 0) {
                        // 如果 TTL <= 0，移除元素
                        iterator.remove();
                        log.debug("移除过期分片，ThreadId: {}", threadId);
                    }
                }
                log.debug("TTL 清理完成，ThreadId: {}, 队列大小: {}", threadId, queue.size());
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
