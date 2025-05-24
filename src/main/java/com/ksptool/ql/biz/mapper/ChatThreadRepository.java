package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.dto.GetThreadListDto;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatThreadPo 数据访问接口
 */
@Repository
public interface ChatThreadRepository extends JpaRepository<ChatThreadPo, Long>, JpaSpecificationExecutor<ChatThreadPo> {

    @Query("""
            SELECT DISTINCT COUNT(t.id)
            FROM ChatThreadPo t
            WHERE t.player.id = :playerId
            """)
    long getPlayerThreadCount(Long playerId);


    @Query("""
            SELECT ctp FROM ChatThreadPo ctp
            LEFT JOIN FETCH ctp.lastMessage
            LEFT JOIN ctp.npc npc
            WHERE
            ctp.player.id = :playerId
            AND ctp.user.id = :userId
            AND ctp.type = :type
            AND (:npcId IS NULL OR npc.id = :npcId)
            ORDER BY ctp.createTime DESC
            """)
    Page<ChatThreadPo> getThreadListWithLastMessage(@Param("playerId") Long playerId,
                                                   @Param("userId") Long userId,
                                                   @Param("npcId") Long npcId,
                                                   @Param("type") Integer type,
                                                   Pageable page);

    @Query("""
       UPDATE ChatThreadPo ctp SET ctp.active = 0
       WHERE
       ctp.type = :type
       AND ctp.id != :elseThreadId
    """)
    @Modifying
    void deActiveAllThread(@Param("elseThreadId") Long elseThreadId,@Param("type") Integer type);


    @Query("SELECT ctp FROM ChatThreadPo ctp WHERE ctp.id = :id")
    ChatThreadPo getThread(@Param("id") Long id);

} 