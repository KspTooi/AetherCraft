package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveThemeDto {

    // 主题ID
    @NotNull
    private Long themeId;

}