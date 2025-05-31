package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelSeriesPo;
import com.ksptool.ql.biz.model.vo.GetAdminModelSeriesListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelSeriesRepository extends JpaRepository<ModelSeriesPo, Long>{

    @Query("""
            SELECT m FROM ModelSeriesPo m
            WHERE (:keyword IS NULL OR :keyword = '' OR 
                   m.code LIKE CONCAT('%', :keyword, '%') OR 
                   m.name LIKE CONCAT('%', :keyword, '%') OR 
                   m.series LIKE CONCAT('%', :keyword, '%'))
            AND (:enabled IS NULL OR m.enabled = :enabled)
            ORDER BY m.seq ASC, m.createTime DESC
            """)
    Page<ModelSeriesPo> getAdminModelSeriesList(
            @Param("keyword") String keyword,
            @Param("enabled") Integer enabled,
            Pageable pageable
    );

    // 检查模型代码是否已存在（用于新增时验证）
    boolean existsByCode(String code);

    // 检查模型代码是否已存在（排除指定ID，用于编辑时验证）
    boolean existsByCodeAndIdNot(String code, Long id);

}
