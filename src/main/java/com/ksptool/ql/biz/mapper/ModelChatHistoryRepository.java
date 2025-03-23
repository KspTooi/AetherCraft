package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelChatHistoryPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    
    /**
     * 获取指定会话中最后一条指定类型的消息
     * @param threadId 会话ID
     * @param role 消息类型/角色 (0-用户消息, 1-AI消息)
     * @return 最后一条消息
     */
    @Query("SELECT h FROM ModelChatHistoryPo h WHERE h.thread.id = :threadId AND h.role = :role ORDER BY h.sequence DESC LIMIT 1")
    ModelChatHistoryPo getLastMessage(@Param("threadId") Long threadId, @Param("role") Integer role);
    
    /**
     * 获取指定会话ID的所有历史记录，按序列号排序
     * @param threadId 会话ID
     * @return 排序后的历史记录列表
     */
    @Query("SELECT h FROM ModelChatHistoryPo h WHERE h.thread.id = :threadId ORDER BY h.sequence ASC")
    List<ModelChatHistoryPo> getByThreadId(@Param("threadId") Long threadId);
    
    /**
     * 删除指定会话指定序号之后的所有历史记录
     * @param threadId 会话ID
     * @param sequence 序号
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ModelChatHistoryPo h WHERE h.thread.id = :threadId AND h.sequence > :sequence")
    void removeHistoryAfter(@Param("threadId") Long threadId, @Param("sequence") Integer sequence);
} 