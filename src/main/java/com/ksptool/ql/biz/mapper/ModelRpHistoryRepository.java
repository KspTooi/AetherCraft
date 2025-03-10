package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRpHistoryPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RP对话历史记录数据访问接口
 */
@Repository
public interface ModelRpHistoryRepository extends JpaRepository<ModelRpHistoryPo, Long> {
    
    /**
     * 查询存档的所有历史记录
     */
    @Query("SELECT h FROM ModelRpHistoryPo h WHERE h.thread.id = :threadId ORDER BY h.sequence ASC")
    List<ModelRpHistoryPo> findByThreadIdOrderBySequence(@Param("threadId") Long threadId);
    
    /**
     * 查询会话的最大序号
     * @param threadId 会话ID
     * @return 最大序号，如果没有记录则返回0
     */
    @Query("SELECT COALESCE(MAX(h.sequence), 0) FROM ModelRpHistoryPo h WHERE h.thread.id = :threadId")
    int findMaxSequenceByThreadId(@Param("threadId") Long threadId);
} 