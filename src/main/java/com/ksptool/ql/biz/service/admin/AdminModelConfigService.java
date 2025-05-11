package com.ksptool.ql.biz.service.admin;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ApiKeyAuthorizationRepository;
import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.mapper.ModelApiKeyConfigRepository;
import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.po.ModelApiKeyConfigPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.vo.GetAdminModelConfigVo;
import com.ksptool.ql.biz.service.*;
import com.ksptool.ql.biz.service.ApiKeyService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static com.ksptool.ql.commons.enums.PermissionEnum.PANEL_MODEL_EDIT_GLOBAL_PROXY;
import static com.ksptool.ql.commons.enums.PermissionEnum.PANEL_MODEL_EDIT_USER_PROXY;

@Service
public class AdminModelConfigService {

    @Autowired
    private PlayerConfigService playerConfigService;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private ModelApiKeyConfigRepository modelApiKeyConfigRepository;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ApiKeyAuthorizationRepository apiKeyAuthorizationRepository;

    @Autowired
    private ModelGeminiService modelGeminiService;

    @Autowired
    private ModelGrokService modelGrokService;

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GROK_BASE_URL = "https://api.x.ai/v1/chat/completions";

    public GetAdminModelConfigVo getModelConfig(GetModelConfigDto dto) throws BizException {

        AIModelEnum byCode = AIModelEnum.getByCode(dto.getModelCode());

        if(byCode == null){
            throw new BizException("无效的模型代码:"+dto.getModelCode());
        }

        GetAdminModelConfigVo vo = new GetAdminModelConfigVo();
        vo.setModelCode(byCode.getCode());
        vo.setModelName(byCode.getName());

        // 从配置服务加载配置
        String baseKey = "ai.model.cfg." + byCode.getCode() + ".";

        // 获取全局代理配置和用户代理配置
        vo.setGlobalProxyConfig(globalConfigService.getValue("model.proxy.config"));
        vo.setUserProxyConfig(playerConfigService.getString("model.proxy.config",null));

        // 获取温度值，默认0.7
        vo.setTemperature(playerConfigService.getDouble(baseKey + "temperature",0.7D));

        // 获取top_p值，默认1.0
        vo.setTopP(playerConfigService.getDouble(baseKey + "topP",1.0D));

        // 获取top_k值，默认40
        vo.setTopK(playerConfigService.getInt(baseKey, 40));

        // 获取最大输出长度，默认4096
        vo.setMaxOutputTokens(playerConfigService.getInt(baseKey, 4096));

        // 获取可用的API密钥列表 - 只返回对应系列的密钥
        vo.setApiKeys(apiKeyService.getCurrentPlayerAvailableApiKey(byCode.getSeries()));

        // 获取当前使用的API密钥ID
        ModelApiKeyConfigPo currentConfig = modelApiKeyConfigRepository.getByPlayerIdAnyModeCode(byCode.getCode(),AuthService.getCurrentPlayerId());

        if (currentConfig != null) {
            vo.setCurrentApiKeyId(currentConfig.getApiKeyId());
        }

        return vo;
    }

    @Transactional
    public void saveModelConfig(SaveAdminModelConfigDto dto) throws BizException{

        // 验证模型是否存在
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModelCode());
        if (modelEnum == null) {
            throw new IllegalArgumentException("无效的模型代码");
        }

        // 获取当前人物ID
        Long playerId = AuthService.getCurrentPlayerId();

        // 验证API密钥权限
        if (dto.getApiKeyId() != null) {

            ApiKeyPo apiKey = apiKeyRepository.findById(dto.getApiKeyId()).orElseThrow(() -> new BizException("API密钥不存在"));

            // 检查是否是自己的密钥
            if (!apiKey.getPlayer().getId().equals(playerId)) {
                // 不是自己的密钥，检查是否有授权
                if (apiKeyAuthorizationRepository.countByAuthorized(playerId, dto.getApiKeyId(), 1) == 0) {
                    throw new BizException("您没有使用此API密钥的权限");
                }
            }

            // 保存模型API密钥配置
            ModelApiKeyConfigPo config = modelApiKeyConfigRepository.getByPlayerIdAnyModeCode(modelEnum.getCode(),playerId);

            if (config == null) {
                config = new ModelApiKeyConfigPo();
            }

            config.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));
            config.setModelCode(dto.getModelCode());
            config.setApiKeyId(dto.getApiKeyId());
            modelApiKeyConfigRepository.save(config);
        }

        // 构建配置键前缀
        String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";

        // 保存其他配置
        playerConfigService.put(baseKey + "temperature", dto.getTemperature());
        playerConfigService.put(baseKey + "topP", dto.getTopP());
        playerConfigService.put(baseKey + "topK", dto.getTopK());
        playerConfigService.put(baseKey + "maxOutputTokens", dto.getMaxOutputTokens());

        // 保存用户代理配置
        if(AuthService.hasPermission(PANEL_MODEL_EDIT_USER_PROXY.getCode())){
            //playerConfigService.put("model.proxy.config", (String) null);
            if(StringUtils.isNotBlank(dto.getUserProxyConfig())){
                playerConfigService.put("model.proxy.config", dto.getUserProxyConfig());
            }
        }

        //保存全局代理配置
        if(AuthService.hasPermission(PANEL_MODEL_EDIT_GLOBAL_PROXY.getCode())){
            globalConfigService.setValue("model.proxy.config", null);
            if(StringUtils.isNotBlank(dto.getGlobalProxyConfig())){
                globalConfigService.setValue("model.proxy.config", dto.getGlobalProxyConfig());
            }
        }

    }

    public String testModelConnection(TestModelConnectionDto dto) throws BizException{
        // 验证模型是否存在
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModelCode());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        // 获取当前玩家ID
        Long playerId = AuthService.getCurrentPlayerId();

        // 获取API密钥
        String apiKey = apiKeyService.getApiKey(modelEnum.getCode(), playerId);
        if (StringUtils.isBlank(apiKey)) {
            throw new BizException("未配置API Key，请先选择API Key并保存配置");
        }

        // 构建配置键前缀
        String baseKey = "ai.model.cfg." + modelEnum.getCode() + ".";

        // 获取代理配置 - 首先检查用户级别的代理配置
        String proxyConfig = playerConfigService.getString("model.proxy.config", null);

        // 如果用户未配置代理，则使用全局代理配置
        if (StringUtils.isBlank(proxyConfig)) {
            proxyConfig = globalConfigService.get("model.proxy.config", null);
        }

        // 获取其他参数
        double temperature = playerConfigService.getDouble(baseKey + "temperature", 0.7);

        double topP = playerConfigService.getDouble(baseKey + "topP", 1.0);
        int topK = playerConfigService.getInt(baseKey + "topK", 40);
        int maxOutputTokens = playerConfigService.getInt(baseKey + "maxOutputTokens", 4096);

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
