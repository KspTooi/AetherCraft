package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRpHistoryPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RP对话历史记录数据访问接口
 */
@Repository
public interface ModelRpHistoryRepository extends JpaRepository<ModelRpHistoryPo, Long> {
    
} 