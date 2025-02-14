package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 权限编辑视图对象
 */
@Data
public class EditPanelPermissionVo {
    
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
} 