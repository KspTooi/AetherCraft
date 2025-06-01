package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelVariantParamTemplatePo;
import com.ksptool.ql.biz.model.vo.GetModelVariantParamTemplateListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelVariantParamTemplateRepository extends JpaRepository<ModelVariantParamTemplatePo, Long> {

    /**
     * 查询当前用户的模板列表，支持关键字搜索，按创建时间倒序
     */
    @Query("""
            SELECT new com.ksptool.ql.biz.model.vo.GetModelVariantParamTemplateListVo(
                t.id,
                t.name,
                CAST((SELECT COUNT(v) FROM ModelVariantParamTemplateValuePo v WHERE v.template.id = t.id) AS long),
                t.createTime,
                t.updateTime
            )
            FROM ModelVariantParamTemplatePo t
            WHERE t.user.id = :userId
            AND (:keyword IS NULL OR :keyword = '' OR t.name LIKE CONCAT('%', :keyword, '%'))
            ORDER BY t.createTime DESC
            """)
    Page<GetModelVariantParamTemplateListVo> getModelVariantParamTemplateListByUser(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 检查用户范围内模板名称是否已存在（支持排除指定模板ID）
     */
    @Query("""
            SELECT t FROM ModelVariantParamTemplatePo t
            WHERE t.user.id = :userId AND t.name = :name
            AND (:excludeTemplateId IS NULL OR t.id != :excludeTemplateId)
            """)
    Optional<ModelVariantParamTemplatePo> getTemplateByUserIdAndNameExcludeId(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("excludeTemplateId") Long excludeTemplateId
    );

    /**
     * 统计指定模板的参数值数量
     */
    @Query("""
            SELECT COUNT(v) FROM ModelVariantParamTemplateValuePo v 
            WHERE v.template.id = :templateId
            """)
    Long countTemplateValuesByTemplateId(@Param("templateId") Long templateId);

} 