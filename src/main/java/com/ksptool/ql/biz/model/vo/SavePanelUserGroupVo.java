package com.ksptool.ql.biz.model.vo;

import lombok.Data;

// 用户编辑视图中的用户组信息
@Data
public class SavePanelUserGroupVo {
    
    // 用户组ID
    private Long id;
    
    // 用户组名称
    private String name;
    
    // 用户组标识
    private String code;
    
    // 用户组描述
    private String description;
    
    // 排序号
    private Integer sortOrder;
    
    // 状态（0:禁用 1:启用）
    private Integer status;
    
    // 用户是否在此组中
    private Boolean hasGroup;
} 