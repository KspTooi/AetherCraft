package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ChatMessageRepository;
import com.ksptool.ql.biz.mapper.ChatThreadRepository;
import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.po.ChatMessagePo;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import com.ksptool.ql.biz.model.record.CgiCallbackContext;
import com.ksptool.ql.biz.model.vo.MessageFragmentVo;
import com.ksptool.ql.biz.model.vo.SendMessageVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.mccq.ChatFragment;
import com.ksptool.ql.commons.utils.mccq.MemoryChatControlQueue;
import com.ksptool.ql.restcgi.model.CgiChatMessage;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import com.ksptool.ql.restcgi.service.ModelRestCgi;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Service
@Slf4j
public class ChatConversationService {

    private final MemoryChatControlQueue mccq = new MemoryChatControlQueue();

    @Autowired
    private ChatThreadService chatThreadService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private ModelRestCgi restCgi;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatThreadRepository chatThreadRepository;

    @Transactional
    public SendMessageVo sendMessage(SendMessageDto dto) throws BizException {

        var player = AuthService.requirePlayer();
        AIModelEnum model = AIModelEnum.ensureModelCodeExists(dto.getModelCode());

        ChatThreadPo threadPo = null;

        //Thread为-1时创建新会话
        if(dto.getThreadId() == -1L){
            threadPo = chatThreadService.createSelfThread(model,dto.getType());
        }
        if(dto.getThreadId() != -1L){
            threadPo = chatThreadService.getSelfThread(dto.getThreadId());
        }

        var apikey = apiKeyService.getApiKey(model.getCode(), player.getPlayerId());

        if (StringUtils.isBlank(apikey)) {
            throw new BizException("没有为模型:"+model.getCode()+"配置APIKEY");
        }

        //检查会话是否已锁定
        if(mccq.isStreamOpen(threadPo.getId())){
            throw new BizException("该会话正在处理中,请等待模型响应完成.");
        }

        //锁定会话
        String streamId = mccq.openStream(threadPo.getId());
        threadPo.setModelCode(model.getCode());

        /*
         * 0:标准会话 1:RP会话 2:增强会话
         * 标准会话为匿名会话 不会注入Player信息到聊天上下文
         * RP会话为具名会话 会注入对话双方Npc+Player的全部个人信息
         * 增强会话介于之间 只注入Player的个人信息到聊天上下文
         */
        var playerName = AuthService.requirePlayerName();

        if(threadPo.getType() == 0){
            playerName = "User";
        }

        //获取该Thread下全部聊天记录
        List<ChatMessagePo> messagesPos = threadPo.getMessages();

        //保存用户发来的消息为一条历史记录
        var chatMessagePo = new ChatMessagePo();
        chatMessagePo.setThread(threadPo);
        chatMessagePo.setSenderRole(0); //发送人角色 0:Player 1:Model
        chatMessagePo.setSenderName(playerName);
        chatMessagePo.setContent(css.encryptForCurUser(dto.getMessage())); //保存消息为密文
        chatMessagePo.setSeq(messagesPos.size() + 1);
        messagesPos.add(chatMessagePo);
        threadPo.setLastMessage(chatMessagePo);

        chatThreadRepository.save(threadPo);
        chatMessageRepository.save(chatMessagePo);

        //需通过CGI发送的历史记录
        var cgiHistoryMessages = new ArrayList<CgiChatMessage>();

        //组装需通过CGI发送的聊天历史记录
        for(var item : messagesPos){
            var cgiItem = new CgiChatMessage();
            cgiItem.setSenderType(item.getSenderRole()); //发送人类型 0:玩家 1:模型
            cgiItem.setContent(css.decryptForCurUser(item.getContent()));
            cgiItem.setSeq(item.getSeq());
            cgiHistoryMessages.add(cgiItem);
        }

        //创建起始分片
        var cf = new ChatFragment();
        cf.setType(0); //0:起始 1:数据 2:结束 10:错误
        cf.setPlayerId(player.getPlayerId());
        cf.setThreadId(threadPo.getId());
        cf.setContent(dto.getMessage()); //起始消息是用户发送的内容
        cf.setSeq(0);
        cf.setSenderName(playerName);
        cf.setSenderAvatarUrl(player.getPlayerAvatarUrl());
        cf.setStreamId(streamId);
        cf.setMessageId(chatMessagePo.getId());
        cf.setSendTime(chatMessagePo.getCreateTime());
        mccq.receive(cf);

        var msg = new CgiChatMessage();
        msg.setSenderType(0);
        msg.setContent(dto.getMessage());
        msg.setSeq(1);

        CgiChatParam p = new CgiChatParam();
        p.setModel(model);
        p.setApikey(apikey);
        p.setHistoryMessages(cgiHistoryMessages);
        p.setMessage(msg);

        restCgi.sendMessage(p, onCgiCallback(new CgiCallbackContext(
                threadPo.getId(),
                player.getUserId(),
                player.getPlayerId(),
                streamId,
                playerName,
                player.getPlayerAvatarUrl(),
                model.getSeries(),
                ""
        )));

        var vo = new SendMessageVo();
        vo.setThreadId(threadPo.getId());
        vo.setTitle(threadPo.getTitle());
        vo.setNewThreadCreated(0);
        vo.setStreamId(streamId);
        if(dto.getThreadId() == -1){
            vo.setNewThreadCreated(1);
        }
        return vo;
    }


    public String regenerate(RegenerateDto dto) {
        return null;
    }


