package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelUserRolePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 用户角色数据访问接口
 */
@Repository
public interface ModelUserRoleRepository extends JpaRepository<ModelUserRolePo, Long> {
    
    /**
     * 将所有角色设置为非默认
     */
    @Modifying
    @Query("UPDATE ModelUserRolePo r SET r.isDefault = 0")
    void updateAllToNonDefault();
} 