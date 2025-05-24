package com.ksptool.ql.restcgi.model;

import lombok.Data;

@Data
public class CgiChatMessage {

    public CgiChatMessage(int senderType, String content, int seq) {
        this.senderType = senderType;
        this.content = content;
        this.seq = seq;
    }

    public CgiChatMessage(int senderType, String content) {
        this.senderType = senderType;
        this.content = content;
        this.seq = 0;
    }

    public CgiChatMessage(String content) {
        this.senderType = 0;
        this.content = content;
        this.seq = 0;
    }

    public CgiChatMessage() {
    }

    //发送人类型 0:玩家 1:模型
    private int senderType;

    //内容
    private String content;

    //顺序
    private int seq;

}
