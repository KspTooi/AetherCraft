package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.NpcChatExamplePo;
import org.springframework.context.annotation.Lazy;
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

    /**
     * 获取指定NPC下一个对话示例的排序号
     * 如果没有现有记录，返回0，否则返回最大排序号+1
     * @param npcId 角色ID
     * @return 下一个排序号
     */
    @Query("""
        SELECT COALESCE(MAX(e.sortOrder), -1) + 1
        FROM NpcChatExamplePo e
        WHERE e.npc.id = :npcId
    """)
    int getNextOrder(@Param("npcId") Long npcId);
    
} 