package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.ApiKeyAuthorizationRepository;
import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.model.dto.SaveModelConfigDto;
import com.ksptool.ql.biz.model.po.ApiKeyAuthorizationPo;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.vo.AvailableApiKeyVo;
import com.ksptool.ql.biz.model.vo.ModelConfigVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.ConfigService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PanelModelConfigService {
    
    @Autowired
    private ConfigService configService;
    
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    
    @Autowired
    private ApiKeyAuthorizationRepository apiKeyAuthorizationRepository;
    
    /**
     * 获取当前用户可用的API密钥列表
     */
    public List<AvailableApiKeyVo> getAvailableApiKey() {
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        List<AvailableApiKeyVo> result = new ArrayList<>();
        
        // 查询用户自己的API密钥
        List<ApiKeyPo> ownedKeys = apiKeyRepository.findByUserId(userId);
        if (!ownedKeys.isEmpty()) {
            for (ApiKeyPo key : ownedKeys) {
                if (key.getStatus() != 1) {
                    continue;
                }
                AvailableApiKeyVo vo = new AvailableApiKeyVo();
                vo.setApiKeyId(key.getId());
                vo.setKeyName(key.getKeyName());
                vo.setKeyType(key.getKeyType());
                vo.setOwnerUsername(key.getUser().getUsername());
                result.add(vo);
            }
        }
        
        // 查询被授权的API密钥
        List<ApiKeyAuthorizationPo> authorizedKeys = apiKeyAuthorizationRepository.findByAuthorizedUserIdAndStatus(userId, 1);
        if (!authorizedKeys.isEmpty()) {
            for (ApiKeyAuthorizationPo auth : authorizedKeys) {
                ApiKeyPo key = auth.getApiKey();
                if (key.getStatus() != 1) {
                    continue;
                }
                
                // 检查使用限制
                if (auth.getUsageLimit() != null && auth.getUsageCount() >= auth.getUsageLimit()) {
                    continue;
                }
                
                // 检查是否过期
                if (auth.getExpireTime() != null && auth.getExpireTime().before(new Date())) {
                    continue;
                }
                
                AvailableApiKeyVo vo = new AvailableApiKeyVo();
                vo.setApiKeyId(key.getId());
                vo.setKeyName(key.getKeyName());
                vo.setKeyType(key.getKeyType());
                vo.setOwnerUsername(key.getUser().getUsername());
                result.add(vo);
            }
        }
        
        return result;
    }
    
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
        Long userId = AuthService.getCurrentUserId();
        
        // 从配置服务加载配置
        String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
        
        // 检查API Key是否已保存，但不返回具体值
        String apiKey = configService.getValue(baseKey + "apiKey", userId);
        config.setHasApiKey(StringUtils.hasText(apiKey));
        
        config.setProxy(configService.getValue(baseKey + "proxy", userId));
        
        // 获取温度值，默认0.7
        String tempStr = configService.getValue(baseKey + "temperature", userId);
        config.setTemperature(tempStr != null ? Double.parseDouble(tempStr) : 0.7);
        
        // 获取top_p值，默认1.0
        String topPStr = configService.getValue(baseKey + "topP", userId);
        config.setTopP(topPStr != null ? Double.parseDouble(topPStr) : 1.0);
        
        // 获取top_k值，默认40
        String topKStr = configService.getValue(baseKey + "topK", userId);
        config.setTopK(topKStr != null ? Integer.parseInt(topKStr) : 40);
        
        // 获取最大输出长度，默认800
        String maxOutputTokensStr = configService.getValue(baseKey + "maxOutputTokens", userId);
        config.setMaxOutputTokens(maxOutputTokensStr != null ? Integer.parseInt(maxOutputTokensStr) : 800);
        
        // 获取可用的API密钥列表
        config.setApiKeys(getAvailableApiKey());
        
        return config;
    }
    
    /**
     * 保存模型配置
     */
    public void saveConfig(SaveModelConfigDto dto) {
        // 验证模型是否存在
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
        if (modelEnum == null) {
            throw new IllegalArgumentException("无效的模型代码");
        }
        
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 构建配置键前缀
        String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
        
        // 只在API Key不为空时保存
        if (StringUtils.hasText(dto.getApiKey())) {
            configService.setValue(baseKey + "apiKey", dto.getApiKey(), userId);
        }
        
        // 保存其他配置
        configService.setValue(baseKey + "proxy", dto.getProxy(), userId);
        configService.setValue(baseKey + "temperature", String.valueOf(dto.getTemperature()), userId);
        configService.setValue(baseKey + "topP", String.valueOf(dto.getTopP()), userId);
        configService.setValue(baseKey + "topK", String.valueOf(dto.getTopK()), userId);
        configService.setValue(baseKey + "maxOutputTokens", String.valueOf(dto.getMaxOutputTokens()), userId);
    }
} 