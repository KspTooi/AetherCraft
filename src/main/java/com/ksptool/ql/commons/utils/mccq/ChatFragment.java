package com.ksptool.ql.commons.utils.mccq;

import lombok.Data;

@Data
public class ChatFragment {

    //分片类型 1:数据 2:结束 3:错误
    private int type;

    //玩家ID
    private long playerId;

    //对话线程ID
    private long threadId;

    //消息ID
    private long messageId;

    //分片内容
    private String content;

    //分片序号
    private long seq;

    //分片生存时间 为0时从队列销毁
    private int ttl;

}
