package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchRpCompleteDto {

    //RP对话ID
    @NotNull
    private Long thread;

    //模型代码
    private String model;

    //对话内容
    private String message;

    @NotNull
    private Integer queryKind; //0:发送消息 1:查询响应流 2:终止AI响应 3:重新生成AI最后一条回复

} 