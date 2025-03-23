package com.ksptool.ql.biz.service;

import com.google.gson.Gson;
import com.ksptool.ql.biz.mapper.ModelChatThreadRepository;
import com.ksptool.ql.biz.mapper.ModelChatHistoryRepository;
import com.ksptool.ql.biz.model.dto.RecoverChatDto;
import com.ksptool.ql.biz.model.po.ModelRpHistoryPo;
import com.ksptool.ql.biz.model.vo.RecoverChatVo;
import com.ksptool.ql.biz.model.vo.RecoverChatHistoryVo;
import com.ksptool.ql.biz.model.po.ModelChatThreadPo;
import com.ksptool.ql.biz.model.po.ModelChatHistoryPo;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;

import static com.ksptool.entities.Entities.as;

import java.util.function.Consumer;

import com.ksptool.ql.biz.mapper.ModelChatSegmentRepository;
import com.ksptool.ql.biz.model.po.ModelChatSegmentPo;
import com.ksptool.ql.biz.model.vo.ChatSegmentVo;
import com.ksptool.ql.biz.model.dto.BatchChatCompleteDto;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import java.util.concurrent.ConcurrentHashMap;

import com.ksptool.ql.biz.service.panel.PanelApiKeyService;
import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.biz.model.vo.ThreadListItemVo;

import com.ksptool.ql.biz.model.dto.CreateEmptyThreadDto;

import java.util.Date;
import com.ksptool.ql.biz.model.vo.CreateEmptyThreadVo;

import com.ksptool.ql.biz.model.dto.EditHistoryDto;

@Slf4j
@Service
public class ModelChatService {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    // 默认模型参数
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final double DEFAULT_TOP_P = 1.0;
    private static final int DEFAULT_TOP_K = 40;

    // 线程安全的容器，记录聊天状态 <threadId, contextId>
    private final ConcurrentHashMap<Long, String> threadToContextIdMap = new ConcurrentHashMap<>();
    
    // 终止列表，存储已经被终止的contextId
    private final ConcurrentHashMap<String, Boolean> terminatedContextIds = new ConcurrentHashMap<>();
    
    private final Gson gson = new Gson();
    
    @Autowired
    private UserConfigService userConfigService;
    
    @Autowired
    private GlobalConfigService globalConfigService;
    
    @Autowired
    private ModelChatThreadRepository threadRepository;
    
    @Autowired
    private ModelChatHistoryRepository historyRepository;
    
    @Autowired
    private ModelChatSegmentRepository segmentRepository;
    
    @Autowired
    private ModelGeminiService modelGeminiService;

    @Autowired
    private PanelApiKeyService panelApiKeyService;


    public ModelChatThreadPo createOrRetrieveThread(Long threadId, Long userId, String modelCode) throws BizException {
        if (threadId == null || threadId == -1) {
            // 创建新的会话
            long count = threadRepository.countByUserId(userId);
            
            ModelChatThreadPo thread = new ModelChatThreadPo();
            thread.setUserId(userId);
            thread.setTitle("新对话" + (count + 1));
            thread.setModelCode(modelCode);
            return threadRepository.save(thread);
        }
        
        // 获取已有会话
        ModelChatThreadPo thread = threadRepository.findByIdWithHistories(threadId);
        if (thread == null) {
            throw new BizException("会话不存在");
        }
        
        return thread;
    }
    
    private ModelChatHistoryPo createHistory(ModelChatThreadPo thread, String content, Integer role) {
        ModelChatHistoryPo history = new ModelChatHistoryPo();
        history.setThread(thread);
        history.setUserId(thread.getUserId());
        history.setContent(content);
        history.setRole(role);
        
        // 获取当前最大序号并加1
        int nextSequence = historyRepository.findMaxSequenceByThreadId(thread.getId()) + 1;
        history.setSequence(nextSequence);
        return historyRepository.save(history);
    }

    /**
     * 创建ModelChatDto并预先填充配置信息
     * @param modelCode 模型代码
     * @param userId 用户ID
     * @return 预填充配置的ModelChatDto
     * @throws BizException 业务异常
     */
    public ModelChatParam createModelChatDto(String modelCode, Long userId) throws BizException {
        // 验证模型代码
        AIModelEnum modelEnum = AIModelEnum.getByCode(modelCode);
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }
        
        // 构建基础配置键
        String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
        
