package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ConfigRepository;
import com.ksptool.ql.biz.model.po.ConfigPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ConfigService {
    
    private final ConfigRepository configRepository;

    public String getValue(String key, Long userId) {
        ConfigPo config = configRepository.findByUserIdAndConfigKey(userId, key);

        if(config == null){
            return null;
        }

        return config.getConfigValue();
    }

    public void setValue(String key, String value, Long userId) {
        ConfigPo config = configRepository.findByUserIdAndConfigKey(userId, key);
        if (config == null) {
            config = new ConfigPo();
            config.setConfigKey(key);
            config.setUserId(userId);
            config.setCreateTime(new Date());
        }
        config.setConfigValue(value);
        config.setUpdateTime(new Date());
        configRepository.save(config);
    }

    public String getValue(String key) {
        return getValue(key, AuthService.getCurrentUserId());
    }

    public void setValue(String key, String value) {
        setValue(key, value, AuthService.getCurrentUserId());
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
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    /**
     * 获取整数配置值
     */
    public int getInt(String key, int defaultValue, Long userId) {
        String value = get(key, userId);
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
    public long getLong(String key, long defaultValue, Long userId) {
        String value = get(key, userId);
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
    public double getDouble(String key, double defaultValue, Long userId) {
        String value = get(key, userId);
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
    public boolean getBoolean(String key, boolean defaultValue, Long userId) {
        String value = get(key, userId);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
} 