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

import java.util.Optional;

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
        DELETE ChatMessagePo cmp
        WHERE cmp.thread.id = :threadId AND cmp.seq > :seq
    """)
    @Modifying
    void removeMessageAfterSeq(@Param("threadId")Long threadId,@Param("seq")Integer seq);

    @Query("""
        SELECT cm FROM ChatMessagePo cm
        WHERE cm.thread.id = :tid
        ORDER BY cm.seq DESC
        LIMIT 1
    """)
    Optional<ChatMessagePo> getTopSeqMessageByThreadId(@Param("tid") Long tid);

    @Query("""
        SELECT cm FROM ChatMessagePo cm
        WHERE cm.senderRole = :senderRole AND cm.thread.id = :threadId
        ORDER BY cm.seq ASC
        LIMIT 1
    """)
    ChatMessagePo getSelfLastMessage(@Param("threadId")Long threadId,@Param("senderRole") Integer senderRole);

}