package com.ksptool.ql.commons.utils.mccq;

import com.ksptool.ql.biz.model.record.CgiCallbackContext;
import lombok.Getter;
import java.util.Date;
import com.ksptool.ql.restcgi.model.CgiChatResult;

@Getter
public class ChatFragment {

    //分片类型(新) 0:起始 1:结束 2:错误 50:思考片段 51:文本
    //分片类型(旧) 0:起始 1:数据 2:结束 10:错误
    private int type;

    //发送人名称
    private String senderName;

    //发送人头像URL
    private String senderAvatarUrl;

    //玩家ID
    private long playerId;

    //对话线程ID
    private long threadId;

    //对话记录ID 当type为1时存在
    private Long messageId;

    //响应流ID
    private String streamId;

    //分片内容
    private String content;

    //分片序号
    private int seq;

    //分片生存时间 为0时从队列销毁
    private int ttl;

    //发送时间
    private Date sendTime;


    /**
     * 设置分片生存时间
     * 当生存时间小于0时，分片从队列中销毁
     */
    public void setTtl(int ttl){
        if(ttl < 0){
            throw new IllegalArgumentException("ttl不能小于0");
        }
        this.ttl = ttl;
    }

    /**
     * 设置分片序号
     */
    public void setSeq(int seq){
        if(seq < 0){
            throw new IllegalArgumentException("seq不能小于0");
        }
        this.seq = seq;
    }

    /**
     * 减少生存时间
     * 当生存时间小于0时，分片从队列中销毁
     */
    public void decrementTtl(){
        if(ttl > 0){
            ttl--;
        }
    }

    /**
     * 判断分片是否已过期
     * 当生存时间小于0时，分片从队列中销毁
     */
    public boolean isExpired(){
        return ttl <= 0;
    }


    public static ChatFragment ofThought(CgiChatResult ccr, CgiCallbackContext ctx){

        if(ccr == null || ctx == null){
            throw new IllegalArgumentException("ccr, ctx不能为空");
        }

        var cf = new ChatFragment();
        cf.type = 50;
        cf.senderName = ctx.modelName();
        cf.senderAvatarUrl = ctx.modelAvatarUrl();
        cf.playerId = ctx.playerId();
        cf.threadId = ctx.threadId();
        cf.streamId = ctx.streamId();
        cf.content = ccr.getContent();
        cf.sendTime = new Date();
        return cf;
    }


    public static ChatFragment ofMessage(CgiChatResult ccr, CgiCallbackContext ctx){

        if(ccr == null || ctx == null){
            throw new IllegalArgumentException("ccr, ctx不能为空");
        }

        var cf = new ChatFragment();
        cf.type = 51;
        cf.senderName = ctx.modelName();
        cf.senderAvatarUrl = ctx.modelAvatarUrl();
        cf.playerId = ctx.playerId();
        cf.threadId = ctx.threadId();
        cf.streamId = ctx.streamId();
        cf.content = ccr.getContent();
        cf.sendTime = new Date();
        return cf;
    }


    public static ChatFragment ofStart(Long playerId,Long threadId,String streamId){

        if(playerId == null || threadId == null || streamId == null){
            throw new IllegalArgumentException("playerId, threadId, streamId不能为空");
        }

        var cf = new ChatFragment();
        cf.type = 0;
        cf.playerId = playerId;
        cf.threadId = threadId;
        cf.streamId = streamId;
        cf.content = "conversation start";
        cf.seq = 0;
        cf.sendTime = new Date();
        return cf;
    }

    public static ChatFragment ofFinish(CgiChatResult ccr, CgiCallbackContext ctx,Long msgId){

        if(ccr == null || ctx == null || msgId == null){
            throw new IllegalArgumentException("ccr, ctx, msgId不能为空");
        }

        var cf = new ChatFragment();
        cf.type = 1;
        cf.senderName = ctx.modelName();
        cf.senderAvatarUrl = ctx.modelAvatarUrl();
        cf.playerId = ctx.playerId();
        cf.threadId = ctx.threadId();
        cf.messageId = msgId;
        cf.streamId = ctx.streamId();
        cf.content = ccr.getContent();
        cf.sendTime = new Date();
        return cf;
    }

    public static ChatFragment ofError(CgiChatResult ccr, CgiCallbackContext ctx,Exception e){

        if(ccr == null || ctx == null){
            throw new IllegalArgumentException("ccr, ctx不能为空");
        }

        var cf = new ChatFragment();
        cf.type = 2;
        cf.senderName = ctx.modelName();
        cf.senderAvatarUrl = ctx.modelAvatarUrl();
        cf.playerId = ctx.playerId();
        cf.threadId = ctx.threadId();
        cf.streamId = ctx.streamId();
        cf.content = e.getMessage() != null ? e.getMessage() : "AI响应错误";
        cf.sendTime = new Date();
        return cf;
    }

}
