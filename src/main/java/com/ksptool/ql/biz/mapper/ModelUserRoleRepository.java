package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelUserRolePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 用户角色数据访问接口
 */
@Repository
public interface ModelUserRoleRepository extends JpaRepository<ModelUserRolePo, Long> {
    
    /**
     * 将该用户下的所有用户角色设置为非默认
     */
    @Modifying
    @Query("""
            UPDATE ModelUserRolePo r
            SET r.isDefault = 0
            WHERE r.userId = :userId
            """)
    void updateAllToNonDefault(@Param("userId") Long userId);
    
    /**
     * 根据角色名称查询角色
     * @param name 角色名称
     * @return 角色对象
     */
    ModelUserRolePo findByName(String name);
    
    /**
     * 根据角色名称查询角色（排除指定ID）
     * @param name 角色名称
     * @param id 排除的角色ID
     * @return 角色对象
     */
    @Query("""
            SELECT r 
            FROM ModelUserRolePo r 
            WHERE r.name = :name 
            AND r.id != :id
            """)
    ModelUserRolePo findByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
    
    /**
     * 检查指定用户下是否存在同名角色（排除指定ID）
     * @param userId 用户ID
     * @param name 角色名称
     * @param id 排除的角色ID
     * @return 角色数量
     */
    @Query("""
            SELECT COUNT(r) 
            FROM ModelUserRolePo r 
            WHERE r.userId = :userId 
            AND r.name = :name 
            AND r.id != :id
            """)
    long countByUserIdAndNameAndIdNot(@Param("userId") Long userId, @Param("name") String name, @Param("id") Long id);
} 