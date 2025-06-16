package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.commons.utils.mccq.ChatFragment;
import lombok.Getter;
import java.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;

@Getter
public class MessageFragmentVo {

    //分片类型(新) 0:起始 1:结束 2:错误 50:思考片段 51:文本
    //分片类型(旧) 0:起始 1:数据 2:结束 10:错误
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

    public static MessageFragmentVo of(ChatFragment cf){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");

        //分片类型 0:起始 1:结束 2:错误 50:思考片段 51:文本
        MessageFragmentVo mfv = new MessageFragmentVo();

        var type = cf.getType();

        if(type != 0 && type != 1 && type != 2 && type != 50 && type != 51){
            throw new IllegalArgumentException("分片类型不支持: " + type);
        }

        //起始片段
        if(type == 0){
            mfv.type = 0;
            mfv.threadId = cf.getThreadId();
            mfv.messageId = -1L;
            mfv.content = "conversation start";
            mfv.seq = 0;
            mfv.senderRole = null; //发送人角色 0:玩家 1:模型
            mfv.senderName = null;
            mfv.senderAvatarUrl = null;
            mfv.sendTime = null;
        }

        //结束片段
        if(type == 1){
            mfv.type = 1;
            mfv.threadId = cf.getThreadId();
            mfv.messageId = cf.getMessageId();
            mfv.content = "conversation end";
            mfv.seq = cf.getSeq();
            mfv.senderRole = 1; //发送人角色 0:玩家 1:模型
            mfv.senderName = cf.getSenderName();
            mfv.senderAvatarUrl = cf.getSenderAvatarUrl();
            mfv.sendTime = sdf.format(cf.getSendTime());
        }

        //错误片段
        if(type == 2){
            mfv.type = 2;
            mfv.threadId = cf.getThreadId();
            mfv.messageId = cf.getMessageId();
            mfv.content = StringUtils.isNotBlank(cf.getContent()) ? cf.getContent() : "模型响应时出现未知错误";
            mfv.seq = cf.getSeq();
            mfv.senderRole = 1; //发送人角色 0:玩家 1:模型
            mfv.senderName = cf.getSenderName();
            mfv.senderAvatarUrl = cf.getSenderAvatarUrl();
            mfv.sendTime = sdf.format(cf.getSendTime());
        }

        //思考片段
        if(type == 50){
            mfv.type = 50;
            mfv.threadId = cf.getThreadId();
            mfv.messageId = -1L;
            mfv.content = cf.getContent();
            mfv.seq = cf.getSeq();
            mfv.senderRole = 1; //发送人角色 0:玩家 1:模型
            mfv.senderName = cf.getSenderName();
            mfv.senderAvatarUrl = cf.getSenderAvatarUrl();
            mfv.sendTime = sdf.format(cf.getSendTime());
        }

        //文本片段
        if(type == 51){
            mfv.type = 51;
            mfv.threadId = cf.getThreadId();
            mfv.messageId = -1L;
            mfv.content = cf.getContent();
            mfv.seq = cf.getSeq();
            mfv.senderRole = 1; //发送人角色 0:玩家 1:模型
            mfv.senderName = cf.getSenderName();
            mfv.senderAvatarUrl = cf.getSenderAvatarUrl();
            mfv.sendTime = sdf.format(cf.getSendTime());
        }

        return mfv;
    }
    

}
