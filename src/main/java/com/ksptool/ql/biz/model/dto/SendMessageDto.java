package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class SendMessageDto {

    //为-1时自动创建新会话
    private Long threadId;

    @NotNull
    @Range(min = 0, max = 2)
    //0:标准会话 1:RP会话 2:增强会话
    private Integer type;

    @NotNull
    private String modelCode;

    @NotNull
    private String message;

}
