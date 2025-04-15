package com.ksptool.ql.biz.user.model.dto;

import lombok.Data;

@Data
public class GetGroupListDto {

    //模糊匹配 组标识、组名称、组描述
    private String keyword;

    //组状态：0:禁用 1:启用
    private Integer status;

}
