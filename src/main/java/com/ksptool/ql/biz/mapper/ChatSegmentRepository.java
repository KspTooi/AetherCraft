package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ChatSegmentPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

/**
 * ChatSegmentPo 数据访问接口
 */
@Repository
public interface ChatSegmentRepository extends JpaRepository<ChatSegmentPo, Long>, JpaSpecificationExecutor<ChatSegmentPo> {


    @Modifying
    Long removeByThreadId(Long threadId);

} 