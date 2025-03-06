package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Date;

/**
 * API密钥授权保存视图对象
 */
@Data
public class SaveApiKeyAuthVo {
    
    // 授权ID
    private Long id;
    
    // API密钥ID
    private Long apiKeyId;
    
    // 被授权人用户名
    private String authorizedUserName;
    
    // 使用次数限制
    private Long usageLimit;
    
    // 已使用次数
    private Long usageCount;
    
    // 过期时间
    private Date expireTime;
    
    // 状态：0-禁用，1-启用
    private Integer status;
} 