package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelApiKeyConfigPo;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模型API密钥配置数据访问层
 */
@Repository
public interface ModelApiKeyConfigRepository extends JpaRepository<ModelApiKeyConfigPo, Long>, JpaSpecificationExecutor<ModelApiKeyConfigPo> {
    
    /**
     * 根据模型代码查询配置
     */
    @Query("""
            SELECT m FROM ModelApiKeyConfigPo m
            WHERE m.modelCode = :modelCode AND m.player.id = :playerId
            """)
    ModelApiKeyConfigPo getByPlayerIdAnyModeCode(@Param("modelCode") String modelCode, @Param("playerId") Long playerId);
    
    /**
     * 根据API密钥ID查询配置
     */
    @Query("""
            SELECT m FROM ModelApiKeyConfigPo m 
            WHERE m.apiKeyId = :apiKeyId
            """)
    List<ModelApiKeyConfigPo> findByApiKeyId(@Param("apiKeyId") Long apiKeyId);
    
    /**
     * 根据模型代码和API密钥ID查询配置
     */
    @Query("""
            SELECT m FROM ModelApiKeyConfigPo m 
            WHERE m.modelCode = :modelCode 
            AND m.apiKeyId = :apiKeyId
            """)
    ModelApiKeyConfigPo findByModelCodeAndApiKeyId(
            @Param("modelCode") String modelCode, 
            @Param("apiKeyId") Long apiKeyId);
            
    /**
     * 删除指定API密钥的所有模型配置
     */
    @Modifying
    @Query("""
            DELETE FROM ModelApiKeyConfigPo m 
            WHERE m.apiKeyId = :apiKeyId
            """)
    void deleteByApiKey(@Param("apiKeyId") Long apiKeyId);
} 