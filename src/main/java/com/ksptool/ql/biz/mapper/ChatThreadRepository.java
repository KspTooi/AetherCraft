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
       ctp.type = 0
       AND ctp.player.id = :pid
       AND ctp.user.id = :uid
    """)
    @Modifying
    void deActiveAllStandardThread(@Param("pid")Long pid,@Param("uid")Long uid);

    @Query("""
       UPDATE ChatThreadPo ctp SET ctp.active = 0
       WHERE
       ctp.type = 1
       AND ctp.npc.id = :npcId
    """)
    @Modifying
    void deActiveThreadByNpc(@Param("npcId")Long npcId);

    @Query("""
       UPDATE ChatThreadPo ctp SET ctp.active = 1
       WHERE ctp.id = :threadId
    """)
    @Modifying
    void activeThread(@Param("threadId")Long threadId);

    @Query("SELECT ctp FROM ChatThreadPo ctp WHERE ctp.id = :id")
    ChatThreadPo getThread(@Param("id") Long id);

    @Query("""
        SELECT ctp FROM ChatThreadPo ctp
        WHERE ctp.player.id = :playerId
          AND ctp.user.id = :userId
          AND ctp.npc.id = :npcId
          AND ctp.active = 1
    """)
    ChatThreadPo getActiveThreadByNpcId(@Param("npcId")Long npcId,
                                        @Param("playerId")Long playerId,
                                        @Param("userId")Long userId);

} 