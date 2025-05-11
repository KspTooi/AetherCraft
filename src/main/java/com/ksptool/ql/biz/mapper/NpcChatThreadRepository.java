package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.NpcChatThreadPo;
import com.ksptool.ql.biz.model.po.NpcPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * RP对话存档数据访问接口
 */
@Repository
public interface NpcChatThreadRepository extends JpaRepository<NpcChatThreadPo, Long> {
    
    /**
     * 查询模型角色的激活存档
     */
    @Query("""
           SELECT t FROM NpcChatThreadPo t
           LEFT JOIN FETCH t.histories h
           LEFT JOIN FETCH t.npc
           WHERE t.npc.id = :npcId AND t.player.id = :playerId AND t.active = 1
           ORDER BY h.sequence ASC
           """)
    NpcChatThreadPo getActiveThreadWithRoleAndHistories(@Param("npcId") Long npcId, @Param("playerId")Long playerId);

    /**
     * 批量设置用户的所有对话存档状态
     */
    @Query("""
            UPDATE NpcChatThreadPo t
            SET t.active = :active
            WHERE t.player.id = :playerId
            AND t.npc.id = :npcId
            """)
    @Modifying
    void setAllThreadActive(@Param("playerId") Long playerId,
                          @Param("npcId") Long npcId,
                          @Param("active") Integer active);
    
    /**
     * 查找用户除指定ID外最新更新的会话
     * 
     * @param playerId 玩家ID
     * @param modelRole 模型角色
     * @param threadId 要排除的会话ID
     * @return 最新更新的会话
     */
    @Query("""
           SELECT t FROM NpcChatThreadPo t
           WHERE t.player.id = :playerId
           AND t.npc = :modelRole
           AND t.id != :threadId
           ORDER BY t.updateTime DESC
           LIMIT 1
           """)
    NpcChatThreadPo findTopByUserIdAndModelRoleAndIdNotOrderByUpdateTimeDesc(
        @Param("playerId") Long playerId,
        @Param("modelRole") NpcPo modelRole,
        @Param("threadId") Long threadId);

    @Query("""
            SELECT t FROM NpcChatThreadPo t
            LEFT JOIN FETCH t.npc mr
            LEFT JOIN FETCH t.histories h
            WHERE t.id = :threadId
            AND t.player.id = :playerName
            ORDER BY h.sequence ASC
            """)
    NpcChatThreadPo getThreadWithRoleAndHistoriesById(
            @Param("threadId") Long threadId,
            @Param("playerName") Long playerName
    );
} 