package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.mapper.ApiKeyAuthorizationRepository;
import com.ksptool.ql.biz.mapper.ModelApiKeyConfigRepository;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.po.ApiKeyAuthorizationPo;
import com.ksptool.ql.biz.model.po.ModelApiKeyConfigPo;
import com.ksptool.ql.biz.model.vo.*;
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

import static com.ksptool.entities.Entities.as;

@Service
public class ApiKeyService {

    @Autowired
    private ApiKeyRepository repository;

    @Autowired
    private ApiKeyAuthorizationRepository authRepository;

    @Autowired
    private ModelApiKeyConfigRepository modelApiKeyConfigRepository;

    @Autowired
    private ApiKeyAuthorizationRepository apiKeyAuthorizationRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;



    /**
     * 根据模型代码和用户ID获取API密钥
     * @param modelCode 模型代码
     * @param userId 用户ID
     * @return API密钥字符串
     * @throws BizException 当无权限或配置不存在时
     */
    @Transactional(rollbackFor = Exception.class)
    public String getApiKey(String modelCode, Long userId) throws BizException {

        ModelApiKeyConfigPo config = modelApiKeyConfigRepository.getByUserIdAnyModeCode(modelCode, userId);

        if (config == null) {

            //查询该用户下是否拥有任意一个可用的密钥
            List<AvailableApiKeyVo> availableApiKey = getCurrentUserAvailableApiKey(AIModelEnum.getByCode(modelCode).getSeries());

            if(availableApiKey.isEmpty()){
                throw new BizException("未找到模型API密钥配置");
            }

            //自动为用户绑定第一个可用的密钥
            config = new ModelApiKeyConfigPo();
            config.setUserId(userId);
            config.setModelCode(modelCode);
            config.setApiKeyId(availableApiKey.getFirst().getApiKeyId());
            modelApiKeyConfigRepository.save(config);
        }

        ApiKeyPo apiKey = repository.findById(config.getApiKeyId())
            .orElseThrow(() -> new BizException("API密钥不存在"));

        if (apiKey.getUser().getId().equals(userId)) {
            // 更新使用次数和最后使用时间
            apiKey.setUsageCount(apiKey.getUsageCount() + 1);
            apiKey.setLastUsedTime(new Date());
            repository.save(apiKey);
            return apiKey.getKeyValue();
        }

        if (authRepository.countByAuthorized(userId, apiKey.getId(), 1) > 0) {
            // 更新使用次数和最后使用时间
            apiKey.setUsageCount(apiKey.getUsageCount() + 1);
            apiKey.setLastUsedTime(new Date());
            repository.save(apiKey);

            // 更新授权记录的使用次数
            ApiKeyAuthorizationPo auth = authRepository.findByApiKeyIdAndAuthorizedUserId(apiKey.getId(), userId);
            if (auth != null) {
                auth.setUsageCount(auth.getUsageCount() + 1);
                authRepository.save(auth);
            }
            return apiKey.getKeyValue();
        }

        throw new BizException("无权使用此API密钥");
    }


    /**
     * 获取当前用户特定模型系列可用的API密钥列表
     * @param series 模型系列，如 Gemini、Grok 等
     * @return 匹配系列的 API 密钥列表
     */
    public List<AvailableApiKeyVo> getCurrentUserAvailableApiKey(String series) {
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

} 