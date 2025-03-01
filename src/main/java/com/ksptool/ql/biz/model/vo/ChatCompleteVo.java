package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class ChatCompleteVo {

    //对话串ID
    private Long chatThread;

    // AI响应的消息内容
    private String content;
} 