package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EditAppDto {
    @NotNull(message = "应用ID不能为空")
    private Long id;

    @NotBlank(message = "应用名称不能为空")
    private String name;

    private String kind;

    @NotBlank(message = "程序路径不能为空")
    private String execPath;

    private String description;
} 