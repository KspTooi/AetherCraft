package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegenerateDto {

    @NotNull
    private Long threadId;

    @NotNull
    private String modelCode;

    //根消息(只能是用户消息) -1为最后一条用户消息
    @NotNull
    private Long rootMessageId;

}
