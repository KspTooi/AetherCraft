package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyModelVariantParamTemplateToGlobalDto {
    
    // 模板ID
    @NotNull(message = "模板ID不能为空")
    private Long templateId;
    
    // 目标模型变体ID
    @NotNull(message = "模型变体ID不能为空")
    private Long modelVariantId;
    
} 