package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 保存模型配置DTO
 */
@Data
public class SaveAdminModelConfigDto {
    
    // 模型代码
    @NotBlank(message = "请选择模型")
    private String modelCode;
    
    // API密钥ID
    private Long apiKeyId;
    
    // 全局代理配置
    private String globalProxyConfig;
    
    // 用户代理配置
    private String userProxyConfig;
    
    // 温度值
    @NotNull(message = "请设置温度值")
    @DecimalMin(value = "0.0", message = "温度值最小为0")
    @DecimalMax(value = "2.0", message = "温度值最大为2")
    private Double temperature;
    
    // 采样值
    @NotNull(message = "请设置Top P值")
    @DecimalMin(value = "0.0", message = "Top P最小为0")
    @DecimalMax(value = "1.0", message = "Top P最大为1")
    private Double topP;
    
    // Top K值
    @NotNull(message = "请设置Top K值")
    @Min(value = 1, message = "Top K最小为1")
    @Max(value = 100, message = "Top K最大为100")
    private Integer topK;
    
    // 最大输出长度
    @NotNull(message = "请设置最大输出长度")
    @Min(value = 1, message = "最大输出长度最小为1")
    @Max(value = 8192000, message = "最大输出长度最大为8192000")
    private Integer maxOutputTokens;
} 