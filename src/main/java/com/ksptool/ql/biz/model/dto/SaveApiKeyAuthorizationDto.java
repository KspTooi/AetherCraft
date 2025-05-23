package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * API密钥授权保存请求
 */
@Data
public class SaveApiKeyAuthorizationDto {
    
    // 授权ID
    private Long id;
    
    // API密钥ID
    @NotNull(message = "API密钥ID不能为空")
    private Long apiKeyId;
    
    // 被授权者人物名
    @NotBlank(message = "被授权者人物名称不能为空")
    private String authorizedPlayerName;
    
    // 使用次数限制
    private Long usageLimit;
    
    // 过期时间
    private Date expireTime;
    
    // 状态：0-禁用，1-启用
    private Integer status;
} 