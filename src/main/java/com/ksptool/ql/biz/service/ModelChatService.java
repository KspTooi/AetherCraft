package com.ksptool.ql.biz.service;

import com.google.gson.Gson;
import com.ksptool.ql.model.dto.ChatCompleteDto;
import com.ksptool.ql.model.vo.ChatCompleteVo;
import com.ksptool.ql.model.gemini.GeminiRequest;
import com.ksptool.ql.model.gemini.GeminiResponse;
import com.ksptool.ql.commons.exception.BizException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Service
public class ModelChatService {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Gson gson = new Gson();

    private String apiKey = "";
    
    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    
    // 配置SOCKS5代理
    private final OkHttpClient client = new OkHttpClient.Builder()
        .proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("192.168.99.50", 9909)))
        .build();
    
    public ChatCompleteVo chatComplete(ChatCompleteDto dto) throws BizException {
        ChatCompleteVo vo = new ChatCompleteVo();
        vo.setChatThread(dto.getChatThread());
        
        try {
            // 构建请求
            GeminiRequest geminiRequest = GeminiRequest.of(dto.getMessage());
            String jsonBody = gson.toJson(geminiRequest);
            
            // 发送请求
            Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + apiKey)
                .post(RequestBody.create(jsonBody, JSON))
                .build();
                
            // 执行请求
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new BizException("调用Gemini API失败: " + response.body().string());
                }
                
                // 解析响应
                String responseBody = response.body().string();
                GeminiResponse geminiResponse = gson.fromJson(responseBody, GeminiResponse.class);
                
                String responseText = geminiResponse.getFirstResponseText();
                if (responseText == null) {
                    throw new BizException("Gemini API 返回内容为空");
                }
                
                // 设置响应
                vo.setContent(responseText);
                vo.setConversationId(java.util.UUID.randomUUID().toString());
            }
            
        } catch (Exception e) {
            throw new BizException("AI对话失败: " + e.getMessage());
        }
        
        return vo;
    }
} 