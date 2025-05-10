package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRpSegmentPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRpSegmentRepository extends JpaRepository<ModelRpSegmentPo, Long> {

    /**
     * 删除指定会话的所有片段
     * @param threadId 会话ID
     */
    void deleteByThreadId(Long threadId);

    /**
     * 查找指定会话的最大序号
     * @param threadId 会话ID
     * @return 最大序号
     */
    @Query("""
            SELECT COALESCE(MAX(s.sequence), 0) 
            FROM ModelRpSegmentPo s 
            WHERE s.thread.id = :threadId
            """)
    int findMaxSequenceByThreadId(@Param("threadId") Long threadId);

    /**
     * 查找指定会话的所有未读片段，按序号排序
     * @param threadId 会话ID
     * @return 所有未读片段列表
     */
    @Query("""
            SELECT s
            FROM ModelRpSegmentPo s
            WHERE
            s.thread.id = :threadId
            AND s.player.id = :playerId
            AND s.status = 0
            ORDER BY s.sequence ASC
            """)
    List<ModelRpSegmentPo> findAllUnreadByThreadIdOrderBySequence(@Param("threadId") Long threadId,@Param("playerId") Long playerId);
} 