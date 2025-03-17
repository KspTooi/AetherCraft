package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ConfigPo;
import com.ksptool.ql.biz.model.vo.ListPanelConfigVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigPo, Long> {
    
    ConfigPo findByConfigKey(String configKey);
    
    ConfigPo findByUserIdAndConfigKey(Long userId, String configKey);
    
    boolean existsByConfigKey(String configKey);
    
    boolean existsByUserIdAndConfigKey(Long userId, String configKey);

    @Query("""
            SELECT new com.ksptool.ql.biz.model.vo.ListPanelConfigVo(
                c.id, c.userId, u.username, c.configKey,
                c.configValue, c.description, c.createTime, c.updateTime
            )
            FROM ConfigPo c
            LEFT JOIN UserPo u ON c.userId = u.id
            WHERE (TRIM(COALESCE(:keyOrValue, '')) = '' OR c.configKey LIKE %:keyOrValue% OR c.configValue LIKE %:keyOrValue%)
            AND (TRIM(COALESCE(:description, '')) = '' OR c.description LIKE %:description%)
            AND (:userId IS NULL OR c.userId = :userId)
            """)
    Page<ListPanelConfigVo> getListView(@Param("keyOrValue") String keyOrValue,
                                       @Param("description") String description,
                                       @Param("userId") Long userId,
                                       Pageable pageable);
} 