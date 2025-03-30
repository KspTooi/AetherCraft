package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

@Data
public class GetThemeValuesVo {
    
    // 主题ID
    private Long id;
    
    // 主题名称
    private String themeName;
    
    // 主题描述
    private String description;
    
    // 是否为默认主题
    private Integer isActive;
    
    // 是否为系统主题
    private Integer isSystem;
    
    // 主题值集合，key为属性名，value为属性值
    private Map<String, String> themeValues = new HashMap<>();
} 