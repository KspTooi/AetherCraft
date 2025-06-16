package com.ksptool.ql.restcgi.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.CallbackHttpClient;
import com.ksptool.ql.commons.utils.GsonUtils;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import com.ksptool.ql.restcgi.model.provider.GrokRequest;
import com.ksptool.ql.restcgi.model.provider.GrokResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

        //加载Variant参数
        JsonElement element = GsonUtils.injectContent(gson.toJsonTree(req), p.getVariantParam());
        String jsonBody = gson.toJson(element);

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

            GrokResponse.Choice choice = grokResponse.getChoices().getFirst();
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
        if(callback == null){
            throw new BizException("callback为空");
        }

        var req = new GrokRequest(p);
        req.setStream(true);

        //加载Variant参数
        JsonElement element = GsonUtils.injectContent(gson.toJsonTree(req), p.getVariantParam());
        String jsonBody = gson.toJson(element);

        // 创建请求对象
        Request request = new Request.Builder()
                .url(GROK_BASE_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + p.getApikey())
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .build();

        CallbackHttpClient client = new CallbackHttpClient(p.getHttpClient());
        AtomicInteger seq = new AtomicInteger(0);
        AtomicReference<GrokResponse> lastRet = new AtomicReference<>();
        StringBuilder allText = new StringBuilder();

        client.onResponse((body) -> {
            try {
                if (!body.startsWith("data: ")) {
                    return;
                }
                if(body.contains("[DONE]")){
                    return;
                }

                String data = body.substring(6);
                GrokResponse gr = gson.fromJson(data, GrokResponse.class);
                lastRet.set(gr);

                if (gr.getChoices() == null || gr.getChoices().isEmpty()) {
                    return;
                }

                GrokResponse.Choice choice = gr.getChoices().getFirst();
                if (choice.getDelta() == null || choice.getDelta().getContent() == null) {
                    return;
                }

                String text = choice.getDelta().getContent();
                allText.append(text);
                CgiChatResult ccr = CgiChatResult.text(p, text, seq.getAndIncrement());
                callback.accept(ccr);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                var error = CgiChatResult.error(p, e.getMessage(), e);
                callback.accept(error);
            }
        });

        client.onError((e) -> {
            var error = CgiChatResult.error(p, e.getMessage(), e);
            callback.accept(error);
        });

        client.onComplete(() -> {
            if (allText.isEmpty() || lastRet.get() == null) {
                var error = CgiChatResult.error(p, "Grok返回内容为空", new BizException("Grok返回内容为空"));
                callback.accept(error);
                return;
            }

            GrokResponse lastGr = lastRet.get();
            CgiChatResult ccr = CgiChatResult.finish(p, allText.toString(), "", seq.getAndIncrement());

            if (lastGr != null && lastGr.getUsage() != null) {
                ccr.setTokenInput(lastGr.getUsage().getPromptTokens());
                ccr.setTokenOutput(lastGr.getUsage().getCompletionTokens());
                if (lastGr.getUsage().getCompletionTokensDetails() != null) {
                    ccr.setTokenThoughtOutput(lastGr.getUsage().getCompletionTokensDetails().getReasoningTokens());
                }
            }
            callback.accept(ccr);
        });

        client.send(request);
    }

    @Override
    public Flux<CgiChatResult> sendMessageFlux(CgiChatParam param) {
        return null;
    }
}
