package com.ksptool.ql.commons.utils.mccq;

import lombok.Data;

@Data
public class ChatFragment {

    //分片类型 0:起始 1:数据 2:结束 10:错误
    private int type;

    //发送人名称
    private String senderName;

    //发送人头像URL
    private String senderAvatarUrl;

    //玩家ID
    private long playerId;

    //对话线程ID
    private long threadId;

    //对话记录ID 当type为2时存在
    private Long messageId;

    //响应流ID
    private String streamId;

    //分片内容
    private String content;

    //分片序号
    private int seq;

    //分片生存时间 为0时从队列销毁
    private int ttl;

}
