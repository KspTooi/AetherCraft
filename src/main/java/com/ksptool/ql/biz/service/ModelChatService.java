package com.ksptool.ql.biz.service;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
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
import com.ksptool.ql.commons.AuthContext;
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
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;
import com.ksptool.ql.biz.model.vo.ModelChatViewVo;
import com.ksptool.ql.biz.model.vo.ModelChatViewThreadVo;
import com.ksptool.ql.biz.model.vo.ModelChatViewMessageVo;
import static com.ksptool.entities.Entities.assign;

@Slf4j
@Service
public class ModelChatService {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final Pattern PROXY_PATTERN = Pattern.compile("^(http|socks5):(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})$");
    
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
    
    public ChatCompleteVo chatComplete(ChatCompleteDto dto) throws BizException {
        ChatCompleteVo vo = new ChatCompleteVo();
        
        try {
            // 1. 获取并验证模型配置
            AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
            if (modelEnum == null) {
                throw new BizException("无效的模型代码");
            }
            
            // 2. 获取或创建会话
            ModelChatThreadPo thread = createOrRetrieveThread(dto.getChatThread(), AuthContext.getCurrentUserId(), modelEnum.getCode());
            vo.setChatThread(thread.getId());
            
            String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
            String apiUrl = GEMINI_BASE_URL + modelEnum.getCode() + ":generateContent";
            
            // 3. 获取所有配置
            String apiKey = configService.get(baseKey + "apiKey");
            if (!StringUtils.hasText(apiKey)) {
                throw new BizException("未配置API Key");
            }
            
            String proxyConfig = configService.get(baseKey + "proxy");
            double temperature = configService.getDouble(baseKey + "temperature", DEFAULT_TEMPERATURE);
            double topP = configService.getDouble(baseKey + "topP", DEFAULT_TOP_P);
            int topK = configService.getInt(baseKey + "topK", DEFAULT_TOP_K);
            
            // 4. 保存用户消息
            createHistory(thread, dto.getMessage(), 0);
            
            // 5. 配置HTTP客户端
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            if (StringUtils.hasText(proxyConfig)) {
                if (!PROXY_PATTERN.matcher(proxyConfig).matches()) {
                    throw new BizException("代理配置格式错误，正确格式为: http:127.0.0.1:8080 或 socks5:127.0.0.1:1080");
                }
                
                String[] parts = proxyConfig.split(":");
                String proxyHost = parts[1];
                int proxyPort = Integer.parseInt(parts[2]);
                
                if (proxyPort <= 0 || proxyPort > 65535) {
                    throw new BizException("代理端口必须在1-65535之间");
                }
                
                if (parts[0].equals("http")) {
                    clientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
                }
                
                if (parts[0].equals("socks5")) {
                    clientBuilder.proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort)));
                }
            }
            
            // 6. 构建并发送请求
            GeminiRequest geminiRequest = GeminiRequest.ofHistory(thread.getHistories(), dto.getMessage(), temperature, topP, topK);
            String jsonBody = gson.toJson(geminiRequest);
            
            Request request = new Request.Builder()
                .url(apiUrl + "?key=" + apiKey)
                .post(RequestBody.create(jsonBody, JSON))
                .build();
                
            // 7. 处理响应
            try (Response response = clientBuilder.build().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new BizException("调用Gemini API失败: " + response.body().string());
                }
                
                String responseBody = response.body().string();
                
                // 记录请求和响应
                log.info("Gemini API 请求响应 - 模型: {}, 请求: {}, 响应: {}", 
                    modelEnum.getCode(), 
                    jsonBody.replaceAll("\\s+", ""), 
                    responseBody.replaceAll("\\s+", ""));
                
                GeminiResponse geminiResponse = gson.fromJson(responseBody, GeminiResponse.class);
                
                String responseText = geminiResponse.getFirstResponseText();
                if (responseText == null) {
                    throw new BizException("Gemini API 返回内容为空");
                }
                
                // 8. 保存AI响应
                createHistory(thread, responseText, 1);
                
                vo.setContent(responseText);
                vo.setConversationId(java.util.UUID.randomUUID().toString());
            }
            
        } catch (Exception e) {
            throw new BizException("AI对话失败: " + e.getMessage());
        }
        
        return vo;
    }
    
    /**
     * 获取聊天视图数据
     * @param threadId 会话ID，可为null
     * @return 聊天视图数据
     */
    public ModelChatViewVo getChatView(Long threadId) {
        ModelChatViewVo vo = new ModelChatViewVo();
        
        // 获取当前用户ID
        Long userId = AuthContext.getCurrentUserId();
        
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
} 