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
     * 密钥系列
     */
    private String keySeries;
    
    /**
     * 所属人物名称
     */
    private String ownerPlayerName;
} 