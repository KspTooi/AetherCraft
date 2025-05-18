package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegenerateDto {

    @NotNull
    private Long threadId;

    @NotNull
    private String modelCode;

    @NotNull
    private Long rootMessageId;

}
