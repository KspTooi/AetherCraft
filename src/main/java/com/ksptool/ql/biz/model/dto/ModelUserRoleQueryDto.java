package com.ksptool.ql.biz.model.dto;

import lombok.Data;

/**
 * 用户角色查询Dto
 * 用于接收角色查询参数
 */
@Data
public class ModelUserRoleQueryDto {
    
    // 角色名称（模糊查询）
    private String name;
    
    // 当前选中的角色ID
    private Long selectedId;
} 