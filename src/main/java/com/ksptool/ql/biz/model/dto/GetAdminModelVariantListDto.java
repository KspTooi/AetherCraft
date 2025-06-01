package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetAdminModelVariantListDto extends PageQuery {

    //模糊筛选模型代码、名称、系列
    private String keyword;

    //启用状态 0:禁用 1:启用
    private Integer enabled;

}
