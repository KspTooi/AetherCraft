package com.ksptool.ql.restcgi.model;

import lombok.Data;

@Data
public class CgiChatMessage {

    //发送人类型 0:玩家 1:模型
    private int senderType;

    //内容
    private String content;

    //顺序
    private int seq;

}
