package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRpThreadPo;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RP对话存档数据访问接口
 */
@Repository
public interface ModelRpThreadRepository extends JpaRepository<ModelRpThreadPo, Long> {
    
    /**
     * 查询模型角色的激活存档
     */
    @Query("""
           SELECT t FROM ModelRpThreadPo t
           LEFT JOIN FETCH t.histories h
           LEFT JOIN FETCH t.userRole
           LEFT JOIN FETCH t.modelRole
           WHERE t.modelRole.id = :modelRoleId AND t.active = 1
           ORDER BY h.sequence ASC
           """)
    ModelRpThreadPo getActiveThreadWithRoleAndHistories(@Param("modelRoleId") Long modelRoleId);

    /**
     * 批量设置用户的所有对话存档状态
     */
    @Query("""
            UPDATE ModelRpThreadPo t
            SET t.active = :active
            WHERE t.player.id = :playerId
            AND t.modelRole.id = :modelRoleId
            """)
    @Modifying
    void setAllThreadActive(@Param("playerId") Long playerId,
                          @Param("modelRoleId") Long modelRoleId, 
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
           SELECT t FROM ModelRpThreadPo t
           WHERE t.player.id = :playerId
           AND t.modelRole = :modelRole
           AND t.id != :threadId
           ORDER BY t.updateTime DESC
           LIMIT 1
           """)
    ModelRpThreadPo findTopByUserIdAndModelRoleAndIdNotOrderByUpdateTimeDesc(
        @Param("playerId") Long playerId,
        @Param("modelRole") ModelRolePo modelRole,
        @Param("threadId") Long threadId);

    @Query("""
            SELECT t FROM ModelRpThreadPo t
            LEFT JOIN FETCH t.modelRole mr
            LEFT JOIN FETCH t.userRole ur
            LEFT JOIN FETCH t.histories h
            WHERE t.id = :threadId
            AND t.player.id = :playerName
            ORDER BY h.sequence ASC
            """)
    ModelRpThreadPo getThreadWithRoleAndHistoriesById(
            @Param("threadId") Long threadId,
            @Param("playerName") Long playerName
    );
} 