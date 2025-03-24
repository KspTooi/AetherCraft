package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEmptyThreadDto {
    
    // AI模型代码
    @NotBlank(message = "模型代码不能为空")
    private String model;
} 