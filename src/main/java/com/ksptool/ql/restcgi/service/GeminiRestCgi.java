package com.ksptool.ql.restcgi.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.FluxHttpClient;
import com.ksptool.ql.commons.utils.GsonUtils;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import com.ksptool.ql.restcgi.model.provider.GeminiRequest;
import com.ksptool.ql.restcgi.model.provider.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@Service
public class GeminiRestCgi implements ModelRestCgi {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final Gson gson = new Gson();

    @Override
    public CgiChatResult sendMessage(CgiChatParam p) throws BizException {

        var req = new GeminiRequest(p);

        //加载Variant参数
        JsonElement element = GsonUtils.injectContent(gson.toJsonTree(req), p.getVariantParam());
        String jsonBody = gson.toJson(element);

        // 创建请求对象
        Request request = new Request.Builder()
                .url(GEMINI_BASE_URL + p.getModel().getCode() + ":generateContent?key=" + p.getApikey())
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .build();


        // 发送请求并处理响应
        try (Response response = p.getHttpClient().newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new BizException("调用Gemini API失败: " + response);
            }

            String responseBody = response.body().string();

            // 记录请求和响应
            log.info("Gemini API 请求响应 - 模型: {}, 请求: {}, 响应: {}",
                    p.getModel().getCode(),
                    jsonBody.replaceAll("\\s+", ""),
                    responseBody.replaceAll("\\s+", ""));

            GeminiResponse geminiResponse = gson.fromJson(responseBody, GeminiResponse.class);
            String responseText = geminiResponse.getFirstResponseText();

            if (responseText == null) {
                throw new BizException("Gemini API 返回内容为空");
            }

            var crr = new CgiChatResult();
            crr.setModel(p.getModel());
            crr.setType(1);
            crr.setContent(responseText);
            crr.setSeq(1);
            crr.setTokenInput(geminiResponse.getUsageMetadata().getPromptTokenCount());
            crr.setTokenOutput(geminiResponse.getUsageMetadata().getCandidatesTokenCount());
            crr.setTokenThoughtOutput(geminiResponse.getUsageMetadata().getThoughtsTokenCount());
            return crr;

        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new BizException("发送消息失败: " + e.getMessage());
        }
    }

    @Override
    public void sendMessage(CgiChatParam p, Consumer<CgiChatResult> callback) throws BizException {

        var req = new GeminiRequest(p);

        //加载Variant参数
        JsonElement element = GsonUtils.injectContent(gson.toJsonTree(req), p.getVariantParam());
        String jsonBody = gson.toJson(element);

        //创建请求对象
        Request request = new Request.Builder()
                .url(GEMINI_BASE_URL + p.getModel().getCode() + ":streamGenerateContent?alt=sse&key=" + p.getApikey())
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .build();

        //进入虚拟线程 处理SSE响应
        Thread.startVirtualThread(() -> {

            try (Response response = p.getHttpClient().newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    throw new BizException("调用Gemini API失败 连接已被关闭");
                }

                AtomicInteger seq = new AtomicInteger(0);

                GeminiResponse geminiResponse = null;
                StringBuilder responseBuilder = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if (!"[DONE]".equals(data)) {
                                geminiResponse = gson.fromJson(data, GeminiResponse.class);
                                String text = geminiResponse.getFirstResponseText();
                                if (text != null) {
                                    responseBuilder.append(text);
                                    // 调用回调，返回type为数据(0)的Result
                                    if (callback != null) {
                                        var ccr = new CgiChatResult();
                                        ccr.setModel(p.getModel());
                                        ccr.setType(0);
                                        ccr.setContent(text);
                                        ccr.setSeq(seq.getAndIncrement());
                                        ccr.setException(null);
                                        ccr.setTokenInput(0);
                                        ccr.setTokenOutput(0);
                                        ccr.setTokenThoughtOutput(0);
                                        callback.accept(ccr);
                                    }
                                }
                            }
                        }
                    }
                }

                //整个SSE都响应完成 需通知回调完成
                if(callback != null){
                    var ccr = new CgiChatResult();
                    ccr.setModel(p.getModel());
                    ccr.setType(1); //返回类型 0:数据 1:结束 2:错误
                    ccr.setContent(responseBuilder.toString());
                    ccr.setSeq(seq.get());
                    ccr.setException(null);
                    ccr.setTokenInput(-1);
                    ccr.setTokenOutput(-1);
                    ccr.setTokenThoughtOutput(-1);

                    if(geminiResponse != null && geminiResponse.getUsageMetadata() != null){
                        ccr.setTokenInput(geminiResponse.getUsageMetadata().getPromptTokenCount());
                        ccr.setTokenOutput(geminiResponse.getUsageMetadata().getCandidatesTokenCount());
                        ccr.setTokenThoughtOutput(geminiResponse.getUsageMetadata().getThoughtsTokenCount());
                    }

                    callback.accept(ccr);
                }


            } catch (Exception e) {
                log.error(e.getMessage(),e);
                //流式响应出现错误 需通知回调
                if(callback != null){
                    var ccr = new CgiChatResult();
                    ccr.setModel(p.getModel());
                    ccr.setType(2); //0:数据 1:结束 2:错误
                    ccr.setContent(e.getMessage());
                    ccr.setSeq(-1);
                    ccr.setException(e);
                    ccr.setTokenInput(-1);
                    ccr.setTokenOutput(-1);
                    ccr.setTokenThoughtOutput(-1);
                    callback.accept(ccr);
                }
            }
        });


    }

    @Override
    public Flux<CgiChatResult> sendMessageFlux(CgiChatParam p) {

        var req = new GeminiRequest(p);

        //加载Variant参数
        JsonElement element = GsonUtils.injectContent(gson.toJsonTree(req), p.getVariantParam());
        String jsonBody = gson.toJson(element);

        //创建请求对象
        Request request = new Request.Builder()
                .url(GEMINI_BASE_URL + p.getModel().getCode() + ":streamGenerateContent?alt=sse&key=" + p.getApikey())
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .build();

        FluxHttpClient fhc = new FluxHttpClient(p.getHttpClient());
        AtomicInteger seq = new AtomicInteger(0);
        StringBuilder finalText = new StringBuilder();

        return fhc.send(request)
                .filter(line->line.startsWith("data: "))
                .map(line -> line.substring(6).trim())
                .filter(line->!line.equals("[DONE]"))
                .map(line -> processFragment(line,p,seq,finalText))
                .concatWith(Flux.defer(() -> {

                    if (StringUtils.isBlank(finalText.toString())) {
                        return Flux.error(new BizException("Gemini响应内容为空!"));
                    }
                    
                    var result = new CgiChatResult();
                    result.setModel(p.getModel());
                    result.setType(50);
                    result.setContent(finalText.toString());
                    result.setSeq(seq.getAndIncrement());
                    result.setException(null);
                    result.setTokenInput(-1);
                    result.setTokenOutput(-1);
                    result.setTokenThoughtOutput(-1);
                    return Flux.just(result);
                }))
                .onErrorResume(error -> processException(p,error));
    }
    
    private CgiChatResult processFragment(String line, CgiChatParam p, AtomicInteger seq, StringBuilder finalText) {
        try {
            GeminiResponse geminiResponse = gson.fromJson(line, GeminiResponse.class);
            String text = geminiResponse.getFirstResponseText();

            var result = new CgiChatResult();
            result.setModel(p.getModel());
            result.setType(1);
            result.setContent("");
            result.setSeq(seq.getAndIncrement());
            result.setTokenInput(0);
            result.setTokenOutput(0);
            result.setTokenThoughtOutput(0);

            if(StringUtils.isBlank(text)){
                return result;
            }
            
            finalText.append(text);
            result.setContent(text);
            return result;
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    private Flux<CgiChatResult> processException(CgiChatParam p, Throwable error) {
        var result = new CgiChatResult();
        result.setModel(p.getModel());
        result.setType(51);
        result.setContent("流处理错误:" + error.getMessage());
        result.setSeq(-1);
        result.setException(error instanceof Exception ? (Exception) error : new Exception(error));
        result.setTokenInput(-1);
        result.setTokenOutput(-1);
        result.setTokenThoughtOutput(-1);
        return Flux.just(result);
    }


}
