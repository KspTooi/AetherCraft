package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

/**
 * 保存模型配置DTO
 */
@Data
public class SaveModelConfigDto {
    
    // 模型代码
    @NotBlank(message = "请选择模型")
    private String model;
    
    // API密钥
    @NotBlank(message = "请输入API Key")
    private String apiKey;
    
    // 代理地址
    private String proxy;
    
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
} 