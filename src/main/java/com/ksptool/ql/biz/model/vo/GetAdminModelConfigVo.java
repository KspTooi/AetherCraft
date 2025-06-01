package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型配置VO
 */
@Data
public class GetAdminModelConfigVo {
    
    // 模型代码
    private String modelCode;
    
    // 模型名称
    private String modelName;

    // 全局代理配置
    private String globalProxyConfig;
    
    // 用户代理配置
    private String userProxyConfig;
    
    // 温度值
    private Double temperature = 0.7;
    
    // 采样值
    private Double topP = 1.0;
    
    // Top K值
    private Integer topK = 40;
    
    // 最大输出长度
    private Integer maxOutputTokens = 800;

    // 可用的API密钥列表
    private List<AvailableApiKeyVo> apiKeys = new ArrayList<>();
    
    // 当前使用的API密钥ID
    private Long currentApiKeyId;
} 