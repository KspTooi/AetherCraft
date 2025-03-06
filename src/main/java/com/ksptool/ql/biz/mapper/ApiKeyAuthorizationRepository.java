package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ApiKeyAuthorizationPo;
import com.ksptool.ql.biz.model.vo.ListApiKeyAuthVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyAuthorizationRepository extends JpaRepository<ApiKeyAuthorizationPo, Long> {

    @Query("""
        SELECT new com.ksptool.ql.biz.model.vo.ListApiKeyAuthVo(
            a.id,
            u.username,
            a.usageLimit,
            a.usageCount,
            a.expireTime,
            a.status
        )
        FROM ApiKeyAuthorizationPo a
        JOIN UserPo u ON u.id = a.authorizedUserId
        WHERE a.apiKey.id = :apiKeyId
        AND (:authorizedUserName IS NULL OR u.username LIKE %:authorizedUserName%)
        ORDER BY a.updateTime DESC
    """)
    Page<ListApiKeyAuthVo> findAuthList(
        @Param("apiKeyId") Long apiKeyId,
        @Param("authorizedUserName") String authorizedUserName,
        Pageable pageable
    );

    /**
     * 检查是否已存在授权记录
     * @param apiKeyId API密钥ID
     * @param authorizedUserId 被授权用户ID
     * @param excludeId 排除的授权ID（用于编辑时检查）
     * @return 是否存在
     */
    @Query("SELECT COUNT(a) > 0 FROM ApiKeyAuthorizationPo a " +
           "WHERE a.apiKey.id = :apiKeyId " +
           "AND a.authorizedUserId = :authorizedUserId " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean existsByApiKeyIdAndAuthorizedUserId(
        @Param("apiKeyId") Long apiKeyId,
        @Param("authorizedUserId") Long authorizedUserId,
        @Param("excludeId") Long excludeId
    );
} 