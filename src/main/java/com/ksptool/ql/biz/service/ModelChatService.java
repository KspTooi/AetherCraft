package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.*;
import com.ksptool.ql.biz.model.dto.RecoverChatDto;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.vo.RecoverChatVo;
import com.ksptool.ql.biz.model.vo.RecoverChatHistoryVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.exception.AuthException;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import com.ksptool.ql.commons.utils.ThreadStatusTrack;
import com.ksptool.ql.commons.utils.mccq.ChatFragment;
import com.ksptool.ql.commons.utils.mccq.MemoryChatControlQueue;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import static com.ksptool.entities.Entities.as;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import com.ksptool.ql.biz.model.vo.ChatSegmentVo;
import com.ksptool.ql.biz.model.dto.BatchChatCompleteDto;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.biz.model.dto.CreateEmptyThreadDto;
import com.ksptool.ql.biz.model.vo.CreateEmptyThreadVo;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ModelChatService {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GROK_BASE_URL = "https://api.x.ai/v1/chat/completions";

    //线程会话状态跟踪
    private final ThreadStatusTrack track = new ThreadStatusTrack();

    private final MemoryChatControlQueue mccq = new MemoryChatControlQueue();

    @Autowired
    private PlayerConfigService playerConfigService;
    
    @Autowired
    private GlobalConfigService globalConfigService;
    
    @Autowired
    private ModelChatSegmentRepository modelSegmentRepository;
    
    @Autowired
    private ModelGeminiService modelGeminiService;

    @Autowired
    private ModelGrokService modelGrokService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private ChatThreadRepository threadRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatThreadService chatThreadService;


    /**
     * 创建ModelChatDto并预先填充配置信息
     * @param modelCode 模型代码
     * @param playerId 人物ID
     * @return 预填充配置的ModelChatDto
     * @throws BizException 业务异常
     */
    public ModelChatParam createModelChatDto(String modelCode, Long playerId) throws BizException {
        // 验证模型代码
        AIModelEnum modelEnum = AIModelEnum.ensureModelCodeExists(modelCode);

        // 创建并填充DTO
        ModelChatParam param = new ModelChatParam();
        param.setModelCode(modelEnum.getCode());
        playerConfigService.readPlayerModelParam(param,playerId);
        return param;
    }

    /**
     * 发送批量聊天消息
     * @param dto 批量聊天请求参数
     * @return 聊天片段VO
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = BizException.class)
    public ChatSegmentVo chatCompleteSendBatch(BatchChatCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();

        // 检查该会话是否正在处理中
        if (!threadId.equals(-1L) && track.isLocked(threadId)) {
            throw new BizException("该会话正在处理中，请等待AI响应完成");
        }

        //获取并验证模型Code
        var modelEnum = AIModelEnum.ensureModelCodeExists(dto.getModel());
        var userId = AuthService.requireUserId();
        var playerId = AuthService.requirePlayerId();
        var playerName = AuthService.requirePlayerName();

        String apiKey = apiKeyService.getApiKey(modelEnum.getCode(), playerId);

        if (StringUtils.isBlank(apiKey)) {
            throw new BizException("没有为模型:"+modelEnum.getCode()+"配置APIKEY");
        }

        try {

            //需通过CGI发送的历史记录
            var cgiMessageHistory = new ArrayList<ModelChatParamHistory>();
            var cgiMessage = "";
            long cgiThreadId = dto.getThreadId();
            long playerLastMessageId = -1;

            //发送消息
            if(dto.getQueryKind() == 0){

                //获取Thread
                ChatThreadPo threadPo = chatThreadService.getSelfThread(threadId);

                //创建新会话
                if (threadPo == null) {
                    threadPo = chatThreadService.createThread(userId,playerId,modelEnum.getCode());
                }

                //锁定会话
                track.newSession(threadPo.getId());
                threadPo.setModelCode(modelEnum.getCode());

                //获取该Thread下全部聊天记录
                List<ChatMessagePo> messages = threadPo.getMessages();

                //保存用户发来的消息为一条历史记录
                var msg = new ChatMessagePo();
                msg.setThread(threadPo);
                msg.setSenderRole(0); //发送人角色 0:Player 1:Model
                msg.setSenderName(playerName);
                msg.setContent(css.encryptForCurUser(dto.getMessage())); //保存消息为密文
                msg.setSeq(messages.size() + 1);
                messages.add(msg);
                threadPo.setLastMessage(msg);

                //组装需通过CGI发送的聊天历史记录
                for(var item : messages){
                    var cgiItem = new ModelChatParamHistory();
                    cgiItem.setRole(item.getSenderRole());
                    cgiItem.setContent(css.decryptForCurUser(item.getContent()));
                    cgiItem.setSequence(item.getSeq());
                    cgiMessageHistory.add(cgiItem);
                }
                cgiMessage = dto.getMessage();
                ChatThreadPo save = threadRepository.save(threadPo);
                cgiThreadId = save.getId();
                playerLastMessageId = msg.getId();
            }

            //在指定高度消息重新生成下一条回复
            if(dto.getQueryKind() == 3){

                var rootMessagePo = chatMessageService.getSelfMessage(dto.getRegenerateRootHistoryId());
                var threadPo = rootMessagePo.getThread();
                var threadMessages = threadPo.getMessages();

                //发送人角色 0:Player 1:Model
                if(rootMessagePo.getSenderRole() != 0){
                    throw new BizException("消息发送人类型错误 需为Player");
                }

                //删除根消息记录之后的所有记录
                threadMessages.removeIf(next -> next.getSeq() > rootMessagePo.getSeq());

                //锁定会话
                track.newSession(threadPo.getId());
                threadPo.setModelCode(modelEnum.getCode());

                //组装需通过CGI发送的聊天历史记录
                for(var item : threadMessages){
                    var cgiItem = new ModelChatParamHistory();
                    cgiItem.setRole(item.getSenderRole());
                    cgiItem.setContent(css.decryptForCurUser(item.getContent()));
                    cgiItem.setSequence(item.getSeq());
                    cgiMessageHistory.add(cgiItem);
                }
                cgiMessage = css.decryptForCurUser(rootMessagePo.getContent());
                ChatThreadPo save = threadRepository.save(threadPo);
                cgiThreadId = save.getId();
                playerLastMessageId = rootMessagePo.getId();
            }

            //创建起始分片
            var cf = new ChatFragment();
            cf.setType(0); //0:起始 1:数据 2:结束 10:错误
            cf.setPlayerId(playerId);
            cf.setThreadId(threadId);
            cf.setContent(null); //起始消息无内容
            cf.setSeq(0);
            mccq.receive(cf);

            // 获取代理配置
            String proxyConfig = getProxyConfig(playerId);

            // 创建HTTP客户端
            OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 60);

            // 创建请求DTO
            ModelChatParam modelChatParam = createModelChatDto(modelEnum.getCode(), playerId);
            modelChatParam.setMessage(dto.getMessage());
            modelChatParam.setApiKey(apiKey);

            // 根据模型类型设置不同的URL
            if (dto.getModel().contains("grok")) {
                modelChatParam.setUrl(GROK_BASE_URL);
            } else {
                modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent");
            }

            modelChatParam.setHistories(cgiMessageHistory);
            modelChatParam.setMessage(cgiMessage);

            // 根据模型类型选择不同的服务发送请求
            if (dto.getModel().contains("grok")) {
                // 使用GROK服务
                modelGrokService.sendMessageStream(
                        client,
                        modelChatParam,
                        onModelMessageRcv(cgiThreadId, userId,playerId)
                );
            }
            if(dto.getModel().contains("gemini")){
                // 使用Gemini服务
                modelGeminiService.sendMessageStream(
                        client,
                        modelChatParam,
                        onModelMessageRcv(cgiThreadId, userId,playerId)
                );
            }

            // 返回用户消息作为第一次响应
            ChatSegmentVo vo = new ChatSegmentVo();
            vo.setThreadId(cgiThreadId);
            vo.setHistoryId(playerLastMessageId);
            vo.setRole(0); // 用户角色
            vo.setSequence(cf.getSeq());
            vo.setContent(dto.getMessage()); // 返回用户的消息内容
            vo.setType(0); // 起始类型，而不是数据类型
            vo.setName("User");
            vo.setAvatarPath(null);
            return vo;

        } catch (Exception e) {
            // 发生异常时通知会话失败
            if (!threadId.equals(-1L)) {
                track.notifyFailed(threadId);
                track.closeSession(threadId);
            }

            throw new BizException("发送批量聊天消息失败: " + e.getMessage());
        }
    }


    /**
     * 查询批量聊天响应
     * @param dto 批量聊天请求参数
     * @return 聊天片段VO
     * @throws BizException 业务异常
     */
    public ChatSegmentVo chatCompleteQueryBatch(BatchChatCompleteDto dto) throws BizException {

        long threadId = dto.getThreadId();
        long playerId = AuthService.requirePlayerId();
        var ret = new ChatSegmentVo();

        try{
            //消费消息队列中该Thread的消息片段
            var first = mccq.next(threadId);

            if(first.getPlayerId() != playerId){
                mccq.receive(first);
                throw new AuthException("你没有权限访问该内容");
            }

            /*
             * 分片类型 0:起始 1:数据 2:结束 10:错误
             * 消费逻辑
             * 1.当首个段`firstFragment`为起始、错误片段时需立即返回内容给客户端
             * 2.当首个段为数据段,尝试获取尽可能多的数据段并将其组装到一个客户端响应中
             * 在消费数据片段时获取到"结束"或"错误"片段时立即返回已组装的客户端响应,并将已获取到的"结束"或"错误"片段重新放入队头
             */
            if(first.getType() == 0 || first.getType() == 10){
                ret.setThreadId(threadId);
                ret.setHistoryId(-1L);
                ret.setRole(1);
                ret.setSequence(first.getSeq());
                ret.setContent(first.getContent());
                ret.setType(first.getType());
                ret.setName(first.getSenderName());
                ret.setAvatarPath(first.getSenderAvatarUrl());
                if(first.getType() == 10){
                    ret.setContent(StringUtils.isNotBlank(first.getContent()) ? first.getContent() : "模型响应时出现未知错误");
                }
                return ret;
            }

            ret.setThreadId(threadId);
            ret.setHistoryId(-1L);
            ret.setRole(1);
            ret.setSequence(first.getSeq());
            ret.setContent("----");
            ret.setType(first.getType());
            ret.setName(first.getSenderName());
            ret.setAvatarPath(first.getSenderAvatarUrl());

            StringBuilder content = new StringBuilder(first.getContent());

            //处理消息片段
            while (mccq.hasNext(threadId)){

                ChatFragment next = mccq.next(threadId);

                if(next.getType() == 10 || next.getType() == 2){
                    mccq.receive(next);
                    break;
                }

                content.append(mccq.next(threadId).getContent());
            }

            ret.setContent(content.toString());
            return ret;

        }catch (TimeoutException e){
            throw new BizException(e.getMessage());
        }

    }

    /**
     * 终止批量聊天响应
     * @param dto 批量聊天请求参数
     * @throws BizException 业务异常
     */
    public void chatCompleteTerminateBatch(BatchChatCompleteDto dto) throws BizException {

        var threadPo = chatThreadService.getSelfThread(dto.getThreadId());

        //检查会话状态
        int status = track.getStatus(threadPo.getId());
        if(status != 0 && status != 1){
            throw new BizException("该会话未在进行中或已经终止");
        }

        //通知会话已终止
        track.notifyTerminated(threadPo.getId());

        //创建终止片段
        var cf = new ChatFragment();
        cf.setType(2);
        cf.setSenderName(threadPo.getModelCode());
        cf.setSenderAvatarUrl("");
        cf.setPlayerId(AuthService.requirePlayerId());
        cf.setThreadId(threadPo.getId());
        cf.setContent("用户已终止模型响应");
        mccq.receive(cf);
    }

    /**
     * 恢复会话
     * @param dto 包含threadId参数
     * @return 返回会话信息和历史消息
     * @throws BizException 业务异常
     */
    public RecoverChatVo recoverChat(RecoverChatDto dto) throws BizException {

        var query = new ChatThreadPo();
        query.setId(dto.getThreadId());
        query.setUser(Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class));
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        ChatThreadPo thread = threadRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("会话不存在或无权限访问"));

        playerConfigService.put(UserConfigEnum.MODEL_CHAT_CURRENT_THREAD.key(), thread.getId());

        // 获取会话历史记录
        List<ChatMessagePo> messages = thread.getMessages();
        
        // 创建响应VO
        RecoverChatVo vo = new RecoverChatVo();
        vo.setThreadId(thread.getId());
        vo.setModelCode(thread.getModelCode());
        
        // 转换历史记录
        List<RecoverChatHistoryVo> messageVos = new ArrayList<>();
        if (messages != null && !messages.isEmpty()) {
            for (ChatMessagePo history : messages) {
                RecoverChatHistoryVo messageVo = new RecoverChatHistoryVo();
                
                // 手动映射特定字段
                messageVo.setId(history.getId());
                messageVo.setContent(css.decryptForCurUser(history.getContent()));
                messageVo.setCreateTime(history.getCreateTime());
                
                //发送人角色 0:Player 1:Model
                messageVo.setRole(history.getSenderRole());
                
                // 根据角色设置名称和头像
                if (history.getSenderRole() == 0) {
                    // 用户消息
                    messageVo.setName("User");
                    messageVo.setAvatarPath("");
                }
                
                if (history.getSenderRole() == 1) {
                    // AI消息 - 设置为模型系列名称
                    AIModelEnum modelEnum = AIModelEnum.getByCode(thread.getModelCode());
                    String aiName = modelEnum != null ? modelEnum.getSeries() : "AI助手";
                    messageVo.setName(aiName);
                    messageVo.setAvatarPath("");
                }
                
                messageVos.add(messageVo);
            }
        }
        
        vo.setMessages(messageVos);
        return vo;
    }

    /**
     * 获取用户空间或全局的代理配置
     * @param playerId 玩家ID
     * @return 代理url
     */
    public String getProxyConfig(Long playerId){
        // 获取代理配置 - 首先检查用户级别的代理配置
        String proxyConfig = playerConfigService.getString("model.proxy.config", null,playerId);

        // 如果用户未配置代理，则使用全局代理配置
        if (StringUtils.isBlank(proxyConfig)) {
            proxyConfig = globalConfigService.get("model.proxy.config");
        }
        return proxyConfig;
    }

    /**
     * 创建处理模型消息回调的Consumer
     *
     * @param threadId 聊天线程ID
     * @param userId 用户ID
     * @return 处理模型消息的Consumer
     */
    private Consumer<ModelChatContext> onModelMessageRcv(long threadId, Long userId,Long playerId) {
        return context -> {
            try {
                String modelCode = context.getModelCode();
                AIModelEnum modelEnum = AIModelEnum.getByCode(modelCode);

                //检查该线程是否被终止 0:正在等待响应(Pending) 1:正在回复(Receive) 2:已结束(Finished) 10:已失败(Failed) 11:已终止(Terminated)
                if(track.getStatus(threadId) == 11){
                    // 如果是完成或错误回调，需要关闭Session
                    if (context.getType() == 1 || context.getType() == 2) {
                        track.closeSession(threadId);
                    }
                    return;
                }

                //当为正在等待响应状态 首次收到消息时需要转换状态
                if(track.getStatus(threadId) == 0){
                    track.notifyReceive(threadId);
                }

                // 获取当前最大序号
                int nextSequence = modelSegmentRepository.findMaxSequenceByThreadId(threadId) + 1;

                // 根据context.type处理不同类型的消息
                if (context.getType() == 0) {
                    // 数据类型 - 创建数据片段
                    var cf = new ChatFragment();
                    cf.setType(0);
                    cf.setSenderName(context.getModelCode());
                    cf.setSenderAvatarUrl("");
                    cf.setPlayerId(playerId);
                    cf.setThreadId(threadId);
                    cf.setContent(context.getContent());
                    cf.setSeq(nextSequence);
                    mccq.receive(cf);
                    return;
                }

                if (context.getType() == 1) {

                    var messagePo = new ChatMessagePo();
                    messagePo.setThread(Any.of().val("id", threadId).as(ChatThreadPo.class));
                    messagePo.setSenderRole(1);
                    messagePo.setSenderName(modelCode);
                    messagePo.setContent(css.encryptForCurUser(context.getContent()));
                    messagePo.setSeq(messageRepository.getCountByThreadId(threadId) + 1);
                    messagePo.setTokenInput(Long.valueOf(context.getTokenInput()));
                    messagePo.setTokenOutput(Long.valueOf(context.getTokenOutput()));
                    messagePo.setTokenThoughts(Long.valueOf(context.getTokenThoughtsOutput()));
                    messageRepository.save(messagePo);

                    var cf = new ChatFragment();
                    cf.setType(2);
                    cf.setSenderName(modelCode);
                    cf.setSenderAvatarUrl("");
                    cf.setPlayerId(playerId);
                    cf.setThreadId(threadId);
                    cf.setContent(context.getContent());
                    mccq.receive(cf);

                    // 通知会话已结束
                    track.notifyFinished(threadId);
                    track.closeSession(threadId);

                    if (modelEnum != null) {
                        //异步生成会话标题
                        /*Thread.ofVirtual().start(()->{
                            try {
                                generateThreadTitle(thread.getId(), modelEnum.getCode(),userId,playerId);
                            } catch (Exception e) {
                                log.error("生成会话标题失败", e);
                                // 生成标题失败不影响主流程
                            }
                        });*/
                    }
                    return;
                }

                if (context.getType() == 2) {
                    // 错误类型 - 创建错误片段
                    var cf = new ChatFragment();
                    cf.setType(10);
                    cf.setSenderName(modelCode);
                    cf.setSenderAvatarUrl("");
                    cf.setPlayerId(playerId);
                    cf.setThreadId(threadId);
                    cf.setContent(context.getException() != null ? "AI响应错误: " + context.getException().getMessage() : "AI响应错误");
                    mccq.receive(cf);

                    // 通知会话已失败
                    track.notifyFailed(threadId);
                    track.closeSession(threadId);
                }
            } catch (Exception e) {
                log.error("处理聊天片段失败", e);
            }
        };
    }

    /**
     * 创建空会话
     * @param dto 创建空会话请求参数
     * @return 新创建的会话ID
     * @throws BizException 业务异常
     */
    public CreateEmptyThreadVo createEmptyThread(CreateEmptyThreadDto dto) throws BizException {

        //获取当前玩家ID
        Long playerId = AuthService.getCurrentPlayerId();
        
        //获取当前玩家已有的会话数量
        long threadCount = threadRepository.getPlayerThreadCount(playerId);

        //生成新会话标题
        String title = String.format("新会话#%d", threadCount + 1);
        
        //创建新会话
        ChatThreadPo thread = new ChatThreadPo();
        thread.setType(0);//Thread类型 0:标准会话 1:RP会话 2:标准增强会话
        thread.setUser(Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class));
        thread.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));
        thread.setNpc(null);
        thread.setTitle(title);
        thread.setPublicInfo(null);
        thread.setDescription(null);
        thread.setTitleGenerated(0);
        thread.setModelCode(dto.getModel());
        thread.setActive(-1);
        thread.setDescription(css.encryptForCurUser(thread.getDescription()));
        threadRepository.save(thread);
        // 返回新创建的会话ID
        return CreateEmptyThreadVo.of(thread.getId());
    }

}