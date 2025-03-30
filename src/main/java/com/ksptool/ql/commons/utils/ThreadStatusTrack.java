package com.ksptool.ql.commons.utils;

import com.ksptool.ql.commons.exception.BizException;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadStatusTrack {

    // 使用线程安全的ConcurrentHashMap存储会话状态
    private static final ConcurrentHashMap<Long, Integer> threadStatusMap = new ConcurrentHashMap<>();
    
    // 使用线程安全的ConcurrentHashMap存储会话锁定状态
    private static final ConcurrentHashMap<Long, Boolean> threadLockMap = new ConcurrentHashMap<>();

    /**
     * 获取会话状态
     * @param threadId 会话线程ID
     * @return 0:正在等待响应(Pending) 1:正在回复(Receive) 2:已结束(Finished) 10:已失败(Failed) 11:已终止(Terminated) 12
     */
    public int getStatus(Long threadId){
        return threadStatusMap.getOrDefault(threadId, 2);
    }

    /**
     * 检查会话是否被锁定
     * @param threadId 会话线程ID
     * @return 如果会话被锁定返回true，否则返回false
     */
    public boolean isLocked(Long threadId){
        return threadLockMap.getOrDefault(threadId, false);
    }

    /**
     * 创建新会话，如果会话已存在且状态不为2(已结束)或10(已失败)或11(已终止)，则抛出异常
     * @param threadId 会话线程ID
     * @throws BizException 会话已存在且状态不为已结束或已失败或已终止时抛出异常
     */
    public void newSession(Long threadId) throws BizException {
        Integer currentStatus = threadStatusMap.get(threadId);
        
        if (currentStatus != null && currentStatus != 2 && currentStatus != 10 && currentStatus != 11) {
            throw new BizException("该会话正在处理中，请等待完成");
        }
        
        // 设置状态为等待响应
        threadStatusMap.put(threadId, 0);
        // 解锁会话
        threadLockMap.put(threadId, false);
    }

    /**
     * 通知会话开始接收回复
     * @param threadId 会话线程ID
     */
    public void notifyReceive(Long threadId){
        threadStatusMap.put(threadId, 1);
        threadLockMap.put(threadId, true);
    }

    /**
     * 通知会话已结束
     * @param threadId 会话线程ID
     */
    public void notifyFinished(Long threadId){
        threadStatusMap.put(threadId, 2);
        threadLockMap.put(threadId, false);
    }

    /**
     * 通知会话已失败
     * @param threadId 会话线程ID
     */
    public void notifyFailed(Long threadId){
        threadStatusMap.put(threadId, 10);
        threadLockMap.put(threadId, false);
    }

    /**
     * 通知会话已终止
     * @param threadId 会话线程ID
     */
    public void notifyTerminated(Long threadId){
        threadStatusMap.put(threadId, 11);
        threadLockMap.put(threadId, false);
    }
    
    /**
     * 关闭会话，只有在状态为2(已结束)、10(已失败)或11(已终止)时才可以关闭
     * @param threadId 会话线程ID
     * @return 如果关闭成功返回true，否则返回false
     * @throws BizException 如果会话不存在或状态不允许关闭时抛出异常
     */
    public boolean closeSession(Long threadId) throws BizException {
        Integer status = threadStatusMap.get(threadId);
        
        if (status == null) {
            throw new BizException("会话不存在");
        }
        
        if (status != 2 && status != 10 && status != 11) {
            throw new BizException("会话当前状态为" + status + "，无法关闭");
        }
        
        // 从映射中移除会话状态和锁定状态
        threadStatusMap.remove(threadId);
        threadLockMap.remove(threadId);
        return true;
    }
}
