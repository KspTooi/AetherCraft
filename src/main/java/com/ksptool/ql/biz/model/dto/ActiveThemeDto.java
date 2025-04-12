package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActiveThemeDto {

    // 需要激活的主题ID
    @NotNull
    private Long themeId;
} 