package com.ksptool.ql.biz.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ModelChatSegmentRepository extends JpaRepository<ModelChatSegmentPo, Long>, JpaSpecificationExecutor<ModelChatSegmentPo> {
    

    /**
     * 查询指定会话中的最大序号
     * @param threadId 会话ID
     * @return 最大序号
     */
    @Query("""
            SELECT COALESCE(MAX(s.sequence), 0)
            FROM ModelChatSegmentPo s
            WHERE s.thread.id = :threadId
            """)
    int findMaxSequenceByThreadId(@Param("threadId") Long threadId);
    
    /**
     * 删除指定会话的所有片段
     *
     * @param threadId 会话ID
     */
    @Modifying
    @Transactional
    @Query("""
            DELETE FROM ModelChatSegmentPo s
            WHERE s.thread.id = :threadId
            """)
    void deleteByThreadId(@Param("threadId") Long threadId);

    /**
     * 获取指定会话中所有未读取的片段，按序号排序
     * @param threadId 会话ID
     * @return 所有未读取的片段
     */
    @Query("""
            SELECT s
            FROM ModelChatSegmentPo s
            WHERE s.thread.id = :threadId
            AND s.player.id = :playerId
            AND s.status = 0
            ORDER BY s.sequence ASC
            """)
    List<ModelChatSegmentPo> findAllUnreadByThreadIdOrderBySequence(@Param("threadId") Long threadId,@Param("playerId")Long playerId);
} 