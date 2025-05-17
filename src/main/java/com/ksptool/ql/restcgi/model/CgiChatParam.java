package com.ksptool.ql.restcgi.model;

import com.ksptool.ql.commons.enums.AIModelEnum;
import lombok.Data;

import java.util.List;

@Data
public class CgiChatParam {

    //模型枚举
    private AIModelEnum model;

    //消息内容
    private CgiChatMessage message;

    //历史对话内容
    private List<CgiChatMessage> historyMessages;

    //温度参数
    private double temperature;

    //TopP参数
    private double topP;

    //TopK参数
    private int topK;

    //最大输出令牌数
    private int maxOutputTokens;

    //APIKEY
    private String apikey;

    //系统Prompt
    private String systemPrompt;

}
