package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
