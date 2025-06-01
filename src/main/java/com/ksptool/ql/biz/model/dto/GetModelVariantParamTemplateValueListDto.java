package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetModelVariantParamTemplateValueListDto extends PageQuery {

    // 所属模板ID
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    // 关键字查询（参数键或描述）
    private String keyword;

} 