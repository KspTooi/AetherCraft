package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelVariantParamTemplateValuePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelVariantParamTemplateValueRepository extends JpaRepository<ModelVariantParamTemplateValuePo, Long> {

    /**
     * 查询指定模板的所有参数值，按排序号排序
     */
    @Query("""
            SELECT v FROM ModelVariantParamTemplateValuePo v
            WHERE v.template.id = :templateId
            ORDER BY v.seq ASC, v.createTime ASC
            """)
    List<ModelVariantParamTemplateValuePo> findByTemplateIdOrderBySeq(@Param("templateId") Long templateId);

} 