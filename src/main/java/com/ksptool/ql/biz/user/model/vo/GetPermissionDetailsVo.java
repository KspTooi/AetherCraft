package com.ksptool.ql.biz.user.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class GetPermissionDetailsVo {
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
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 是否为系统权限（1-是，0-否）
     */
    private Integer isSystem;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 更新时间
     */
    private String updateTime;
    
    /**
     * 关联的用户组
     */
    private List<UserGroupVo> groups;
}
