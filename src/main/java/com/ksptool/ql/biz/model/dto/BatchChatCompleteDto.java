package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchChatCompleteDto {

    /**
     * 会话ID，-1表示创建新会话
     */
    @NotNull
    private Long threadId;

    //模型代码
    private String model;

    //消息内容
    private String message;

    @NotNull
    private Integer queryKind; //0:发送消息 1:查询响应流 2:终止AI响应 3:重新生成AI最后一条回复

    //最后一条消息的消息记录 从这条记录处开始生成(只能是用户消息) 将会清除之后的所有对话
    private Long regenerateRootHistoryId;

} 