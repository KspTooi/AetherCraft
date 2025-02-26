package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ConfigRepository;
import com.ksptool.ql.biz.model.po.ConfigPo;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalConfigService {
    
    private final ConfigRepository configRepository;
    private static final Long GLOBAL_USER_ID = -1L;

    public String getValue(String key) {
        ConfigPo config = configRepository.findByUserIdAndConfigKey(GLOBAL_USER_ID, key);
        if(config == null){
            return null;
        }
        return config.getConfigValue();
    }

    public void setValue(String key, String value) {
        ConfigPo config = configRepository.findByUserIdAndConfigKey(GLOBAL_USER_ID, key);
        if (config == null) {
            config = new ConfigPo();
            config.setConfigKey(key);
            config.setUserId(GLOBAL_USER_ID);
            config.setCreateTime(new Date());
        }
        config.setConfigValue(value);
        config.setUpdateTime(new Date());
        configRepository.save(config);
    }

    /**
     * 获取全局配置值
     */
    public String get(String key) {
        return getValue(key);
    }

    /**
     * 获取全局配置值，如果不存在返回默认值
     */
    public String get(String key, String defaultValue) {
        String value = get(key);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    /**
     * 获取整数配置值
     */
    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (!StringUtils.hasText(value)) {
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
    public long getLong(String key, long defaultValue) {
        String value = get(key);
        if (!StringUtils.hasText(value)) {
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
    public double getDouble(String key, double defaultValue) {
        String value = get(key);
        if (!StringUtils.hasText(value)) {
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
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * 校验系统全局配置项
     * 检查数据库中是否存在所有系统内置的全局配置项，如果不存在则自动创建
     */
    @Transactional
    public String validateSystemConfigs() {
        List<ConfigPo> addedConfigs = new ArrayList<>();
        int existCount = 0;

        for (GlobalConfigEnum config : GlobalConfigEnum.values()) {
            ConfigPo existingConfig = configRepository.findByUserIdAndConfigKey(GLOBAL_USER_ID, config.getKey());
            
            if (existingConfig == null) {
                ConfigPo newConfig = new ConfigPo();
                newConfig.setUserId(GLOBAL_USER_ID);
                newConfig.setConfigKey(config.getKey());
                newConfig.setConfigValue(config.getDefaultValue());
                newConfig.setDescription(config.getDescription());
                newConfig.setCreateTime(new Date());
                newConfig.setUpdateTime(new Date());
                addedConfigs.add(newConfig);
            } else {
                existCount++;
            }
        }

        if (!addedConfigs.isEmpty()) {
            configRepository.saveAll(addedConfigs);
            return String.format("校验完成，已添加 %d 个缺失的配置项，已存在 %d 个配置项", 
                addedConfigs.size(), existCount);
        }

        return String.format("校验完成，所有 %d 个系统配置项均已存在", existCount);
    }
} 