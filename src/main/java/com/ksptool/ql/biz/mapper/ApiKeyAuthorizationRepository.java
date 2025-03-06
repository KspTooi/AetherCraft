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
        WHERE a.apiKeyId = :apiKeyId
        AND (:authorizedUserName IS NULL OR u.username LIKE %:authorizedUserName%)
        ORDER BY a.updateTime DESC
    """)
    Page<ListApiKeyAuthVo> findAuthList(
        @Param("apiKeyId") Long apiKeyId,
        @Param("authorizedUserName") String authorizedUserName,
        Pageable pageable
    );
} 