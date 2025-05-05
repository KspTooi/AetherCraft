package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.po.UserPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserPo, Long> {

    // 根据用户名查找用户
    UserPo findByUsername(String username);

    // 获取用户编辑视图，包含用户组信息
    @Query("""
            SELECT u 
            FROM UserPo u 
            LEFT JOIN FETCH u.groups 
            WHERE u.id = :id
            """)
    UserPo getEditView(@Param("id") Long id);

    // 获取用户的所有权限（通过用户组）
    @Query("""
            SELECT DISTINCT p
            FROM UserPo u
            JOIN u.groups g
            JOIN g.permissions p
            WHERE u.id = :userId
            """)
    List<PermissionPo> findUserPermissions(@Param("userId") Long userId);
}