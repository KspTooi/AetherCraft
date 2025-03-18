package com.ksptool.ql.biz.service.panel;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ApiKeyAuthorizationRepository;
import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.mapper.ModelApiKeyConfigRepository;
import com.ksptool.ql.biz.model.dto.SaveModelConfigDto;
import com.ksptool.ql.biz.model.po.ApiKeyAuthorizationPo;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.po.ModelApiKeyConfigPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.AvailableApiKeyVo;
import com.ksptool.ql.biz.model.vo.ModelConfigVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.UserConfigService;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.service.ModelGeminiService;
import com.ksptool.ql.biz.service.ModelGrokService;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import okhttp3.OkHttpClient;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class PanelModelConfigService {
    
    @Autowired
    private UserConfigService userConfigService;
    
    @Autowired
    private GlobalConfigService globalConfigService;
    
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    
    @Autowired
    private ApiKeyAuthorizationRepository apiKeyAuthorizationRepository;
    
    @Autowired
    private ModelApiKeyConfigRepository modelApiKeyConfigRepository;
    
    @Autowired
    private PanelApiKeyService panelApiKeyService;
    
    @Autowired
    private ModelGeminiService modelGeminiService;
    
    @Autowired
    private ModelGrokService modelGrokService;
    
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GROK_BASE_URL = "https://api.x.ai/v1/chat/completions";


    /**
     * 获取当前用户特定模型系列可用的API密钥列表
     * @param series 模型系列，如 Gemini、Grok 等
     * @return 匹配系列的 API 密钥列表
     */
    public List<AvailableApiKeyVo> getAvailableApiKey(String series) {
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        List<AvailableApiKeyVo> result = new ArrayList<>();
        
        // 创建查询对象
        ApiKeyPo probe = new ApiKeyPo();
        UserPo userPo = new UserPo();
        userPo.setId(userId);
        probe.setUser(userPo);
        probe.setStatus(1);
        
        // 如果指定了系列，设置系列条件
        if (StringUtils.isNotBlank(series)) {
            probe.setKeySeries(series);
        }
        
        // 创建匹配器，忽略系列大小写
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withMatcher("keySeries", ExampleMatcher.GenericPropertyMatchers.ignoreCase());
        
        // 创建Example并执行查询
        Example<ApiKeyPo> example = Example.of(probe, matcher);
        List<ApiKeyPo> ownedKeys = apiKeyRepository.findAll(example);


        if (!ownedKeys.isEmpty()) {
            for (ApiKeyPo key : ownedKeys) {
                AvailableApiKeyVo vo = new AvailableApiKeyVo();
                vo.setApiKeyId(key.getId());
                vo.setKeyName(key.getKeyName());
                vo.setKeySeries(key.getKeySeries());
                vo.setOwnerUsername("");
                result.add(vo);
            }
        }
        
        // 查询被授权的API密钥 - 使用新的JPQL查询
        List<ApiKeyPo> authorizedKeys = apiKeyAuthorizationRepository.getApiKeyFromAuthorized(userId, 1, series);
        
        if (authorizedKeys != null && !authorizedKeys.isEmpty()) {
            for (ApiKeyPo key : authorizedKeys) {
                // 此处不需要再次检查status和series，因为JPQL查询已经过滤了
                
                ApiKeyAuthorizationPo auth = apiKeyAuthorizationRepository.findByApiKeyIdAndAuthorizedUserId(key.getId(), userId);
                
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
                vo.setKeySeries(key.getKeySeries());
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
        String apiKey = userConfigService.getValue(baseKey + "apiKey");
        config.setHasApiKey(StringUtils.isNotBlank(apiKey));
        
        // 获取全局代理配置和用户代理配置
        config.setGlobalProxyConfig(globalConfigService.getValue("model.proxy.config"));
        config.setUserProxyConfig(userConfigService.getValue("model.proxy.config"));
        
        // 获取温度值，默认0.7
        String tempStr = userConfigService.getValue(baseKey + "temperature");
        config.setTemperature(tempStr != null ? Double.parseDouble(tempStr) : 0.7);
        
        // 获取top_p值，默认1.0
        String topPStr = userConfigService.getValue(baseKey + "topP");
        config.setTopP(topPStr != null ? Double.parseDouble(topPStr) : 1.0);
        
        // 获取top_k值，默认40
        String topKStr = userConfigService.getValue(baseKey + "topK");
        config.setTopK(topKStr != null ? Integer.parseInt(topKStr) : 40);
        
        // 获取最大输出长度，默认800
        String maxOutputTokensStr = userConfigService.getValue(baseKey + "maxOutputTokens");
        config.setMaxOutputTokens(maxOutputTokensStr != null ? Integer.parseInt(maxOutputTokensStr) : 800);
        
        // 获取可用的API密钥列表 - 只返回对应系列的密钥
        config.setApiKeys(getAvailableApiKey(modelEnum.getSeries()));
        
        // 获取当前使用的API密钥ID
        ModelApiKeyConfigPo currentConfig = modelApiKeyConfigRepository.getByUserIdAnyModeCode(modelEnum.getCode(),userId);

        if (currentConfig != null) {
            config.setCurrentApiKeyId(currentConfig.getApiKey());
        }
        
        return config;
    }
    
    /**
     * 保存模型配置
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(SaveModelConfigDto dto) throws BizException {
        // 验证模型是否存在
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
        if (modelEnum == null) {
            throw new IllegalArgumentException("无效的模型代码");
        }
        
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();

        // 验证API密钥权限
        if (dto.getApiKeyId() != null) {

            ApiKeyPo apiKey = apiKeyRepository.findById(dto.getApiKeyId()).orElseThrow(() -> new BizException("API密钥不存在"));

            // 检查是否是自己的密钥
            if (!apiKey.getUser().getId().equals(userId)) {
                // 不是自己的密钥，检查是否有授权
                if (apiKeyAuthorizationRepository.countByAuthorized(userId, dto.getApiKeyId(), 1) == 0) {
                    throw new BizException("您没有使用此API密钥的权限");
                }
            }

            // 保存模型API密钥配置
            ModelApiKeyConfigPo config = modelApiKeyConfigRepository.getByUserIdAnyModeCode(modelEnum.getCode(),userId);

            if (config == null) {
                config = new ModelApiKeyConfigPo();
            }

            config.setUserId(userId);
            config.setModelCode(dto.getModel());
            config.setApiKey(dto.getApiKeyId());
            modelApiKeyConfigRepository.save(config);
        }
        
        // 构建配置键前缀
        String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
        
        // 保存其他配置
        userConfigService.setValue(baseKey + "temperature", String.valueOf(dto.getTemperature()));
        userConfigService.setValue(baseKey + "topP", String.valueOf(dto.getTopP()));
        userConfigService.setValue(baseKey + "topK", String.valueOf(dto.getTopK()));
        userConfigService.setValue(baseKey + "maxOutputTokens", String.valueOf(dto.getMaxOutputTokens()));

        // 保存用户代理配置
        userConfigService.setValue("model.proxy.config", null);

        if(StringUtils.isNotBlank(dto.getUserProxyConfig())){
            userConfigService.setValue("model.proxy.config", dto.getUserProxyConfig());
        }

        //保存全局代理配置
        if(AuthService.hasPermission("panel:model:edit:global:proxy")){

            globalConfigService.setValue("model.proxy.config", null);

            if(StringUtils.isNotBlank(dto.getGlobalProxyConfig())){
                globalConfigService.setValue("model.proxy.config", dto.getGlobalProxyConfig());
            }

        }
    }

    /**
     * 测试模型配置是否能够联通
     * @param modelCode 模型代码
     * @return 测试结果，成功返回模型响应内容，失败抛出异常
     * @throws BizException 业务异常
     */
    public String testModelConfig(String modelCode) throws BizException {
        // 验证模型是否存在
        AIModelEnum modelEnum = AIModelEnum.getByCode(modelCode);
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }
        
        // 获取当前用户ID
        Long userId = AuthService.getCurrentUserId();
        
        // 获取API密钥
        String apiKey = panelApiKeyService.getApiKey(modelEnum.getCode(), userId);
        if (StringUtils.isBlank(apiKey)) {
            throw new BizException("未配置API Key，请先选择API Key并保存配置");
        }
        
        // 构建配置键前缀
        String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";
        
        // 获取代理配置 - 首先检查用户级别的代理配置
        String proxyConfig = userConfigService.getValue("model.proxy.config");
        
        // 如果用户未配置代理，则使用全局代理配置
        if (StringUtils.isBlank(proxyConfig)) {
            proxyConfig = globalConfigService.getValue("model.proxy.config");
        }
        
        // 获取其他参数
        String tempStr = userConfigService.getValue(baseKey + "temperature");
        double temperature = tempStr != null ? Double.parseDouble(tempStr) : 0.7;
        
        String topPStr = userConfigService.getValue(baseKey + "topP");
        double topP = topPStr != null ? Double.parseDouble(topPStr) : 1.0;
        
        String topKStr = userConfigService.getValue(baseKey + "topK");
        int topK = topKStr != null ? Integer.parseInt(topKStr) : 40;
        
        String maxOutputTokensStr = userConfigService.getValue(baseKey + "maxOutputTokens");
        int maxOutputTokens = maxOutputTokensStr != null ? Integer.parseInt(maxOutputTokensStr) : 800;
        
        try {
            // 创建HTTP客户端
            OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 30);
            
            // 创建请求参数
            ModelChatParam modelChatParam = new ModelChatParam();
            modelChatParam.setModelCode(modelEnum.getCode());
            modelChatParam.setMessage("你好，这是一条测试消息，请简短回复。");
            modelChatParam.setTemperature(temperature);
            modelChatParam.setTopP(topP);
            modelChatParam.setTopK(topK);
            modelChatParam.setMaxOutputTokens(maxOutputTokens);
            modelChatParam.setHistories(new ArrayList<>());
            modelChatParam.setSystemPrompt("你是一个有用的AI助手。这是一条测试消息，请简短回复。");
            
            // 根据模型类型设置不同的URL并调用相应的服务
            if (modelEnum.getCode().contains("grok")) {
                modelChatParam.setUrl(GROK_BASE_URL);
                modelChatParam.setApiKey(apiKey);
                return modelGrokService.sendMessageSync(client, modelChatParam);
            } else if (modelEnum.getCode().contains("gemini")) {
                modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":generateContent");
                modelChatParam.setApiKey(apiKey);
                return modelGeminiService.sendMessageSync(client, modelChatParam);
            } else {
                throw new BizException("不支持的模型类型");
            }
        } catch (Exception e) {
            throw new BizException("测试失败: " + e.getMessage());
        }
    }
} 