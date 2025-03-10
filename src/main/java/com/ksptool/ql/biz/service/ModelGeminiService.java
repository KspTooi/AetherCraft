package com.ksptool.ql.biz.service;

import com.google.gson.Gson;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import com.ksptool.ql.biz.model.gemini.GeminiRequest;
import com.ksptool.ql.biz.model.gemini.GeminiResponse;
import com.ksptool.ql.biz.model.po.ModelChatHistoryPo;
import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.ksptool.entities.Entities.as;

/**
 * Gemini模型服务
 */
@Slf4j
@Service
public class ModelGeminiService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String SSE_PARAM = "?alt=sse";
    private final Gson gson = new Gson();
    
    /**
     * 同步发送消息并获取响应
     * @param client HTTP客户端
     * @param dto 聊天请求参数
     * @return 模型响应文本
     * @throws BizException 业务异常
     */
    public String sendMessageSync(OkHttpClient client, ModelChatParam dto) throws BizException {
        try {
            // 验证参数
            validateParams(dto);
            
            // 构建请求
            GeminiRequest geminiRequest = GeminiRequest.ofHistory(
                    as(dto.getHistories(),ModelChatHistoryPo.class),
                    dto.getMessage(),
                    dto.getTemperature(),
                    dto.getTopP(),
                    dto.getTopK(),
                    dto.getMaxOutputTokens()
            );
            String jsonBody = gson.toJson(geminiRequest);
            
            // 创建请求对象
            Request request = new Request.Builder()
                .url(dto.getUrl() + "?key=" + dto.getApiKey())
                .post(RequestBody.create(jsonBody, JSON))
                .build();
                
            // 发送请求并处理响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new BizException("调用Gemini API失败: " + response.body().string());
                }
                
                String responseBody = response.body().string();
                
                // 记录请求和响应
                log.info("Gemini API 请求响应 - 模型: {}, 请求: {}, 响应: {}", 
                    dto.getModelCode(), 
                    jsonBody.replaceAll("\\s+", ""), 
                    responseBody.replaceAll("\\s+", ""));
                
                GeminiResponse geminiResponse = gson.fromJson(responseBody, GeminiResponse.class);
                
                String responseText = geminiResponse.getFirstResponseText();
                if (responseText == null) {
                    throw new BizException("Gemini API 返回内容为空");
                }
                
                return responseText;
            }
        } catch (IOException e) {
            throw new BizException("发送消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 流式发送消息并通过回调处理响应
     * @param client HTTP客户端
     * @param param 聊天请求参数
     * @param callback 统一回调 - 处理所有类型的消息和错误
     */
    public void sendMessageStream(
            OkHttpClient client, 
            ModelChatParam param,
            Consumer<ModelChatContext> callback) {
        
        // 生成唯一的上下文ID
        String contextId = UUID.randomUUID().toString();
        
        // 使用原子整数跟踪序列号
        AtomicInteger sequence = new AtomicInteger(0);
        
        // 使用虚拟线程执行请求，但不使用try-with-resources，避免过早关闭executor
        Thread.startVirtualThread(() -> {
            StringBuilder responseBuilder = new StringBuilder();
            
            try {
                // 验证参数
                validateParams(param);
                
                // 构建请求
                GeminiRequest geminiRequest = GeminiRequest.ofHistory(
                        as(param.getHistories(), ModelChatHistoryPo.class),
                        param.getMessage(),
                        param.getTemperature(),
                        param.getTopP(),
                        param.getTopK(),
                        param.getMaxOutputTokens()
                );
                String jsonBody = gson.toJson(geminiRequest);
                
                // 创建请求对象 - 使用流式API
                Request request = new Request.Builder()
                    .url(param.getUrl() + SSE_PARAM + "&key=" + param.getApiKey())
                    .post(RequestBody.create(jsonBody, JSON))
                    .build();
                
                // 处理SSE响应
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
                                        // 调用回调，返回type为0的ModelChatContext
                                        if (callback != null) {
                                            ModelChatContext context = new ModelChatContext();
                                            context.setContextId(contextId);
                                            context.setType(0); // 数据类型
                                            context.setContent(text);
                                            context.setSequence(sequence.getAndIncrement());
                                            callback.accept(context);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 获取完整响应
                String fullResponse = responseBuilder.toString();
                if (!StringUtils.hasText(fullResponse)) {
                    throw new BizException("Gemini API 返回内容为空");
                }
                
                // 记录完整请求和响应
                log.info("Gemini API 流式请求响应 - 模型: {}, 请求: {}, 完整响应长度: {}", 
                    param.getModelCode(),
                    jsonBody.replaceAll("\\s+", ""), 
                    fullResponse.length());
                
                // 调用回调，返回type为1的ModelChatContext
                if (callback != null) {
                    ModelChatContext context = new ModelChatContext();
                    context.setContextId(contextId);
                    context.setType(1); // 结束类型
                    context.setContent(fullResponse);
                    context.setSequence(sequence.get()); // 使用当前序列号
                    context.setModelCode(param.getModelCode());
                    callback.accept(context);
                }
                
            } catch (Exception e) {
                // 统一处理所有异常
                log.error("Gemini API 请求异常: ", e);
                
                // 调用回调，返回type为2的ModelChatContext
                if (callback != null) {
                    ModelChatContext context = new ModelChatContext();
                    context.setContextId(contextId);
                    context.setType(2); // 错误类型
                    context.setContent(e.getMessage());
                    context.setException(e);
                    context.setSequence(sequence.get()); // 使用当前序列号
                    callback.accept(context);
                }
            }
        });
    }
    

    /**
     * 验证请求参数
     * @param dto 聊天请求参数
     * @throws BizException 业务异常
     */
    private void validateParams(ModelChatParam dto) throws BizException {
        if (dto == null) {
            throw new BizException("请求参数不能为空");
        }
        
        if (!StringUtils.hasText(dto.getModelCode())) {
            throw new BizException("模型代码不能为空");
        }
        
        if (!StringUtils.hasText(dto.getMessage())) {
            throw new BizException("消息内容不能为空");
        }
        
        if (!StringUtils.hasText(dto.getUrl())) {
            throw new BizException("API URL不能为空");
        }
        
        if (!StringUtils.hasText(dto.getApiKey())) {
            throw new BizException("API Key不能为空");
        }
    }
} 