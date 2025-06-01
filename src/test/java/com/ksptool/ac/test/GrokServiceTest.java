package com.ksptool.ac.test;

import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.vo.ModelChatContext;
import okhttp3.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.ksptool.ql.commons.utils.HttpClientUtils.createHttpClient;

/**
 * Grok服务测试类
 * 用于测试Grok API的连接和响应
 */
public class GrokServiceTest {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    /**
     * 测试入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // API URL
        String apiUrl = "https://api.x.ai/v1/chat/completions";
        
        // 从系统环境变量获取API Key
        String apiKey = System.getenv("GROK_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("错误: 未设置环境变量 GROK_API_KEY");
            System.err.println("请设置环境变量: set GROK_API_KEY=xai-XXXXXXXXX");
            return;
        }
        
        System.out.println("开始测试Grok API连接...");
        
        try {
            String response = testGrokApi(apiUrl, apiKey);
            System.out.println("测试成功！");
            System.out.println("响应内容: " + response);
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试Grok API连接
     * @param apiUrl API URL
     * @param apiKey API Key
     * @return 响应内容
     * @throws Exception 异常信息
     */
    private static String testGrokApi(String apiUrl, String apiKey) throws Exception {
        // 创建HTTP客户端
        OkHttpClient client = createHttpClient(System.getenv("HTTP-FORWARD-NEXT"), 6);
        
        // 创建请求参数
        ModelChatParam param = new ModelChatParam();
        param.setModelCode("grok-2");
        param.setMessage("测试消息：请简要介绍一下你自己。");
        param.setUrl(apiUrl);
        param.setApiKey(apiKey);
        param.setTemperature(0.7);
        param.setTopP(1.0);
        param.setMaxOutputTokens(800);
        param.setSystemPrompt("你是一个非常有用的AI助手。");
        param.setHistories(new ArrayList<>());
        
        // 用于等待响应完成
        CountDownLatch latch = new CountDownLatch(1);
        
        // 存储完整响应
        AtomicReference<String> fullResponse = new AtomicReference<>("");
        
        // 存储错误信息
        AtomicReference<Exception> error = new AtomicReference<>();
        
        // 创建ModelGrokService实例
        //ModelGrokService grokService = new ModelGrokService();
        
        // 创建回调函数
        Consumer<ModelChatContext> callback = context -> {
            try {
                if (context.getType() == 0) {
                    // 数据类型 - 打印片段
                    System.out.println("收到片段响应: " + context.getContent());
                } else if (context.getType() == 1) {
                    // 结束类型 - 存储完整响应并释放锁
                    System.out.println("收到完整响应，长度: " + context.getContent().length());
                    fullResponse.set(context.getContent());
                    latch.countDown();
                } else if (context.getType() == 2) {
                    // 错误类型 - 存储错误并释放锁
                    System.err.println("收到错误: " + context.getContent());
                    error.set(context.getException());
                    latch.countDown();
                }
            } catch (Exception e) {
                System.err.println("回调处理异常: " + e.getMessage());
                error.set(e);
                latch.countDown();
            }
        };
        
        // 发送消息
        System.out.println("开始发送测试消息...");
        //grokService.sendMessageStream(client, param, callback);
        
        // 等待响应完成或超时
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        
        if (!completed) {
            throw new Exception("请求超时");
        }
        
        if (error.get() != null) {
            throw error.get();
        }
        
        return fullResponse.get();
    }
} 