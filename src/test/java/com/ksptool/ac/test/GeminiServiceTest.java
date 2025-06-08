package com.ksptool.ac.test;

import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import com.ksptool.ql.restcgi.model.CgiChatMessage;
import com.ksptool.ql.restcgi.service.GeminiRestCgi;
import com.ksptool.ql.biz.model.schema.ModelVariantSchema;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Gemini服务测试类
 * 用于测试Gemini API的连接和响应
 */
public class GeminiServiceTest {

    @Test
    public void testGeminiCgiFlux() throws BizException {

        // 从系统环境变量获取API Key
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("错误: 未设置环境变量 GEMINI_API_KEY");
            System.err.println("请设置环境变量: set GEMINI_API_KEY=XXXXXXX");
            return;
        }

        OkHttpClient httpClient = HttpClientUtils.createHttpClient("http://192.168.99.2:58000", 30);

        // 创建测试参数
        CgiChatParam param = new CgiChatParam();
        param.setApikey(apiKey);
        param.setHttpClient(httpClient);
        param.setMessage(new CgiChatMessage("你好，请介绍一下你自己"));
        
        // 创建模型信息
        ModelVariantSchema model = new ModelVariantSchema();
        model.setCode("gemini-2.0-flash");
        model.setName("Gemini 2.0 Flash");
        param.setModel(model);

        // 创建服务实例
        GeminiRestCgi geminiService = new GeminiRestCgi();

        System.out.println("开始测试 Gemini Flux API...");
        System.out.println("模型: " + model.getCode());
        System.out.println("消息: " + param.getMessage());
        System.out.println("----------------------------------------");

        try {
            // 调用流式接口
            Flux<CgiChatResult> flux = geminiService.sendMessageFlux(param);
            
            flux.timeout(Duration.ofMinutes(2))
                .doOnNext(result -> {
                    System.out.println("类型: " + result.getType() + 
                                     ", 序号: " + result.getSeq() + 
                                     ", 内容长度: " + result.getContent().length());
                    
                    if (result.getType() == 51) {
                        System.err.println("错误: " + result.getContent());
                        //if (result.getException() != null) {
                        //    result.getException().printStackTrace();
                        //}
                    }

                })
                .doOnComplete(() -> {
                    System.out.println("----------------------------------------");
                    System.out.println("流正常完成");
                })
                .doOnError(error -> {
                    System.err.println("流处理错误: " + error.getMessage());
                    //error.printStackTrace();
                })
                .blockLast();

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

} 