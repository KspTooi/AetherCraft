package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ChatMessageRepository;
import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.po.ChatMessagePo;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import com.ksptool.ql.biz.model.vo.MessageFragmentVo;
import com.ksptool.ql.biz.model.vo.SendMessageVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.ThreadStatusTrack;
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
import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
public class ConversationService {

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
        cf.setContent(null); //起始消息无内容
        cf.setSeq(0);
        cf.setSenderName(playerName);
        cf.setSenderAvatarUrl(player.getPlayerAvatarUrl());
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

        restCgi.sendMessage(p,onModelMessageRcv(threadPo.getId(), player.getUserId(), player.getPlayerId()));

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


    public MessageFragmentVo queryMessage(QueryStreamDto dto) {
        return null;
    }


    public String abortConversation(AbortConversationDto dto) {
        return null;
    }

    private Consumer<CgiChatResult> onModelMessageRcv(long tid, long uid, long pid) {
        return (ccr)->{
            try{
                //检查该线程是否被终止 0:正在等待响应(Pending) 1:正在回复(Receive) 2:已结束(Finished) 10:已失败(Failed) 11:已终止(Terminated)
                if(track.getStatus(tid) == 11){
                    // 如果是完成或错误回调，需要关闭Session
                    if (ccr.getType() == 1 || ccr.getType() == 2) {
                        track.closeSession(tid);
                    }
                    return;
                }

                //当为正在等待响应状态 首次收到消息时需要转换状态
                if(track.getStatus(tid) == 0){
                    track.notifyReceive(tid);
                }

                //数据类型 - 创建数据片段
                if (ccr.getType() == 0) {
                    var cf = new ChatFragment();
                    cf.setType(0);
                    cf.setSenderName(ccr.getModel().getCode());
                    cf.setSenderAvatarUrl("");
                    cf.setPlayerId(pid);
                    cf.setThreadId(tid);
                    cf.setContent(ccr.getContent());
                    mccq.receive(cf);
                    return;
                }

                //结束类型
                if (ccr.getType() == 1) {
                    var messagePo = new ChatMessagePo();
                    messagePo.setThread(Any.of().val("id", tid).as(ChatThreadPo.class));
                    messagePo.setSenderRole(1);
                    messagePo.setSenderName(ccr.getModel().getSeries());
                    messagePo.setContent(css.encrypt(ccr.getContent(),uid));
                    messagePo.setSeq(chatMessageRepository.getCountByThreadId(tid) + 1);
                    messagePo.setTokenInput(ccr.getTokenInput());
                    messagePo.setTokenOutput(ccr.getTokenOutput());
                    messagePo.setTokenThoughts(ccr.getTokenThoughtOutput());
                    chatMessageRepository.save(messagePo);

                    var cf = new ChatFragment();
                    cf.setType(2);
                    cf.setSenderName(ccr.getModel().getCode());
                    cf.setSenderAvatarUrl("");
                    cf.setPlayerId(pid);
                    cf.setThreadId(tid);
                    cf.setContent(ccr.getContent());
                    mccq.receive(cf);

                    // 通知会话已结束
                    track.notifyFinished(tid);
                    track.closeSession(tid);
                    return;
                }

                //错误类型 - 创建错误片段
                if (ccr.getType() == 2) {
                    var cf = new ChatFragment();
                    cf.setType(10);
                    cf.setSenderName(ccr.getModel().getSeries());
                    cf.setSenderAvatarUrl("");
                    cf.setPlayerId(pid);
                    cf.setThreadId(tid);
                    cf.setContent(ccr.getException() != null ? "AI响应错误: " + ccr.getException().getMessage() : "AI响应错误");
                    mccq.receive(cf);

                    // 通知会话已失败
                    track.notifyFailed(tid);
                    track.closeSession(tid);
                }

            }catch (Exception e){
                log.error("处理聊天片段失败!",e);
            }
        };
    }


}
