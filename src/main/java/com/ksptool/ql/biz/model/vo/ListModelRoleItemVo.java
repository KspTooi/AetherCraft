package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Date;

/**
 * 模型角色列表项视图对象
 * 用于前端左侧角色列表展示
 */
@Data
public class ListModelRoleItemVo {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 头像路径
     */
    private String avatarPath;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 