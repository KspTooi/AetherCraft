package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.po.ChatMessagePo;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import com.ksptool.ql.biz.model.vo.MessageFragmentVo;
import com.ksptool.ql.biz.model.vo.SendMessageVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.commons.utils.ThreadStatusTrack;
import com.ksptool.ql.commons.utils.mccq.ChatFragment;
import com.ksptool.ql.commons.utils.mccq.MemoryChatControlQueue;
import com.ksptool.ql.commons.web.Result;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationService {

    //线程会话状态跟踪
    private final ThreadStatusTrack track = new ThreadStatusTrack();

    private final MemoryChatControlQueue mccq = new MemoryChatControlQueue();

    @Autowired
    private ChatThreadService chatThreadService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private PlayerConfigService playerConfigService;

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

        //锁定会话
        track.newSession(threadPo.getId());
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
        var msg = new ChatMessagePo();
        msg.setThread(threadPo);
        msg.setSenderRole(0); //发送人角色 0:Player 1:Model
        msg.setSenderName(playerName);
        msg.setContent(css.encryptForCurUser(dto.getMessage())); //保存消息为密文
        msg.setSeq(messagesPos.size() + 1);
        messagesPos.add(msg);
        threadPo.setLastMessage(msg);

        //需通过CGI发送的历史记录
        var cgiMessageHistory = new ArrayList<ModelChatParamHistory>();

        //组装需通过CGI发送的聊天历史记录
        for(var item : messagesPos){
            var cgiItem = new ModelChatParamHistory();
            cgiItem.setRole(item.getSenderRole());
            cgiItem.setContent(css.decryptForCurUser(item.getContent()));
            cgiItem.setSequence(item.getSeq());
            cgiMessageHistory.add(cgiItem);
        }


        OkHttpClient client = HttpClientUtils.createHttpClient(playerConfigService.getSelfProxyUrl(), 60);

        var param = new ModelChatParam();
        param.setModelCode(model.getCode());
        param.setMessage(dto.getMessage());
        param.setApiKey(apikey);
        param.setHistories(cgiMessageHistory);
        playerConfigService.readSelfModelParam(param);

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


        return null;
    }


    public String regenerate(RegenerateDto dto) {
        return null;
    }


    public MessageFragmentVo queryMessage(QueryMessageDto dto) {
        return null;
    }


    public String abortConversation(AbortConversationDto dto) {
        return null;
    }


}
