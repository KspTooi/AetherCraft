package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameFileDto {
    @NotBlank(message = "原始路径不能为空")
    private String oldPath;

    @NotBlank(message = "新名称不能为空")
    private String newName;
} 