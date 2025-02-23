package com.ksptool.ql.biz.service;

import com.google.gson.Gson;
import com.ksptool.ql.biz.model.dto.ChatCompleteDto;
import com.ksptool.ql.biz.model.vo.ChatCompleteVo;
import com.ksptool.ql.biz.model.gemini.GeminiRequest;
import com.ksptool.ql.biz.model.gemini.GeminiResponse;
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
    
    public ChatCompleteVo chatComplete(ChatCompleteDto dto) throws BizException {
        ChatCompleteVo vo = new ChatCompleteVo();
        vo.setChatThread(dto.getChatThread());
        
        try {
            // 1. 获取并验证模型配置
            AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
            if (modelEnum == null) {
                throw new BizException("无效的模型代码");
            }
            
            String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
            String apiUrl = GEMINI_BASE_URL + modelEnum.getCode() + ":generateContent";
            
            // 2. 获取所有配置
            String apiKey = configService.get(baseKey + "apiKey");
            if (!StringUtils.hasText(apiKey)) {
                throw new BizException("未配置API Key");
            }
            
            String proxyConfig = configService.get(baseKey + "proxy");
            double temperature = configService.getDouble(baseKey + "temperature", DEFAULT_TEMPERATURE);
            double topP = configService.getDouble(baseKey + "topP", DEFAULT_TOP_P);
            int topK = configService.getInt(baseKey + "topK", DEFAULT_TOP_K);
            
            // 3. 配置HTTP客户端
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
            
            // 4. 构建并发送请求
            GeminiRequest geminiRequest = GeminiRequest.of(dto.getMessage(), temperature, topP, topK);
            String jsonBody = gson.toJson(geminiRequest);
            
            Request request = new Request.Builder()
                .url(apiUrl + "?key=" + apiKey)
                .post(RequestBody.create(jsonBody, JSON))
                .build();
                
            // 5. 处理响应
            try (Response response = clientBuilder.build().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new BizException("调用Gemini API失败: " + response.body().string());
                }
                
                String responseBody = response.body().string();
                GeminiResponse geminiResponse = gson.fromJson(responseBody, GeminiResponse.class);
                
                String responseText = geminiResponse.getFirstResponseText();
                if (responseText == null) {
                    throw new BizException("Gemini API 返回内容为空");
                }
                
                vo.setContent(responseText);
                vo.setConversationId(java.util.UUID.randomUUID().toString());
            }
            
        } catch (Exception e) {
            throw new BizException("AI对话失败: " + e.getMessage());
        }
        
        return vo;
    }
} 