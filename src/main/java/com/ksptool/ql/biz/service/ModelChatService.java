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
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import com.ksptool.ql.biz.model.vo.ModelChatViewVo;
import com.ksptool.ql.biz.model.vo.ModelChatViewThreadVo;
import com.ksptool.ql.biz.model.vo.ModelChatViewMessageVo;
import static com.ksptool.entities.Entities.assign;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.concurrent.TimeUnit;
import com.ksptool.ql.biz.mapper.ModelChatSegmentRepository;
import com.ksptool.ql.biz.model.po.ModelChatSegmentPo;
import com.ksptool.ql.biz.model.vo.ChatSegmentVo;
import com.ksptool.ql.biz.model.dto.BatchChatCompleteDto;
import com.ksptool.ql.biz.model.dto.ModelChatDto;
import com.ksptool.ql.biz.model.dto.ModelChatHistoryDto;

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
    
    private final Gson gson = new Gson();
    
    @Autowired
    private ConfigService configService;
    
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
    public ModelChatDto createModelChatDto(String modelCode) throws BizException {
        return createModelChatDto(modelCode, AuthService.getCurrentUserId());
    }
    
    /**
     * 创建ModelChatDto并预先填充配置信息
     * @param modelCode 模型代码
     * @param userId 用户ID
     * @return 预填充配置的ModelChatDto
     * @throws BizException 业务异常
     */
    public ModelChatDto createModelChatDto(String modelCode, Long userId) throws BizException {
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
        ModelChatDto dto = new ModelChatDto();
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
            String apiUrl = GEMINI_BASE_URL + modelEnum.getCode() + ":generateContent";
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
            ModelChatDto modelChatDto = createModelChatDto(modelEnum.getCode(), userId);
            modelChatDto.setMessage(dto.getMessage());
            modelChatDto.setUrl(apiUrl);
            modelChatDto.setApiKey(apiKey);
            
            //转换历史记录
            List<ModelChatHistoryDto> historyDtos = new ArrayList<>();
            if (thread.getHistories() != null) {
                for (ModelChatHistoryPo historyPo : thread.getHistories()) {
                    ModelChatHistoryDto historyDto = new ModelChatHistoryDto();
                    historyDto.setRole(historyPo.getRole());
                    historyDto.setContent(historyPo.getContent());
                    historyDto.setSequence(historyPo.getSequence());
                    historyDtos.add(historyDto);
                }
            }
            modelChatDto.setHistories(historyDtos);
            
            //调用ModelGeminiService发送请求
            String responseText = modelGeminiService.sendMessageSync(HttpClientUtils.createHttpClient(proxyUrl, 30), modelChatDto);
            
            //保存AI响应
            createHistory(thread, responseText, 1);
            
            //更新会话使用的模型
            thread.setModelCode(modelEnum.getCode());
            threadRepository.save(thread);
            
            ret.setContent(responseText);
            ret.setConversationId(java.util.UUID.randomUUID().toString());
            
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
        if (thread != null) {
            ModelChatViewThreadVo currentThread = new ModelChatViewThreadVo();
            assign(thread, currentThread);
            vo.setCurrentThread(currentThread);
            vo.setSelectedModel(thread.getModelCode());
        }
        
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
                Double.valueOf(temperature), 
                Double.valueOf(topP), 
                Integer.valueOf(topK), 
                Integer.valueOf(maxOutputTokens)
            );
            String jsonBody = gson.toJson(geminiRequest);
            
            // 创建请求对象
            Request request = new Request.Builder()
                .url(apiUrl + "&key=" + apiKey)
                .post(RequestBody.create(jsonBody, JSON))
                .build();
            
            StringBuilder responseBuilder = new StringBuilder();
            
            //处理SSE响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new BizException("调用Gemini API失败: " + response.body().string());
                }
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if (!"[DONE]".equals(data)) {
                                GeminiResponse geminiResponse = gson.fromJson(data, GeminiResponse.class);
                                String text = geminiResponse.getFirstResponseText();
                                if (text != null) {
                                    responseBuilder.append(text);
                                    callback.accept(text);
                                }
                            }
                        }
                    }
                }
            }
            
            //保存完整的AI响应
            String fullResponse = responseBuilder.toString();
            if (!StringUtils.hasText(fullResponse)) {
                throw new BizException("Gemini API 返回内容为空");
            }
            
            createHistory(thread, fullResponse, 1);
            
            //更新会话使用的模型
            thread.setModelCode(modelEnum.getCode());
            threadRepository.save(thread);
            
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
        
        threadRepository.delete(thread);
    }

    /**
     * 批量聊天完成 - 长轮询方式
     * @param dto 批量聊天请求参数
     * @return 聊天片段VO
     * @throws BizException 业务异常
     */
    public ChatSegmentVo chatCompleteBatch(BatchChatCompleteDto dto) throws BizException {
        if (dto == null) {
            throw new BizException("请求参数不能为空");
        }
        
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 根据queryKind处理不同的请求类型
        if (dto.getQueryKind() == 0) { // 发送消息
            return handleSendMessage(dto, userId);
        }
        
        if (dto.getQueryKind() == 1) { // 查询响应流
            // 暂不实现
            throw new BizException("暂不支持查询响应流");
        }
        
        if (dto.getQueryKind() == 2) { // 终止AI响应
            // 暂不实现
            throw new BizException("暂不支持终止AI响应");
        }
        
        // 默认情况
        throw new BizException("无效的查询类型");
    }
    
    /**
     * 处理发送消息请求
     * @param dto 批量聊天请求参数
     * @param userId 用户ID
     * @return 聊天片段VO
     * @throws BizException 业务异常
     */
    private ChatSegmentVo handleSendMessage(BatchChatCompleteDto dto, Long userId) throws BizException {
        // 1. 验证参数
        if (dto.getChatThread() == null) {
            throw new BizException("会话ID不能为空");
        }
        
        if (!StringUtils.hasText(dto.getModel())) {
            throw new BizException("模型代码不能为空");
        }
        
        if (!StringUtils.hasText(dto.getMessage())) {
            throw new BizException("消息内容不能为空");
        }
        
        // 2. 获取或创建会话
        ModelChatThreadPo thread = createOrRetrieveThread(dto.getChatThread(), userId, dto.getModel());
        
        // 3. 检查当前会话是否有未完成的AI响应
        int unreadCount = segmentRepository.countUnreadByThreadId(thread.getId());
        if (unreadCount > 0) {
            // 获取最后一个片段，检查是否为结束或错误类型
            List<ModelChatSegmentPo> segments = segmentRepository.findByThreadIdOrderBySequenceAsc(thread.getId());
            if (!segments.isEmpty()) {
                ModelChatSegmentPo lastSegment = segments.get(segments.size() - 1);
                if (lastSegment.getType() != 2 && lastSegment.getType() != 10) {
                    throw new BizException("当前会话有未完成的AI响应，请等待响应完成后再发送新消息");
                }
            }
        }
        
        // 4. 清除之前的所有片段
        segmentRepository.deleteByThreadId(thread.getId());
        
        // 5. 保存用户消息到历史记录
        createHistory(thread, dto.getMessage(), 0);
        
        // 6. 创建开始片段
        ModelChatSegmentPo startSegment = new ModelChatSegmentPo();
        startSegment.setUserId(userId);
        startSegment.setThread(thread);
        startSegment.setSequence(0);
        startSegment.setContent("开始生成响应...");
        startSegment.setStatus(0); // 未读取
        startSegment.setType(0); // 开始类型
        startSegment = segmentRepository.save(startSegment);
        
        // 7. 启动虚拟线程处理AI响应
        Thread.startVirtualThread(() -> {
            try {
                // 获取模型配置
                AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
                if (modelEnum == null) {
                    createErrorSegment(thread, userId, "无效的模型代码");
                    return;
                }
                
                String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
                String apiUrl = GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent" + SSE_PARAM;
                
                // 获取所有配置
                String apiKey = configService.get(baseKey + "apiKey", userId);
                if (!StringUtils.hasText(apiKey)) {
                    createErrorSegment(thread, userId, "未配置API Key");
                    return;
                }
                
                String proxyConfig = configService.get(baseKey + "proxy", userId);
                double temperature = configService.getDouble(baseKey + "temperature", DEFAULT_TEMPERATURE, userId);
                double topP = configService.getDouble(baseKey + "topP", DEFAULT_TOP_P, userId);
                int topK = configService.getInt(baseKey + "topK", DEFAULT_TOP_K, userId);
                int maxOutputTokens = configService.getInt(baseKey + "maxOutputTokens", 800, userId);
                
                // 创建HTTP客户端
                OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 60);
                
                // 构建并发送请求
                GeminiRequest geminiRequest = GeminiRequest.ofHistory(
                    thread.getHistories(), 
                    dto.getMessage(), 
                    Double.valueOf(temperature), 
                    Double.valueOf(topP), 
                    Integer.valueOf(topK), 
                    Integer.valueOf(maxOutputTokens)
                );
                String jsonBody = gson.toJson(geminiRequest);
                
                // 创建请求对象
                Request request = new Request.Builder()
                    .url(apiUrl + "&key=" + apiKey)
                    .post(RequestBody.create(jsonBody, JSON))
                    .build();
                
                StringBuilder responseBuilder = new StringBuilder();
                int segmentSequence = 1; // 开始片段序号为0，数据片段从1开始
                
                // 处理SSE响应
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        createErrorSegment(thread, userId, "调用Gemini API失败: " + response.body().string());
                        return;
                    }
                    
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                if (!"[DONE]".equals(data)) {
                                    GeminiResponse geminiResponse = gson.fromJson(data, GeminiResponse.class);
                                    String text = geminiResponse.getFirstResponseText();
                                    if (text != null) {
                                        responseBuilder.append(text);
                                        
                                        // 创建数据片段
                                        ModelChatSegmentPo dataSegment = new ModelChatSegmentPo();
                                        dataSegment.setUserId(userId);
                                        dataSegment.setThread(thread);
                                        dataSegment.setSequence(segmentSequence++);
                                        dataSegment.setContent(text);
                                        dataSegment.setStatus(0); // 未读取
                                        dataSegment.setType(1); // 数据类型
                                        segmentRepository.save(dataSegment);
                                    }
                                }
                            }
                        }
                    }
                    
                    // 获取完整响应
                    String fullResponse = responseBuilder.toString();
                    if (!StringUtils.hasText(fullResponse)) {
                        createErrorSegment(thread, userId, "Gemini API 返回内容为空");
                        return;
                    }
                    
                    // 保存完整的AI响应到历史记录
                    createHistory(thread, fullResponse, 1);
                    
                    // 创建结束片段
                    ModelChatSegmentPo endSegment = new ModelChatSegmentPo();
                    endSegment.setUserId(userId);
                    endSegment.setThread(thread);
                    endSegment.setSequence(segmentSequence);
                    endSegment.setContent("生成完成");
                    endSegment.setStatus(0); // 未读取
                    endSegment.setType(2); // 结束类型
                    segmentRepository.save(endSegment);
                    
                    // 更新会话使用的模型
                    thread.setModelCode(modelEnum.getCode());
                    threadRepository.save(thread);
                }
            } catch (Exception e) {
                log.error("AI对话失败", e);
                createErrorSegment(thread, userId, "AI对话失败: " + e.getMessage());
            }
        });
        
        // 8. 返回开始片段
        ChatSegmentVo vo = new ChatSegmentVo();
        vo.setThreadId(thread.getId());
        vo.setSequence(startSegment.getSequence());
        vo.setContent(startSegment.getContent());
        vo.setType(startSegment.getType());
        vo.setHasMore(true); // 还有更多片段
        
        return vo;
    }
    
    /**
     * 创建错误片段
     * @param thread 会话
     * @param userId 用户ID
     * @param errorMessage 错误信息
     */
    private void createErrorSegment(ModelChatThreadPo thread, Long userId, String errorMessage) {
        try {
            ModelChatSegmentPo errorSegment = new ModelChatSegmentPo();
            errorSegment.setUserId(userId);
            errorSegment.setThread(thread);
            errorSegment.setSequence(segmentRepository.findMaxSequenceByThreadId(thread.getId()) + 1);
            errorSegment.setContent(errorMessage);
            errorSegment.setStatus(0); // 未读取
            errorSegment.setType(10); // 错误类型
            segmentRepository.save(errorSegment);
        } catch (Exception e) {
            log.error("创建错误片段失败", e);
        }
    }
}