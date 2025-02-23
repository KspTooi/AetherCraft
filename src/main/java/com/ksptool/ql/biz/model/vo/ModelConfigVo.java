package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.commons.enums.AIModelEnum;
import lombok.Data;
import java.util.List;
import java.util.Arrays;

/**
 * 模型配置VO
 */
@Data
public class ModelConfigVo {
    
    // 模型代码
    private String model;
    
    // 模型名称
    private String modelName;
    
    // API密钥
    private String apiKey;
    
    // 代理地址
    private String proxy;
    
    // 温度值
    private Double temperature = 0.7;
    
    // 采样值
    private Double topP = 1.0;
    
    // 可用的模型列表
    private List<AIModelEnum> models = Arrays.asList(AIModelEnum.values());
} 