package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchChatCompleteDto {

    @NotNull
    private Long thread;

    private String model;

    private String message;

    @NotNull
    private Integer queryKind; //0:发送消息 1:查询响应流 2:终止AI响应

} 