package com.ksptool.ql.restcgi.model;

import lombok.Data;

@Data
public class CgiChatMessage {

    //发送人类型 0:玩家 1:模型
    private int senderType;

    private String content;

    private int seq;

}
