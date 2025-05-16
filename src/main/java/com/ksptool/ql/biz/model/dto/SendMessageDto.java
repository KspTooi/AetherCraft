package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageDto {

    //为-1时自动创建新会话
    private Long threadId;

    @NotNull
    private String modelCode;

    @NotNull
    private String message;

}
