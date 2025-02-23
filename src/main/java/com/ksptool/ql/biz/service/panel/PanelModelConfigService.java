package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.model.vo.ModelConfigVo;
import com.ksptool.ql.biz.service.ConfigService;
import com.ksptool.ql.commons.AuthContext;
import com.ksptool.ql.commons.enums.AIModelEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PanelModelConfigService {
    
    @Autowired
    private ConfigService configService;
    
    /**
     * 获取模型配置编辑视图
     */
    public ModelConfigVo getEditView(String modelCode) {
        // 创建配置VO
        ModelConfigVo config = new ModelConfigVo();
        
        // 获取目标模型
        AIModelEnum modelEnum = null;
        if (modelCode != null) {
            modelEnum = AIModelEnum.getByCode(modelCode);
        }
        
        // 如果模型未指定或无效，使用第一个模型
        if (modelEnum == null) {
            modelEnum = AIModelEnum.values()[0];
        }
        
        // 设置模型信息
        config.setModel(modelEnum.getCode());
        config.setModelName(modelEnum.getName());
        
        // 获取当前用户ID
        Long userId = AuthContext.getCurrentUserId();
        
        // 从配置服务加载配置
        String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
        config.setApiKey(configService.getConfigValue(baseKey + "apiKey", userId));
        config.setProxy(configService.getConfigValue(baseKey + "proxy", userId));
        
        // 获取温度值，默认0.7
        String tempStr = configService.getConfigValue(baseKey + "temperature", userId);
        config.setTemperature(tempStr != null ? Double.parseDouble(tempStr) : 0.7);
        
        // 获取top_p值，默认1.0
        String topPStr = configService.getConfigValue(baseKey + "topP", userId);
        config.setTopP(topPStr != null ? Double.parseDouble(topPStr) : 1.0);
        
        return config;
    }
} 