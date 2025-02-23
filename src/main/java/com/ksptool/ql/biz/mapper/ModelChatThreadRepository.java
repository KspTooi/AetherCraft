package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelChatThreadPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelChatThreadRepository extends JpaRepository<ModelChatThreadPo, Long>, JpaSpecificationExecutor<ModelChatThreadPo> {

} 