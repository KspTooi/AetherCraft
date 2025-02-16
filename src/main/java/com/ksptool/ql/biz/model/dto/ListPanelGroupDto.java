package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

// 用户组列表查询参数
@Data
public class ListPanelGroupDto {

    // 用户组名称
    private String name;

    // 用户组标识
    private String code;

    // 用户组描述
    private String description;

    // 页码
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    // 每页大小
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

}