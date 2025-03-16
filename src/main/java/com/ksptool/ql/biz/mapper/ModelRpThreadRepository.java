package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRpThreadPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * RP对话存档数据访问接口
 */
@Repository
public interface ModelRpThreadRepository extends JpaRepository<ModelRpThreadPo, Long> {
    
    /**
     * 查询模型角色的激活存档
     */
    @Query("SELECT t FROM ModelRpThreadPo t " +
           "LEFT JOIN FETCH t.histories h " +
           "WHERE t.modelRole.id = :modelRoleId AND t.active = 1 " +
           "ORDER BY h.sequence ASC")
    ModelRpThreadPo findActiveThreadByModelRoleId(@Param("modelRoleId") Long modelRoleId);

    /**
     * 批量设置用户的所有对话存档状态
     */
    @Query("""
            UPDATE ModelRpThreadPo t
            SET t.active = :active
            WHERE t.userId = :userId
            AND t.modelRole.id = :modelRoleId
            """)
    @Modifying
    void setAllThreadActive(@Param("userId") Long userId, 
                          @Param("modelRoleId") Long modelRoleId, 
                          @Param("active") Integer active);
} 