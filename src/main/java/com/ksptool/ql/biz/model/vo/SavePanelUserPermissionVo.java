package com.ksptool.ql.biz.model.vo;

import lombok.Data;

// 用户编辑视图中的权限信息
@Data
public class SavePanelUserPermissionVo {
    
    // 权限标识
    private String code;
    
    // 权限名称
    private String name;
    
    // 权限描述
    private String description;
} 