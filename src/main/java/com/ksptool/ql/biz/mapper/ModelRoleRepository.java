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
     * 根据关键字查询模型角色（分页）
     * 
     * @param keyword 关键字（角色名称或描述）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT r FROM ModelRolePo r WHERE " +
           "(:keyword IS NULL OR r.name LIKE %:keyword% OR r.description LIKE %:keyword%) " +
           "ORDER BY r.sortOrder ASC, r.updateTime DESC")
    Page<ModelRolePo> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据ID查询模型角色（包含对话示例）
     * 
     * @param id 角色ID
     * @return 角色对象
     */
    @Query("SELECT r FROM ModelRolePo r LEFT JOIN FETCH r.chatTemplates WHERE r.id = :id")
    ModelRolePo findByIdWithTemplates(@Param("id") Long id);
    
    /**
     * 检查角色名称是否已存在（新增时使用）
     * 
     * @param name 角色名称
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM ModelRolePo r WHERE r.name = :name")
    boolean existsByName(@Param("name") String name);
    
    /**
     * 检查角色名称是否已被其他角色使用（更新时使用）
     * 
     * @param name 角色名称
     * @param id 当前角色ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM ModelRolePo r WHERE r.name = :name AND r.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
} 