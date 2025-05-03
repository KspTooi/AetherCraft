package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveConfigDto {
    // 主键ID，新增时为null
    private Long id;
    
    // 用户ID，可选，为null时表示全局配置
    private Long userId;
    
    // 配置键
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    
    // 配置值
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    
    // 配置描述
    private String description;
} 