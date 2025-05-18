package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class SelectThreadMessageVo {

    private Long id;

    //发送人名称
    private String senderName;

    //发送人头像
    private String senderAvatarUrl;

    //发送人角色 0:Player 1:Model
    private Integer senderRole;

    //消息内容
    private String content;

    //消息发送时间 yyyy年mm月dd日 HH:mm:ss
    private String createTime;

}
