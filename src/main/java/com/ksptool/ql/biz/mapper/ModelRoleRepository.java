package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRolePo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 模型角色数据访问接口
 */
@Repository
public interface ModelRoleRepository extends JpaRepository<ModelRolePo, Long> {
    
    /**
     * 获取用户的角色列表（分页）
     * 
     * @param userId 用户ID
     * @param keyword 关键字（角色名称，为空时不过滤）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("""
            SELECT r 
            FROM ModelRolePo r 
            WHERE r.userId = :userId 
            AND (:keyword IS NULL OR :keyword = '' OR r.name LIKE %:keyword%) 
            ORDER BY r.sortOrder ASC, r.updateTime DESC
            """)
    Page<ModelRolePo> getModelRoleList(
        @Param("userId") Long userId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    
    /**
     * 检查角色名称是否已存在（新增时使用）
     * 
     * @param name 角色名称
     * @return 是否存在
     */
    @Query("""
            SELECT COUNT(r) > 0 
            FROM ModelRolePo r 
            WHERE r.name = :name
            """)
    boolean existsByName(@Param("name") String name);
    
    /**
     * 检查角色名称是否已被其他角色使用（更新时使用）
     * 
     * @param name 角色名称
     * @param id 当前角色ID
     * @return 是否存在
     */
    @Query("""
            SELECT COUNT(r) > 0 
            FROM ModelRolePo r 
            WHERE r.name = :name 
            AND r.userId = :userId 
            AND r.id != :id
            """)
    boolean existsByNameAndIdNot(@Param("userId") Long userId,@Param("name") String name, @Param("id") Long id);
} 