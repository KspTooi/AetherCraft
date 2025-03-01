package com.ksptool.ql.biz.service;

import com.google.gson.Gson;
import com.ksptool.ql.biz.mapper.ModelChatThreadRepository;
import com.ksptool.ql.biz.mapper.ModelChatHistoryRepository;
import com.ksptool.ql.biz.model.dto.ChatCompleteDto;
import com.ksptool.ql.biz.model.vo.ChatCompleteVo;
import com.ksptool.ql.biz.model.gemini.GeminiRequest;
import com.ksptool.ql.biz.model.gemini.GeminiResponse;
import com.ksptool.ql.biz.model.po.ModelChatThreadPo;
import com.ksptool.ql.biz.model.po.ModelChatHistoryPo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import com.ksptool.ql.biz.model.vo.ModelChatViewVo;
import com.ksptool.ql.biz.model.vo.ModelChatViewThreadVo;
import com.ksptool.ql.biz.model.vo.ModelChatViewMessageVo;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import com.ksptool.ql.biz.mapper.ModelChatSegmentRepository;
import com.ksptool.ql.biz.model.po.ModelChatSegmentPo;
import com.ksptool.ql.biz.model.vo.ChatSegmentVo;
import com.ksptool.ql.biz.model.dto.BatchChatCompleteDto;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import java.util.concurrent.ConcurrentHashMap;
import com.ksptool.ql.biz.service.ConfigService;
import com.ksptool.ql.biz.service.GlobalConfigService;

@Slf4j
@Service
public class ModelChatService {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    // SSE相关常量
    private static final String SSE_PARAM = "?alt=sse";
    
    // 默认模型参数
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final double DEFAULT_TOP_P = 1.0;
    private static final int DEFAULT_TOP_K = 40;


    // 线程安全的容器，记录聊天状态 <threadId, contextId>
    private final ConcurrentHashMap<Long, String> chatThreadProcessingStatus = new ConcurrentHashMap<>();
    
    // 终止列表，存储已经被终止的contextId
    private final ConcurrentHashMap<String, Boolean> terminatedContextIds = new ConcurrentHashMap<>();
    
    private final Gson gson = new Gson();
    
    @Autowired
    private ConfigService configService;
    
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
    
    /**
     * 验证会话ID是否有效
     * @param threadId 会话ID
     * @return 如果会话存在且属于当前用户则返回true
     */
    public boolean isValidThread(Long threadId) {
        if (threadId == null) {
            return false;
        }
        Long userId = AuthService.getCurrentUserId();
        ModelChatThreadPo thread = threadRepository.findByIdWithHistories(threadId);
        return thread != null && thread.getUserId().equals(userId);
    }

    /**
     * 创建新的空会话线程
     * @param modelCode 模型代码
     * @return 新创建的会话ID
     * @throws BizException 如果模型代码无效
     */
    public Long createNewThread(String modelCode) throws BizException {
        if (!StringUtils.hasText(modelCode)) {
            throw new BizException("模型代码不能为空");
        }

        AIModelEnum modelEnum = AIModelEnum.getByCode(modelCode);
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        Long userId = AuthService.getCurrentUserId();
        long count = threadRepository.countByUserId(userId);
        
        ModelChatThreadPo thread = new ModelChatThreadPo();
        thread.setUserId(userId);
        thread.setTitle("新对话" + (count + 1));
        thread.setModelCode(modelCode);
        
        thread = threadRepository.save(thread);
        return thread.getId();
    }

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
    
    private void createHistory(ModelChatThreadPo thread, String content, Integer role) {
        ModelChatHistoryPo history = new ModelChatHistoryPo();
        history.setThread(thread);
        history.setUserId(thread.getUserId());
        history.setContent(content);
        history.setRole(role);
        
        // 获取当前最大序号并加1
        int nextSequence = historyRepository.findMaxSequenceByThreadId(thread.getId()) + 1;
        history.setSequence(nextSequence);
        historyRepository.save(history);
    }
    
