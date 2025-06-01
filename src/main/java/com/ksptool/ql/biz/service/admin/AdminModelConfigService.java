package com.ksptool.ql.biz.service.admin;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ApiKeyAuthorizationRepository;
import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.mapper.ModelApiKeyConfigRepository;
import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.po.ModelApiKeyConfigPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.schema.ModelVariantSchema;
import com.ksptool.ql.biz.model.vo.GetAdminModelConfigVo;
import com.ksptool.ql.biz.service.*;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.restcgi.model.CgiChatMessage;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import com.ksptool.ql.restcgi.service.ModelRestCgi;
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
    private ModelVariantService modelVariantService;

    @Autowired
    private ModelRestCgi restCgi;

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GROK_BASE_URL = "https://api.x.ai/v1/chat/completions";

    public GetAdminModelConfigVo getModelConfig(GetModelConfigDto dto) throws BizException {

        ModelVariantSchema byCode = modelVariantService.getModelSchema(dto.getModelCode());

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
        vo.setTopK(playerConfigService.getInt(baseKey + "topK", 40));

        // 获取最大输出长度，默认4096
        vo.setMaxOutputTokens(playerConfigService.getInt(baseKey + "maxOutputTokens", 4096));

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
        ModelVariantSchema model = modelVariantService.requireModelSchema(dto.getModelCode());

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
            ModelApiKeyConfigPo config = modelApiKeyConfigRepository.getByPlayerIdAnyModeCode(model.getCode(),playerId);

            if (config == null) {
                config = new ModelApiKeyConfigPo();
            }

            config.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));
            config.setModelCode(dto.getModelCode());
            config.setApiKeyId(dto.getApiKeyId());
            modelApiKeyConfigRepository.save(config);
        }

        // 构建配置键前缀
        String baseKey = "ai.model.cfg." + model.getCode() + ".";

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
        ModelVariantSchema model = modelVariantService.requireModelSchema(dto.getModelCode());

        // 获取当前玩家ID
        Long playerId = AuthService.getCurrentPlayerId();

        // 获取API密钥
        String apiKey = apiKeyService.getApiKey(model.getCode(), playerId);
        if (StringUtils.isBlank(apiKey)) {
            throw new BizException("未配置API Key，请先选择API Key并保存配置");
        }

        try {

            var p = new CgiChatParam();
            p.setModel(model);
            p.setMessage(new CgiChatMessage("你好,这是一条测试消息,请简短回复."));
            p.setHistoryMessages(new ArrayList<>());
            p.setApikey(apiKey);
            p.setSystemPrompt("你是一个有用的AI助手,在该测试场景下,请使用一个文字回复.");

            CgiChatResult r = restCgi.sendMessage(p);
            return r.getContent();
        } catch (Exception e) {
            throw new BizException("测试失败: " + e.getMessage());
        }
    }

}
