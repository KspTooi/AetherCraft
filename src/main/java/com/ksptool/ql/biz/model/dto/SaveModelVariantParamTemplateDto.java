package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveModelVariantParamTemplateDto {
    
    // 模板ID（编辑时传入，新增时为null）
    private Long templateId;
    
    // 模板名称
    @NotBlank(message = "模板名称不能为空")
    private String name;
    
} 