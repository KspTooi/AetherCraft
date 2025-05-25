package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNpcListDto extends PageQuery {
    // 关键字搜索(角色名称或描述)
    private String keyword;

    //状态：0:禁用 1:启用
    private Integer status;
} 