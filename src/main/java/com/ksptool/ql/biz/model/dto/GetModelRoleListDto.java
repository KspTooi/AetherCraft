package com.ksptool.ql.biz.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetModelRoleListDto extends PageQuery {
    // 关键字搜索(角色名称或描述)
    private String keyword;
} 