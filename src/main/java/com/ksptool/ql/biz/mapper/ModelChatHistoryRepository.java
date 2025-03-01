package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelChatHistoryPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ModelChatHistoryRepository extends JpaRepository<ModelChatHistoryPo, Long>, JpaSpecificationExecutor<ModelChatHistoryPo> {
    
    @Query("SELECT COALESCE(MAX(h.sequence), 0) FROM ModelChatHistoryPo h WHERE h.thread.id = :threadId")
    int findMaxSequenceByThreadId(@Param("threadId") Long threadId);
    
    /**
     * 删除指定会话ID的所有历史记录
     * @param threadId 会话ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ModelChatHistoryPo h WHERE h.thread.id = :threadId")
    void deleteByThreadId(@Param("threadId") Long threadId);
} 