package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ChatMessagePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * ChatMessagePo 数据访问接口
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessagePo, Long>, JpaSpecificationExecutor<ChatMessagePo> {

} 