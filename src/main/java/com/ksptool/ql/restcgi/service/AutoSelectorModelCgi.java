package com.ksptool.ql.restcgi.service;

import com.ksptool.ql.biz.service.PlayerConfigService;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.exception.ModelSeriesNotExistsException;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.function.Consumer;

@Primary
@Service
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

    @Override
    public CgiChatResult sendMessage(CgiChatParam param) throws BizException {
        ModelRestCgi selectedCgi = selectModelCgi(param);
        selectHttpClient(param);
        return selectedCgi.sendMessage(param);
    }

    @Override
    public void sendMessage(CgiChatParam param, Consumer<CgiChatResult> callback) throws BizException {
        ModelRestCgi selectedCgi = selectModelCgi(param);
        selectHttpClient(param);
        selectedCgi.sendMessage(param, callback);
    }

    private void selectModelSettings(CgiChatParam param) throws BizException{

        var modelCode = param.getModel().getCode();

        PreparedPrompt temperatureK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_TEMPERATURE.key())
                .setParameter("modelCode", modelCode);

        PreparedPrompt topPK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_TOP_P.key())
                .setParameter("modelCode", modelCode);

        PreparedPrompt topKK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_TOP_K.key())
                .setParameter("modelCode", modelCode);

        PreparedPrompt maxOutputTokensK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_MAX_OUTPUT_TOKENS.key())
                .setParameter("modelCode", modelCode);

        // 获取配置参数
        double temperature = playerConfigService.getDouble(temperatureK.execute(), 0.7);
        double topP = playerConfigService.getDouble(topPK.execute(), 1.0);
        int topK = playerConfigService.getInt(topKK.execute(), 40);
        int maxOutputTokens = playerConfigService.getInt(maxOutputTokensK.execute(), 4096);
        param.setTemperature(temperature);
        param.setTopP(topP);
        param.setTopK(topK);
        param.setMaxOutputTokens(maxOutputTokens);
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
}
