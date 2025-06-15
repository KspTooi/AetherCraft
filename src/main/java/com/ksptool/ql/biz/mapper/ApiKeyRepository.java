package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.dto.GetApiKeyListDto;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.vo.GetApiKeyListVo;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyPo, Long> {

    @Query(value = """
            SELECT new com.ksptool.ql.biz.model.vo.GetApiKeyListVo(
                k.id,
                k.keyName,
                k.keySeries,
                k.isShared,
                COUNT(DISTINCT auth.id),
                k.usageCount,
                k.lastUsedTime,
                k.status
            )
            FROM ApiKeyPo k
            LEFT JOIN ApiKeyAuthorizationPo auth ON auth.apiKey.id = k.id
            WHERE k.player.id = :playerId
            AND (:#{#dto.keyName} IS NULL OR k.keyName LIKE CONCAT('%', :#{#dto.keyName}, '%'))
            AND (:#{#dto.keySeries} IS NULL OR k.keySeries LIKE CONCAT('%', :#{#dto.keySeries}, '%'))
            AND (:#{#dto.status} IS NULL OR k.status = :#{#dto.status})
            GROUP BY k.id, k.keyName, k.keySeries, k.isShared, k.usageCount, k.lastUsedTime, k.status, k.updateTime
            ORDER BY k.updateTime DESC
            """)
    Page<GetApiKeyListVo> getApiKeyList(@Param("dto") GetApiKeyListDto dto, @Param("playerId") Long playerId, Pageable pageable);

    /**
     * 检查密钥名称在指定用户下是否重复
     * @param keyName 密钥名称
     * @param playerId 人物ID
     * @param excludeId 需要排除的密钥ID（可以为null）
     * @return 是否存在重复
     */
    @Query("""
        SELECT COUNT(k) > 0
        FROM ApiKeyPo k
        WHERE k.keyName = :keyName
        AND k.player.id = :playerId
        AND (:excludeId IS NULL OR k.id != :excludeId)
        """)
    boolean existsByKeyNameAndPlayerId(
        @Param("keyName") String keyName,
        @Param("playerId") Long playerId,
        @Param("excludeId") Long excludeId
    );
} 