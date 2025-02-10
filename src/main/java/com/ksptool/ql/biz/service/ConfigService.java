package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ConfigRepository;
import com.ksptool.ql.biz.model.po.ConfigPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ConfigService {
    
    private final ConfigRepository configRepository;
    
    public void saveConfig(ConfigPo config) {
        config.setCreateTime(new Date());
        config.setUpdateTime(new Date());
        configRepository.save(config);
    }
    
    public ConfigPo getConfigByKey(String configKey) {
        return configRepository.findByConfigKey(configKey);
    }
    
    public String getConfigValue(String key) {
        ConfigPo config = configRepository.findByConfigKey(key);
        return config != null ? config.getConfigValue() : null;
    }
    
    public void setConfigValue(String key, String value) {
        ConfigPo config = configRepository.findByConfigKey(key);
        if (config == null) {
            config = new ConfigPo();
            config.setConfigKey(key);
            config.setCreateTime(new Date());
        }
        config.setConfigValue(value);
        config.setUpdateTime(new Date());
        configRepository.save(config);
    }
    
    public void updateConfig(ConfigPo config) {
        ConfigPo existingConfig = configRepository.findByConfigKey(config.getConfigKey());
        if (existingConfig != null) {
            existingConfig.setConfigValue(config.getConfigValue());
            existingConfig.setDescription(config.getDescription());
            existingConfig.setUpdateTime(new Date());
            configRepository.save(existingConfig);
        }
    }
    
    public void deleteConfig(String configKey) {
        ConfigPo config = configRepository.findByConfigKey(configKey);
        if (config != null) {
            configRepository.delete(config);
        }
    }
    
    public boolean existsConfigKey(String configKey) {
        return configRepository.existsByConfigKey(configKey);
    }
} 