package com.ksptool.ql.biz.model.dto;

import lombok.Data;

@Data
public class ModelRpQueryDto {
    // 关键字搜索(角色名称或描述)
    private String keyword;
    
    // 当前页码
    private Integer currentPage = 1;
    
    // 每页大小
    private Integer pageSize = 10;
} 