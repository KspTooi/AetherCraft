package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户角色Vo
 * 用于角色管理页面的数据响应
 */
@Data
public class ModelUserRoleVo {
    
    // 角色ID
    private Long id;
    
    // 用户ID
    private Long userId;
    
    // 头像路径
    private String avatarPath;
    
    // 角色名称
    private String name;
    
    // 角色描述
    private String description;
    
    // 排序号
    private Integer sortOrder;
    
    // 是否默认角色：0-否，1-是
    private Integer isDefault;
    
    // 创建时间
    private Date createTime;
    
    // 修改时间
    private Date updateTime;
} 