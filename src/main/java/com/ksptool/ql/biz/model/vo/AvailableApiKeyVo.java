package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 可用API密钥视图对象
 */
@Data
public class AvailableApiKeyVo {
    
    /**
     * API密钥ID
     */
    private Long apiKeyId;
    
    /**
     * 密钥名称
     */
    private String keyName;
    
    /**
     * 密钥类型
     */
    private String keyType;
    
    /**
     * 所属用户名
     */
    private String ownerUsername;
} 