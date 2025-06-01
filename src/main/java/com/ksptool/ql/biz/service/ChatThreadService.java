package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ChatMessageRepository;
import com.ksptool.ql.biz.mapper.ChatThreadRepository;
import com.ksptool.ql.biz.mapper.NpcRepository;
import com.ksptool.ql.biz.mapper.PlayerRepository;
import com.ksptool.ql.biz.model.dto.GetThreadListDto;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.dto.SelectThreadDto;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.schema.ModelVariantSchema;
import com.ksptool.ql.biz.model.vo.GetThreadListVo;
import com.ksptool.ql.biz.model.vo.SelectThreadMessageVo;
import com.ksptool.ql.biz.model.vo.SelectThreadVo;
import com.ksptool.ql.biz.model.vo.UserSessionVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.restcgi.model.CgiChatMessage;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import com.ksptool.ql.restcgi.service.ModelRestCgi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatThreadService {

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
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ModelRestCgi restCgi;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private NpcRepository npcRepository;

    @Autowired
    private ModelVariantService modelVariantService;


    //玩家选中Thread
    @Transactional
    public SelectThreadVo selectThread(SelectThreadDto dto) throws BizException {

        var player = AuthService.requirePlayer();
        var ret = new SelectThreadVo();

        ChatThreadPo threadPo = null;

        //根据直接指定的ThreadId获取
        if(dto.getThreadId() != null){
            threadPo = getSelfThread(dto.getThreadId());
        }
        //根据NPC指定
        if(dto.getNpcId() != null){
            threadPo = repository.getActiveThreadByNpcId(dto.getNpcId(),AuthService.requirePlayerId(),AuthService.requireUserId());
        }

        if(threadPo == null){

            //如果是NPC会话 则后端自动创建新会话
            if(dto.getNpcId() != null){
                threadPo = createSelfNpcThread(modelVariantService.requireModelSchema(dto.getModelCode()),dto.getNpcId());
            }
            if(dto.getNpcId() == null){
                throw new BizException("未能找到活跃会话");
            }
        }

        ret.setThreadId(threadPo.getId());
        ret.setModelCode(threadPo.getModelCode());

        //分页查询Message
        Page<ChatMessagePo> pPos = chatMessageRepository.getByThreadId(ret.getThreadId(), dto.pageRequest());

        var msgVos = new ArrayList<SelectThreadMessageVo>();
        var sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");

        //转换消息为vo
        for(var po : pPos.getContent()){
            var vo = new SelectThreadMessageVo();
            vo.setId(po.getId());
            vo.setSenderName(po.getSenderName());
            vo.setSenderAvatarUrl("");
            vo.setSenderRole(po.getSenderRole());
            vo.setContent(css.decryptForCurUser(po.getContent()));
            vo.setCreateTime(sdf.format(po.getCreateTime()));

            //处理头像 发送人角色 0:Player 1:Model
            if(po.getSenderRole() == 0){
                vo.setSenderAvatarUrl(player.getPlayerAvatarUrl());
            }

            //如果Thread是一个RP会话 则需要使用所属NPC的头像与名称
            //Thread类型 0:标准会话 1:RP会话 2:标准增强会话
            if(po.getSenderRole() == 1 && threadPo.getType() == 1){
                vo.setSenderAvatarUrl(threadPo.getNpc().getAvatarUrlPt(css));
            }

            if(po.getSenderRole() == 1 && threadPo.getType() != 1){
                vo.setSenderAvatarUrl("");
            }

            msgVos.add(vo);
        }

        ret.setMessages(new RestPageableView<>(msgVos, pPos.getTotalElements()));

        if(threadPo.getNpc() == null){
            //取消其他所有会话的激活
            repository.deActiveAllStandardThread(player.getPlayerId(),player.getUserId());
            //直接激活当前会话
            repository.activeThread(threadPo.getId());
        }

        if(threadPo.getNpc() != null){
            Long npcId = threadPo.getNpc().getId();
            repository.deActiveThreadByNpc(npcId);
            repository.activeThread(threadPo.getId());
            npcRepository.deActiveAllNpc(player.getPlayerId());
            npcRepository.activeNpc(npcId);
        }

        return ret;
    }

    //获取对话历史列表
    public RestPageableView<GetThreadListVo> getThreadList(GetThreadListDto dto) throws BizException {

        Long uid = AuthService.requireUserId();
        Long pid = AuthService.requirePlayerId();
        Long nid = null;

        if(dto.getType() == 1){
            nid = dto.getNpcId();
        }

        Page<GetThreadListVo> pPos = repository.getThreadListWithLastMessage(pid,uid,nid,dto.getType(),dto.getTitle(), dto.pageRequest());
        List<GetThreadListVo> vos = new ArrayList<>();

        for(var vo : pPos.getContent()){

            // 获取最后一条消息（如果有）作为预览

            if(vo.getLastMessage() != null){
                //截取前50个字符作为预览
                String content = css.decryptForCurUser(vo.getLastMessage());
                vo.setLastMessage(content.length() > 50 ?
                        content.substring(0, 50) + "..." :
                        content);
            }
            if(vo.getLastMessage() == null){
                vo.setLastMessage("无消息……");
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


    public ChatThreadPo getSelfThread(long threadId) throws BizException{
        var query = new ChatThreadPo();
        query.setId(threadId);
        query.setUser(Any.of().val("id",AuthService.requireUserId()).as(UserPo.class));
        query.setPlayer(Any.of().val("id",AuthService.requirePlayerId()).as(PlayerPo.class));
        return repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("会话不存在或无权访问"));
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

            ChatThreadPo threadPo = repository.getThread(threadId);
            Long playerId = threadPo.getPlayer().getId();

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
            ModelVariantSchema model = modelVariantService.requireModelSchema(threadPo.getModelCode());

            if(model == null){
                log.warn("无法为会话 {} 生成标题 原因:Thread未配置modelCode", threadId);
                return;
            }

            String apikey = apiKeyService.getApiKey(model.getCode(), playerId);

            if(StringUtils.isBlank(apikey)){
                log.warn("无法为会话 {} 生成标题 原因:未配置APIKEY", threadId);
                return;
            }

            // 从配置获取提示语模板
            String promptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_PROMPT.getKey(),
                    GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_PROMPT.getDefaultValue());

            String plainUserDek = css.getPlainUserDek(threadPo.getUser().getId());

            //组合Prompt
            PreparedPrompt prompt = PreparedPrompt.prepare(promptTemplate);
            prompt.setParameter("userContent", css.decryptForCurUser(playerMessage.getContent(),plainUserDek));
            prompt.setParameter("modelContent", css.decryptForCurUser(modelMessage.getContent(),plainUserDek));

            var param = new ModelChatParam();
            param.setMessage(prompt.execute());

            var p = new CgiChatParam();
            p.setModel(model);
            p.setApikey(apiKeyService.getApiKey(model.getCode(),playerId));
            p.setHttpClient(HttpClientUtils.createHttpClient(playerConfigService.getSelfProxyUrl(playerId), 30));
            p.setTemperature(0.7);
            p.setTopP(1);
            p.setTopK(40);
            p.setMaxOutputTokens(128);
            p.setMessage(new CgiChatMessage(prompt.execute()));
            CgiChatResult result = restCgi.sendMessage(p);

            var title = result.getContent();

            if(StringUtils.isBlank(title)){
                log.warn("无法为会话 {} 生成标题 原因:模型返回空内容:{}", threadPo.getId(),title);
                return;
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
    public ChatThreadPo createSelfThread(ModelVariantSchema model,int type) throws BizException {

        if(type != 0 && type != 2){
            throw new BizException("对话类型只允许 标准会话、增强会话");
        }

        var playerPo = playerRepository.findById(AuthService.requirePlayerId())
                .orElseThrow(() -> new BizException("Player不存在或无权访问"));

        var userPo = Any.of().val("id",AuthService.requireUserId()).as(UserPo.class);
        var insert = new ChatThreadPo();
        insert.setType(type); //0:标准会话 1:RP会话 2:标准增强会话
        insert.setUser(userPo);
        insert.setPlayer(playerPo);
        insert.setNpc(null);
        insert.setTitle("新会话" + (getSelfThreadCount(0) + 1));
        insert.setPublicInfo(null);
        insert.setDescription(null);
        insert.setTitleGenerated(0);
        insert.setModelCode(model.getCode());
        insert.setActive(1);
        insert.setMessages(new ArrayList<>());
        return repository.save(insert);
    }

    @Transactional(rollbackFor = BizException.class)
    public ChatThreadPo createSelfNpcThread(ModelVariantSchema model, Long npcId) throws BizException {

        var playerPo = playerRepository.findById(AuthService.requirePlayerId())
                .orElseThrow(() -> new BizException("Player不存在或无权访问"));
        var npcPo = npcRepository.findById(npcId)
                .orElseThrow(() -> new BizException("Npc不存在或无权访问"));

        //将该NPC下其他会话置于存档
        repository.deActiveThreadByNpc(npcPo.getId());

        UserSessionVo player = AuthService.requirePlayer();
        var userPo = Any.of().val("id",player.getUserId()).as(UserPo.class);
        var insert = new ChatThreadPo();
        insert.setType(1); //0:标准会话 1:RP会话 2:标准增强会话
        insert.setUser(userPo);
        insert.setPlayer(playerPo);
        insert.setNpc(npcPo);
        var prompt = new PreparedPrompt("#{player}与#{npc}的对话");
        prompt.setParameter("player",player.getPlayerName());
        prompt.setParameter("npc",npcPo.getName());
        insert.setTitle(prompt.execute());
        insert.setPublicInfo(prompt.execute());
        insert.setDescription(null);
        insert.setTitleGenerated(1);
        insert.setModelCode(model.getCode());
        insert.setActive(1);
        insert.setMessages(new ArrayList<>());
        insert.setLastMessage(null);

        //如果NPC有开场白 需要解析开场白并创建历史消息
        String firstMessage = css.decryptForCurUser(npcPo.getFirstMessage());
        ArrayList<ChatMessagePo> messagePos = new ArrayList<>();

        if(StringUtils.isNotBlank(firstMessage)){
            var cmp = new ChatMessagePo();
            cmp.setThread(insert);
            cmp.setSenderRole(1); //发送人角色 0:Player 1:Model
            cmp.setSenderName(npcPo.getName());
            var fmp = PreparedPrompt.prepare(firstMessage);
            fmp.setParameter("player",playerPo.getName());
            fmp.setParameter("npc",npcPo.getName());
            cmp.setContent(css.encryptForCurUser(fmp.executeNested(false)));
            cmp.setSeq(0);
            messagePos.add(cmp);
            insert.setMessages(messagePos);
        }

        //取消其他NPC的选中
        npcRepository.deActiveAllNpc(playerPo.getId());
        npcRepository.activeNpc(npcPo.getId());
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

    //将玩家下的所有会话设定为不活跃
    public void deActiveAllSelfThread(){



    }

    //将玩家下某一个NPC下的所有会话设置为不活跃
    public void deActiveAllNpcSelfThread(long npcId){




    }




}
