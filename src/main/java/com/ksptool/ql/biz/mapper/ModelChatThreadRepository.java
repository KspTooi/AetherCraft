package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelChatThreadPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelChatThreadRepository extends JpaRepository<ModelChatThreadPo, Long>, JpaSpecificationExecutor<ModelChatThreadPo> {
    
    @Query("SELECT COUNT(t) FROM ModelChatThreadPo t WHERE t.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM ModelChatThreadPo t LEFT JOIN FETCH t.histories h WHERE t.id = :threadId ORDER BY h.sequence ASC")
    ModelChatThreadPo findByIdWithHistories(@Param("threadId") Long threadId);
} 