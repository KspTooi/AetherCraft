package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RemoveAppDto {

    @NotBlank
    private Long id; //应用ID

}
