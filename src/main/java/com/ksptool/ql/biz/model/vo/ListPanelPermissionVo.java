package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 权限列表视图对象
 */
@Data
public class ListPanelPermissionVo {
    
    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限标识
     */
    private String code;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 是否系统权限 0:非系统权限 1:系统权限
     */
    private Integer isSystem;
} 