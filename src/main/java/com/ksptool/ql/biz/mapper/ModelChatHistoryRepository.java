package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelChatHistoryPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelChatHistoryRepository extends JpaRepository<ModelChatHistoryPo, Long>, JpaSpecificationExecutor<ModelChatHistoryPo> {

} 