package com.ksptool.ql.restcgi.model;

import com.ksptool.ql.commons.enums.AIModelEnum;
import lombok.Data;
import okhttp3.OkHttpClient;

import java.util.List;

@Data
public class CgiChatParam {

    //模型枚举
    private AIModelEnum model;

    //消息内容
    private CgiChatMessage message;

    //历史对话内容
    private List<CgiChatMessage> historyMessages;

    //温度参数       (CGI自动获取)
    private double temperature;

    //TopP参数      (CGI自动获取)
    private double topP;

    //TopK参数      (CGI自动获取)
    private int topK;

    //最大输出令牌数  (CGI自动获取)
    private int maxOutputTokens;

    //APIKEY
    private String apikey;

    //系统Prompt
    private String systemPrompt;

    //用于发起REST请求的HttpClient (CGI自动获取 可被覆盖)
    private OkHttpClient httpClient;

}