    public MessageFragmentVo queryMessage(QueryStreamDto dto) throws BizException {

        try{
            var ret = new MessageFragmentVo();

            //消费消息队列中该Thread的消息片段
            var first = mccq.next(dto.getStreamId());

            /*
             * 分片类型 0:起始 1:数据 2:结束 10:错误
             * 消费逻辑
             * 1.当首个段`firstFragment`为起始、错误片段时需立即返回内容给客户端
             * 2.当首个段为数据段,尝试获取尽可能多的数据段并将其组装到一个客户端响应中
             * 在消费数据片段时获取到"结束"或"错误"片段时立即返回已组装的客户端响应,并将已获取到的"结束"或"错误"片段重新放入队头
             */
            if(first.getType() == 0 || first.getType() == 10 || first.getType() == 2){
                ret.setThreadId(first.getThreadId());
                ret.setMessageId(first.getMessageId());
                ret.setSenderRole(1);
                ret.setSeq(first.getSeq());
                ret.setContent(first.getContent());
                ret.setType(first.getType());
                ret.setSenderName(first.getSenderName());
                ret.setSenderAvatarUrl(first.getSenderAvatarUrl());
                ret.setSendTime(first.getSendTime());
                if(first.getType() == 10){
                    ret.setContent(StringUtils.isNotBlank(first.getContent()) ? first.getContent() : "模型响应时出现未知错误");
                }
                if(first.getType() == 2){
                    ret.setContent("conversation end");
                }
                return ret;
            }

            ret.setThreadId(first.getThreadId());
            ret.setMessageId(-1L);
            ret.setSenderRole(1);
            ret.setSeq(first.getSeq());
            ret.setContent("----");
            ret.setType(first.getType());
            ret.setSenderName(first.getSenderName());
            ret.setSenderAvatarUrl(first.getSenderAvatarUrl());
            ret.setSendTime(first.getSendTime());

            StringBuilder content = new StringBuilder(first.getContent());

            //处理消息片段
            while (mccq.hasNext(dto.getStreamId())){

                ChatFragment next = mccq.next(dto.getStreamId());

                if(next.getType() == 10 || next.getType() == 2){
                    mccq.receive(next);
                    break;
                }

                content.append(next.getContent());
            }

            ret.setContent(content.toString());
            return ret;

        }catch (TimeoutException e){
            throw new BizException(e.getMessage());
        }

    }

    public String abortConversation(AbortConversationDto dto) {
        return null;
    }

    private Consumer<CgiChatResult> onCgiCallback(CgiCallbackContext ctx) {
        return (ccr)->{
            try{

                if(!mccq.isStreamOpen(ctx.threadId(),ctx.streamId())){
                    return;
                }

                //数据类型 - 创建数据片段
                if (ccr.getType() == 0) {
                    var cf = new ChatFragment();
                    cf.setType(1);
                    cf.setSenderName(ctx.modelName());
                    cf.setSenderAvatarUrl(ctx.modelAvatarUrl());
                    cf.setPlayerId(ctx.playerId());
                    cf.setThreadId(ctx.threadId());
                    cf.setStreamId(ctx.streamId());
                    cf.setContent(ccr.getContent());
                    cf.setSendTime(new Date());
                    mccq.receive(cf);
                    return;
                }

                //结束类型
                if (ccr.getType() == 1) {
                    var messagePo = new ChatMessagePo();
                    messagePo.setThread(Any.of().val("id", ctx.threadId()).as(ChatThreadPo.class));
                    messagePo.setSenderRole(1);
                    messagePo.setSenderName(ccr.getModel().getSeries());
                    messagePo.setContent(css.encrypt(ccr.getContent(),ctx.userId()));
                    messagePo.setSeq(chatMessageRepository.getCountByThreadId(ctx.threadId()) + 1);
                    messagePo.setTokenInput(ccr.getTokenInput());
                    messagePo.setTokenOutput(ccr.getTokenOutput());
                    messagePo.setTokenThoughts(ccr.getTokenThoughtOutput());
                    chatMessageRepository.save(messagePo);

                    var cf = new ChatFragment();
                    cf.setType(2);
                    cf.setSenderName(ctx.modelName());
                    cf.setSenderAvatarUrl(ctx.modelAvatarUrl());
                    cf.setPlayerId(ctx.playerId());
                    cf.setThreadId(ctx.threadId());
                    cf.setContent(ccr.getContent());
                    cf.setMessageId(messagePo.getId());
                    cf.setStreamId(ctx.streamId());
                    cf.setSendTime(messagePo.getCreateTime());
                    mccq.receive(cf);

                    // 通知会话已结束
                    mccq.closeStream(ctx.threadId());
                    return;
                }

                //错误类型 - 创建错误片段
                if (ccr.getType() == 2) {
                    var cf = new ChatFragment();
                    cf.setType(10);
                    cf.setSenderName(ctx.modelName());
                    cf.setSenderAvatarUrl(ctx.modelAvatarUrl());
                    cf.setPlayerId(ctx.playerId());
                    cf.setThreadId(ctx.threadId());
                    cf.setContent(ccr.getException() != null ? "AI响应错误: " + ccr.getException().getMessage() : "AI响应错误");
                    cf.setStreamId(ctx.streamId());
                    cf.setSendTime(new Date());
                    mccq.receive(cf);

                    // 通知会话已失败
                    mccq.closeStream(ctx.threadId());
                }

            }catch (Exception e){
                log.error("处理聊天片段失败!",e);
            }
        };
    }


}
