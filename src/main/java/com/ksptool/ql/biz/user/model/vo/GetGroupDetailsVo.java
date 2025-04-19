package com.ksptool.ql.biz.user.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class GetGroupDetailsVo {
    
    // 组ID
    private Long id;

    // 组标识
    private String code;

    // 组名称
    private String name;

    // 组描述
    private String description;

    // 是否系统内置组
    private Boolean isSystem;

    // 组状态：0-禁用，1-启用
    private Integer status;

    // 排序号
    private Integer sortOrder;

    // 权限节点列表
    private List<GroupPermissionDefinitionVo> permissions;
}
