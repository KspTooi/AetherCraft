package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestModelConnectionDto {

    //模型代码
    @NotBlank
    private String modelCode;

}
