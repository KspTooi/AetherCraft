package com.ksptool.ql.biz.model.dto;

import lombok.Data;

@Data
public class ListPanelConfigDto {
    // 配置键或值
    private String keyOrValue;
    
    // 描述
    private String description;
    
    // 分页参数
    private Integer page = 1;
    private Integer pageSize = 10;
} 