package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.GroupPo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<GroupPo, Long> {
    
    /**
     * 按排序号升序查询所有用户组
     */
    List<GroupPo> findAllByOrderBySortOrderAsc(Pageable pageable);

    /**
     * 检查用户组标识是否存在
     */
    boolean existsByCode(String code);
} 