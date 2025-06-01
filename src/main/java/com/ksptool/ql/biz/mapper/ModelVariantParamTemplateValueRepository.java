package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelVariantParamTemplateValuePo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    /**
     * 检查同一模板下参数键是否已存在
     */
    @Query("""
            SELECT v FROM ModelVariantParamTemplateValuePo v
            WHERE v.template.id = :templateId AND v.paramKey = :paramKey
            """)
    Optional<ModelVariantParamTemplateValuePo> findByTemplateIdAndParamKey(
            @Param("templateId") Long templateId,
            @Param("paramKey") String paramKey
    );

    /**
     * 检查同一模板下参数键是否已存在（排除指定ID）
     */
    @Query("""
            SELECT v FROM ModelVariantParamTemplateValuePo v
            WHERE v.template.id = :templateId AND v.paramKey = :paramKey
            AND v.id != :excludeId
            """)
    Optional<ModelVariantParamTemplateValuePo> findByTemplateIdAndParamKeyAndIdNot(
            @Param("templateId") Long templateId,
            @Param("paramKey") String paramKey,
            @Param("excludeId") Long excludeId
    );

    /**
     * 查询指定模板下最大的排序号
     */
    @Query("""
            SELECT MAX(v.seq) FROM ModelVariantParamTemplateValuePo v
            WHERE v.template.id = :templateId
            """)
    Long findMaxSeqByTemplateId(@Param("templateId") Long templateId);

    /**
     * 查询指定模板的参数值列表，支持关键字搜索和分页
     */
    @Query("""
            SELECT v FROM ModelVariantParamTemplateValuePo v
            WHERE v.template.id = :templateId
            AND (:keyword IS NULL OR :keyword = '' OR v.paramKey LIKE CONCAT('%', :keyword, '%') OR v.description LIKE CONCAT('%', :keyword, '%'))
            ORDER BY v.seq ASC, v.createTime ASC
            """)
    Page<ModelVariantParamTemplateValuePo> findByTemplateIdAndKeyword(
            @Param("templateId") Long templateId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
} 