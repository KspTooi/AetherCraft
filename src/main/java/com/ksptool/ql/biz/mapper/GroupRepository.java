package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.dto.ListPanelGroupDto;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.vo.ListPanelGroupVo;
import com.ksptool.ql.biz.model.dto.GetGroupListDto;
import com.ksptool.ql.biz.model.vo.GetGroupListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupPo, Long>, JpaSpecificationExecutor<GroupPo> {

    /**
     * 检查用户组标识是否存在
     */
    boolean existsByCode(String code);

    @EntityGraph(value = "with-permissions")
    GroupPo getGroupDetailsById(@Param("id") Long id);


    @Query("""
            SELECT g FROM GroupPo g
            LEFT JOIN FETCH g.permissions
            LEFT JOIN FETCH g.users
            WHERE g.id = :id
            """)
    GroupPo getGroupWithUserAndPermission(@Param("id") Long id);

    /**
     * 获取最大排序号
     * @return 最大排序号，如果没有记录则返回0
     */
    @Query("""
            SELECT COALESCE(MAX(g.sortOrder), 0) 
            FROM GroupPo g
            """)
    Integer findMaxSortOrder();

    /**
     * 查询用户组列表，并统计权限数量
     */
    @Query("""
            SELECT new com.ksptool.ql.biz.model.vo.ListPanelGroupVo(
                g.id,
                g.name,
                g.code,
                g.description,
                g.status,
                g.isSystem,
                g.sortOrder,
                CAST((SELECT COUNT(u) t FROM g.users u) AS int),
                CAST((SELECT COUNT(p) t FROM g.permissions p) AS int)
            )
            FROM GroupPo g
            WHERE (:#{#dto.name} IS NULL OR g.name LIKE %:#{#dto.name}%)
            AND (:#{#dto.code} IS NULL OR g.code LIKE %:#{#dto.code}%)
            AND (:#{#dto.description} IS NULL OR g.description LIKE %:#{#dto.description}%)
            """)
    Page<ListPanelGroupVo> getListView(@Param("dto") ListPanelGroupDto dto, Pageable pageable);

    @Query("""
            SELECT new com.ksptool.ql.biz.model.vo.GetGroupListVo(
                g.id,
                g.code,
                g.name,
                SIZE(g.users),
                SIZE(g.permissions),
                g.isSystem,
                g.status,
                g.createTime
            )
            FROM GroupPo g
            WHERE (:#{#dto.keyword} IS NULL OR g.code LIKE %:#{#dto.keyword}%
                OR g.name LIKE %:#{#dto.keyword}%
                OR g.description LIKE %:#{#dto.keyword}%)
            AND (:#{#dto.status} IS NULL OR g.status = :#{#dto.status})
            ORDER BY g.sortOrder ASC, g.id DESC
            """)
    Page<GetGroupListVo> getGroupList(@Param("dto") GetGroupListDto dto, Pageable pageable);

}