package com.ksptool.ac.test.restcgi;

import com.ksptool.ql.AetherLauncher;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.restcgi.model.CgiChatMessage;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import com.ksptool.ql.restcgi.service.ModelRestCgi;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@SpringBootTest(classes = AetherLauncher.class)
public class ModelRestCgiTest {

    @Autowired
    private ModelRestCgi restCgi;

    private OkHttpClient httpClient;
    private String geminiApiKey;
    private String grokApiKey;

    @BeforeEach
    void setUp() throws BizException {
        // 从环境变量获取代理配置
        String proxyConfig = System.getenv("HTTP-FORWARD-NEXT");
        httpClient = HttpClientUtils.createHttpClient(proxyConfig, 60);

        // 从环境变量获取API密钥
        geminiApiKey = System.getenv("GEMINI_API_KEY");
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            log.warn("未设置环境变量 GEMINI_API_KEY");
        }

        grokApiKey = System.getenv("GROK_API_KEY");
        if (grokApiKey == null || grokApiKey.trim().isEmpty()) {
            log.warn("未设置环境变量 GROK_API_KEY");
        }
    }

    @Test
    void testGeminiSync() throws Exception {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            log.error("未设置环境变量 GEMINI_API_KEY，跳过测试");
            return;
        }

        var param = new CgiChatParam();
        param.setModel(AIModelEnum.GEMINI_2_FLASH);
        param.setApikey(geminiApiKey);
        param.setHttpClient(httpClient);
        
        var message = new CgiChatMessage();
        message.setSenderType(0);
        message.setContent("你好，请简单介绍一下你自己。");
        message.setSeq(1);
        param.setMessage(message);
        
        param.setTemperature(0.7);
        param.setTopP(1.0);
        param.setTopK(40);
        param.setMaxOutputTokens(2048);
        
        CgiChatResult result = restCgi.sendMessage(param);
        
        log.info("Gemini同步响应: {}", result.getContent());
        log.info("输入Token: {}, 输出Token: {}, 思考Token: {}", 
                result.getTokenInput(), 
                result.getTokenOutput(), 
                result.getTokenThoughtOutput());
    }

    @Test
    void testGeminiAsync() throws Exception {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            log.error("未设置环境变量 GEMINI_API_KEY，跳过测试");
            return;
        }

        var param = new CgiChatParam();
        param.setModel(AIModelEnum.GEMINI_15_PRO);
        param.setApikey(geminiApiKey);
        param.setHttpClient(httpClient);
        
        var message = new CgiChatMessage();
        message.setSenderType(0);
        message.setContent("请用300字介绍一下人工智能的发展历史。");
        message.setSeq(1);
        param.setMessage(message);
        
        param.setTemperature(0.7);
        param.setTopP(1.0);
        param.setTopK(40);
        param.setMaxOutputTokens(2048);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> fullResponse = new AtomicReference<>("");
        
        restCgi.sendMessage(param, result -> {
            if (result.getType() == 0) {
                // 流式数据
                log.info("收到数据片段: {}", result.getContent());
            } else if (result.getType() == 1) {
                // 完整响应
                fullResponse.set(result.getContent());
                log.info("输入Token: {}, 输出Token: {}, 思考Token: {}", 
                        result.getTokenInput(), 
                        result.getTokenOutput(), 
                        result.getTokenThoughtOutput());
                latch.countDown();
            } else if (result.getType() == 2) {
                // 错误
                log.error("发生错误: {}", result.getContent());
                latch.countDown();
            }
        });

        latch.await(60, TimeUnit.SECONDS);
        log.info("Gemini异步完整响应: {}", fullResponse.get());
    }

    @Test
    void testGrokSync() throws Exception {
        if (grokApiKey == null || grokApiKey.trim().isEmpty()) {
            log.error("未设置环境变量 GROK_API_KEY，跳过测试");
            return;
        }

        var param = new CgiChatParam();
        param.setModel(AIModelEnum.GROK_3_BETA);
        param.setApikey(grokApiKey);
        param.setHttpClient(httpClient);
        
        var message = new CgiChatMessage();
        message.setSenderType(0);
        message.setContent("你好，请简单介绍一下你自己。");
        message.setSeq(1);
        param.setMessage(message);
        
        param.setTemperature(0.7);
        param.setTopP(1.0);
        param.setTopK(40);
        param.setMaxOutputTokens(2048);
        
        CgiChatResult result = restCgi.sendMessage(param);
        
        log.info("Grok同步响应: {}", result.getContent());
        log.info("输入Token: {}, 输出Token: {}, 思考Token: {}", 
                result.getTokenInput(), 
                result.getTokenOutput(), 
                result.getTokenThoughtOutput());
    }

    @Test
    void testGrokAsync() throws Exception {
        if (grokApiKey == null || grokApiKey.trim().isEmpty()) {
            log.error("未设置环境变量 GROK_API_KEY，跳过测试");
            return;
        }

        var param = new CgiChatParam();
        param.setModel(AIModelEnum.GROK_3_BETA);
        param.setApikey(grokApiKey);
        param.setHttpClient(httpClient);
        
        var message = new CgiChatMessage();
        message.setSenderType(0);
        message.setContent("请用300字介绍一下人工智能的发展历史。");
        message.setSeq(1);
        param.setMessage(message);
        
        param.setTemperature(0.7);
        param.setTopP(1.0);
        param.setTopK(40);
        param.setMaxOutputTokens(2048);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> fullResponse = new AtomicReference<>("");
        
        restCgi.sendMessage(param, result -> {
            if (result.getType() == 0) {
                // 流式数据
                log.info("收到数据片段: {}", result.getContent());
            } else if (result.getType() == 1) {
                // 完整响应
                fullResponse.set(result.getContent());
                log.info("输入Token: {}, 输出Token: {}, 思考Token: {}", 
                        result.getTokenInput(), 
                        result.getTokenOutput(), 
                        result.getTokenThoughtOutput());
                latch.countDown();
            } else if (result.getType() == 2) {
                // 错误
                log.error("发生错误: {}", result.getContent());
                latch.countDown();
            }
        });

        latch.await(60, TimeUnit.SECONDS);
        log.info("Grok异步完整响应: {}", fullResponse.get());
    }
}
