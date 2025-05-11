package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ChatThreadPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * ChatThreadPo 数据访问接口
 */
@Repository
public interface ChatThreadRepository extends JpaRepository<ChatThreadPo, Long>, JpaSpecificationExecutor<ChatThreadPo> {

} 