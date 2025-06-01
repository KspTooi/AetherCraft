package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;

@Data
public class GetModelVariantParamTemplateListDto extends PageQuery {
    
    // 关键字查询（模板名称）
    private String keyword;
    
} 