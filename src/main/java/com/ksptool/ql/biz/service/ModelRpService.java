package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.*;
import com.ksptool.ql.biz.model.dto.BatchRpCompleteDto;
import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.dto.RecoverRpChatDto;
import com.ksptool.ql.biz.model.dto.DeActiveThreadDto;
import com.ksptool.ql.biz.model.dto.RemoveRpHistoryDto;
import com.ksptool.ql.biz.model.dto.EditRpHistoryDto;
import com.ksptool.ql.biz.model.dto.RemoveThreadDto;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.po.ModelRpHistoryPo;
import com.ksptool.ql.biz.model.po.ModelRpSegmentPo;
import com.ksptool.ql.biz.model.po.ModelRpThreadPo;
import com.ksptool.ql.biz.model.po.ModelUserRolePo;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.biz.model.vo.RecoverRpChatHistoryVo;
import com.ksptool.ql.biz.model.vo.RecoverRpChatVo;
import com.ksptool.ql.biz.model.vo.RpSegmentVo;
import com.ksptool.ql.biz.service.panel.PanelApiKeyService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import com.ksptool.ql.commons.web.SimpleExample;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.biz.model.vo.ModelRoleThreadListVo;
import com.ksptool.ql.biz.model.dto.GetModelRoleThreadListDto;

@Slf4j
@Service
public class ModelRpService {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GROK_BASE_URL = "https://api.x.ai/v1/chat/completions";

    // 线程安全的容器，记录RP聊天状态 <threadId, contextId>
    private final ConcurrentHashMap<Long, String> rpThreadToContextIdMap = new ConcurrentHashMap<>();
    
    // 终止列表，存储已经被终止的contextId
    private final ConcurrentHashMap<String, Boolean> terminatedContextIds = new ConcurrentHashMap<>();

    @Autowired
    private ModelRoleRepository modelRoleRepository;

    @Autowired
    private ModelRpThreadRepository threadRepository;

    @Autowired
    private ModelRpHistoryRepository historyRepository;
    
    @Autowired
    private ModelRpSegmentRepository segmentRepository;

    @Autowired
    private ModelGeminiService modelGeminiService;
    
    @Autowired
    private UserConfigService userConfigService;
    
    @Autowired
    private PanelApiKeyService panelApiKeyService;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private ModelGrokService modelGrokService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ModelUserRoleRepository modelUserRoleRepository;

    @Autowired
    private ModelUserRoleService modelUserRoleService;

    public PageableView<GetModelRoleListVo> getModelRoleList(GetModelRoleListDto dto) {

        var query = new ModelRolePo();
        query.setName(dto.getKeyword());
        query.setStatus(1);
        query.setUserId(AuthService.getCurrentUserId());

        SimpleExample<ModelRolePo> example = SimpleExample.of(query);
        example.like("name").orderBy("sortOrder");

        Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getPageSize(), example.getSort());

        Page<ModelRolePo> page = modelRoleRepository.findAll(example.get(), pageable);

