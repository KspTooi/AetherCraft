package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAppDto {

    @NotBlank(message = "应用名称不能为空")
    private String name; //应用名称

    private String kind; //应用类型（可选，留空则自动推断）

    @NotBlank(message = "程序路径不能为空")
    private String execPath; //程序路径

    private String command; //快捷启动命令

    private String description;

}
