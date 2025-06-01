package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetModelVariantParamListDto extends PageQuery {
    
    // 模型变体ID
    @NotNull(message = "模型变体ID不能为空")
    private Long modelVariantId;
    
    // 关键字查询
    private String keyword;

} 