package com.ksptool.ql.biz.user.model.dto;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;

@Data
public class GetPermissionListDto extends PageQuery {
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
     * 是否只查询系统权限（1-是，0-否）
     */
    private Integer isSystem;
}
