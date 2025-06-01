package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ConfigPo;
import com.ksptool.ql.biz.model.vo.GetConfigListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigPo, Long> {


    @Query("""
        SELECT c FROM ConfigPo c
        WHERE c.player IS NULL AND c.configKey = :key
        """)
    ConfigPo getGlobalConfig(@Param("key") String key);


    boolean existsByPlayerIdAndConfigKey(Long playerId, String configKey);


    @Query("""
            SELECT new com.ksptool.ql.biz.model.vo.GetConfigListVo(
                c.id, cpl.name, c.configKey,
                c.configValue, c.description, c.createTime, c.updateTime
            )
            FROM ConfigPo c
            LEFT JOIN c.player cpl
            WHERE (:keyword IS NULL
                OR c.configKey LIKE %:keyword%
                OR c.configValue LIKE %:keyword%
                OR c.description LIKE %:keyword%
            )
            AND (:playerName IS NULL
                OR (cpl.name LIKE CONCAT('%', :playerName, '%') AND :playerName != '全局')
                OR (cpl IS NULL AND :playerName = '全局')
            )
            AND (:playerId IS NULL OR cpl.id = :playerId)
            ORDER BY c.configKey ASC,c.updateTime DESC
            """)
    Page<GetConfigListVo> getConfigList(@Param("keyword") String keyword,
                                        @Param("playerName") String playerName,
                                        @Param("playerId") Long playerId,
                                        Pageable pageable);

}