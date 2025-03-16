package com.ksptool.ql.biz.service;

import com.google.gson.Gson;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import com.ksptool.ql.biz.model.grok.GrokRequest;
import com.ksptool.ql.biz.model.grok.GrokResponse;
import com.ksptool.ql.biz.model.po.ModelChatHistoryPo;
import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.ksptool.entities.Entities.as;

/**
 * Grok模型服务
 */
@Slf4j
@Service
public class ModelGrokService implements ModelRestCI{

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    // Grok API 不使用URL参数来指定流式输出，而是在请求体中设置stream字段
    private final Gson gson = new Gson();

    @Override
    public String sendMessageSync(OkHttpClient client, ModelChatParam dto) throws BizException {
        try {
            // 验证参数
            validateParams(dto);
            
            // 构建请求
            GrokRequest grokRequest = GrokRequest.ofHistory(
                    as(dto.getHistories(), ModelChatHistoryPo.class),
                    dto.getMessage(),
                    dto.getTemperature(),
                    dto.getTopP(),
                    dto.getMaxOutputTokens(),
                    dto.getSystemPrompt()
            );
            // 设置为非流式输出
            grokRequest.setStream(false);
            grokRequest.setModel(dto.getModelCode());
            
            String jsonBody = gson.toJson(grokRequest);
            
            // 创建请求对象
            Request request = new Request.Builder()
                .url(dto.getUrl())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + dto.getApiKey())
                .post(RequestBody.create(jsonBody, JSON))
                .build();
            
            // 发送请求并处理响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new BizException("调用Grok API失败: " + response.body().string());
                }
                
                String responseBody = response.body().string();
                
                // 记录请求和响应
                log.info("Grok API 同步请求响应 - 模型: {}, 请求: {}, 响应长度: {}", 
                    dto.getModelCode(), 
                    jsonBody.replaceAll("\\s+", ""), 
                    responseBody.length());
                
                GrokResponse grokResponse = gson.fromJson(responseBody, GrokResponse.class);
                
                // 从响应中提取内容
                if (grokResponse.getChoices() == null || grokResponse.getChoices().isEmpty()) {
                    throw new BizException("Grok API 返回内容为空");
                }
                
                GrokResponse.Choice choice = grokResponse.getChoices().get(0);
                if (choice.getMessage() == null || StringUtils.isBlank(choice.getMessage().getContent())) {
                    throw new BizException("Grok API 返回内容为空");
                }
                
                return choice.getMessage().getContent();
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
        
