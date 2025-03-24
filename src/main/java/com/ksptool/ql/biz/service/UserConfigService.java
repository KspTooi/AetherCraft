package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ConfigRepository;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.po.ConfigPo;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserConfigService {

    private final ConfigRepository rep;

    public UserConfigService(ConfigRepository configRepository) {
        this.rep = configRepository;
    }

    public String getValue(String key, Long userId) {
        ConfigPo config = rep.findByUserIdAndConfigKey(userId, key);

        if(config == null){
            return null;
        }

        return config.getConfigValue();
    }

    @Transactional
    public void remove(String key){
        rep.removeByConfigKeyAndUserId(key,AuthService.getCurrentUserId());
    }

    public void setValue(String key, String value, Long userId) {
        ConfigPo config = rep.findByUserIdAndConfigKey(userId, key);
        if (config == null) {
            config = new ConfigPo();
            config.setConfigKey(key);
            config.setUserId(userId);
            config.setCreateTime(new Date());
        }
        config.setConfigValue(value);
        config.setUpdateTime(new Date());
        rep.save(config);
    }

    public String getValue(String key) {
        return getValue(key, AuthService.getCurrentUserId());
    }

    public void setValue(String key, String value) {
        setValue(key, value, AuthService.getCurrentUserId());
    }

    public void setValue(String key, Long value) {
        setValue(key, value+"", AuthService.getCurrentUserId());
    }

    /**
     * 获取当前用户空间下的配置值
     */
    public String get(String key) {
        return getValue(key, AuthService.getCurrentUserId());
    }

    /**
     * 获取当前用户空间下的配置值，如果不存在返回默认值
     */
    public String get(String key, String defaultValue) {
        String value = get(key);
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }

    /**
     * 获取整数配置值
     */
    public int getInt(String key, int defaultValue) {
        return getInt(key, defaultValue, AuthService.getCurrentUserId());
    }

    /**
     * 获取长整数配置值
     */
    public long getLong(String key, long defaultValue) {
        return getLong(key, defaultValue, AuthService.getCurrentUserId());
    }

    /**
     * 获取双精度浮点数配置值
     */
    public double getDouble(String key, double defaultValue) {
        return getDouble(key, defaultValue, AuthService.getCurrentUserId());
    }

    /**
     * 获取布尔配置值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key, defaultValue, AuthService.getCurrentUserId());
    }

    /**
     * 获取指定用户空间下的配置值
     */
    public String get(String key, Long userId) {
        return getValue(key, userId);
    }

    /**
     * 获取指定用户空间下的配置值，如果不存在返回默认值
     */
    public String get(String key, String defaultValue, Long userId) {
        String value = get(key, userId);
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }

    /**
     * 获取整数配置值
     */
    public int getInt(String key, int defaultValue, Long userId) {
        String value = get(key, userId);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取长整数配置值
     */
    public long getLong(String key, long defaultValue, Long userId) {
        String value = get(key, userId);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取双精度浮点数配置值
     */
    public double getDouble(String key, double defaultValue, Long userId) {
        String value = get(key, userId);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取布尔配置值
     */
    public boolean getBoolean(String key, boolean defaultValue, Long userId) {
        String value = get(key, userId);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }



    public void readUserModelParam(ModelChatParam param,Long inputUid){

        var uid = -1L;

        if(inputUid != null){
            uid = inputUid;
        }
        if(inputUid == null){
            var authUid = AuthService.getCurrentUserId();
            if(authUid!=null){
                uid = authUid;
            }
        }
        if(uid == -1){
            log.warn("生成标题失败，用户ID无效!");
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
        double temperature = getDouble(temperatureK.execute(), 0.7,uid);
        double topP = getDouble(topPK.execute(), 1.0,uid);
        int topK = getInt(topKK.execute(), 40,uid);
        int maxOutputTokens = getInt(maxOutputTokensK.execute(), 800,uid);
        
        // 设置参数到ModelChatParam对象
        param.setTemperature(temperature);
        param.setTopP(topP);
        param.setTopK(topK);
        param.setMaxOutputTokens(maxOutputTokens);
    }

} 