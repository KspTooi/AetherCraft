package com.ksptool.ql.restcgi.service;

import com.ksptool.ql.biz.service.ModelVariantService;
import com.ksptool.ql.biz.service.PlayerConfigService;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.exception.AuthException;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.exception.ModelSeriesNotExistsException;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.function.Consumer;


@Primary
@Service
@Slf4j
public class AutoSelectorModelCgi implements ModelRestCgi {

    @Autowired
    private ClaudeRestCgi claudeRestCgi;
    
    @Autowired
    private MistralRestCgi mistralRestCgi;
    
    @Autowired
    private ChatGptRestCgi chatGptRestCgi;
    
    @Autowired
    private DeepseekRestCgi deepseekRestCgi;
    
    @Autowired
    private GrokRestCgi grokRestCgi;
    
    @Autowired
    private GeminiRestCgi geminiRestCgi;

    @Autowired
    private PlayerConfigService playerConfigService;

    @Autowired
    private ModelVariantService variantService;


    @Override
    public CgiChatResult sendMessage(CgiChatParam param) throws BizException {
        ModelRestCgi selectedCgi = selectModelCgi(param);
        selectHttpClient(param);
        selectModelSettings(param);
        ensureCgiParamValid(param);
        return selectedCgi.sendMessage(param);
    }

    @Override
    public void sendMessage(CgiChatParam param, Consumer<CgiChatResult> callback) throws BizException {
        ModelRestCgi selectedCgi = selectModelCgi(param);
        selectHttpClient(param);
        selectModelSettings(param);
        ensureCgiParamValid(param);
        selectedCgi.sendMessage(param, callback);
    }

    private void selectModelSettings(CgiChatParam param){

        var modelCode = param.getModel().getCode();

        //填充可选参数
        if(param.getVariantParam() == null){
            try {
                param.setVariantParam(variantService.getVariantParam(param.getModel().getId()));
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                log.error("获取模型参数时出现授权异常");
            }
        }

        // 只在参数为-1时自动获取配置
        if (param.getTemperature() == -1) {
            PreparedPrompt temperatureK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_TEMPERATURE.key())
                    .setParameter("modelCode", modelCode);
            param.setTemperature(playerConfigService.getDouble(temperatureK.execute(), 0.7));
        }

        if (param.getTopP() == -1) {
            PreparedPrompt topPK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_TOP_P.key())
                    .setParameter("modelCode", modelCode);
            param.setTopP(playerConfigService.getDouble(topPK.execute(), 1.0));
        }

        if (param.getTopK() == -1) {
            PreparedPrompt topKK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_TOP_K.key())
                    .setParameter("modelCode", modelCode);
            param.setTopK(playerConfigService.getInt(topKK.execute(), 40));
        }

        if (param.getMaxOutputTokens() == -1) {
            PreparedPrompt maxOutputTokensK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_MAX_OUTPUT_TOKENS.key())
                    .setParameter("modelCode", modelCode);
            param.setMaxOutputTokens(playerConfigService.getInt(maxOutputTokensK.execute(), 4096));
        }
    }

    private void selectHttpClient(CgiChatParam param) throws BizException {
        if (param == null || param.getHttpClient() != null) {
            return;
        }
        param.setHttpClient(HttpClientUtils.createHttpClient(playerConfigService.getSelfProxyUrl(), 60));
    }

    private ModelRestCgi selectModelCgi(CgiChatParam param) throws ModelSeriesNotExistsException {
        if (param == null || param.getModel() == null) {
            throw new ModelSeriesNotExistsException("选择模型CGI失败,模型Series为空");
        }

        String series = param.getModel().getSeries();

        if ("Claude".equals(series)) {
            return claudeRestCgi;
        }

        if ("Mistral".equals(series)) {
            return mistralRestCgi;
        }

        if ("ChatGPT".equals(series)) {
            return chatGptRestCgi;
        }

        if ("DeepSeek".equals(series)) {
            return deepseekRestCgi;
        }

        if ("Grok".equals(series)) {
            return grokRestCgi;
        }

        if ("Gemini".equals(series)) {
            return geminiRestCgi;
        }

        throw new ModelSeriesNotExistsException("选择模型CGI失败,模型Series不支持.");
    }

    public void ensureCgiParamValid(CgiChatParam p) throws BizException {

        if (p.getModel() == null) {
            throw new BizException("[CGI参数错误] 模型枚举不可为空");
        }

        if (p.getMessage() == null) {
            throw new BizException("[CGI参数错误] 消息内容不可为空");
        }

        if (p.getApikey() == null || p.getApikey().trim().isEmpty()) {
            throw new BizException("[CGI参数错误] ApiKey不可为空");
        }

        if (p.getHistoryMessages() == null) {
            throw new BizException("[CGI参数错误] 历史消息不允许为null");
        }
    }
}
