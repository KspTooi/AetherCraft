package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelChatSegmentPo;
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
     * 根据会话ID查询所有片段，按序号排序
     * @param threadId 会话ID
     * @return 片段列表
     */
    @Query("""
            SELECT s 
            FROM ModelChatSegmentPo s 
            WHERE s.thread.id = :threadId 
            ORDER BY s.sequence ASC
            """)
    List<ModelChatSegmentPo> findByThreadIdOrderBySequenceAsc(@Param("threadId") Long threadId);
    
    /**
     * 根据会话ID和状态查询片段，按序号排序
     * @param threadId 会话ID
     * @param status 状态
     * @return 片段列表
     */
    @Query("""
            SELECT s 
            FROM ModelChatSegmentPo s 
            WHERE s.thread.id = :threadId 
            AND s.status = :status 
            ORDER BY s.sequence ASC
            """)
    List<ModelChatSegmentPo> findByThreadIdAndStatusOrderBySequenceAsc(@Param("threadId") Long threadId, @Param("status") Integer status);
    
    /**
     * 根据会话ID和用户ID查询片段，按序号排序
     * @param threadId 会话ID
     * @param userId 用户ID
     * @return 片段列表
     */
    @Query("""
            SELECT s 
            FROM ModelChatSegmentPo s 
            WHERE s.thread.id = :threadId 
            AND s.userId = :userId 
            ORDER BY s.sequence ASC
            """)
    List<ModelChatSegmentPo> findByThreadIdAndUserIdOrderBySequenceAsc(@Param("threadId") Long threadId, @Param("userId") Long userId);
    
    /**
     * 更新会话片段状态
     * @param threadId 会话ID
     * @param status 状态
     * @return 更新的记录数
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE ModelChatSegmentPo s 
            SET s.status = :status 
            WHERE s.thread.id = :threadId
            """)
    int updateStatusByThreadId(@Param("threadId") Long threadId, @Param("status") Integer status);
    
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
     * @param threadId 会话ID
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("""
            DELETE FROM ModelChatSegmentPo s 
            WHERE s.thread.id = :threadId
            """)
    int deleteByThreadId(@Param("threadId") Long threadId);
    
    /**
     * 获取指定会话中未读取的片段数量
     * @param threadId 会话ID
     * @return 未读取的片段数量
     */
    @Query("""
            SELECT COUNT(s) 
            FROM ModelChatSegmentPo s 
            WHERE s.thread.id = :threadId 
            AND s.status = 0
            """)
    int countUnreadByThreadId(@Param("threadId") Long threadId);
    
    /**
     * 获取指定会话中下一个未读取的片段
     * @param threadId 会话ID
     * @return 下一个未读取的片段
     */
    @Query("""
            SELECT s 
            FROM ModelChatSegmentPo s 
            WHERE s.thread.id = :threadId 
            AND s.status = 0 
            ORDER BY s.sequence ASC
            """)
    List<ModelChatSegmentPo> findNextUnreadByThreadId(@Param("threadId") Long threadId);
    
    /**
     * 获取指定会话中所有未读取的片段，按序号排序
     * @param threadId 会话ID
     * @return 所有未读取的片段
     */
    @Query("""
            SELECT s 
            FROM ModelChatSegmentPo s 
            WHERE s.thread.id = :threadId 
            AND s.status = 0 
            ORDER BY s.sequence ASC
            """)
    List<ModelChatSegmentPo> findAllUnreadByThreadIdOrderBySequence(@Param("threadId") Long threadId);
} 