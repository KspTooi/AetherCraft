package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetModelVariantParamDetailsDto {
    
    // 模型变体ID
    @NotNull(message = "模型变体ID不能为空")
    private Long modelVariantId;
    
    // 参数键
    @NotNull(message = "参数键不能为空")
    private String paramKey;
    
    // 是否为全局默认参数 0:否 1:是
    @NotNull(message = "参数类型标识不能为空")
    private Integer global;
    
} 