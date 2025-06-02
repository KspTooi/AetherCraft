package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ApplyModelVariantParamTemplateDto {
    
    // 模板ID
    @NotNull(message = "模板ID不能为空")
    private Long templateId;
    
    // 模型变体ID列表（支持批量应用）
    @NotEmpty(message = "模型变体ID列表不能为空")
    private List<Long> modelVariantIds;
    
    // 应用范围：0=个人参数, 1=全局参数
    @NotNull(message = "应用范围不能为空")
    private Integer global;
    
} 