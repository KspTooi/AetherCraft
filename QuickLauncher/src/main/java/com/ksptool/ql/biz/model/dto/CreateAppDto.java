package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAppDto {

    @NotBlank(message = "应用名称不能为空")
    private String name; //应用名称

    @NotNull
    private Integer kind; //应用类型 0:EXE 1:BAT

    @NotBlank(message = "程序路径不能为空")
    private String execPath; //程序路径

    private String description;

}
