package com.ksptool.ql.biz.model.dto;

import lombok.Data;

/**
 * 用户列表查询条件
 */
@Data
public class ListPanelUserDto {
    
    /**
     * 用户名（模糊查询）
     */
    private String username;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页条数
     */
    private Integer size = 10;
} 