        // 使用虚拟线程执行请求
        Thread.startVirtualThread(() -> {
            StringBuilder responseBuilder = new StringBuilder();
            
            try {
                // 验证参数
                validateParams(param);
                
                // 构建请求
                GrokRequest grokRequest = GrokRequest.ofHistory(
                        as(param.getHistories(), ModelChatHistoryPo.class),
                        param.getMessage(),
                        param.getTemperature(),
                        param.getTopP(),
                        param.getMaxOutputTokens(),
                        param.getSystemPrompt()
                );
                // 设置流式输出
                grokRequest.setStream(true);
                grokRequest.setModel(param.getModelCode());
                
                String jsonBody = gson.toJson(grokRequest);
                
                // 创建请求对象 - 使用流式API
                // 根据CURL命令，Grok API的URL格式为 https://api.x.ai/v1/chat/completions
                Request request = new Request.Builder()
                    .url(param.getUrl())
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + param.getApiKey())
                    .post(RequestBody.create(jsonBody, JSON))
                    .build();
                
                // 处理SSE响应
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new BizException("调用Grok API失败: " + response.body().string());
                    }
                    
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            log.debug("原始响应行: {}", line);
                            
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                if ("[DONE]".equals(data)) {
                                    log.debug("接收到流式响应结束标记");
                                    break;
                                }
                                
                                try {
                                    GrokResponse grokResponse = gson.fromJson(data, GrokResponse.class);
                                    
                                    // 从响应中提取delta内容
                                    if (grokResponse.getChoices() != null && !grokResponse.getChoices().isEmpty()) {
                                        GrokResponse.Choice choice = grokResponse.getChoices().get(0);
                                        
                                        // 检查是否有finish_reason，如果有且不为null，则跳过
                                        if (choice.getFinishReason() != null && !choice.getFinishReason().isEmpty()) {
                                            log.debug("接收到完成原因: {}", choice.getFinishReason());
                                            continue;
                                        }
                                        
                                        // 检查delta中是否有content
                                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                                            String deltaContent = choice.getDelta().getContent();
                                            responseBuilder.append(deltaContent);
                                            
                                            // 调用回调，返回type为0的ModelChatContext
                                            if (callback != null) {
                                                ModelChatContext context = new ModelChatContext();
                                                context.setContextId(contextId);
                                                context.setType(0); // 数据类型
                                                context.setContent(deltaContent);
                                                context.setSequence(sequence.getAndIncrement());
                                                callback.accept(context);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.warn("解析响应数据失败: {}", e.getMessage());
                                }
                            }
                        }
                    }
                }
                
                // 获取完整响应
                String fullResponse = responseBuilder.toString();
                if (StringUtils.isBlank(fullResponse)) {
                    throw new BizException("Grok API 返回内容为空");
                }
                
                // 记录完整请求和响应
                log.info("Grok API 流式请求响应 - 模型: {}, 请求: {}, 完整响应长度: {}", 
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
                log.error("Grok API 请求异常: ", e);
                
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
     * 测试函数 - 发送一条测试消息并等待响应
     * @param apiUrl Grok API的URL，例如 https://api.x.ai/v1/chat/completions
     * @param apiKey Grok API的密钥
     * @return 模型的完整响应文本
     * @throws BizException 业务异常
     * @throws InterruptedException 中断异常
     */
    public String testSendMessage(String apiUrl, String apiKey) throws BizException, InterruptedException {
        // 创建HTTP客户端
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        
        // 创建请求参数
        ModelChatParam param = new ModelChatParam();
        param.setModelCode("grok-2-latest");
        param.setMessage("测试消息：请简要介绍一下你自己。");
        param.setUrl(apiUrl);
        param.setApiKey(apiKey);
        param.setTemperature(0.7);
        param.setTopP(1.0);
        param.setMaxOutputTokens(800);
        param.setSystemPrompt("你是一个有用的AI助手。");
        param.setHistories(new ArrayList<>());
        
        // 用于等待响应完成
        CountDownLatch latch = new CountDownLatch(1);
        
        // 存储完整响应
        AtomicReference<String> fullResponse = new AtomicReference<>("");
        
        // 存储错误信息
        AtomicReference<Exception> error = new AtomicReference<>();
        
        // 创建回调函数
        Consumer<ModelChatContext> callback = context -> {
            try {
                if (context.getType() == 0) {
                    // 数据类型 - 打印片段
                    log.info("收到片段响应: {}", context.getContent());
                } else if (context.getType() == 1) {
                    // 结束类型 - 存储完整响应并释放锁
                    log.info("收到完整响应，长度: {}", context.getContent().length());
                    fullResponse.set(context.getContent());
                    latch.countDown();
                } else if (context.getType() == 2) {
                    // 错误类型 - 存储错误并释放锁
                    log.error("收到错误: {}", context.getContent());
                    error.set(context.getException());
                    latch.countDown();
                }
            } catch (Exception e) {
                log.error("回调处理异常", e);
                error.set(e);
                latch.countDown();
            }
        };
        
        // 发送消息
        log.info("开始发送测试消息...");
        sendMessageStream(client, param, callback);
        
        // 等待响应完成或超时
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        
        if (!completed) {
            throw new BizException("请求超时");
        }
        
        if (error.get() != null) {
            if (error.get() instanceof BizException) {
                throw (BizException) error.get();
            } else {
                throw new BizException("请求异常: " + error.get().getMessage());
            }
        }
        
        return fullResponse.get();
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
        
        if (StringUtils.isBlank(dto.getModelCode())) {
            throw new BizException("模型代码不能为空");
        }
        
        if (StringUtils.isBlank(dto.getMessage())) {
            throw new BizException("消息内容不能为空");
        }
        
        if (StringUtils.isBlank(dto.getUrl())) {
            throw new BizException("API URL不能为空");
        }
        
        if (StringUtils.isBlank(dto.getApiKey())) {
            throw new BizException("API Key不能为空");
        }
    }
}