        // 获取配置参数
        double temperature = userConfigService.getDouble(baseKey + "temperature", DEFAULT_TEMPERATURE, userId);
        double topP = userConfigService.getDouble(baseKey + "topP", DEFAULT_TOP_P, userId);
        int topK = userConfigService.getInt(baseKey + "topK", DEFAULT_TOP_K, userId);
        int maxOutputTokens = userConfigService.getInt(baseKey + "maxOutputTokens", 800, userId);
        
        // 创建并填充DTO
        ModelChatParam dto = new ModelChatParam();
        dto.setModelCode(modelEnum.getCode());
        dto.setTemperature(temperature);
        dto.setTopP(topP);
        dto.setTopK(topK);
        dto.setMaxOutputTokens(maxOutputTokens);
        return dto;
    }

    /**
     * 删除会话
     * @param threadId 会话ID
     * @throws BizException 业务异常
     */
    public void removeThread(Long threadId) throws BizException {
        Long userId = AuthService.getCurrentUserId();
        ModelChatThreadPo thread = threadRepository.findByIdWithHistories(threadId);
        if (thread == null) {
            throw new BizException("会话不存在");
        }
        
        if (!thread.getUserId().equals(userId)) {
            throw new BizException("无权删除该会话");
        }
        
        // 先删除关联的聊天片段
        segmentRepository.deleteByThreadId(threadId);
        
        // 再删除会话
        threadRepository.delete(thread);
    }


    /**
     * 发送批量聊天消息
     * @param dto 批量聊天请求参数
     * @return 聊天片段VO
     * @throws BizException 业务异常
     */
    public ChatSegmentVo chatCompleteSendBatch(BatchChatCompleteDto dto) throws BizException {

        Long threadId = dto.getThreadId();

        // 检查该会话是否正在处理中
        if (!threadId.equals(-1L) && threadToContextIdMap.containsKey(threadId)) {
            throw new BizException("该会话正在处理中，请等待AI响应完成");
        }

        try {
            // 获取并验证模型配置
            AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
            if (modelEnum == null) {
                throw new BizException("无效的模型代码");
            }

            // 获取当前用户ID
            Long userId = AuthService.getCurrentUserId();

            String apiKey = panelApiKeyService.getApiKey(modelEnum.getCode(), userId);

            if (StringUtils.isBlank(apiKey)) {
                throw new BizException("未配置API Key");
            }

            // 获取或创建会话
            ModelChatThreadPo thread = createOrRetrieveThread(threadId, userId, modelEnum.getCode());

            // 处理重新生成逻辑
            Long userHistoryId = null;

            // 保存用户消息
            if(dto.getQueryKind() == 0){
                ModelChatHistoryPo userHistory = createHistory(thread, dto.getMessage(), 0);
                userHistoryId = userHistory.getId();
            }

            if (dto.getQueryKind() == 3 && dto.getRegenerateRootHistoryId() != null) {
                // 获取指定的根消息记录
                ModelChatHistoryPo rootHistory = historyRepository.findById(dto.getRegenerateRootHistoryId())
                    .orElseThrow(() -> new BizException("指定的根消息记录不存在"));
                
                // 检查权限和关联
                if (!rootHistory.getUserId().equals(userId) || !rootHistory.getThread().getId().equals(threadId)) {
                    throw new BizException("无权访问该消息或消息不属于该会话");
                }
                
                // 检查消息类型，只能是用户消息
                if (rootHistory.getRole() != 0) {
                    throw new BizException("只能以用户消息作为重新生成的起点");
                }
                
                // 删除根消息之后的所有消息
                historyRepository.removeHistoryAfter(threadId, rootHistory.getSequence());
                
                // 使用根消息的ID作为当前用户消息ID
                userHistoryId = rootHistory.getId();
                thread = createOrRetrieveThread(threadId, userId, modelEnum.getCode());
            }

            // 清理之前的片段（如果有）
            segmentRepository.deleteByThreadId(thread.getId());

            // 创建开始片段并返回
            ModelChatSegmentPo startSegment = new ModelChatSegmentPo();
            startSegment.setUserId(userId);
            startSegment.setThread(thread);
            startSegment.setSequence(1);
            startSegment.setContent(null);
            startSegment.setStatus(0); // 未读状态
            startSegment.setType(0); // 开始类型
            segmentRepository.save(startSegment);

            // 获取代理配置
            String proxyConfig = getProxyConfig(userId);

            // 创建HTTP客户端
            OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 60);

            // 创建请求DTO
            ModelChatParam modelChatParam = createModelChatDto(modelEnum.getCode(), userId);
            modelChatParam.setMessage(dto.getMessage());
            modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent");
            modelChatParam.setApiKey(apiKey);

            //将新模型选项保存到Thread
            thread.setModelCode(modelEnum.getCode());
            threadRepository.save(thread);

            //获取全部消息历史
            List<ModelChatHistoryPo> historyPos = historyRepository.getByThreadId(thread.getId());
            modelChatParam.setHistories(as(historyPos, ModelChatParamHistory.class));

            // 异步调用ModelGeminiService发送流式请求
            modelGeminiService.sendMessageStream(
                    client,
                    modelChatParam,
                    // 使用封装后的回调函数
                    onModelMessageRcv(thread, userId)
            );

            // 返回用户消息作为第一次响应
            ChatSegmentVo vo = new ChatSegmentVo();
            vo.setThreadId(thread.getId());
            vo.setHistoryId(userHistoryId);
            vo.setRole(0); // 用户角色
            vo.setSequence(startSegment.getSequence());
            vo.setContent(dto.getMessage()); // 返回用户的消息内容
            vo.setType(0); // 起始类型，而不是数据类型
            return vo;

        } catch (Exception e) {
            // 发生异常时清理会话状态
            if (!threadId.equals(-1L)) {
                threadToContextIdMap.remove(threadId);
            }

            // 清理所有片段
            try {
                if (!threadId.equals(-1L)) {
                    segmentRepository.deleteByThreadId(threadId);
                }
            } catch (Exception ex) {
                log.error("清理聊天片段失败", ex);
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
        Long threadId = dto.getThreadId();
        Long userId = AuthService.getCurrentUserId();
        
        // 最大等待次数和等待时间
        final int MAX_WAIT_TIMES = 3;
        final long WAIT_INTERVAL_MS = 300;
        
        // 尝试获取所有未读片段，最多等待3次
        List<ModelChatSegmentPo> unreadSegments = null;
        int waitTimes = 0;
        
        while (waitTimes < MAX_WAIT_TIMES) {
            // 一次性查询所有未读片段，按sequence排序
            unreadSegments = segmentRepository.findAllUnreadByThreadIdOrderBySequence(threadId);
            
            // 如果有未读片段，跳出循环
            if (!unreadSegments.isEmpty()) {
                break;
            }
            
            // 如果没有未读片段，且会话不在处理中，直接返回null
            if (!threadToContextIdMap.containsKey(threadId)) {
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
        for (ModelChatSegmentPo segment : unreadSegments) {
            // 开始片段，只标记该片段为已读并返回
            if (segment.getType() == 0) {
                segment.setStatus(1);
                segmentRepository.save(segment);
                ChatSegmentVo vo = new ChatSegmentVo();
                vo.setThreadId(threadId);
                vo.setHistoryId(segment.getHistoryId());
                vo.setSequence(segment.getSequence());
                vo.setContent(segment.getContent());
                vo.setType(segment.getType());
                vo.setRole(1); // AI助手角色
                return vo;
            }

            // 错误片段，标记所有片段为已读并返回错误信息
            if (segment.getType() == 10) {
                for (ModelChatSegmentPo seg : unreadSegments) {
                    seg.setStatus(1);
                }
                segmentRepository.saveAll(unreadSegments);
                ChatSegmentVo vo = new ChatSegmentVo();
                vo.setThreadId(threadId);
                vo.setSequence(segment.getSequence());
                vo.setContent(segment.getContent() != null ? segment.getContent() : "AI响应出错");
                vo.setType(segment.getType());
                vo.setRole(1); // AI助手角色
                return vo;
            }
        }

        // 获取第一个片段
        ModelChatSegmentPo firstSegment = unreadSegments.get(0);
        
        // 如果第一个片段是数据片段(type=1)，则合并后续的数据片段
        if (firstSegment.getType() == 1) {
            StringBuilder combinedContent = new StringBuilder();
            List<ModelChatSegmentPo> segmentsToMark = new ArrayList<>();
            
            // 遍历所有片段，合并type=1的片段，直到遇到非type=1的片段
            for (ModelChatSegmentPo segment : unreadSegments) {
                if (segment.getType() != 1) {
                    break;
                }
                
                combinedContent.append(segment.getContent());
                segment.setStatus(1); // 标记为已读
                segmentsToMark.add(segment);
            }
            
            segmentRepository.saveAll(segmentsToMark);
            
            // 返回合并后的数据片段
            ChatSegmentVo vo = new ChatSegmentVo();
            vo.setThreadId(threadId);
            vo.setHistoryId(null);
            vo.setSequence(firstSegment.getSequence());
            vo.setContent(combinedContent.toString());
            vo.setType(1); // 数据类型
            vo.setRole(1); // AI助手角色
            return vo;
        }
        
        // 如果第一个片段是type=2结束片段
        firstSegment.setStatus(1); // 标记为已读
        segmentRepository.save(firstSegment);
        
        ChatSegmentVo vo = new ChatSegmentVo();
        vo.setThreadId(threadId);
        vo.setHistoryId(firstSegment.getHistoryId());
        vo.setSequence(firstSegment.getSequence());
        vo.setContent(firstSegment.getContent());
        vo.setType(firstSegment.getType());
        vo.setRole(1); // AI助手角色
        return vo;
    }

    /**
     * 终止批量聊天响应
     * @param dto 批量聊天请求参数
     * @throws BizException 业务异常
     */
    public void chatCompleteTerminateBatch(BatchChatCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();
        Long userId = AuthService.getCurrentUserId();

        // 检查会话是否存在
        ModelChatThreadPo thread = threadRepository.findById(threadId).orElse(null);
        if (thread == null) {
            throw new BizException("会话不存在");
        }

        // 检查权限
        if (!thread.getUserId().equals(userId)) {
            throw new BizException("无权访问该会话");
        }

        // 获取当前正在进行的contextId
        String contextId = threadToContextIdMap.get(threadId);
        if (contextId == null) {
            throw new BizException("该会话未在进行中或已经终止");
        }

        // 将contextId加入终止列表
        terminatedContextIds.put(contextId, true);
        
        // 清理会话状态
        threadToContextIdMap.remove(threadId);

        // 获取当前最大序号
        int nextSequence = segmentRepository.findMaxSequenceByThreadId(threadId) + 1;

        // 创建终止片段
        ModelChatSegmentPo endSegment = new ModelChatSegmentPo();
        endSegment.setUserId(userId);
        endSegment.setThread(thread);
        endSegment.setSequence(nextSequence);
        endSegment.setContent("用户终止了AI响应");
        endSegment.setStatus(0); // 未读状态
        endSegment.setType(2); // 结束类型
        segmentRepository.save(endSegment);
    }

    /**
     * 编辑会话标题
     *
     * @param threadId 会话ID
     * @param newTitle 新标题
     * @throws BizException 业务异常
     */
    public void editThreadTitle(Long threadId, String newTitle) throws BizException {
        if (threadId == null) {
            throw new BizException("会话ID不能为空");
        }
        
        if (StringUtils.isBlank(newTitle)) {
            throw new BizException("标题不能为空");
        }
        
        // 限制标题长度
        if (newTitle.length() > 100) {
            newTitle = newTitle.substring(0, 97) + "...";
        }
        
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 获取会话
        ModelChatThreadPo thread = threadRepository.findById(threadId).orElse(null);
        if (thread == null) {
            throw new BizException("会话不存在");
        }
        
        // 检查权限
        if (!thread.getUserId().equals(userId)) {
            throw new BizException("无权编辑该会话");
        }
        
        // 更新会话标题
        thread.setTitle(newTitle);
        // 标记为手动编辑的标题
        thread.setTitleGenerated(1);
        threadRepository.save(thread);
    }

    /**
     * 生成会话标题
     * @param threadId 会话ID
     * @param model 模型代码
     */
    public void generateThreadTitle(Long threadId, String model) {
        try {
            // 检查是否需要生成标题
            boolean shouldGenerateTitle = globalConfigService.getBoolean("model.chat.gen.thread.title", true);
            if (!shouldGenerateTitle) {
                return; // 配置为不生成标题，直接返回
            }
            
            // 获取会话
            ModelChatThreadPo thread = threadRepository.findByIdWithHistories(threadId);
            if (thread == null) {
                throw new BizException("会话不存在");
            }
            
            // 检查是否已生成过标题
            if (thread.getTitleGenerated() != null && thread.getTitleGenerated() == 1) {
                return; // 已生成过标题，直接返回
            }
            
            // 获取第一条用户消息和对应的AI回复
            List<ModelChatHistoryPo> histories = thread.getHistories();
            if (histories == null || histories.isEmpty()) {
                return; // 没有历史记录，无法生成标题
            }
            
            // 查找第一条用户消息和对应的AI回复
            ModelChatHistoryPo firstUserMessage = null;
            ModelChatHistoryPo firstAIResponse = null;
            boolean foundUser = false;
            
            for (ModelChatHistoryPo history : histories) {
                if (!foundUser && history.getRole() == 0) { // 用户消息
                    firstUserMessage = history;
                    foundUser = true;
                    continue;
                }
                
                if (foundUser && history.getRole() == 1) { // AI回复
                    firstAIResponse = history;
                    break;
                }
            }
            
            if (firstUserMessage == null || firstAIResponse == null || 
                StringUtils.isBlank(firstUserMessage.getContent()) || 
                StringUtils.isBlank(firstAIResponse.getContent())) {
                return; // 必须同时有用户消息和AI回复才生成标题
            }

            // 从配置获取提示语模板
            String promptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_PROMPT.getKey(),
                    GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_PROMPT.getDefaultValue());

            PreparedPrompt prompt = PreparedPrompt.prepare(promptTemplate);
            prompt.setParameter("userContent", firstUserMessage.getContent());
            prompt.setParameter("modelContent", firstAIResponse.getContent());

            // 获取当前用户ID
            Long userId = thread.getUserId();
            
            // 创建请求DTO
            ModelChatParam modelChatParam = createModelChatDto(model, userId);
            modelChatParam.setMessage(prompt.execute());
                  
            // 获取代理配置
            String proxyUrl = getProxyConfig(userId);
            
            String apiKey = panelApiKeyService.getApiKey(model, userId);
            
            if (StringUtils.isBlank(apiKey)) {
                throw new BizException("未配置API Key");
            }
            
            modelChatParam.setUrl(GEMINI_BASE_URL + model + ":generateContent");
            modelChatParam.setApiKey(apiKey);
            
            // 发送请求
            String title = modelGeminiService.sendMessageSync(HttpClientUtils.createHttpClient(proxyUrl, 30), modelChatParam);
            
            // 处理标题（去除引号和多余空格，限制长度）
            title = title.replaceAll("^\"|\"$", "").trim();
            if (title.length() > 100) {
                title = title.substring(0, 97) + "...";
            }
            
            // 更新会话标题
            thread.setTitle(title);
            thread.setTitleGenerated(1); // 标记为已生成标题
            threadRepository.save(thread);
            
            log.info("已为会话 {} 生成标题: {}", threadId, title);
        } catch (Exception e) {
            log.error("生成会话标题失败", e);
            // 生成标题失败不抛出异常，不影响主流程
        }
    }

    /**
     * 重新生成AI最后一条回复
     * @param dto 批量聊天请求参数
     * @return 聊天片段VO
     * @throws BizException 业务异常
     */
    public ChatSegmentVo chatCompleteRegenerateBatch(BatchChatCompleteDto dto) throws BizException {
        // 检查该会话是否正在处理中
        if (threadToContextIdMap.containsKey(dto.getThreadId())) {
            throw new BizException("该会话正在处理中，请等待AI响应完成");
        }

        // 获取并验证模型配置
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();

        // 使用Example查询当前用户的会话
        ModelChatThreadPo threadQuery = new ModelChatThreadPo();
        threadQuery.setId(dto.getThreadId());
        threadQuery.setUserId(userId);
        
        Example<ModelChatThreadPo> example = Example.of(threadQuery);

        if(threadRepository.count(example) < 1){
            throw new BizException("会话不存在或不可用!");
        }

        // 查找最后一条用户消息
        ModelChatHistoryPo lastUserMessage = historyRepository.getLastMessage(dto.getThreadId(), 0);
        if (lastUserMessage == null) {
            throw new BizException("未找到任何用户消息");
        }

        // 使用找到的最后一条用户消息作为重新生成的起点
        BatchChatCompleteDto batchSendDto = new BatchChatCompleteDto();
        batchSendDto.setThreadId(dto.getThreadId());
        batchSendDto.setModel(dto.getModel());
        batchSendDto.setMessage(lastUserMessage.getContent());
        batchSendDto.setQueryKind(3); // 重新生成AI最后一条回复
        batchSendDto.setRegenerateRootHistoryId(lastUserMessage.getId());

        return chatCompleteSendBatch(batchSendDto);
    }

    /**
     * 删除指定的历史消息
     * @param threadId 会话ID
     * @param historyId 历史消息ID
     * @throws BizException 业务异常
     */
    public void removeHistory(Long threadId, Long historyId) throws BizException {
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 检查会话是否存在
        ModelChatThreadPo thread = threadRepository.findById(threadId).orElse(null);
        if (thread == null) {
            throw new BizException("会话不存在");
        }
        
        // 检查权限
        if (!thread.getUserId().equals(userId)) {
            throw new BizException("无权删除该历史消息");
        }
        
        // 检查历史消息是否存在
        ModelChatHistoryPo history = historyRepository.findById(historyId).orElse(null);
        if (history == null) {
            throw new BizException("历史消息不存在");
        }
        
        // 检查历史消息是否属于该会话
        if (!history.getThread().getId().equals(threadId)) {
            throw new BizException("历史消息不属于该会话");
        }
        
        // 删除历史消息
        historyRepository.delete(history);
        
        log.info("已删除会话 {} 的历史消息 {}", threadId, historyId);
    }
    /**
     * 恢复会话
     * @param dto 包含threadId参数
     * @return 返回会话信息和历史消息
     * @throws BizException 业务异常
     */
    public RecoverChatVo recoverChat(RecoverChatDto dto) throws BizException {
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 获取会话ID
        Long threadId = dto.getThreadId();
        
        // 获取会话
        ModelChatThreadPo thread = threadRepository.findByIdWithHistories(threadId);
        if (thread == null) {
            throw new BizException("会话不存在");
        }
        
        // 检查权限
        if (!thread.getUserId().equals(userId)) {
            throw new BizException("无权访问该会话");
        }
        
        // 获取会话历史记录
        List<ModelChatHistoryPo> histories = thread.getHistories();
        
        // 创建响应VO
        RecoverChatVo vo = new RecoverChatVo();
        vo.setThreadId(thread.getId());
        vo.setModelCode(thread.getModelCode());
        
        // 转换历史记录
        List<RecoverChatHistoryVo> messageVos = new ArrayList<>();
        if (histories != null && !histories.isEmpty()) {
            for (ModelChatHistoryPo history : histories) {
                RecoverChatHistoryVo messageVo = new RecoverChatHistoryVo();
                
                // 手动映射特定字段
                messageVo.setId(history.getId());
                messageVo.setContent(history.getContent());
                messageVo.setCreateTime(history.getCreateTime());
                
                // 将role映射到type (0-用户消息，1-AI消息)
                messageVo.setRole(history.getRole());
                
                // 根据角色设置名称和头像
                if (history.getRole() == 0) {
                    // 用户消息
                    messageVo.setName("User");
                    messageVo.setAvatarPath("");
                }
                
                if (history.getRole() == 1) {
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
     * @param uid 用户ID
     * @return 代理url
     */
    public String getProxyConfig(Long uid){
        // 获取代理配置 - 首先检查用户级别的代理配置
        String proxyConfig = userConfigService.get("model.proxy.config", uid);

        // 如果用户未配置代理，则使用全局代理配置
        if (StringUtils.isBlank(proxyConfig)) {
            proxyConfig = globalConfigService.get("model.proxy.config");
        }
        return proxyConfig;
    }

    /**
     * 创建处理模型消息回调的Consumer
     *
     * @param thread 聊天线程
     * @param userId 用户ID
     * @return 处理模型消息的Consumer
     */
    private Consumer<ModelChatContext> onModelMessageRcv(ModelChatThreadPo thread, Long userId) {
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
                if (!threadToContextIdMap.containsValue(contextId)) {
                    threadToContextIdMap.put(threadId, contextId);
                }

                // 获取当前最大序号
                int nextSequence = segmentRepository.findMaxSequenceByThreadId(threadId) + 1;

                // 根据context.type处理不同类型的消息
                if (context.getType() == 0) {
                    // 数据类型 - 创建数据片段
                    ModelChatSegmentPo dataSegment = new ModelChatSegmentPo();
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
                    ModelChatHistoryPo aiHistory = createHistory(thread, context.getContent(), 1);

                    // 完成类型 - 创建结束片段
                    ModelChatSegmentPo endSegment = new ModelChatSegmentPo();
                    endSegment.setUserId(userId);
                    endSegment.setThread(thread);
                    endSegment.setSequence(nextSequence);
                    endSegment.setContent(null);
                    endSegment.setStatus(0); // 未读状态
                    endSegment.setType(2); // 结束类型
                    endSegment.setHistoryId(aiHistory.getId()); // 设置关联的历史记录ID
                    segmentRepository.save(endSegment);

                    if (modelEnum != null) {

                        // 尝试生成会话标题
                        try {
                            generateThreadTitle(thread.getId(), modelEnum.getCode());
                        } catch (Exception e) {
                            log.error("生成会话标题失败", e);
                            // 生成标题失败不影响主流程
                        }
                    }

                    // 清理会话状态
                    threadToContextIdMap.remove(threadId);
                    return;
                }

                if (context.getType() == 2) {
                    // 错误类型 - 创建错误片段
                    ModelChatSegmentPo errorSegment = new ModelChatSegmentPo();
                    errorSegment.setUserId(userId);
                    errorSegment.setThread(thread);
                    errorSegment.setSequence(nextSequence);
                    errorSegment.setContent(context.getException() != null ? "AI响应错误: " + context.getException().getMessage() : "AI响应错误");
                    errorSegment.setStatus(0); // 未读状态
                    errorSegment.setType(10); // 错误类型
                    segmentRepository.save(errorSegment);

                    // 清理会话状态
                    threadToContextIdMap.remove(threadId);
                }
            } catch (Exception e) {
                log.error("处理聊天片段失败", e);
            }
        };
    }

    /**
     * 获取当前用户的会话列表
     * @return 会话列表
     * @throws BizException 业务异常
     */
    public List<ThreadListItemVo> getThreadList() throws BizException {
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 查询该用户的所有会话，按更新时间排序
        List<ModelChatThreadPo> threads = threadRepository.findByUserIdOrderByUpdateTimeDesc(userId);
        
        // 转换为VO
        List<ThreadListItemVo> voList = new ArrayList<>();
        
        for (ModelChatThreadPo thread : threads) {
            ThreadListItemVo vo = new ThreadListItemVo();
            vo.setId(thread.getId());
            vo.setTitle(thread.getTitle());
            vo.setModelCode(thread.getModelCode());
            voList.add(vo);
        }
        
        return voList;
    }

    /**
     * 创建空会话
     * @param dto 创建空会话请求参数
     * @return 新创建的会话ID
     * @throws BizException 业务异常
     */
    public CreateEmptyThreadVo createEmptyThread(CreateEmptyThreadDto dto) throws BizException {
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 获取当前用户已有的会话数量
        long threadCount = threadRepository.countByUserId(userId);
        
        // 生成新会话标题
        String title = String.format("新会话#%d", threadCount + 1);
        
        // 创建新会话
        ModelChatThreadPo thread = new ModelChatThreadPo();
        thread.setTitle(title);
        thread.setModelCode(dto.getModel());
        thread.setUserId(userId);
        thread.setCreateTime(new Date());
        thread.setUpdateTime(new Date());
        threadRepository.save(thread);
        
        // 返回新创建的会话ID
        return CreateEmptyThreadVo.of(thread.getId());
    }

    /**
     * 编辑聊天历史记录
     * @param dto 编辑聊天历史记录的请求参数
     * @throws BizException 业务异常
     */
    public void editHistory(EditHistoryDto dto) throws BizException {
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 创建Example查询对象
        ModelChatHistoryPo historyCriteria = new ModelChatHistoryPo();
        historyCriteria.setId(dto.getHistoryId());
        historyCriteria.setUserId(userId);
        
        Example<ModelChatHistoryPo> example = Example.of(historyCriteria);
        
        // 查询符合条件的记录
        ModelChatHistoryPo history = historyRepository.findOne(example)
            .orElseThrow(() -> new BizException("历史记录不存在或无权编辑"));
            
        // 更新消息内容
        history.setContent(dto.getContent());
        historyRepository.save(history);
    }
}