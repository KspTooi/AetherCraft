package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.PlayerDefaultGroupPo;
import com.ksptool.ql.biz.model.vo.GetPlayerDefaultGroupListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerDefaultGroupRepository extends JpaRepository<PlayerDefaultGroupPo, Long> {

    
    @Query("""
            SELECT NEW com.ksptool.ql.biz.model.vo.GetPlayerDefaultGroupListVo(
                pdg.id,
                g.code,
                g.name,
                SIZE(g.users),
                SIZE(g.permissions),
                g.isSystem,
                g.status,
                pdg.createTime
            )
            FROM PlayerDefaultGroupPo pdg
            LEFT JOIN pdg.group g
            ORDER BY pdg.createTime DESC
            """)
    Page<GetPlayerDefaultGroupListVo> getPlayerDefaultGroupList(Pageable pageable);

    List<PlayerDefaultGroupPo> findByGroup_IdIn(List<Long> groupIds);

}
