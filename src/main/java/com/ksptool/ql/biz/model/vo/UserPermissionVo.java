package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 用户权限VO
 */
@Data
public class UserPermissionVo {
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限键
     */
    private String permKey;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 权限描述
     */
    private String description;
    
    /**
     * 是否为系统内置权限
     */
    private Boolean isSystem;
} 