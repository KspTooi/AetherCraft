package com.ksptool.ql.biz.model.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;

/**
 * 模型角色列表查询条件
 */
@Data
public class ListModelRoleDto {
    
    /**
     * 当前选中的角色ID
     */
    private Long id;

    /**
     * 是否为新建角色模式
     */
    private Boolean isNew = false;
    
    /**
     * 角色名称或描述（模糊查询）
     */
    private String keyword;
    
    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer currentPage = 1;
    
    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数最小为1")
    private Integer pageSize = 10;
} 