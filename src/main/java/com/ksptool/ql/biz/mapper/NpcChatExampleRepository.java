package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.NpcChatExamplePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NpcChatExampleRepository extends JpaRepository<NpcChatExamplePo, Long>, JpaSpecificationExecutor<NpcChatExamplePo> {
    
    /**
     * 根据角色ID查询所有对话示例
     * @param npcId 角色ID
     * @return 对话示例列表
     */
    @Query("""
        SELECT e FROM NpcChatExamplePo e
        WHERE e.npc.id = :npcId
        ORDER BY e.sortOrder ASC
    """)
    List<NpcChatExamplePo> getByNpcId(@Param("npcId") Long npcId);
    
    /**
     * 根据角色ID删除所有对话示例
     * @param npcId 角色ID
     * @return 删除的记录数量
     */
    @Modifying
    @Query("""
        DELETE FROM NpcChatExamplePo e
        WHERE e.npc.id = :npcId
    """)
    int removeByNpcId(@Param("npcId") Long npcId);
    
} 