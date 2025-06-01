package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveModelVariantParamTemplateValueDto {

    // 模板值ID，新增时为null，编辑时必填
    private Long id;

    // 所属模板ID
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    // 参数键
    @NotNull(message = "参数键不能为空")
    private String paramKey;

    // 参数值
    @NotNull(message = "参数值不能为空")
    private String paramVal;

    // 参数类型 0:string 1:int 2:boolean
    @NotNull(message = "参数类型不能为空")
    private Integer type;

    // 参数描述
    private String description;

    // 排序号
    @NotNull(message = "排序号不能为空")
    private Integer seq;

} 