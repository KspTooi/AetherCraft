package com.ksptool.ql.biz.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 模型聊天请求参数
 */
@Data
public class ModelChatParam {
    
    /**
     * 模型代码
     */
    private String modelCode;
    
    /**
     * 消息内容
     */
    private String message;
    
    /**
     * 历史记录列表
     */
    private List<ModelChatParamHistory> histories;
    
    /**
     * 温度参数
     */
    private Double temperature;
    
    /**
     * Top P参数
     */
    private Double topP;
    
    /**
     * Top K参数
     */
    private Integer topK;
    
    /**
     * 最大输出令牌数
     */
    private Integer maxOutputTokens;
    
    /**
     * API URL
     */
    private String url;
    
    /**
     * API Key
     */
    private String apiKey;

} 