package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRpSegmentPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRpSegmentRepository extends JpaRepository<ModelRpSegmentPo, Long> {
    
    /**
     * 根据会话ID删除所有片段
     * @param threadId 会话ID
     */
    @Modifying
    @Query("DELETE FROM ModelRpSegmentPo s WHERE s.thread.id = :threadId")
    void deleteByThreadId(@Param("threadId") Long threadId);
    
    /**
     * 查询会话的最大序号
     * @param threadId 会话ID
     * @return 最大序号，如果没有记录则返回0
     */
    @Query("SELECT COALESCE(MAX(s.sequence), 0) FROM ModelRpSegmentPo s WHERE s.thread.id = :threadId")
    int findMaxSequenceByThreadId(@Param("threadId") Long threadId);
} 