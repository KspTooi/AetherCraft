package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ChatMessageRepository;
import com.ksptool.ql.biz.mapper.ChatThreadRepository;
import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.po.ChatMessagePo;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import com.ksptool.ql.biz.model.po.NpcPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.record.CgiCallbackContext;
import com.ksptool.ql.biz.model.vo.MessageFragmentVo;
import com.ksptool.ql.biz.model.vo.SendMessageVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.PreparedPrompt;
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

import java.text.SimpleDateFormat;
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

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private NpcScriptService npcScriptService;

    @Transactional
    public SendMessageVo sendMessage(SendMessageDto dto) throws BizException {

        var player = AuthService.requirePlayer();
        AIModelEnum model = AIModelEnum.ensureModelCodeExists(dto.getModelCode());

        ChatThreadPo threadPo = null;

        //Thread为-1时创建新会话
        if(dto.getThreadId() == -1L){

            //NPC会话无法自动创建
            if(dto.getType() == 1){
                throw new BizException("NPC会话无法自动创建");
            }

            //创建标准会话
            if(dto.getType() == 0){
                chatThreadRepository.deActiveAllStandardThread(player.getPlayerId(),player.getUserId());
                threadPo = chatThreadService.createSelfThread(model,dto.getType());
            }

        }

        if(dto.getThreadId() != -1L){
            threadPo = chatThreadService.getSelfThread(dto.getThreadId());
        }

        if(threadPo == null){
            throw new BizException("创建或获取会话失败!");
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

        try{

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

            //保存用户发来的消息为一条历史记录
            var playerMessage = new ChatMessagePo();
            playerMessage.setThread(threadPo);
            playerMessage.setSenderRole(0); //发送人角色 0:Player 1:Model
            playerMessage.setSenderName(playerName);
            playerMessage.setContent(css.encryptForCurUser(dto.getMessage())); //保存消息为密文
            playerMessage.setSeq(messagesPos.size() + 1);
            playerMessage.setCreateTime(new Date());
            chatMessageRepository.save(playerMessage);
            messagesPos.add(playerMessage);
            threadPo.setLastMessage(playerMessage);
            chatThreadRepository.save(threadPo);

            //创建起始分片
            var cf = new ChatFragment();
            cf.setType(0); //0:起始 1:数据 2:结束 10:错误
            cf.setPlayerId(player.getPlayerId());
            cf.setThreadId(threadPo.getId());
            cf.setContent("conversation start"); //起始消息是用户发送的内容
            cf.setSeq(0);
            cf.setStreamId(streamId);
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

            var modelAvatarUrl = "";
            var modelName = model.getSeries();

            //当为NPC会话时需注入增强Prompt上下文
            if(dto.getType() == 1){

                NpcPo npc = threadPo.getNpc();
                PlayerPo playerPo = threadPo.getPlayer();

                var mainPromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_MAIN.getKey());
                var rolePromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_ROLE.getKey());

                PreparedPrompt systemPrompt = PreparedPrompt.prepare(mainPromptTemplate).union(rolePromptTemplate);
                systemPrompt.setParameter("npc", npc.getName());
                systemPrompt.setParameter("player", playerPo.getName());
                systemPrompt.setParameter("npcDescription", css.decryptForCurUser(npc.getDescription()));
                systemPrompt.setParameter("npcRoleSummary", css.decryptForCurUser(npc.getRoleSummary()));
                systemPrompt.setParameter("npcScenario", css.decryptForCurUser(npc.getScenario()));
                systemPrompt.setParameter("npcScenario", css.decryptForCurUser(npc.getScenario()));
                systemPrompt.setParameter("playerDesc", css.decryptForCurUser(playerPo.getDescription()));
                systemPrompt = npcScriptService.appendExamplePrompt(npc.getId(), systemPrompt);
                p.setSystemPrompt(systemPrompt.executeNested());

                PreparedPrompt msgPrompt = new PreparedPrompt(dto.getMessage());
                systemPrompt.setParameter("npc", npc.getName());
                systemPrompt.setParameter("player", playerPo.getName());
                msg.setContent(msgPrompt.executeNested());
                modelAvatarUrl = npc.getAvatarUrlPt(css);
                modelName = npc.getName();
            }


            restCgi.sendMessage(p, onCgiCallback(new CgiCallbackContext(
                    threadPo.getId(),
                    player.getUserId(),
                    player.getPlayerId(),
                    streamId,
                    playerName,
                    player.getPlayerAvatarUrl(),
                    modelName,
                    modelAvatarUrl
            )));

            var sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            var vo = new SendMessageVo();
            vo.setThreadId(threadPo.getId());
            vo.setMessageId(playerMessage.getId());
            vo.setContent(dto.getMessage());
            vo.setSenderName(playerName);
            vo.setSenderAvatarUrl(player.getPlayerAvatarUrl());
            vo.setSendTime(sdf.format(playerMessage.getCreateTime()));
            vo.setTitle(threadPo.getTitle());
            vo.setNewThreadCreated(0);
            vo.setStreamId(streamId);
            if(dto.getThreadId() == -1){
                vo.setNewThreadCreated(1);
            }
            return vo;

        }catch (BizException ex){
            mccq.closeStream(threadPo.getId());
            throw ex;
        }

    }


    @Transactional
    public SendMessageVo regenerate(RegenerateDto dto) throws BizException {

        //检查会话是否已锁定
        if(mccq.isStreamOpen(dto.getThreadId())){
            throw new BizException("该会话正在处理中,请等待模型响应完成.");
        }

        var model = AIModelEnum.ensureModelCodeExists(dto.getModelCode());
        ChatThreadPo threadPo = chatThreadService.getSelfThread(dto.getThreadId());
        threadPo.setModelCode(model.getCode());

        //获取根消息
        ChatMessagePo rootMessagePo = null;

        if(dto.getRootMessageId() == -1){
            rootMessagePo = chatMessageService.getSelfLastMessage(threadPo.getId(),0);
        }
        if(dto.getRootMessageId() != -1){
            rootMessagePo = chatMessageService.getSelfMessage(dto.getRootMessageId());
        }

        //发送人角色 0:Player 1:Model
        if(rootMessagePo.getSenderRole() != 0){
            throw new BizException("消息发送人类型错误 需为Player");
        }

        //删除根消息记录之后的所有记录
        chatMessageRepository.removeMessageAfterSeq(threadPo.getId(),rootMessagePo.getSeq());
        threadPo.setLastMessage(rootMessagePo);
        chatThreadRepository.save(threadPo);

        //锁定会话
        String streamId = mccq.openStream(threadPo.getId());

        try{

            //需通过CGI发送的历史记录
            var cgiHistoryMessages = new ArrayList<CgiChatMessage>();

            //组装需通过CGI发送的聊天历史记录
            for(var item : threadPo.getMessages()){

                //根消息不参与历史记录
                if(item.getId().equals(rootMessagePo.getId())){
                    continue;
                }

                var cgiItem = new CgiChatMessage();
                cgiItem.setSenderType(item.getSenderRole()); //发送人类型 0:玩家 1:模型
                cgiItem.setContent(css.decryptForCurUser(item.getContent()));
                cgiItem.setSeq(item.getSeq());
                cgiHistoryMessages.add(cgiItem);
            }

            var player = AuthService.requirePlayer();

            //创建起始分片
            var cf = new ChatFragment();
            cf.setType(0); //0:起始 1:数据 2:结束 10:错误
            cf.setPlayerId(player.getPlayerId());
            cf.setThreadId(threadPo.getId());
            cf.setSeq(0);
            cf.setStreamId(streamId);
            mccq.receive(cf);

            var msg = new CgiChatMessage();
            msg.setSenderType(0);
            msg.setContent(css.decryptForCurUser(rootMessagePo.getContent()));
            msg.setSeq(1);

            var apikey = apiKeyService.getApiKey(model.getCode(), player.getPlayerId());

            if (StringUtils.isBlank(apikey)) {
                throw new BizException("没有为模型:"+model.getCode()+"配置APIKEY");
            }

            CgiChatParam p = new CgiChatParam();
            p.setModel(model);
            p.setApikey(apikey);
            p.setHistoryMessages(cgiHistoryMessages);
            p.setMessage(msg);

            var modelAvatarUrl = "";
            var modelName = model.getSeries();

            //Thread类型 0:标准会话 1:RP会话 2:标准增强会话
            if(threadPo.getType() == 1){

                NpcPo npc = threadPo.getNpc();
                PlayerPo playerPo = threadPo.getPlayer();

                var mainPromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_MAIN.getKey());
                var rolePromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_ROLE.getKey());

                PreparedPrompt systemPrompt = PreparedPrompt.prepare(mainPromptTemplate).union(rolePromptTemplate);
                systemPrompt.setParameter("npc", npc.getName());
                systemPrompt.setParameter("player", playerPo.getName());
                systemPrompt.setParameter("npcDescription", css.decryptForCurUser(npc.getDescription()));
                systemPrompt.setParameter("npcRoleSummary", css.decryptForCurUser(npc.getRoleSummary()));
                systemPrompt.setParameter("npcScenario", css.decryptForCurUser(npc.getScenario()));
                systemPrompt.setParameter("npcScenario", css.decryptForCurUser(npc.getScenario()));
                systemPrompt.setParameter("playerDesc", css.decryptForCurUser(playerPo.getDescription()));
                systemPrompt = npcScriptService.appendExamplePrompt(npc.getId(),systemPrompt);
                p.setSystemPrompt(systemPrompt.executeNested());

                PreparedPrompt msgPrompt = new PreparedPrompt(css.decryptForCurUser(rootMessagePo.getContent()));
                systemPrompt.setParameter("npc", npc.getName());
                systemPrompt.setParameter("player", playerPo.getName());
                msg.setContent(msgPrompt.executeNested());
                modelAvatarUrl = npc.getAvatarUrlPt(css);
                modelName = npc.getName();
            }

            restCgi.sendMessage(p, onCgiCallback(new CgiCallbackContext(
                    threadPo.getId(),
                    player.getUserId(),
                    player.getPlayerId(),
                    streamId,
                    rootMessagePo.getSenderName(),
                    player.getPlayerAvatarUrl(),
                    modelName,
                    modelAvatarUrl
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


        }catch (BizException ex){
            mccq.closeStream(threadPo.getId());
            throw ex;
        }


    }

    public MessageFragmentVo queryMessage(QueryStreamDto dto) throws BizException {

        try{
            var ret = new MessageFragmentVo();

            //消费消息队列中该Thread的消息片段
            var first = mccq.next(dto.getStreamId());

            var sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            /*
             * 分片类型 0:起始 1:数据 2:结束 10:错误
             * 消费逻辑
             * 1.当首个段`firstFragment`为起始、错误片段时需立即返回内容给客户端
             * 2.当首个段为数据段,尝试获取尽可能多的数据段并将其组装到一个客户端响应中
             * 在消费数据片段时获取到"结束"或"错误"片段时立即返回已组装的客户端响应,并将已获取到的"结束"或"错误"片段重新放入队头
             */
            if(first.getType() == 0){
                ret.setType(0);
                ret.setThreadId(first.getThreadId());
                ret.setMessageId(-1L);
                ret.setContent("conversation start");
                ret.setSeq(0);
                ret.setSenderRole(null);
                ret.setSenderName(null);
                ret.setSenderAvatarUrl(null);
                ret.setSendTime(null);
                return ret;
            }
            if(first.getType() == 2){
                ret.setType(2);
                ret.setThreadId(first.getThreadId());
                ret.setMessageId(first.getMessageId());
                ret.setContent("conversation end");
                ret.setSeq(first.getSeq());
                ret.setSenderRole(1);
                ret.setSenderName(first.getSenderName());
                ret.setSenderAvatarUrl(first.getSenderAvatarUrl());
                ret.setSendTime(sdf.format(first.getSendTime()));
                return ret;
            }
            if(first.getType() == 10){
                ret.setType(10);
                ret.setThreadId(first.getThreadId());
                ret.setMessageId(first.getMessageId());
                ret.setContent(StringUtils.isNotBlank(first.getContent()) ? first.getContent() : "模型响应时出现未知错误");
                ret.setSeq(first.getSeq());
                ret.setSenderRole(1);
                ret.setSenderName(first.getSenderName());
                ret.setSenderAvatarUrl(first.getSenderAvatarUrl());
                ret.setSendTime(sdf.format(first.getSendTime()));
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
            ret.setSendTime(sdf.format(first.getSendTime()));

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

    public void abortConversation(AbortConversationDto dto) throws BizException {
        ChatThreadPo threadPo = chatThreadService.getSelfThread(dto.getThreadId());
        mccq.closeStream(threadPo.getId());
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

                    ChatThreadPo chatThreadPo = chatThreadRepository.getThread(ctx.threadId());

                    if(chatThreadPo == null){
                        log.warn("消息创建失败,消息所属的Thread不存在: {}",ctx.threadId());
                        mccq.closeStream(ctx.threadId());
                        return;
                    }

                    var messagePo = new ChatMessagePo();
                    messagePo.setThread(chatThreadPo);
                    messagePo.setSenderRole(1);
                    messagePo.setSenderName(ccr.getModel().getSeries());

                    if(chatThreadPo.getType() == 1){
                        messagePo.setSenderName(ctx.modelName());
                    }

                    messagePo.setContent(css.encrypt(ccr.getContent(),ctx.userId()));
                    messagePo.setSeq(chatMessageRepository.getCountByThreadId(ctx.threadId()) + 1);
                    messagePo.setTokenInput(ccr.getTokenInput());
                    messagePo.setTokenOutput(ccr.getTokenOutput());
                    messagePo.setTokenThoughts(ccr.getTokenThoughtOutput());
                    chatMessageRepository.save(messagePo);
                    chatThreadPo.setLastMessage(messagePo);
                    chatThreadRepository.save(chatThreadPo);
                    log.info("为Thread:{} 创建新的消息:{}",ctx.threadId(),messagePo.getId());

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

                    //自动生成会话标题
                    chatThreadService.generateThreadTitleAsync(chatThreadPo.getId());
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