    /**
     * 创建ModelChatDto并预先填充配置信息（使用当前登录用户）
     * @param modelCode 模型代码
     * @return 预填充配置的ModelChatDto
     * @throws BizException 业务异常
     */
    public ModelChatParam createModelChatDto(String modelCode) throws BizException {
        return createModelChatDto(modelCode, AuthService.getCurrentUserId());
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
        double temperature = configService.getDouble(baseKey + "temperature", DEFAULT_TEMPERATURE, userId);
        double topP = configService.getDouble(baseKey + "topP", DEFAULT_TOP_P, userId);
        int topK = configService.getInt(baseKey + "topK", DEFAULT_TOP_K, userId);
        int maxOutputTokens = configService.getInt(baseKey + "maxOutputTokens", 800, userId);
        
        // 创建并填充DTO
        ModelChatParam dto = new ModelChatParam();
        dto.setModelCode(modelEnum.getCode());
        dto.setTemperature(temperature);
        dto.setTopP(topP);
        dto.setTopK(topK);
        dto.setMaxOutputTokens(maxOutputTokens);
        return dto;
    }
    
    public ChatCompleteVo chatComplete(ChatCompleteDto dto) throws BizException {

        ChatCompleteVo ret = new ChatCompleteVo();
        
        try {

            // 1. 获取并验证模型配置
            AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
            if (modelEnum == null) {
                throw new BizException("无效的模型代码");
            }

            ///设置API URL和API Key Proxy
            String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
            String proxyUrl = configService.get(baseKey + "proxy");
            String apiKey = configService.get(baseKey + "apiKey");

            if (!StringUtils.hasText(apiKey)) {
                throw new BizException("未配置API Key");
            }
            
            //获取或创建会话
            Long userId = AuthService.getCurrentUserId();
            ModelChatThreadPo thread = createOrRetrieveThread(dto.getChatThread(), userId, modelEnum.getCode());
            ret.setChatThread(thread.getId());
            
            //保存用户消息
            createHistory(thread, dto.getMessage(), 0);

            //使用createModelChatDto创建请求DTO
            ModelChatParam modelChatParam = createModelChatDto(modelEnum.getCode(), userId);
            modelChatParam.setMessage(dto.getMessage());
            modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":generateContent");
            modelChatParam.setApiKey(apiKey);
            
            //转换历史记录
            modelChatParam.setHistories(as(thread.getHistories(), ModelChatParamHistory.class));
            
            //调用ModelGeminiService发送请求
            String responseText = modelGeminiService.sendMessageSync(HttpClientUtils.createHttpClient(proxyUrl, 30), modelChatParam);
            
            //保存AI响应
            createHistory(thread, responseText, 1);
            
            //更新会话使用的模型
            thread.setModelCode(modelEnum.getCode());
            threadRepository.save(thread);
            
            ret.setContent(responseText);
            
        } catch (Exception e) {
            throw new BizException("AI对话失败: " + e.getMessage());
        }
        
