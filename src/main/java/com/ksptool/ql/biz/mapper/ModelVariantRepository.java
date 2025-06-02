package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelVariantPo;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelVariantRepository extends JpaRepository<ModelVariantPo, Long>{

    @Query("""
            SELECT new com.ksptool.ql.biz.model.vo.GetAdminModelVariantListVo(
                m.id,
                m.code,
                m.name,
                m.type,
                m.series,
                m.thinking,
                m.scale,
                m.speed,
                m.intelligence,
                m.enabled,
                CAST(
                 (SELECT COUNT(mvp) FROM ModelVariantParamPo mvp
                 WHERE mvp.modelVariant.id = m.id AND mvp.user IS NULL AND mvp.player IS NULL) AS Integer),
                m.createTime,
                m.updateTime
            )
            FROM ModelVariantPo m
            WHERE (:keyword IS NULL OR :keyword = '' OR
                   m.code LIKE CONCAT('%', :keyword, '%') OR
                   m.name LIKE CONCAT('%', :keyword, '%') OR
                   m.series LIKE CONCAT('%', :keyword, '%'))
            AND (:enabled IS NULL OR m.enabled = :enabled)
            ORDER BY m.seq ASC, m.createTime DESC
            """)
    Page<GetAdminModelVariantListVo> getAdminModelVariantList(
            @Param("keyword") String keyword,
            @Param("enabled") Integer enabled,
            Pageable pageable
    );

    // 检查模型代码是否已存在（用于新增时验证）
    boolean existsByCode(String code);

    // 检查模型代码是否已存在（排除指定ID，用于编辑时验证）
    boolean existsByCodeAndIdNot(String code, Long id);

    // 查询所有启用的模型变体，按排序号和创建时间排序（供客户端使用）
    @Query("""
            SELECT m FROM ModelVariantPo m
            WHERE m.enabled = :enabled
            ORDER BY m.seq ASC, m.createTime DESC
            """)
    List<ModelVariantPo> findEnabledModelVariants(@Param("enabled") Integer enabled);

}
