package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存API密钥请求数据
 */
@Data
public class SaveApiKeyDto {
    
    /**
     * 密钥ID（新增时为null）
     */
    private Long id;
    
    /**
     * 密钥名称
     */
    @NotBlank(message = "密钥名称不能为空")
    private String keyName;
    
    /**
     * 密钥类型
     */
    @NotBlank(message = "密钥类型不能为空")
    private String keyType;
    
    /**
     * 密钥值
     */
    @NotBlank(message = "密钥值不能为空")
    private String keyValue;
    
    /**
     * 是否共享：0-不共享，1-共享
     */
    @NotNull(message = "共享状态不能为空")
    @Min(value = 0, message = "共享状态只能是0或1")
    @Max(value = 1, message = "共享状态只能是0或1")
    private Integer isShared;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status;
} 