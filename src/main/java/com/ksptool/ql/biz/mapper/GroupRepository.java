package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.GroupPo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupPo, Long>, JpaSpecificationExecutor<GroupPo> {
    
    /**
     * 按排序号升序查询所有用户组
     */
    List<GroupPo> findAllByOrderBySortOrderAsc(Pageable pageable);

    /**
     * 检查用户组标识是否存在
     */
    boolean existsByCode(String code);

    @EntityGraph(value = "with-permissions")
    GroupPo getGroupDetailsById(@Param("id") Long id);

    /**
     * 获取最大排序号
     * @return 最大排序号，如果没有记录则返回0
     */
    @Query("SELECT COALESCE(MAX(g.sortOrder), 0) FROM GroupPo g")
    Integer findMaxSortOrder();
}