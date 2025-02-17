package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class SavePanelGroupPermissionVo {
    // 权限ID
    private Long id;
    // 权限标识
    private String code;
    // 权限名称
    private String name;
    // 权限描述
    private String description;
    // 是否拥有该权限
    private Boolean hasPermission;
} 