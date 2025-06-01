package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetModelVariantParamTemplateValueDetailsDto {

    // 模板值ID
    @NotNull(message = "模板值ID不能为空")
    private Long id;

} 