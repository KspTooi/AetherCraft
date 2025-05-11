package com.ksptool.ql.biz.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModelChatThreadRepository extends JpaRepository<ModelChatThreadPo, Long>, JpaSpecificationExecutor<ModelChatThreadPo> {
    
    @Query("""
            SELECT COUNT(t)
            FROM ModelChatThreadPo t
            WHERE t.player.id = :playerId
            """)
    long countByPlayerId(@Param("playerId") Long playerId);
    
    @Query("""
            SELECT t
            FROM ModelChatThreadPo t
            LEFT JOIN FETCH t.histories h
            WHERE t.id = :threadId
            ORDER BY h.sequence ASC
            """)
    ModelChatThreadPo findByIdWithHistories(@Param("threadId") Long threadId);
    
    /**
     * 查询玩家的所有会话，按更新时间倒序排序
     */
    @Query("""
            SELECT t
            FROM ModelChatThreadPo t
            WHERE t.player.id = :playerId
            ORDER BY t.updateTime DESC
            """)
    List<ModelChatThreadPo> findByPlayerIdOrderByUpdateTimeDesc(@Param("playerId") Long playerId);
} 