        page.getContent().forEach(po -> {
            if(StringUtils.isNotBlank(po.getAvatarPath())){
                po.setAvatarPath("/res/"+po.getAvatarPath());
            }
        });
        return new PageableView<>(page, GetModelRoleListVo.class);
    }

    /**
     * 恢复或创建RP对话
     */
    @Transactional
    public RecoverRpChatVo recoverRpChat(RecoverRpChatDto dto) throws BizException{

        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModelCode());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        //查询用户拥有的模型角色
        ModelRolePo example = new ModelRolePo();
        example.setId(dto.getModelRoleId());
        example.setUserId(AuthService.getCurrentUserId());

        ModelRolePo modelPlayRole = modelRoleRepository.findOne(Example.of(example))
            .orElseThrow(() -> new BizException("模型角色不存在或无权访问"));

        //处理存档激活逻辑
        if(dto.getThreadId()!=null){

            //查询指定ID的存档
            var query = new ModelRpThreadPo();
            query.setUserId(AuthService.getCurrentUserId());
            query.setModelRole(modelPlayRole);
            query.setId(dto.getThreadId());

            ModelRpThreadPo thread = threadRepository.findOne(Example.of(query)).orElseThrow(() -> new BizException("ThreadId无效!"));

            //取消全部Thread的激活状态
            threadRepository.setAllThreadActive(AuthService.getCurrentUserId(),modelPlayRole.getId(),0);
            entityManager.clear();

            //激活指定存档
            thread.setActive(1);
            threadRepository.save(thread);
        }

        // 2. 查询激活的存档
        ModelRpThreadPo thread = threadRepository.findActiveThreadByModelRoleId(dto.getModelRoleId());

        // 如果 newThread=0，将已有激活的存档置为未激活
        if (dto.getNewThread() == 0) {
            thread.setActive(0);
            threadRepository.save(thread);
            thread = null; // 重置thread，后续会创建新的存档
        }

        // 如果没有激活的Thread，创建新Thread
        if (thread == null) {

            //查询用户当前所扮演的角色
            ModelUserRolePo userPlayRole = modelUserRoleService.getUserPlayRole(AuthService.getCurrentUserId());

            if(userPlayRole == null){
                throw new BizException("未找到用户所扮演的角色!");
            }

            thread = new ModelRpThreadPo();
            thread.setUserId(AuthService.getCurrentUserId());
            thread.setModelCode(dto.getModelCode());
            thread.setModelRole(modelPlayRole);
            thread.setUserRole(userPlayRole);
            thread.setTitle(userPlayRole.getName() + "与" + modelPlayRole.getName() + "的对话");
            thread.setActive(1);
            thread = threadRepository.save(thread);
            
            // 创建首条消息(如果有)
            if (StringUtils.isNotBlank(modelPlayRole.getFirstMessage())) {
                ModelRpHistoryPo history = new ModelRpHistoryPo();
                history.setThread(thread);
                history.setType(1); // AI消息
                history.setRawContent(modelPlayRole.getFirstMessage());
                history.setRpContent(modelPlayRole.getFirstMessage()); // 这里可能需要通过RpHandler处理
                history.setSequence(1);
                historyRepository.save(history);
            }
        }
        
        // 4. 构建返回数据
        RecoverRpChatVo vo = new RecoverRpChatVo();
        vo.setThreadId(thread.getId());
        vo.setModelCode(thread.getModelCode());

        //查询用户角色
        ModelUserRolePo userRole = thread.getUserRole();

        // 5. 获取历史记录
        List<ModelRpHistoryPo> histories = historyRepository.findByThreadIdOrderBySequence(thread.getId());
        List<RecoverRpChatHistoryVo> messages = new ArrayList<>();
        
        for (ModelRpHistoryPo history : histories) {
            RecoverRpChatHistoryVo message = new RecoverRpChatHistoryVo();
            message.setId(history.getId());
            message.setType(history.getType());
            message.setRawContent(history.getRawContent());
            message.setCreateTime(history.getCreateTime());
            
            // 设置发送者信息
            if (history.getType() == 0) { // 用户消息
                message.setName("user");
                message.setAvatarPath("");

                //如果用户扮演的角色被删除
                try{
                    if(userRole != null){
                        message.setName(thread.getUserRole().getName());
                        message.setAvatarPath(thread.getUserRole().getAvatarPath());
                    }
                }catch (Exception e){}

            }

            // AI消息
            if(history.getType() == 1){
                message.setName(modelPlayRole.getName());
                message.setAvatarPath(modelPlayRole.getAvatarPath());
            }
            
            messages.add(message);
        }

        vo.setMessages(messages);
        messages.forEach(m->{
            if(StringUtils.isNotBlank(m.getAvatarPath())){
                m.setAvatarPath("/res/"+m.getAvatarPath());
            }
        });
        return vo;
    }

    /**
     * 取消激活RP对话
     */
    @Transactional
    public void deActiveThread(DeActiveThreadDto dto) throws BizException {
        // 1. 查询用户拥有的存档
        ModelRpThreadPo thread = threadRepository.findById(dto.getThreadId())
            .orElseThrow(() -> new BizException("存档不存在"));

        // 2. 验证权限
        if (!thread.getUserId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权操作此存档");
        }

        // 3. 检查是否已经是非激活状态
        if (thread.getActive() == 0) {
            throw new BizException("存档已经是非激活状态");
        }

        // 4. 设置为非激活状态
        thread.setActive(0);
        threadRepository.save(thread);
    }

    /**
     * 发送RP对话消息
     * @param dto 批量完成RP对话的请求参数
     * @return 返回对话片段信息
     * @throws BizException 业务异常
     */
    @Transactional
    public RpSegmentVo rpCompleteSendBatch(BatchRpCompleteDto dto) throws BizException {

        // 检查该会话是否正在处理中
        if (rpThreadToContextIdMap.containsKey(dto.getThreadId())) {
            throw new BizException("该会话正在处理中，请等待AI响应完成");
        }

        //获取会话信息
        ModelRpThreadPo query = new ModelRpThreadPo();
        query.setId(dto.getThreadId());
        query.setUserId(AuthService.getCurrentUserId());
        query.setActive(1); //是否为当前激活的对话 0-存档 1-激活

        ModelRpThreadPo thread = threadRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("会话不存在或不可用"));

        //获取并验证模型配置
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        String apiKey = panelApiKeyService.getApiKey(modelEnum.getCode(), thread.getUserId());
        if (StringUtils.isBlank(apiKey)) {
            throw new BizException("未配置API Key");
        }

        //获取用户扮演的角色信息
        ModelUserRolePo userPlayRole = thread.getUserRole();

        //用户扮演的角色有可能被删除
        if(userPlayRole != null){
            userPlayRole = modelUserRoleRepository.findById(userPlayRole.getId()).orElse(null);
        }

        //获取模型扮演的角色信息
        ModelRolePo modelRole = thread.getModelRole();
        if (modelRole == null) {
            throw new BizException("模型角色信息不存在");
        }

        var mainPromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_MAIN.getKey());
        var rolePromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_ROLE.getKey());

        PreparedPrompt prompt = PreparedPrompt.prepare(mainPromptTemplate).union(rolePromptTemplate);
        prompt.setParameter("model",modelRole.getName());
        prompt.setParameter("user","user");
        prompt.setParameter("userDesc","");
        prompt.setParameter("modelDescription",modelRole.getDescription());
        prompt.setParameter("modelRoleSummary", modelRole.getRoleSummary());
        prompt.setParameter("modelScenario",modelRole.getScenario());

        if(userPlayRole != null){
            prompt.setParameter("user", userPlayRole.getName());
            prompt.setParameter("userDesc", userPlayRole.getDescription());
        }

        String finalPrompt = prompt.executeNested();

        // 保存用户消息历史
        ModelRpHistoryPo userHistory = new ModelRpHistoryPo();
        userHistory.setThread(thread);
        userHistory.setType(0); // 用户消息
        userHistory.setRawContent(dto.getMessage());
        userHistory.setRpContent(dto.getMessage()); // 这里可能需要通过RpHandler处理
        userHistory.setSequence(historyRepository.findMaxSequenceByThreadId(thread.getId()) + 1);
        historyRepository.save(userHistory);

        try {

            // 清理之前的片段（如果有）
            segmentRepository.deleteByThreadId(thread.getId());

            // 创建开始片段并返回
            ModelRpSegmentPo startSegment = new ModelRpSegmentPo();
            startSegment.setUserId(thread.getUserId());
            startSegment.setThread(thread);
            startSegment.setSequence(1);
            startSegment.setContent(null);
            startSegment.setStatus(0); // 未读状态
            startSegment.setType(0); // 开始类型
            segmentRepository.save(startSegment);

            // 获取配置
            String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
            
            // 获取代理配置 - 首先检查用户级别的代理配置
            String proxyConfig = userConfigService.get("model.proxy.config", thread.getUserId());
            
            // 如果用户未配置代理，则使用全局代理配置
            if (StringUtils.isBlank(proxyConfig)) {
                proxyConfig = globalConfigService.get("model.proxy.config");
            }
            
            OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 60);

            // 创建请求参数
            ModelChatParam modelChatParam = new ModelChatParam();
            modelChatParam.setMessage(dto.getMessage());
            modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent");
            modelChatParam.setApiKey(apiKey);
            modelChatParam.setModelCode(dto.getModel());
            modelChatParam.setSystemPrompt(finalPrompt);

            // 获取历史记录并转换为ModelChatParamHistory
            List<ModelRpHistoryPo> histories = historyRepository.findByThreadIdOrderBySequence(thread.getId());
            List<ModelChatParamHistory> paramHistories = new ArrayList<>();
            
            for (ModelRpHistoryPo history : histories) {
                ModelChatParamHistory paramHistory = new ModelChatParamHistory();
                paramHistory.setRole(history.getType()); // 设置角色类型：0-用户 1-AI助手
                paramHistory.setContent(history.getRawContent());
                paramHistories.add(paramHistory);
            }
            
            modelChatParam.setHistories(paramHistories);

            if(dto.getModel().contains("grok")){
                modelChatParam.setUrl(GROK_BASE_URL);
                // 异步调用ModelGrokService发送流式请求
                modelGrokService.sendMessageStream(
                        client,
                        modelChatParam,
                        onModelRpMessageRcv(thread, thread.getUserId())
                );
            }

            if(dto.getModel().contains("gemini")){
                // 异步调用ModelGeminiService发送流式请求
                modelGeminiService.sendMessageStream(
                        client,
                        modelChatParam,
                        onModelRpMessageRcv(thread, thread.getUserId())
                );
            }

            //将新模型选项保存到Thread
            thread.setModelCode(modelEnum.getCode());
            threadRepository.save(thread);

            // 返回用户消息作为第一次响应
            RpSegmentVo vo = new RpSegmentVo();
            vo.setThreadId(thread.getId());
            vo.setHistoryId(userHistory.getId());
            vo.setRole(0); // 用户角色
            vo.setSequence(startSegment.getSequence());
            vo.setContent(dto.getMessage()); // 返回用户的消息内容
            vo.setType(0); // 起始类型
            
            // 设置角色信息
            vo.setRoleId(null);
            vo.setRoleName("user");
            vo.setRoleAvatarPath(null);
            if(userPlayRole != null){
                vo.setRoleId(userPlayRole.getId());
                vo.setRoleName(userPlayRole.getName());
                vo.setRoleAvatarPath("/res/"+userPlayRole.getAvatarPath());
            }

            return vo;

        } catch (Exception e) {
            // 发生异常时清理会话状态
            rpThreadToContextIdMap.remove(thread.getId());

            // 清理所有片段
            try {
                segmentRepository.deleteByThreadId(thread.getId());
            } catch (Exception ex) {
                log.error("清理RP对话片段失败", ex);
            }

            throw new BizException("发送RP对话消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询RP对话响应流
     * @param dto 批量完成RP对话的请求参数
     * @return 返回对话片段信息
     * @throws BizException 业务异常
     */
    public RpSegmentVo rpCompleteQueryBatch(BatchRpCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();
        Long userId = AuthService.getCurrentUserId();
        
        // 最大等待次数和等待时间
        final int MAX_WAIT_TIMES = 3;
        final long WAIT_INTERVAL_MS = 300;
        
        // 尝试获取所有未读片段，最多等待3次
        List<ModelRpSegmentPo> unreadSegments = null;
        int waitTimes = 0;
        
        while (waitTimes < MAX_WAIT_TIMES) {
            // 一次性查询所有未读片段，按sequence排序
            unreadSegments = segmentRepository.findAllUnreadByThreadIdOrderBySequence(threadId);

            // 如果有未读片段，跳出循环
            if (!unreadSegments.isEmpty()) {
                break;
            }
            
            // 如果没有未读片段，且会话不在处理中，直接返回null
            if (!rpThreadToContextIdMap.containsKey(threadId)) {
                return null;
            }
            
            // 等待一段时间后再次尝试
            try {
                Thread.sleep(WAIT_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BizException("等待片段时被中断");
            }
            
            waitTimes++;
        }
        
        // 如果等待超时仍未获取到片段
        if (unreadSegments.isEmpty()) {
            return null;
        }
        
        // 检查权限
        if (!unreadSegments.getFirst().getUserId().equals(userId)) {
            throw new BizException("无权访问该会话");
        }

        // 优先处理错误片段(type=10)和开始片段(type=0)
        for (ModelRpSegmentPo segment : unreadSegments) {
            //开始片段，只标记该片段为已读并返回
            if (segment.getType() == 0) {
                segment.setStatus(1);
                segmentRepository.save(segment);
                RpSegmentVo vo = new RpSegmentVo();
                vo.setThreadId(threadId);
                vo.setHistoryId(segment.getHistoryId());
                vo.setSequence(segment.getSequence());
                vo.setContent(segment.getContent());
                vo.setType(segment.getType());
                vo.setRole(1);
                return vo;
            }

            //错误片段，标记所有片段为已读并返回错误信息
            if (segment.getType() == 10) {
                for (ModelRpSegmentPo seg : unreadSegments) {
                    seg.setStatus(1);
                }
                segmentRepository.saveAll(unreadSegments);
                RpSegmentVo vo = new RpSegmentVo();
                vo.setThreadId(threadId);
                vo.setSequence(segment.getSequence());
                vo.setContent(segment.getContent() != null ? segment.getContent() : "AI响应出错");
                vo.setType(segment.getType());
                vo.setRole(1);
                return vo;
            }
        }

        // 获取第一个片段
        ModelRpSegmentPo firstSegment = unreadSegments.getFirst();
        
        // 如果第一个片段是数据片段(type=1)，则合并后续的数据片段
        if (firstSegment.getType() == 1) {
            StringBuilder combinedContent = new StringBuilder();
            List<ModelRpSegmentPo> segmentsToMark = new ArrayList<>();
            
            // 遍历所有片段，合并type=1的片段，直到遇到非type=1的片段
            for (ModelRpSegmentPo segment : unreadSegments) {

                if (segment.getType() != 1) {
                    break;
                }

                combinedContent.append(segment.getContent());
                segment.setStatus(1);
                segmentsToMark.add(segment);
            }

            segmentRepository.saveAll(segmentsToMark);
            
            // 返回合并后的数据片段
            RpSegmentVo vo = new RpSegmentVo();
            vo.setThreadId(threadId);
            vo.setHistoryId(null);
            vo.setSequence(firstSegment.getSequence());
            vo.setContent(combinedContent.toString().toString());
            vo.setType(1);
            vo.setRole(1);
            return vo;
        }
        
        // 如果第一个片段是type=2结束片段
        firstSegment.setStatus(1);
        segmentRepository.save(firstSegment);
        
        RpSegmentVo vo = new RpSegmentVo();
        vo.setThreadId(threadId);
        vo.setHistoryId(firstSegment.getHistoryId());
        vo.setSequence(firstSegment.getSequence());
        vo.setContent(firstSegment.getContent());
        vo.setType(firstSegment.getType());
        vo.setRole(1);
        return vo;
    }
    
    /**
     * 终止RP对话响应
     * @param dto 批量完成RP对话的请求参数
     * @throws BizException 业务异常
     */
    public void rpCompleteTerminateBatch(BatchRpCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();
        Long userId = AuthService.getCurrentUserId();

        // 检查会话是否存在
        ModelRpThreadPo thread = threadRepository.findById(threadId).orElse(null);
        if (thread == null) {
            throw new BizException("会话不存在");
        }

        // 检查权限
        if (!thread.getUserId().equals(userId)) {
            throw new BizException("无权访问该会话");
        }

        // 获取当前正在进行的contextId
        String contextId = rpThreadToContextIdMap.get(threadId);
        if (contextId == null) {
            throw new BizException("该会话未在进行中或已经终止");
        }

        // 将contextId加入终止列表
        terminatedContextIds.put(contextId, true);
        
        // 清理会话状态
        rpThreadToContextIdMap.remove(threadId);

        // 获取当前最大序号
        int nextSequence = segmentRepository.findMaxSequenceByThreadId(threadId) + 1;

        // 创建终止片段
        ModelRpSegmentPo endSegment = new ModelRpSegmentPo();
        endSegment.setUserId(userId);
        endSegment.setThread(thread);
        endSegment.setSequence(nextSequence);
        endSegment.setContent("用户终止了AI响应");
        endSegment.setStatus(0); // 未读状态
        endSegment.setType(2); // 结束类型
        segmentRepository.save(endSegment);
    }

    /**
     * 创建处理模型消息回调的Consumer
     * @param thread RP聊天线程
     * @param userId 用户ID
     * @return 处理模型消息的Consumer
     */
    private Consumer<ModelChatContext> onModelRpMessageRcv(ModelRpThreadPo thread, Long userId) {
        return context -> {
            try {
                // 从ModelChatContext中获取contextId
                String contextId = context.getContextId();
                Long threadId = thread.getId();
                String modelCode = context.getModelCode();
                AIModelEnum modelEnum = AIModelEnum.getByCode(modelCode);

                // 检查该contextId是否已被终止
                if (terminatedContextIds.containsKey(contextId)) {
                    // 如果是完成或错误回调，从终止列表中移除
                    if (context.getType() == 1 || context.getType() == 2) {
                        terminatedContextIds.remove(contextId);
                    }
                    return;
                }

                // 首次收到消息时，将contextId存入映射表
                if (!rpThreadToContextIdMap.containsValue(contextId)) {
                    rpThreadToContextIdMap.put(threadId, contextId);
                }

                // 获取当前最大序号
                int nextSequence = segmentRepository.findMaxSequenceByThreadId(threadId) + 1;
                
                // 根据context.type处理不同类型的消息
                if (context.getType() == 0) {
                    // 数据类型 - 创建数据片段
                    ModelRpSegmentPo dataSegment = new ModelRpSegmentPo();
                    dataSegment.setUserId(userId);
                    dataSegment.setThread(thread);
                    dataSegment.setSequence(nextSequence);
                    dataSegment.setContent(context.getContent());
                    dataSegment.setStatus(0); // 未读状态
                    dataSegment.setType(1); // 数据类型
                    segmentRepository.save(dataSegment);
                    return;
                }
                
                if (context.getType() == 1) {
                    // 保存AI响应到历史记录
                    ModelRpHistoryPo aiHistory = new ModelRpHistoryPo();
                    aiHistory.setThread(thread);
                    aiHistory.setType(1); // AI消息
                    aiHistory.setRawContent(context.getContent());
                    aiHistory.setRpContent(context.getContent()); // 这里可能需要通过RpHandler处理
                    aiHistory.setSequence(historyRepository.findMaxSequenceByThreadId(threadId) + 1);
                    historyRepository.save(aiHistory);
                    
                    // 完成类型 - 创建结束片段
                    ModelRpSegmentPo endSegment = new ModelRpSegmentPo();
                    endSegment.setUserId(userId);
                    endSegment.setThread(thread);
                    endSegment.setSequence(nextSequence);
                    endSegment.setContent(null);
                    endSegment.setStatus(0); // 未读状态
                    endSegment.setType(2); // 结束类型
                    endSegment.setHistoryId(aiHistory.getId()); // 设置关联的历史记录ID
                    segmentRepository.save(endSegment);

                    // 更新会话使用的模型
                    if (modelEnum != null) {
                        thread.setModelCode(modelEnum.getCode());
                        threadRepository.save(thread);
                    }
                    
                    // 清理会话状态
                    rpThreadToContextIdMap.remove(threadId);
                    return;
                }
                
                if (context.getType() == 2) {
                    // 错误类型 - 创建错误片段
                    ModelRpSegmentPo errorSegment = new ModelRpSegmentPo();
                    errorSegment.setUserId(userId);
                    errorSegment.setThread(thread);
                    errorSegment.setSequence(nextSequence);
                    errorSegment.setContent(context.getException() != null ? "AI响应错误: " + context.getException().getMessage() : "AI响应错误");
                    errorSegment.setStatus(0); // 未读状态
                    errorSegment.setType(10); // 错误类型
                    segmentRepository.save(errorSegment);

                    // 清理会话状态
                    rpThreadToContextIdMap.remove(threadId);
                }
            } catch (Exception e) {
                log.error("处理RP对话片段失败", e);
            }
        };
    }

    /**
     * 重新生成AI最后一条回复
     * @param dto 批量完成RP对话的请求参数
     * @return 返回对话片段信息
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public RpSegmentVo rpCompleteRegenerateBatch(BatchRpCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();
        Long userId = AuthService.getCurrentUserId();

        // 检查会话是否存在
        ModelRpThreadPo thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new BizException("会话不存在"));

        // 检查权限
        if (!thread.getUserId().equals(userId)) {
            throw new BizException("无权访问该会话");
        }

        // 获取所有历史记录
        List<ModelRpHistoryPo> histories = historyRepository.findByThreadIdOrderBySequence(threadId);
        if (histories.isEmpty()) {
            throw new BizException("会话历史记录为空");
        }

        // 获取最后一条历史记录
        ModelRpHistoryPo lastHistory = histories.getLast();
        String messageToRegenerate;

        // 如果最后一条是AI的回复，则删除它
        if (lastHistory.getType() == 1) {
            histories.removeLast();
            historyRepository.delete(lastHistory);
            
            if (histories.isEmpty()) {
                throw new BizException("未找到任何历史消息");
            }
            
            lastHistory = histories.getLast();
            if (lastHistory.getType() != 0) {
                throw new BizException("未找到用户消息");
            }
        }

        // 使用最后一条用户消息作为当前要发送的消息
        messageToRegenerate = lastHistory.getRawContent();

        // 获取并验证模型配置
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        String apiKey = panelApiKeyService.getApiKey(modelEnum.getCode(), userId);
        if (StringUtils.isBlank(apiKey)) {
            throw new BizException("未配置API Key");
        }

        // 获取角色信息
        ModelRolePo modelRole = thread.getModelRole();
        ModelUserRolePo userPlayRole = thread.getUserRole();

        //用户扮演的角色有可能被删除
        if(userPlayRole != null){
            userPlayRole = modelUserRoleRepository.findById(userPlayRole.getId()).orElse(null);
        }

        if (modelRole == null) {
            throw new BizException("模型角色信息不存在");
        }

        // 准备提示词
        var mainPromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_MAIN.getKey());
        var rolePromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_ROLE.getKey());

        PreparedPrompt prompt = PreparedPrompt.prepare(mainPromptTemplate).union(rolePromptTemplate);
        prompt.setParameter("model",modelRole.getName());
        prompt.setParameter("user","user");
        prompt.setParameter("userDesc","");
        prompt.setParameter("modelDescription",modelRole.getDescription());
        prompt.setParameter("modelRoleSummary", modelRole.getRoleSummary());
        prompt.setParameter("modelScenario",modelRole.getScenario());

        if(userPlayRole != null){
            prompt.setParameter("user", userPlayRole.getName());
            prompt.setParameter("userDesc", userPlayRole.getDescription());
        }

        String finalPrompt = prompt.executeNested();

        try {
            // 清理之前的片段（如果有）
            segmentRepository.deleteByThreadId(thread.getId());

            // 创建开始片段并返回
            ModelRpSegmentPo startSegment = new ModelRpSegmentPo();
            startSegment.setUserId(userId);
            startSegment.setThread(thread);
            startSegment.setSequence(1);
            startSegment.setContent(null);
            startSegment.setStatus(0); // 未读状态
            startSegment.setType(0); // 开始类型
            segmentRepository.save(startSegment);

            // 获取配置
            String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
            
            // 获取代理配置 - 首先检查用户级别的代理配置
            String proxyConfig = userConfigService.get("model.proxy.config", userId);
            
            // 如果用户未配置代理，则使用全局代理配置
            if (StringUtils.isBlank(proxyConfig)) {
                proxyConfig = globalConfigService.get("model.proxy.config");
            }
            
            OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 60);

            // 创建请求参数
            ModelChatParam modelChatParam = new ModelChatParam();
            modelChatParam.setMessage(messageToRegenerate);
            modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent");
            modelChatParam.setApiKey(apiKey);
            modelChatParam.setModelCode(modelEnum.getCode());
            modelChatParam.setSystemPrompt(finalPrompt);

            // 转换历史记录为ModelChatParamHistory
            List<ModelChatParamHistory> paramHistories = new ArrayList<>();
            for (ModelRpHistoryPo history : histories) {
                ModelChatParamHistory paramHistory = new ModelChatParamHistory();
                paramHistory.setRole(history.getType());
                paramHistory.setContent(history.getRawContent());
                paramHistories.add(paramHistory);
            }
            modelChatParam.setHistories(paramHistories);

            if(dto.getModel().contains("grok")){
                modelChatParam.setUrl(GROK_BASE_URL);
                // 异步调用ModelGrokService发送流式请求
                modelGrokService.sendMessageStream(
                        client,
                        modelChatParam,
                        onModelRpMessageRcv(thread, thread.getUserId())
                );
            }

            if(dto.getModel().contains("gemini")){
                // 异步调用ModelGeminiService发送流式请求
                modelGeminiService.sendMessageStream(
                        client,
                        modelChatParam,
                        onModelRpMessageRcv(thread, thread.getUserId())
                );
            }

            // 返回开始片段
            RpSegmentVo vo = new RpSegmentVo();
            vo.setThreadId(thread.getId());
            vo.setSequence(startSegment.getSequence());
            vo.setContent(messageToRegenerate); // 返回用户的最后一条消息
            vo.setType(0); // 起始类型
            vo.setRole(0); // 用户角色
            
            // 设置用户角色信息
            vo.setRoleId(null);
            vo.setRoleName("user");
            vo.setRoleAvatarPath(null);
            if(userPlayRole != null){
                vo.setRoleId(userPlayRole.getId());
                vo.setRoleName(userPlayRole.getName());
                vo.setRoleAvatarPath(userPlayRole.getAvatarPath());
            }

            return vo;

        } catch (Exception e) {
            // 发生异常时清理会话状态
            rpThreadToContextIdMap.remove(thread.getId());

            // 清理所有片段
            try {
                segmentRepository.deleteByThreadId(thread.getId());
            } catch (Exception ex) {
                log.error("清理RP对话片段失败", ex);
            }

            throw new BizException("重新生成AI回复失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定的消息历史记录
     * @param dto 包含要删除的消息ID
     * @throws BizException 如果消息不存在或不属于当前用户
     */
    @Transactional
    public void removeRpHistory(RemoveRpHistoryDto dto) throws BizException {
        Long currentUserId = AuthService.getCurrentUserId();
        
        // 查询消息记录
        ModelRpHistoryPo history = historyRepository.findById(dto.getHistoryId())
            .orElseThrow(() -> new BizException("消息不存在"));
        
        // 验证消息所属的会话是否属于当前用户
        ModelRpThreadPo thread = history.getThread();
        if (!thread.getUserId().equals(currentUserId)) {
            throw new BizException("无权删除此消息");
        }
        
        // 删除消息
        historyRepository.delete(history);
    }

    /**
     * 编辑RP对话历史记录
     * @param dto 编辑RP对话历史记录的请求参数
     * @throws BizException 业务异常
     */
    @Transactional
    public void editRpHistory(EditRpHistoryDto dto) throws BizException {
        // 1. 查询历史记录
        ModelRpHistoryPo history = historyRepository.findById(dto.getHistoryId())
                .orElseThrow(() -> new BizException("历史记录不存在"));

        // 2. 验证用户权限
        if (!history.getThread().getUserId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权编辑此消息");
        }

        // 3. 更新消息内容
        history.setRawContent(dto.getContent());
        history.setRpContent(dto.getContent()); // 这里可能需要通过RpHandler处理
        historyRepository.save(history);
    }

    /**
     * 获取指定模型角色的所有会话列表
     * 用于用户管理和选择不同的会话
     * 
     * @param dto 包含模型角色ID的DTO
     * @return 会话列表
     * @throws BizException 业务异常
     */
    public List<ModelRoleThreadListVo> getModelRoleThreadList(GetModelRoleThreadListDto dto) throws BizException {
        // 构建查询条件
        ModelRpThreadPo query = new ModelRpThreadPo();
        query.setUserId(AuthService.getCurrentUserId()); // 获取当前用户ID
        
        // 使用Example构建查询
        SimpleExample<ModelRpThreadPo> example = SimpleExample.of(query);
        
        // 添加排序（按更新时间倒序）
        example.orderByDesc("updateTime");
        
        // 执行查询，查询指定modelRoleId的记录
        List<ModelRpThreadPo> threads = threadRepository.findAll(example.get());
        
        // 过滤出指定modelRoleId的记录，同时获取每个线程的最后一条消息作为预览
        List<ModelRoleThreadListVo> result = new ArrayList<>();
        
        for (ModelRpThreadPo thread : threads) {
            // 判断是否是指定角色的线程
            if (thread.getModelRole() != null && thread.getModelRole().getId().equals(dto.getModelRoleId())) {
                ModelRoleThreadListVo vo = new ModelRoleThreadListVo();
                
                // 将PO的基本字段映射到VO
                vo.setId(thread.getId());
                vo.setTitle(thread.getTitle());
                vo.setDescription(thread.getDescription());
                vo.setModelCode(thread.getModelCode());
                vo.setActive(thread.getActive());
                vo.setCreateTime(thread.getCreateTime());
                vo.setUpdateTime(thread.getUpdateTime());
                
                // 获取最后一条消息（如果有）作为预览
                String lastMessage = "";
                if (thread.getHistories() != null && !thread.getHistories().isEmpty()) {
                    // 找出序号最大的那条消息
                    ModelRpHistoryPo lastHistoryItem = thread.getHistories().stream()
                        .max((h1, h2) -> h1.getSequence().compareTo(h2.getSequence()))
                        .orElse(null);
                    
                    if (lastHistoryItem != null) {
                        // 截取前50个字符作为预览
                        String content = lastHistoryItem.getRawContent();
                        lastMessage = content.length() > 50 ? 
                            content.substring(0, 50) + "..." : 
                            content;
                    }
                }
                vo.setLastMessage(lastMessage);
                
                result.add(vo);
            }
        }
        
        return result;
    }

    /**
     * 删除指定的会话及其相关历史记录
     * 
     * @param dto 包含threadId参数
     * @throws BizException 业务异常
     */
    @Transactional
    public void removeThread(RemoveThreadDto dto) throws BizException {
        Long currentUserId = AuthService.getCurrentUserId();
        Long threadId = dto.getThreadId();
        
        // 1. 根据用户ID+ThreadID查询是否有该Thread
        ModelRpThreadPo query = new ModelRpThreadPo();
        query.setId(threadId);
        query.setUserId(currentUserId);
        
        ModelRpThreadPo thread = threadRepository.findOne(Example.of(query))
            .orElseThrow(() -> new BizException("会话不存在或无权删除"));
        
        // 2. 判断该thread是否为激活的
        if (thread.getActive() == 1) {
            // 如果当前是激活的，需要查询updateTime最新的一个thread将其设置为激活
            ModelRpThreadPo newActiveThread = threadRepository.findTopByUserIdAndModelRoleAndIdNotOrderByUpdateTimeDesc(
                currentUserId, 
                thread.getModelRole(), 
                threadId
            );
            
            if (newActiveThread != null) {
                // 将找到的最新线程设置为激活状态
                newActiveThread.setActive(1);
                threadRepository.save(newActiveThread);
            }
        }
        
        // 3. 删除该thread下的所有history
        historyRepository.deleteByThreadId(threadId);
        
        // 4. 删除该thread下的所有segment
        segmentRepository.deleteByThreadId(threadId);
        
        // 5. 最后删除thread本身
        threadRepository.delete(thread);
    }
}