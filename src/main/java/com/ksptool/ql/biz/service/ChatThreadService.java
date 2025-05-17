package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ChatMessageRepository;
import com.ksptool.ql.biz.mapper.ChatThreadRepository;
import com.ksptool.ql.biz.model.dto.GetThreadListDto;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.vo.GetThreadListVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.exception.AuthException;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import com.ksptool.ql.commons.web.RestPageableView;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ksptool.entities.Entities.as;

@Slf4j
@Service
public class ChatThreadService {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GROK_BASE_URL = "https://api.x.ai/v1/chat/completions";

    @Autowired
    private ChatThreadRepository repository;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private PlayerConfigService playerConfigService;

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ModelGrokService modelGrokService;

    @Autowired
    private ModelGeminiService modelGeminiService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;


    //获取对话历史列表
    public RestPageableView<GetThreadListVo> getThreadList(GetThreadListDto dto) throws BizException {

        Long uid = AuthService.requireUserId();
        Long pid = AuthService.requirePlayerId();
        Long nid = null;

        if(dto.getType() == 1){
            nid = dto.getNpcId();
        }

        Page<ChatThreadPo> pPos = repository.getThreadListWithLastMessage(pid,uid,nid,dto.getType(), dto.pageRequest());
        List<GetThreadListVo> vos = new ArrayList<>();

        for(var po : pPos.getContent()){
            GetThreadListVo vo = as(po,GetThreadListVo.class);

            // 获取最后一条消息（如果有）作为预览
            vo.setLastMessage("无消息……");

            if(po.getLastMessage() != null){
                //截取前50个字符作为预览
                String content = css.decryptForCurUser(po.getLastMessage().getContent());
                vo.setLastMessage(content.length() > 50 ?
                        content.substring(0, 50) + "..." :
                        content);
            }

            vos.add(vo);
        }

        return new RestPageableView<>(vos, pPos.getTotalElements());
    }

