package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRpThreadPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RP对话存档数据访问接口
 */
@Repository
public interface ModelRpThreadRepository extends JpaRepository<ModelRpThreadPo, Long> {
    
} 