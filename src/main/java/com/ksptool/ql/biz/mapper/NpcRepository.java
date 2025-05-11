package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.NpcPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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


} 