    @Transactional
    public void removeThread(Long threadId)throws BizException{

        //查询当前玩家下的Thread
        var query = new ChatThreadPo();
        query.setId(threadId);
        query.setUser(Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class));
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        ChatThreadPo po = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("消息记录不存在或无权访问"));

        //移除关联记录
        chatMessageRepository.removeByThreadId(po.getId());

        //移除Thread
        repository.delete(po);
    }

    public ChatThreadPo getSelfThread(long threadId) throws BizException{
        var query = new ChatThreadPo();
        query.setId(threadId);
        query.setUser(Any.of().val("id",AuthService.requireUserId()).as(UserPo.class));
        query.setPlayer(Any.of().val("id",AuthService.requirePlayerId()).as(PlayerPo.class));
        return repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("会话不存在或无权访问"));
    }

    @Transactional(rollbackFor = BizException.class)
    public void editThreadTitle(long threadId, String newTitle) throws BizException {

        var threadPo = this.getSelfThread(threadId);

        // 限制标题长度
        if (newTitle.length() > 100) {
            newTitle = newTitle.substring(0, 97) + "...";
        }

        threadPo.setTitle(newTitle);
        threadPo.setTitleGenerated(1);
        repository.save(threadPo);
    }

    @Async
    @Transactional(rollbackFor = BizException.class)
    public void generateThreadTitleAsync(long threadId) throws BizException {

        try{
            // 检查是否需要生成标题
            boolean shouldGenerateTitle = globalConfigService.getBoolean(GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_TITLE.getKey(), true);
            if (!shouldGenerateTitle) {
                return; // 配置为不生成标题，直接返回
            }

            ChatThreadPo threadPo = this.getSelfThread(threadId);

            if(threadPo.getTitleGenerated() != 0){
                return;
            }

            List<ChatMessagePo> messages = threadPo.getMessages();

            //没有足够的消息用于生成标题 生成标题的最低要求为一条用户消息与一条AI回复
            if(messages.size() < 2){
                log.warn("无法为会话 {} 生成标题 原因:没有足够的消息用于生成标题 当前消息计数:{}", threadId,messages.size());
                return;
            }

            ChatMessagePo playerMessage = messages.get(0);
            ChatMessagePo modelMessage = messages.get(1);

            //消息发送人类型不对应 发送人角色 0:Player 1:Model
            if(playerMessage.getSenderRole() != 0 || modelMessage.getSenderRole() != 1){
                log.warn("无法为会话 {} 生成标题 原因:消息发送人类型不对应 首条消息:{} 第二条消息:{}", threadId,playerMessage.getSenderRole(),modelMessage.getSenderRole());
                return;
            }

            //获取Thread绑定的模型及其系列
            AIModelEnum model = AIModelEnum.getByCode(threadPo.getModelCode());

            if(model == null){
                log.warn("无法为会话 {} 生成标题 原因:Thread未配置modelCode", threadId);
                return;
            }

            // 从配置获取提示语模板
            String promptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_PROMPT.getKey(),
                    GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_PROMPT.getDefaultValue());

            //组合Prompt
            PreparedPrompt prompt = PreparedPrompt.prepare(promptTemplate);
            prompt.setParameter("userContent", css.decryptForCurUser(playerMessage.getContent()));
            prompt.setParameter("modelContent", css.decryptForCurUser(modelMessage.getContent()));

            var param = new ModelChatParam();
            param.setMessage(prompt.execute());

            var proxyUrl = playerConfigService.getSelfProxyUrl();
            var apikey = apiKeyService.getSelfApiKey(model.getCode());

            if(StringUtils.isBlank(apikey)){
                log.warn("无法为会话 {} 生成标题 原因:未配置APIKEY", threadId);
                return;
            }

            param.setApiKey(apikey);

            var title = "";

            //根据模型类型选择不同的服务发送请求
            if (model.getSeries().contains("Grok")) {
                param.setUrl(GROK_BASE_URL);
                title = modelGrokService.sendMessageSync(HttpClientUtils.createHttpClient(proxyUrl, 30), param);
            } else {
                param.setUrl(GEMINI_BASE_URL + model + ":generateContent");
                title = modelGeminiService.sendMessageSync(HttpClientUtils.createHttpClient(proxyUrl, 30), param);
            }

            // 处理标题（去除引号和多余空格，限制长度）
            title = title.replaceAll("^\"|\"$", "").trim();
            if (title.length() > 100) {
                title = title.substring(0, 97) + "...";
            }

            threadPo.setTitle(title);
            threadPo.setTitleGenerated(1);
            repository.save(threadPo);
            log.info("已为会话 {} 生成标题: {}", threadId, title);
        }catch (Exception e){
            log.error("生成会话标题失败", e);
            // 生成标题失败不抛出异常，不影响主流程
        }

    }

    @Transactional(rollbackFor = BizException.class)
    public ChatThreadPo createSelfThread(AIModelEnum model,int type) throws BizException {

        if(type != 0 && type != 2){
            throw new BizException("对话类型只允许 标准会话、增强会话");
        }

        var userPo = Any.of().val("id",AuthService.requireUserId()).as(UserPo.class);
        var playerPo = Any.of().val("id",AuthService.requirePlayerId()).as(PlayerPo.class);
        var insert = new ChatThreadPo();
        insert.setType(type); //0:标准会话 1:RP会话 2:标准增强会话
        insert.setUser(userPo);
        insert.setPlayer(playerPo);
        insert.setNpc(null);
        insert.setTitle("新会话" + getSelfThreadCount(0) + 1);
        insert.setPublicInfo(null);
        insert.setDescription(null);
        insert.setTitleGenerated(0);
        insert.setModelCode(model.getCode());
        insert.setActive(1);
        insert.setMessages(new ArrayList<>());
        return repository.save(insert);
    }

    //创建新的标准会话
    public ChatThreadPo createThread(long userId,long playerId,String modelCode){
        var user = Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class);
        var player = Any.of().val("id",playerId).as(PlayerPo.class);
        var insert = new ChatThreadPo();
        insert.setType(0); //0:标准会话 1:RP会话 2:标准增强会话
        insert.setUser(user);
        insert.setPlayer(player);
        insert.setNpc(null);
        insert.setTitle("新会话");
        insert.setPublicInfo(null);
        insert.setDescription(null);
        insert.setTitleGenerated(0);
        insert.setModelCode(modelCode);
        insert.setActive(1);
        insert.setMessages(new ArrayList<>());
        return repository.save(insert);
    }

    public int getSelfThreadCount(int type){
        var query = new ChatThreadPo();
        query.setUser(Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class));
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
        query.setType(type);
        return (int)repository.count(Example.of(query));
    }





}
