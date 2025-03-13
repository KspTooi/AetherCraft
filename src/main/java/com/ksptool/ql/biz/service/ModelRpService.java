package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.mapper.ModelRpHistoryRepository;
import com.ksptool.ql.biz.mapper.ModelRpSegmentRepository;
import com.ksptool.ql.biz.mapper.ModelRpThreadRepository;
import com.ksptool.ql.biz.model.dto.BatchRpCompleteDto;
import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.dto.RecoverRpChatDto;
import com.ksptool.ql.biz.model.dto.DeActiveThreadDto;
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
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.biz.model.dto.RemoveHistoryDto;

@Slf4j
@Service
public class ModelRpService {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

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
    private ConfigService configService;
    
    @Autowired
    private PanelApiKeyService panelApiKeyService;
    @Autowired
    private GlobalConfigService globalConfigService;

    public PageableView<GetModelRoleListVo> getModelRoleList(GetModelRoleListDto queryDto) {
        Page<ModelRolePo> page = modelRoleRepository.getModelRoleList(
            AuthService.getCurrentUserId(),
            queryDto.getKeyword(),
            queryDto.pageRequest()
        );
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

        // 1. 查询用户拥有的模型角色
        ModelRolePo example = new ModelRolePo();
        example.setId(dto.getModelRoleId());
        example.setUserId(AuthService.getCurrentUserId());

        ModelRolePo modelRole = modelRoleRepository.findOne(Example.of(example))
            .orElseThrow(() -> new BizException("模型角色不存在或无权访问"));
            
        // 2. 查询激活的存档
        ModelRpThreadPo thread = threadRepository.findActiveThreadByModelRoleId(dto.getModelRoleId());

        // 3. 如果没有激活的存档，创建新存档
        if (thread == null) {

            thread = new ModelRpThreadPo();
            thread.setUserId(AuthService.getCurrentUserId());
            thread.setModelCode(dto.getModelCode());
            thread.setModelRole(modelRole);
            thread.setTitle("与" + modelRole.getName() + "的对话");
            thread.setActive(1);
            thread = threadRepository.save(thread);
            
            // 创建首条消息
            if (StringUtils.isNotBlank(modelRole.getFirstMessage())) {
                ModelRpHistoryPo history = new ModelRpHistoryPo();
                history.setThread(thread);
                history.setType(1); // AI消息
                history.setRawContent(modelRole.getFirstMessage());
                history.setRpContent(modelRole.getFirstMessage()); // 这里可能需要通过RpHandler处理
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
                if(userRole != null){
                    message.setName(thread.getUserRole().getName());
                    message.setAvatarPath(thread.getUserRole().getAvatarPath());
                }
            }

            // AI消息
            if(history.getType() == 1){
                message.setName(modelRole.getName());
                message.setAvatarPath(modelRole.getAvatarPath());
            }
            
            messages.add(message);
        }
        
        vo.setMessages(messages);
        messages.forEach(m->{
            m.setAvatarPath("/res/"+m.getAvatarPath());
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
        ModelUserRolePo userRole = thread.getUserRole();
        //if (userRole == null) {
        //    throw new BizException("用户角色信息不存在");
        //}

        //获取模型扮演的角色信息
        ModelRolePo modelRole = thread.getModelRole();
        if (modelRole == null) {
            throw new BizException("模型角色信息不存在");
        }

        PreparedPrompt ctxPrompt  = PreparedPrompt.prepare(globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_MAIN.getKey()));
        ctxPrompt.setParameter("model",modelRole.getName());
        ctxPrompt.setParameter("user","user");

        PreparedPrompt rolePrompt = PreparedPrompt.prepare(globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_ROLE.getKey()));
        rolePrompt.setParameter("userDesc","");
        rolePrompt.setParameter("modelDescription",modelRole.getDescription());
        rolePrompt.setParameter("modelRoleSummary", modelRole.getRoleSummary());
        rolePrompt.setParameter("modelScenario",modelRole.getScenario());

        if(userRole != null){
            ctxPrompt.setParameter("user", userRole.getName());
        }

        String finalPrompt = ctxPrompt.execute() + rolePrompt.execute();


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
            String proxyConfig = configService.get(baseKey + "proxy", thread.getUserId());

            // 创建HTTP客户端
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

            // 异步调用ModelGeminiService发送流式请求
            modelGeminiService.sendMessageStream(
                    client,
                    modelChatParam,
                    onModelRpMessageRcv(thread, thread.getUserId())
            );

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
            if(userRole != null){
                vo.setRoleId(userRole.getId());
                vo.setRoleName(userRole.getName());
                vo.setRoleAvatarPath(userRole.getAvatarPath());
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
        final int MAX_WAIT_TIMES = 10;
        final long WAIT_INTERVAL_MS = 300;
        
        // 尝试获取未读片段，最多等待10次
        List<ModelRpSegmentPo> unreadSegments = null;
        int waitTimes = 0;
        
        while (waitTimes < MAX_WAIT_TIMES) {
            unreadSegments = segmentRepository.findNextUnreadByThreadId(threadId);
            
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
        if (unreadSegments == null || unreadSegments.isEmpty()) {
            throw new BizException("等待片段超时，请稍后再试");
        }
        
        // 获取第一个未读片段
        ModelRpSegmentPo segment = unreadSegments.get(0);
        
        // 检查权限
        if (!segment.getUserId().equals(userId)) {
            throw new BizException("无权访问该会话");
        }
        
        // 检查是否为错误片段
        if (segment.getType() == 10) {
            // 标记为已读
            segment.setStatus(1);
            segmentRepository.save(segment);
            throw new BizException(segment.getContent() != null ? segment.getContent() : "AI响应出错");
        }
        
        // 标记为已读
        segment.setStatus(1);
        segmentRepository.save(segment);
        
        // 转换为VO
        RpSegmentVo vo = new RpSegmentVo();
        vo.setThreadId(threadId);
        vo.setHistoryId(segment.getHistoryId());
        vo.setSequence(segment.getSequence());
        vo.setContent(segment.getContent());
        vo.setType(segment.getType());
        vo.setRole(1); // AI助手角色
        
        // 如果是数据片段或结束片段，需要设置模型角色信息
        if (segment.getType() == 1 || segment.getType() == 2) {
            ModelRpThreadPo thread = segment.getThread();
            ModelRolePo modelRole = thread.getModelRole();
            if (modelRole != null) {
                vo.setRoleId(modelRole.getId());
                vo.setRoleName(modelRole.getName());
                vo.setRoleAvatarPath(modelRole.getAvatarPath());
            }
        }
        
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
    @Transactional
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
        ModelRpHistoryPo lastHistory = histories.get(histories.size() - 1);
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
        AIModelEnum modelEnum = AIModelEnum.getByCode(thread.getModelCode());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        String apiKey = panelApiKeyService.getApiKey(modelEnum.getCode(), userId);
        if (StringUtils.isBlank(apiKey)) {
            throw new BizException("未配置API Key");
        }

        // 获取角色信息
        ModelRolePo modelRole = thread.getModelRole();
        ModelUserRolePo userRole = thread.getUserRole();
        if (modelRole == null) {
            throw new BizException("模型角色信息不存在");
        }

        // 准备提示词
        PreparedPrompt ctxPrompt = PreparedPrompt.prepare(globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_MAIN.getKey()));
        ctxPrompt.setParameter("model", modelRole.getName());
        ctxPrompt.setParameter("user", userRole != null ? userRole.getName() : "user");

        PreparedPrompt rolePrompt = PreparedPrompt.prepare(globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_ROLE.getKey()));
        rolePrompt.setParameter("userDesc", "");
        rolePrompt.setParameter("modelDescription", modelRole.getDescription());
        rolePrompt.setParameter("modelRoleSummary", modelRole.getRoleSummary());
        rolePrompt.setParameter("modelScenario", modelRole.getScenario());

        String finalPrompt = ctxPrompt.execute() + rolePrompt.execute();

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
            String proxyConfig = configService.get(baseKey + "proxy", userId);

            // 创建HTTP客户端
            OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 60);

            // 创建请求参数
            ModelChatParam modelChatParam = new ModelChatParam();
            modelChatParam.setMessage(messageToRegenerate);
            modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent");
            modelChatParam.setApiKey(apiKey);
            modelChatParam.setModelCode(thread.getModelCode());
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

            // 异步调用ModelGeminiService发送流式请求
            modelGeminiService.sendMessageStream(
                    client,
                    modelChatParam,
                    onModelRpMessageRcv(thread, userId)
            );

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
            if(userRole != null){
                vo.setRoleId(userRole.getId());
                vo.setRoleName(userRole.getName());
                vo.setRoleAvatarPath(userRole.getAvatarPath());
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
    public void removeHistory(RemoveHistoryDto dto) throws BizException {
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
}