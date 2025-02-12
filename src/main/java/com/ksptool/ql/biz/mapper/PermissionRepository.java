package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.PermissionPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 权限数据访问接口
 */
@Repository
public interface PermissionRepository extends JpaRepository<PermissionPo, Long> {
    
    /**
     * 检查权限标识是否存在
     * @param code 权限标识
     * @return 是否存在
     */
    boolean existsByCode(String code);
} 