        return ret;
    }
    
    /**
     * 获取聊天视图数据
     * @param threadId 会话ID，可为null
     * @return 聊天视图数据
     */
    public ModelChatViewVo getChatView(Long threadId) {
        ModelChatViewVo vo = new ModelChatViewVo();
        
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 从枚举中获取所有可用的模型列表
        List<String> models = new ArrayList<>();
        for (AIModelEnum model : AIModelEnum.values()) {
            models.add(model.getCode());
        }
        vo.setModels(models);
        
        // 获取默认模型（枚举中的第一个）
        String defaultModel = AIModelEnum.values()[0].getCode();
        
        // 获取用户的所有会话
        List<ModelChatThreadPo> threads = threadRepository.findByUserIdOrderByUpdateTimeDesc(userId);
        
        // 转换会话列表
        List<ModelChatViewThreadVo> threadVos = new ArrayList<>();
        for (ModelChatThreadPo thread : threads) {
            ModelChatViewThreadVo threadVo = new ModelChatViewThreadVo();
            assign(thread, threadVo);
            threadVos.add(threadVo);
        }
        
        vo.setThreads(threadVos);
        vo.setThreadCount(threadVos.size());
        
        // 如果指定了会话ID，则加载该会话的消息
        if (threadId == null) {
            vo.setMessages(new ArrayList<>());
            vo.setCurrentThreadId(null);
            vo.setSelectedModel(defaultModel);
            return vo;
        }
        
        ModelChatThreadPo thread = threadRepository.findByIdWithHistories(threadId);
        if (thread == null) {
            vo.setMessages(new ArrayList<>());
            vo.setCurrentThreadId(null);
            vo.setSelectedModel(defaultModel);
            return vo;
        }
        
        if (!thread.getUserId().equals(userId)) {
            vo.setMessages(new ArrayList<>());
            vo.setCurrentThreadId(null);
            vo.setSelectedModel(defaultModel);
            return vo;
        }
        
        // 转换消息列表
        List<ModelChatViewMessageVo> messages = new ArrayList<>();
        for (ModelChatHistoryPo history : thread.getHistories()) {
            ModelChatViewMessageVo messageVo = new ModelChatViewMessageVo();
            assign(history, messageVo);
            messages.add(messageVo);
        }
        
        // 按序号排序
        messages.sort((a, b) -> a.getSequence().compareTo(b.getSequence()));
        
        vo.setMessages(messages);
        vo.setCurrentThreadId(threadId);
        
        // 设置当前会话信息
        ModelChatViewThreadVo currentThread = new ModelChatViewThreadVo();
        assign(thread, currentThread);
        vo.setCurrentThread(currentThread);
        vo.setSelectedModel(thread.getModelCode());
        return vo;
    }
    
    /**
     * 流式对话完成
     * @param dto 对话请求参数
     * @param userId 用户ID
     * @param callback 流式响应回调
     * @throws BizException 业务异常
     */
    public void chatCompleteSSE(ChatCompleteDto dto, Long userId, Consumer<String> callback) throws BizException {
        try {
            //获取并验证模型配置
            AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
            if (modelEnum == null) {
                throw new BizException("无效的模型代码");
            }

            String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
            String apiUrl = GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent" + SSE_PARAM;

            //获取所有配置 - 使用传入的userId
            String apiKey = configService.get(baseKey + "apiKey", userId);
            if (!StringUtils.hasText(apiKey)) {
                throw new BizException("未配置API Key");
            }
            
            //获取或创建会话，使用传入的userId而不是从AuthContext获取
            ModelChatThreadPo thread = createOrRetrieveThread(dto.getChatThread(), userId, modelEnum.getCode());

            String proxyConfig = configService.get(baseKey + "proxy", userId);
            double temperature = configService.getDouble(baseKey + "temperature", DEFAULT_TEMPERATURE, userId);
            double topP = configService.getDouble(baseKey + "topP", DEFAULT_TOP_P, userId);
            int topK = configService.getInt(baseKey + "topK", DEFAULT_TOP_K, userId);
            int maxOutputTokens = configService.getInt(baseKey + "maxOutputTokens", 800, userId);
            
            //保存用户消息
            createHistory(thread, dto.getMessage(), 0);
            
            //创建HTTP客户端
            OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 60);
            
            //构建并发送请求
            GeminiRequest geminiRequest = GeminiRequest.ofHistory(
                thread.getHistories(), 
                dto.getMessage(),
                temperature,
                topP,
                topK,
                maxOutputTokens
            );
            String jsonBody = gson.toJson(geminiRequest);
            
            // 创建请求对象
            Request request = new Request.Builder()
                .url(apiUrl + "&key=" + apiKey)
                .post(RequestBody.create(jsonBody, JSON))
                .build();
            
            StringBuilder responseBuilder = new StringBuilder();
            
            // 创建ModelChatParam对象
            ModelChatParam modelChatParam = new ModelChatParam();
            modelChatParam.setModelCode(modelEnum.getCode());
            modelChatParam.setMessage(dto.getMessage());
            modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent");
            modelChatParam.setApiKey(apiKey);
            modelChatParam.setTemperature(temperature);
            modelChatParam.setTopP(topP);
            modelChatParam.setTopK(topK);
            modelChatParam.setMaxOutputTokens(maxOutputTokens);
            modelChatParam.setHistories(as(thread.getHistories(), ModelChatParamHistory.class));
            
            // 使用新的回调机制
            modelGeminiService.sendMessageStream(client, modelChatParam, context -> {
                try {
                    if (context.getType() == 0) {
                        // 数据类型
                        String text = context.getContent();
                                if (text != null) {
                                    responseBuilder.append(text);
                                    callback.accept(text);
                        }
                    } else if (context.getType() == 1) {
                        // 完成类型
                        // 保存完整的AI响应
                        String fullResponse = context.getContent();
            if (!StringUtils.hasText(fullResponse)) {
                throw new BizException("Gemini API 返回内容为空");
            }
            
            createHistory(thread, fullResponse, 1);
            
            //更新会话使用的模型
            thread.setModelCode(modelEnum.getCode());
            threadRepository.save(thread);
                    } else if (context.getType() == 2) {
                        // 错误类型
                        throw new BizException("AI对话失败: " + 
                            (context.getException() != null ? context.getException().getMessage() : "未知错误"));
                    }
                } catch (Exception e) {
                    log.error("处理SSE响应失败", e);
                    throw new RuntimeException(e);
                }
            });
            
        } catch (Exception e) {
            throw new BizException("AI对话失败: " + e.getMessage());
        }
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
        Long threadId = dto.getThread();

        // 检查该会话是否正在处理中
        if (chatThreadProcessingStatus.containsKey(threadId)) {
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

            // 获取或创建会话
            ModelChatThreadPo thread = createOrRetrieveThread(dto.getThread(), userId, modelEnum.getCode());

            // 保存用户消息
            createHistory(thread, dto.getMessage(), 0);

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

            // 获取配置
            String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
            String proxyConfig = configService.get(baseKey + "proxy", userId);
            String apiKey = configService.get(baseKey + "apiKey", userId);
            if (!StringUtils.hasText(apiKey)) {
                throw new BizException("未配置API Key");
            }

            // 创建HTTP客户端
            OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 60);

            // 创建请求DTO
            ModelChatParam modelChatParam = createModelChatDto(modelEnum.getCode(), userId);
            modelChatParam.setMessage(dto.getMessage());
            modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent");
            modelChatParam.setApiKey(apiKey);
            modelChatParam.setHistories(as(thread.getHistories(), ModelChatParamHistory.class));

            // 异步调用ModelGeminiService发送流式请求
            modelGeminiService.sendMessageStream(
                    client,
                    modelChatParam,
                    // 统一回调 - 处理所有类型的消息
                    context -> {
                        try {
                            // 从ModelChatContext中获取contextId
                            String contextId = context.getContextId();

                            // 检查该contextId是否已被终止
                            if (terminatedContextIds.containsKey(contextId)) {
                                // 如果是完成或错误回调，从终止列表中移除
                                if (context.getType() == 1 || context.getType() == 2) {
                                    terminatedContextIds.remove(contextId);
                                }
                                return;
                            }

                            // 首次收到消息时，将contextId存入映射表
                            if (!chatThreadProcessingStatus.containsValue(contextId)) {
                                chatThreadProcessingStatus.put(threadId, contextId);
                            }

                            // 获取当前最大序号
                            int nextSequence = segmentRepository.findMaxSequenceByThreadId(thread.getId()) + 1;
                            
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
                                // 完成类型 - 创建结束片段
                                ModelChatSegmentPo endSegment = new ModelChatSegmentPo();
                                endSegment.setUserId(userId);
                                endSegment.setThread(thread);
                                endSegment.setSequence(nextSequence);
                                endSegment.setContent(null);
                                endSegment.setStatus(0); // 未读状态
                                endSegment.setType(2); // 结束类型
                                segmentRepository.save(endSegment);

                                // 保存AI响应到历史记录
                                createHistory(thread, context.getContent(), 1);

                                // 更新会话使用的模型
                                thread.setModelCode(modelEnum.getCode());
                                threadRepository.save(thread);
                                
                                // 尝试生成会话标题
                                try {
                                    generateThreadTitle(thread.getId(), modelEnum.getCode());
                                } catch (Exception e) {
                                    log.error("生成会话标题失败", e);
                                    // 生成标题失败不影响主流程
                                }
                                
                                // 清理会话状态
                                chatThreadProcessingStatus.remove(thread.getId());
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
                                chatThreadProcessingStatus.remove(thread.getId());
                            }
                        } catch (Exception e) {
                            log.error("处理聊天片段失败", e);
                        }
                    }
            );

            // 返回开始片段
            ChatSegmentVo vo = new ChatSegmentVo();
            vo.setThreadId(thread.getId());
            vo.setSequence(startSegment.getSequence());
            vo.setContent(null);
            vo.setType(0); // 开始类型
            return vo;

        } catch (Exception e) {
            // 发生异常时清理会话状态
            chatThreadProcessingStatus.remove(threadId);

            // 清理所有片段
            try {
                segmentRepository.deleteByThreadId(threadId);
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
        Long threadId = dto.getThread();
        Long userId = AuthService.getCurrentUserId();
        
        // 最大等待次数和等待时间
        final int MAX_WAIT_TIMES = 10;
        final long WAIT_INTERVAL_MS = 300;
        
        // 尝试获取未读片段，最多等待10次
        List<ModelChatSegmentPo> unreadSegments = null;
        int waitTimes = 0;
        
        while (waitTimes < MAX_WAIT_TIMES) {
            unreadSegments = segmentRepository.findNextUnreadByThreadId(threadId);
            
            // 如果有未读片段，跳出循环
            if (!unreadSegments.isEmpty()) {
                break;
            }
            
            // 如果没有未读片段，且会话不在处理中，直接返回null
            if (!chatThreadProcessingStatus.containsKey(threadId)) {
                // 没有未读片段
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
            throw new BizException("等待片段超时，请稍后再试");
        }
        
        // 获取第一个未读片段
        ModelChatSegmentPo segment = unreadSegments.get(0);
        
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
        segment.setStatus(1); // 已读状态
        segmentRepository.save(segment);
        
        // 转换为VO
        ChatSegmentVo vo = new ChatSegmentVo();
        vo.setThreadId(threadId);
        vo.setSequence(segment.getSequence());
        vo.setContent(segment.getContent());
        vo.setType(segment.getType());
        return vo;
    }

    /**
     * 终止批量聊天响应
     * @param dto 批量聊天请求参数
     * @throws BizException 业务异常
     */
    public void chatCompleteTerminateBatch(BatchChatCompleteDto dto) throws BizException {
        Long threadId = dto.getThread();
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
        String contextId = chatThreadProcessingStatus.get(threadId);
        if (contextId == null) {
            throw new BizException("该会话未在进行中或已经终止");
        }

        // 将contextId加入终止列表
        terminatedContextIds.put(contextId, true);
        
        // 清理会话状态
        chatThreadProcessingStatus.remove(threadId);

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
     * @param threadId 会话ID
     * @param newTitle 新标题
     * @return 更新后的会话ID
     * @throws BizException 业务异常
     */
    public Long editThreadTitle(Long threadId, String newTitle) throws BizException {
        if (threadId == null) {
            throw new BizException("会话ID不能为空");
        }
        
        if (!StringUtils.hasText(newTitle)) {
            throw new BizException("会话标题不能为空");
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
        thread.setTitleGenerated(2);
        threadRepository.save(thread);
        
        return thread.getId();
    }

    /**
     * 生成会话标题
     * @param threadId 会话ID
     * @param model 模型代码
     * @throws BizException 业务异常
     */
    public void generateThreadTitle(Long threadId, String model) throws BizException {
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
            
            // 获取第一条用户消息
            List<ModelChatHistoryPo> histories = thread.getHistories();
            if (histories == null || histories.isEmpty()) {
                return; // 没有历史记录，无法生成标题
            }
            
            // 查找第一条用户消息
            ModelChatHistoryPo firstUserMessage = null;
            for (ModelChatHistoryPo history : histories) {
                if (history.getRole() == 0) { // 用户消息
                    firstUserMessage = history;
                    break;
                }
            }
            
            if (firstUserMessage == null || !StringUtils.hasText(firstUserMessage.getContent())) {
                return; // 没有找到用户消息或消息内容为空
            }
            
            // 从配置获取提示语模板
            String promptTemplate = globalConfigService.get("model.chat.gen.thread.prompt", 
                "总结内容并生成一个简短的标题(不超过10个字符),请直接回复标题,不要回复其他任何多余的话! 需总结的内容:#{content}");
            
            // 替换模板中的内容占位符
            String prompt = promptTemplate.replace("#{content}", firstUserMessage.getContent());
            
            // 获取当前用户ID
            Long userId = thread.getUserId();
            
            // 创建请求DTO
            ModelChatParam modelChatParam = createModelChatDto(model, userId);
            modelChatParam.setMessage(prompt);
            
            // 设置API URL和API Key
            String baseKey = "ai.model.cfg." + model + ".";
            String proxyUrl = configService.get(baseKey + "proxy", userId);
            String apiKey = configService.get(baseKey + "apiKey", userId);
            
            if (!StringUtils.hasText(apiKey)) {
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
}