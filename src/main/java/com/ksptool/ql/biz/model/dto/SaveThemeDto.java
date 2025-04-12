package com.ksptool.ql.biz.model.dto;

import jakarta.validation.Valid;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SaveThemeDto {

    // 主题ID
    private Long themeId;

    // 主题名称（可选）
    private String themeName;
    
    // 主题描述（可选）
    private String description;
    
    // 主题值对象
    @NotNull
    @Valid
    private ThemeValuesDto themeValues;
}