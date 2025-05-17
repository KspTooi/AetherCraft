package com.ksptool.ql.restcgi.service;

import com.google.gson.Gson;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import com.ksptool.ql.restcgi.model.provider.GrokRequest;
import com.ksptool.ql.restcgi.model.provider.GrokResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@Service
public class GrokRestCgi implements ModelRestCgi {

    private static final String GROK_BASE_URL = "https://api.x.ai/v1/chat/completions";
    private final Gson gson = new Gson();

    @Override
    public CgiChatResult sendMessage(CgiChatParam p) throws BizException {
        // 使用新的构造函数创建请求对象
        var req = new GrokRequest(p);
        req.setStream(false);
        String jsonBody = gson.toJson(req);

        // 创建请求对象
        Request request = new Request.Builder()
                .url(GROK_BASE_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + p.getApikey())
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .build();

        // 发送请求并处理响应
        try (Response response = p.getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new BizException("调用Grok API失败: " + response.body().string());
            }

            String responseBody = response.body().string();

            // 记录请求和响应
            log.info("Grok API 请求响应 - 模型: {}, 请求: {}, 响应: {}",
                    p.getModel().getCode(),
                    jsonBody.replaceAll("\\s+", ""),
                    responseBody.replaceAll("\\s+", ""));

            GrokResponse grokResponse = gson.fromJson(responseBody, GrokResponse.class);

            if (grokResponse.getChoices() == null || grokResponse.getChoices().isEmpty()) {
                throw new BizException("Grok API 返回内容为空");
            }

            GrokResponse.Choice choice = grokResponse.getChoices().get(0);
            if (choice.getMessage() == null || choice.getMessage().getContent() == null) {
                throw new BizException("Grok API 返回内容为空");
            }

            var crr = new CgiChatResult();
            crr.setModel(p.getModel());
            crr.setType(1);
            crr.setContent(choice.getMessage().getContent());
            crr.setSeq(1);

            if (grokResponse.getUsage() != null) {
                crr.setTokenInput(grokResponse.getUsage().getPromptTokens());
                crr.setTokenOutput(grokResponse.getUsage().getCompletionTokens());
                
                // 从completion_tokens_details中获取reasoning_tokens
                if (grokResponse.getUsage().getCompletionTokensDetails() != null) {
                    crr.setTokenThoughtOutput(grokResponse.getUsage().getCompletionTokensDetails().getReasoningTokens());
                }
            }

            return crr;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("发送消息失败: " + e.getMessage());
        }
    }

    @Override
    public void sendMessage(CgiChatParam p, Consumer<CgiChatResult> callback) throws BizException {
        var req = new GrokRequest(p);
        req.setStream(true);
        String jsonBody = gson.toJson(req);

        // 创建请求对象
        Request request = new Request.Builder()
                .url(GROK_BASE_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + p.getApikey())
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .build();

        // 使用虚拟线程处理SSE响应
        Thread.startVirtualThread(() -> {
            try (Response response = p.getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new BizException("调用Grok API失败: " + response.body().string());
                }

                AtomicInteger seq = new AtomicInteger(0);
                GrokResponse grokResponse = null;
                StringBuilder responseBuilder = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                log.debug("接收到流式响应结束标记");
                                break;
                            }

                            try {
                                grokResponse = gson.fromJson(data, GrokResponse.class);
                                if (grokResponse.getChoices() != null && !grokResponse.getChoices().isEmpty()) {
                                    GrokResponse.Choice choice = grokResponse.getChoices().getFirst();

                                    // 检查是否有finish_reason，如果是stop，则跳过
                                    if ("stop".equals(choice.getFinishReason())) {
                                        log.debug("接收到完成原因: {}", choice.getFinishReason());
                                        continue;
                                    }

                                    // 检查delta中是否有content
                                    if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                                        String deltaContent = choice.getDelta().getContent();
                                        responseBuilder.append(deltaContent);

                                        // 调用回调，返回type为数据(0)的Result
                                        if (callback != null) {
                                            var ccr = new CgiChatResult();
                                            ccr.setModel(p.getModel());
                                            ccr.setType(0);
                                            ccr.setContent(deltaContent);
                                            ccr.setSeq(seq.getAndIncrement());
                                            ccr.setException(null);
                                            ccr.setTokenInput(0);
                                            ccr.setTokenOutput(0);
                                            ccr.setTokenThoughtOutput(0);
                                            callback.accept(ccr);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("解析响应数据失败: {}", e.getMessage());
                            }
                        }
                    }
                }

                // 获取完整响应
                String fullResponse = responseBuilder.toString();
                if (fullResponse.isEmpty()) {
                    throw new BizException("Grok API 返回内容为空");
                }

                // 记录完整请求和响应
                log.info("Grok API 流式请求响应 - 模型: {}, 请求: {}, 完整响应长度: {}",
                        p.getModel().getCode(),
                        jsonBody.replaceAll("\\s+", ""),
                        fullResponse.length());

                // 调用回调，返回type为1的Result
                if (callback != null) {
                    var ccr = new CgiChatResult();
                    ccr.setModel(p.getModel());
                    ccr.setType(1);
                    ccr.setContent(fullResponse);
                    ccr.setSeq(seq.get());

                    if (grokResponse != null && grokResponse.getUsage() != null) {
                        ccr.setTokenInput(grokResponse.getUsage().getPromptTokens());
                        ccr.setTokenOutput(grokResponse.getUsage().getCompletionTokens());
                        
                        // 从completion_tokens_details中获取reasoning_tokens
                        if (grokResponse.getUsage().getCompletionTokensDetails() != null) {
                            ccr.setTokenThoughtOutput(grokResponse.getUsage().getCompletionTokensDetails().getReasoningTokens());
                        }
                    }

                    callback.accept(ccr);
                }

            } catch (Exception e) {
                log.error("Grok API 请求异常: ", e);
                // 调用回调，返回type为2的Result
                if (callback != null) {
                    var ccr = new CgiChatResult();
                    ccr.setModel(p.getModel());
                    ccr.setType(2);
                    ccr.setContent(e.getMessage());
                    ccr.setException(e);
                    ccr.setSeq(-1);
                    callback.accept(ccr);
                }
            }
        });
    }
}
