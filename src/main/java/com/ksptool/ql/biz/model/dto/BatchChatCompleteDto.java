package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchChatCompleteDto {

    @NotNull
    private Long chatThread;

    @NotBlank
    private String model;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    @NotBlank
    private Integer queryKind; //0:发送消息 1:查询响应流 2:终止AI响应

} 