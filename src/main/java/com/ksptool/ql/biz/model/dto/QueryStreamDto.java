package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryStreamDto {

    @NotBlank
    private String streamId;

}
