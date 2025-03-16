package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRpHistoryPo;
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
public interface ModelRpHistoryRepository extends JpaRepository<ModelRpHistoryPo, Long> {
    
    /**
     * 查找指定会话的最大序号
     * @param threadId 会话ID
     * @return 最大序号
     */
    @Query("SELECT COALESCE(MAX(h.sequence), 0) FROM ModelRpHistoryPo h WHERE h.thread.id = :threadId")
    int findMaxSequenceByThreadId(@Param("threadId") Long threadId);

    /**
     * 按序号顺序查找指定会话的所有历史记录
     * @param threadId 会话ID
     * @return 历史记录列表
     */
    List<ModelRpHistoryPo> findByThreadIdOrderBySequence(Long threadId);

    /**
     * 查找指定会话的最后一条历史记录
     * @param threadId 会话ID
     * @return 最后一条历史记录
     */
    @Query("SELECT h FROM ModelRpHistoryPo h WHERE h.thread.id = :threadId ORDER BY h.sequence DESC LIMIT 1")
    ModelRpHistoryPo findFirstByThreadIdOrderBySequenceDesc(@Param("threadId") Long threadId);
    
    /**
     * 删除指定会话的所有历史记录
     * @param threadId 会话ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ModelRpHistoryPo h WHERE h.thread.id = :threadId")
    void deleteByThreadId(@Param("threadId") Long threadId);
} 