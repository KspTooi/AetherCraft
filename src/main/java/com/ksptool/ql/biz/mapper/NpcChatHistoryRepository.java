package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.NpcChatHistoryPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RP对话历史记录数据访问接口
 */
@Repository
public interface NpcChatHistoryRepository extends JpaRepository<NpcChatHistoryPo, Long> {
    
    /**
     * 查找指定会话的最大序号
     * @param threadId 会话ID
     * @return 最大序号
     */
    @Query("""
            SELECT COALESCE(MAX(h.sequence), 0)
            FROM NpcChatHistoryPo h
            WHERE h.thread.id = :threadId
            """)
    int findMaxSequenceByThreadId(@Param("threadId") Long threadId);

    /**
     * 按序号顺序查找指定会话的所有历史记录
     * @param threadId 会话ID
     * @return 历史记录列表
     */
    List<NpcChatHistoryPo> findByThreadIdOrderBySequence(Long threadId);

    /**
     * 删除指定会话的所有历史记录
     * @param threadId 会话ID
     */
    @Modifying
    @Transactional
    @Query("""
            DELETE FROM NpcChatHistoryPo h
            WHERE h.thread.id = :threadId
            """)
    void deleteByThreadId(@Param("threadId") Long threadId);

    /**
     * 删除指定会话中序号大于给定值的所有历史记录
     * @param threadId 会话ID
     * @param sequence 序号
     */
    @Modifying
    @Transactional
    @Query("""
            DELETE FROM NpcChatHistoryPo h
            WHERE h.thread.id = :threadId
            AND h.sequence > :sequence
            """)
    void removeHistoryAfter(@Param("threadId") Long threadId, @Param("sequence") Integer sequence);

    /**
     * 获取指定会话中某类型的最后一条消息
     * @param threadId 会话ID
     * @param type 消息类型 0:用户消息 1:AI消息
     * @return 最后一条消息
     */
    @Query("""
            SELECT h FROM NpcChatHistoryPo h
            WHERE h.thread.id = :threadId
            AND h.type = :type
            ORDER BY h.sequence DESC 
            LIMIT 1
            """)
    NpcChatHistoryPo getLastMessage(@Param("threadId") Long threadId, @Param("type") Integer type);
} 