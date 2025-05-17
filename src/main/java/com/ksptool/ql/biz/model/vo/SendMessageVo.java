package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class SendMessageVo {

    //对话ThreadId
    private Long threadId;

    //对话Thread 标题
    private String title;

    //是否创建了新Thread 0:否 1:是
    private Integer newThreadCreated;

}
