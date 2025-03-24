package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ApiKeyAuthorizationPo;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.vo.ListApiKeyAuthVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
    @Query("""
            SELECT COUNT(a) > 0 
            FROM ApiKeyAuthorizationPo a 
            WHERE a.apiKey.id = :apiKeyId 
            AND a.authorizedUserId = :authorizedUserId 
            AND (:excludeId IS NULL OR a.id != :excludeId)
            """)
    boolean existsByApiKeyIdAndAuthorizedUserId(
        @Param("apiKeyId") Long apiKeyId,
        @Param("authorizedUserId") Long authorizedUserId,
        @Param("excludeId") Long excludeId
    );

    /**
     * 根据被授权用户ID和状态查询授权记录
     */
    @Query("""
            SELECT a FROM ApiKeyAuthorizationPo a
            WHERE a.authorizedUserId = :userId
            AND a.status = :status
            """)
    List<ApiKeyAuthorizationPo> findByAuthorizedUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") Integer status);

    /**
     * 删除指定API密钥的所有授权记录
     */
    @Modifying
    @Query("""
            DELETE FROM ApiKeyAuthorizationPo a 
            WHERE a.apiKey.id = :apiKeyId
            """)
    void deleteByApiKeyId(@Param("apiKeyId") Long apiKeyId);

    /**
     * 统计指定用户和API密钥的授权记录数量
     * @param userId 被授权用户ID
     * @param apiKeyId API密钥ID
     * @param status 授权状态(1:有效 0:无效)
     * @return 符合条件的授权记录数量
     */
    @Query("""
            SELECT COUNT(a) FROM ApiKeyAuthorizationPo a 
            WHERE a.authorizedUserId = :userId 
            AND a.apiKey.id = :apiKeyId 
            AND a.status = :status
            """)
    long countByAuthorized(
            @Param("userId") Long userId,
            @Param("apiKeyId") Long apiKeyId,
            @Param("status") Integer status);

    /**
     * 根据API密钥ID和被授权用户ID查询授权记录
     */
    @Query("""
            SELECT a FROM ApiKeyAuthorizationPo a 
            WHERE a.apiKey.id = :apiKeyId 
            AND a.authorizedUserId = :authorizedUserId 
            AND a.status = 1
            """)
    ApiKeyAuthorizationPo findByApiKeyIdAndAuthorizedUserId(
            @Param("apiKeyId") Long apiKeyId,
            @Param("authorizedUserId") Long authorizedUserId);

    /**
     * 根据被授权用户ID、状态和密钥系列查询API密钥
     * @param userId 被授权用户ID
     * @param status 授权状态(1:有效 0:无效)
     * @param series 密钥系列（可选）
     * @return 符合条件的API密钥列表
     */
    @Query("""
            SELECT k FROM ApiKeyPo k 
            JOIN ApiKeyAuthorizationPo a ON a.apiKey.id = k.id
            WHERE a.authorizedUserId = :userId 
            AND a.status = :status
            AND k.status = 1
            AND (:series IS NULL OR UPPER(k.keySeries) = UPPER(:series))
            """)
    List<ApiKeyPo> getApiKeyFromAuthorized(
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("series") String series);
} 