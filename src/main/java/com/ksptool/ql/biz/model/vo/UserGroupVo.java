package com.ksptool.ql.biz.model.vo;

import lombok.Data;

/**
 * 用户组VO
 */
@Data
public class UserGroupVo {
    /**
     * 用户组ID
     */
    private Long id;
    
    /**
     * 用户组名称
     */
    private String name;
    
    /**
     * 用户组描述
     */
    private String description;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 是否为系统内置组
     */
    private Boolean isSystem;
    
    /**
     * 用户是否属于此组
     */
    private Boolean hasGroup;
} 