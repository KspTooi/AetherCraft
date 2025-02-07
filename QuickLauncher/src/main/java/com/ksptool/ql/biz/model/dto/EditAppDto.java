package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EditAppDto {
    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private Integer kind;

    @NotBlank
    private String execPath;
} 