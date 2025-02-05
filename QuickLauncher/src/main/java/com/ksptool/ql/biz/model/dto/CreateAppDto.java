package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAppDto {

    @NotBlank
    private String name; //应用名称

    @NotNull
    private Integer kind; //应用类型 0:EXE 1:BAT

    @NotBlank
    private String execPath; //程序路径

}
