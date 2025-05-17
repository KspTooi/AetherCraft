package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.dto.GetThreadListDto;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatThreadPo 数据访问接口
 */
@Repository
public interface ChatThreadRepository extends JpaRepository<ChatThreadPo, Long>, JpaSpecificationExecutor<ChatThreadPo> {

    @Query("""
            SELECT DISTINCT COUNT(t.id)
            FROM ChatThreadPo t
            WHERE t.player.id = :playerId
            """)
    long getPlayerThreadCount(Long playerId);


    @Query("""
            SELECT ctp FROM ChatThreadPo ctp
            LEFT JOIN FETCH ctp.lastMessage
            LEFT JOIN ctp.npc npc
            WHERE
            (ctp.player.id = :#{#po.player.id})
            AND (ctp.user.id = :#{#po.user.id})
            AND (ctp.type = :#{#po.type})
            AND (:#{#po.npc.id} IS NULL OR npc.id = :#{#po.npc.id})
            ORDER BY ctp.updateTime DESC
            """)
    Page<ChatThreadPo> getThreadListWithLastMessage(ChatThreadPo po, Pageable page);



} 