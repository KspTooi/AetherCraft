package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ApiKeyPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyRepository extends JpaRepository<ApiKeyPo, Long> {

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