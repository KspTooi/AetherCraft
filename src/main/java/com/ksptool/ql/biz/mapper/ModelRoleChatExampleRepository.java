package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRoleChatExamplePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRoleChatExampleRepository extends JpaRepository<ModelRoleChatExamplePo, Long>, JpaSpecificationExecutor<ModelRoleChatExamplePo> {
    
    /**
     * 根据角色ID查询所有对话示例
     * @param modelRoleId 角色ID
     * @return 对话示例列表
     */
    @Query("""
        SELECT e FROM ModelRoleChatExamplePo e
        WHERE e.modelRole.id = :modelRoleId
        ORDER BY e.sortOrder ASC
    """)
    List<ModelRoleChatExamplePo> getByModelRoleId(@Param("modelRoleId") Long modelRoleId);
    
    /**
     * 根据角色ID删除所有对话示例
     * @param modelRoleId 角色ID
     * @return 删除的记录数量
     */
    @Modifying
    @Query("""
        DELETE FROM ModelRoleChatExamplePo e
        WHERE e.modelRole.id = :modelRoleId
    """)
    int removeByModelRoleId(@Param("modelRoleId") Long modelRoleId);
    
} 