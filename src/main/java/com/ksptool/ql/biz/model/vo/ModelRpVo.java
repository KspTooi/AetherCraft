package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ModelRpVo {
    // 角色ID
    private Long id;
    
    // 角色名称
    private String name;
    
    // 角色描述
    private String description;
    
    // 排序顺序
    private Integer sortOrder;
    
    // 更新时间
    private LocalDateTime updateTime;
    
    // 创建时间
    private LocalDateTime createTime;
} 