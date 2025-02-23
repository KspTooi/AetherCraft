package com.ksptool.ql.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatCompleteVo {

    //对话ID
    private String conversationId;

    //对话串ID
    private Long chatThread;

    // AI响应的消息内容
    private String content;
} 