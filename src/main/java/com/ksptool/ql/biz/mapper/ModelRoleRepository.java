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
} 