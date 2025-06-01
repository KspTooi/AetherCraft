package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class MessageFragmentVo {

    //片段类型 0:起始 1:数据 2:结束 10:错误
    private Integer type;

    //对话串ID
    private Long threadId;

    //消息ID (-1为临时消息)
    private Long messageId;

    //消息内容
    private String content;

    //顺序
    private Integer seq;

    //发送人角色 0:玩家 1:模型
    private Integer senderRole;

    //发送人姓名
    private String senderName;

    //发送人头像URL
    private String senderAvatarUrl;

    //发送时间 yyyy年MM月dd日 HH:mm:ss
    private String sendTime;

}
