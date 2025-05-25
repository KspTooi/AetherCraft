package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.NpcPo;
import com.ksptool.ql.biz.model.vo.GetNpcListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模型角色数据访问接口
 */
@Repository
public interface NpcRepository extends JpaRepository<NpcPo, Long> {

    /**
     * 检查角色名称是否已被其他角色使用（更新时使用）
     * 
     * @param name 角色名称
     * @param id 当前角色ID
     * @return 是否存在
     */
    @Query("""
            SELECT COUNT(r) > 0
            FROM NpcPo r
            WHERE r.name = :name
            AND r.player.id = :playerId
            AND r.id != :id
            """)
    boolean existsByNameAndIdNot(@Param("playerId") Long playerId,@Param("name") String name, @Param("id") Long id);

    @Query("""
            SELECT new com.ksptool.ql.biz.model.vo.GetNpcListVo(
                n.id,
                n.name,
                n.avatarUrl,
                CAST((SELECT COUNT(t) FROM ChatThreadPo t WHERE t.npc.id = n.id) AS Integer),
                n.active
            )
            FROM NpcPo n
            WHERE n.player.id = :playerId
            AND (:keyword IS NULL OR :keyword = '' OR n.name LIKE CONCAT('%', :keyword, '%'))
            ORDER BY n.seq ASC
            """)
    Page<GetNpcListVo> getNpcList(@Param("keyword") String keyword,
                           @Param("playerId") Long playerId,
                           Pageable pageable);

    @Query("""
        UPDATE NpcPo SET active = 1
        WHERE id = :npcId
    """)
    @Modifying
    void activeNpc(@Param("npcId")Long npcId);

    @Query("""
        UPDATE NpcPo SET active = 0
        WHERE player.id = :playerId
    """)
    @Modifying
    void deActiveAllNpc(@Param("playerId")Long playerId);

} 