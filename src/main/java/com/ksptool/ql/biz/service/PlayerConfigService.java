package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ConfigRepository;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.exception.AuthException;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import com.ksptool.ql.biz.model.po.ConfigPo;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Service
public class PlayerConfigService {

    @Autowired
    private ConfigRepository repository;

    @Transactional
    public void remove(String key){

        Long playerId = AuthService.getCurrentPlayerId();

        if(playerId == null){
            log.error("remove config value failed! Player ID is null CurrentUserId:{}",AuthService.getCurrentUserId());
            return;
        }

        var query = new ConfigPo();
        query.setConfigKey(key);
        query.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));

        repository.findOne(Example.of(query))
                .ifPresent(repository::delete);
        repository.flush();
    }

    private void putValue(String key, String value){
        Long playerId = AuthService.getCurrentPlayerId();

        if(playerId == null){
            log.error("update config value failed! Player ID is null CurrentUserId:{}",AuthService.getCurrentUserId());
            return;
        }

        var query = new ConfigPo();
        query.setConfigKey(key);
        query.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));

        repository.findOne(Example.of(query)).ifPresentOrElse((po)->{
            po.setConfigValue(value);
            po.setUpdateTime(new Date());
            repository.save(po);
        },()->{

            var create = new ConfigPo();
            create.setPlayer(query.getPlayer());
            create.setConfigKey(key);
            create.setConfigValue(value);
            create.setDescription(null);
            create.setCreateTime(new Date());
            create.setUpdateTime(new Date());
            repository.save(create);
        });

        repository.flush();
    }

    @Transactional
    public void put(String key, String value){
        putValue(key, value);
    }
    @Transactional
    public void put(String key, Integer value){
        putValue(key, String.valueOf(value));
    }
    @Transactional
    public void put(String key, Long value){
        putValue(key, String.valueOf(value));
    }
    @Transactional
    public void put(String key, Double value){
        putValue(key, String.valueOf(value));
    }
    @Transactional
    public void put(String key, Float value){
        putValue(key, String.valueOf(value));
    }
    @Transactional
    public void put(String key, Boolean value){
        putValue(key, String.valueOf(value));
    }

    private String getValue(String key, Long playerId) {
        if (playerId == null) {
            log.warn("getValue with playerId failed! Player ID is null. Key: {}", key);
            return null;
        }
        var query = new ConfigPo();
        query.setConfigKey(key);
        query.setPlayer(Any.of().val("id", playerId).as(PlayerPo.class));

        ConfigPo config = repository.findOne(Example.of(query)).orElse(null);

        if (config == null) {
            return null;
        }
        return config.getConfigValue();
    }

    private String getValue(String key) {
        Long playerId = AuthService.getCurrentPlayerId();
        if (playerId == null) {
            log.warn("getValue failed! Player ID is null. Key: {}, CurrentUserId:{}", key, AuthService.getCurrentUserId());
            return null;
        }

        var query = new ConfigPo();
        query.setConfigKey(key);
        query.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));

        ConfigPo config = repository.findOne(Example.of(query)).orElse(null);

        if (config == null) {
            return null;
        }

        return config.getConfigValue();
    }

    public String getString(String key,String orElse){
        String value = getValue(key);
        if(StringUtils.isBlank(value)){
            return orElse;
        }
        return value;
    }

    public String getString(String key, String orElse, Long playerId) {
        String value = getValue(key, playerId);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        return value;
    }

    public Integer getInt(String key,Integer orElse){
        String value = getValue(key);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("getInt NumberFormatException for key: {}, value: {}. Returning orElse.", key, value);
            return orElse;
        }
    }

    public Integer getInt(String key, Integer orElse, Long playerId) {
        String value = getValue(key, playerId);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("getInt NumberFormatException for key: {}, value: {}. Returning orElse.", key, value);
            return orElse;
        }
    }

    public Long getLong(String key,Long orElse){
        String value = getValue(key);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("getLong NumberFormatException for key: {}, value: {}. Returning orElse.", key, value);
            return orElse;
        }
    }

    public Long getLong(String key, Long orElse, Long playerId) {
        String value = getValue(key, playerId);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("getLong NumberFormatException for key: {}, value: {}. Returning orElse.", key, value);
            return orElse;
        }
    }

    public Double getDouble(String key,Double orElse){
        String value = getValue(key);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("getDouble NumberFormatException for key: {}, value: {}. Returning orElse.", key, value);
            return orElse;
        }
    }

    public Double getDouble(String key, Double orElse, Long playerId) {
        String value = getValue(key, playerId);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("getDouble NumberFormatException for key: {}, value: {}. Returning orElse.", key, value);
            return orElse;
        }
    }

    public Float getFloat(String key,Float orElse){
        String value = getValue(key);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            log.warn("getFloat NumberFormatException for key: {}, value: {}. Returning orElse.", key, value);
            return orElse;
        }
    }

    public Float getFloat(String key, Float orElse, Long playerId) {
        String value = getValue(key, playerId);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            log.warn("getFloat NumberFormatException for key: {}, value: {}. Returning orElse.", key, value);
            return orElse;
        }
    }

    public Boolean getBoolean(String key,Boolean orElse){
        String value = getValue(key);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        return Boolean.parseBoolean(value);
    }

    public Boolean getBoolean(String key, Boolean orElse, Long playerId) {
        String value = getValue(key, playerId);
        if (StringUtils.isBlank(value)) {
            return orElse;
        }
        return Boolean.parseBoolean(value);
    }

    public void readSelfModelParam(ModelChatParam param){
        readPlayerModelParam(param,null);
    }

    public void readPlayerModelParam(ModelChatParam param, Long inputPlayerId){

        var playerId = -1L;

        if(inputPlayerId != null){
            playerId = inputPlayerId;
        }
        if(inputPlayerId == null){
            var authPlayerId = AuthService.getCurrentPlayerId();
            if(authPlayerId!=null){
                playerId = authPlayerId;
            }
        }

        PreparedPrompt temperatureK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_TEMPERATURE.key())
                .setParameter("modelCode", param.getModelCode());

        PreparedPrompt topPK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_TOP_P.key())
                .setParameter("modelCode", param.getModelCode());

        PreparedPrompt topKK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_TOP_K.key())
                .setParameter("modelCode", param.getModelCode());

        PreparedPrompt maxOutputTokensK = PreparedPrompt.prepare(UserConfigEnum.AI_MODEL_MAX_OUTPUT_TOKENS.key())
                .setParameter("modelCode", param.getModelCode());

        // 获取配置参数
        double temperature = getDouble(temperatureK.execute(), 0.7,playerId);
        double topP = getDouble(topPK.execute(), 1.0,playerId);
        int topK = getInt(topKK.execute(), 40,playerId);
        int maxOutputTokens = getInt(maxOutputTokensK.execute(), 4096,playerId);

        // 设置参数到ModelChatParam对象
        param.setTemperature(temperature);
        param.setTopP(topP);
        param.setTopK(topK);
        param.setMaxOutputTokens(maxOutputTokens);
    }

    /**
     * 获取玩家自身或全局的代理配置
     * @return 代理url
     */
    public String getSelfProxyUrl() throws AuthException {
        // 获取代理配置 - 首先检查用户级别的代理配置
        String proxyConfig = this.getString("model.proxy.config", null,AuthService.requirePlayerId());

        // 如果用户未配置代理，则使用全局代理配置
        if (StringUtils.isBlank(proxyConfig)) {
            ConfigPo config = repository.getGlobalConfig("model.proxy.config");
            if (config != null) {
                proxyConfig = config.getConfigValue();
            }
        }
        return proxyConfig;
    }

}
