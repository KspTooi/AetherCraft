package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class SendMessageVo {

    //对话ThreadId
    private Long threadId;

    //用户消息ID
    private Long messageId;

    //响应流ID
    private String streamId;

    //消息内容
    private String content;

    //发送人姓名
    private String senderName;

    //发送人头像URL
    private String senderAvatarUrl;

    //发送时间
    private String sendTime;

    //对话Thread 标题
    private String title;

    //是否创建了新Thread 0:否 1:是
    private Integer newThreadCreated;

}
