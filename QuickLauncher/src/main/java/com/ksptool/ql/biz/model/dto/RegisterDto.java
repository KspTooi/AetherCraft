package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
