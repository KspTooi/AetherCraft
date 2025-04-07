package com.ksptool.ql.biz.model.dto;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

@Data
public class SaveThemeDto {

    // 主题ID
    private Long themeId;

    // 主题名称（可选）
    private String themeName;
    
    // 主题描述（可选）
    private String description;
    
    // 主题值映射，key为属性名，value为属性值
    private Map<String, String> themeValues = new HashMap<>();
}