package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetModelVariantParamTemplateDetailsDto {
    
    // 模板ID
    @NotNull(message = "模板ID不能为空")
    private Long templateId;
    
} 