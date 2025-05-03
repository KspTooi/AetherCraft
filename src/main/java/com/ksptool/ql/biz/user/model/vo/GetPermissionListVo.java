package com.ksptool.ql.biz.user.model.vo;

import lombok.Data;

@Data
public class GetPermissionListVo {
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限代码
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
     * 是否为系统权限（1-是，0-否）
     */
    private Integer isSystem;

}
