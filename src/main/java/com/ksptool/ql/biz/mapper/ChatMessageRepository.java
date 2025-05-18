package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ChatMessagePo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ChatMessagePo 数据访问接口
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessagePo, Long>, JpaSpecificationExecutor<ChatMessagePo> {

    @Modifying
    Long removeByThreadId(Long threadId);

    @Query("""
        SELECT COUNT(DISTINCT cm.id) FROM ChatMessagePo cm
        WHERE cm.thread.id = :tid
    """)
    int getCountByThreadId(@Param("tid") Long tid);


    Page<ChatMessagePo> getByThreadId(@Param("tid") Long tid, Pageable pageable);

    @Query("""
        DELETE ChatMessagePo cmp WHERE cmp.seq > :seq
    """)
    @Modifying
    void removeMessageAfterSeq(@Param("seq")Integer seq);

}