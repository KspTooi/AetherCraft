package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SavePanelAppDto {
    // 应用ID
    private Long id;

    // 应用名称
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 100, message = "应用名称不能超过100个字符")
    private String name;

    // 应用类型
    @NotBlank(message = "应用类型不能为空")
    @Size(max = 20, message = "应用类型不能超过20个字符")
    private String kind;

    // 程序路径
    @NotBlank(message = "程序路径不能为空")
    @Size(max = 255, message = "程序路径不能超过255个字符")
    private String execPath;

    // 图标路径
    @Size(max = 255, message = "图标路径不能超过255个字符")
    private String iconPath;

    // 快捷命令
    @Size(max = 50, message = "快捷命令不能超过50个字符")
    private String command;

    // 描述
    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;
} 