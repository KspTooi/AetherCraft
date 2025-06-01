package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.mapper.ApiKeyAuthorizationRepository;
import com.ksptool.ql.biz.mapper.ModelApiKeyConfigRepository;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.vo.*;
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

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

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

    @Autowired
    private ModelVariantService modelVariantService;


    @Transactional
    public String getSelfApiKey(String modelCode) throws BizException {
        return this.getApiKey(modelCode,AuthService.requirePlayerId());
    }

    /**
     * 根据模型代码和人物ID获取API密钥
     * @param modelCode 模型代码
     * @param playerId 用户ID
     * @return API密钥字符串
     * @throws BizException 当无权限或配置不存在时
     */
    @Transactional(propagation = REQUIRES_NEW,rollbackFor = BizException.class)
    public String getApiKey(String modelCode, Long playerId) throws BizException {

        ModelApiKeyConfigPo config = modelApiKeyConfigRepository.getByPlayerIdAnyModeCode(modelCode, playerId);

        if (config == null) {

            //查询该人物下是否拥有任意一个可用的密钥
            List<AvailableApiKeyVo> availableApiKey = getCurrentPlayerAvailableApiKey(modelVariantService.requireModelSchema(modelCode).getSeries());

            if(availableApiKey.isEmpty()){
                throw new BizException("未找到模型API密钥配置");
            }

            //自动为用户绑定第一个可用的密钥
            config = new ModelApiKeyConfigPo();
            config.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));
            config.setModelCode(modelCode);
            config.setApiKeyId(availableApiKey.getFirst().getApiKeyId());
            modelApiKeyConfigRepository.save(config);
        }

        ApiKeyPo apiKey = repository.findById(config.getApiKeyId())
            .orElseThrow(() -> new BizException("API密钥不存在"));

        if (apiKey.getPlayer().getId().equals(playerId)) {
            // 更新使用次数和最后使用时间
            apiKey.setUsageCount(apiKey.getUsageCount() + 1);
            apiKey.setLastUsedTime(new Date());
            repository.save(apiKey);
            return apiKey.getKeyValue();
        }

        if (authRepository.countByAuthorized(playerId, apiKey.getId(), 1) > 0) {
            // 更新使用次数和最后使用时间
            apiKey.setUsageCount(apiKey.getUsageCount() + 1);
            apiKey.setLastUsedTime(new Date());
            repository.save(apiKey);

            // 更新授权记录的使用次数
            ApiKeyAuthorizationPo auth = authRepository.findByApiKeyIdAndAuthorizedPlayerId(apiKey.getId(), playerId);
            if (auth != null) {
                auth.setUsageCount(auth.getUsageCount() + 1);
                authRepository.save(auth);
            }
            return apiKey.getKeyValue();
        }

        throw new BizException("无权使用此API密钥");
    }


    /**
     * 获取当前人物特定模型系列可用的API密钥列表
     * @param series 模型系列，如 Gemini、Grok 等
     * @return 匹配系列的 API 密钥列表
     */
    public List<AvailableApiKeyVo> getCurrentPlayerAvailableApiKey(String series) {

        // 获取当前人物ID
        Long playerId = AuthService.getCurrentPlayerId();

        List<AvailableApiKeyVo> result = new ArrayList<>();

        // 创建查询对象
        ApiKeyPo probe = new ApiKeyPo();
        PlayerPo playerPo = new PlayerPo();
        playerPo.setId(playerId);
        probe.setPlayer(playerPo);
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
                vo.setOwnerPlayerName("");
                result.add(vo);
            }
        }

        // 查询被授权的API密钥 - 使用新的JPQL查询
        List<ApiKeyPo> authorizedKeys = apiKeyAuthorizationRepository.getApiKeyFromAuthorized(playerId, 1, series);

        if (authorizedKeys != null && !authorizedKeys.isEmpty()) {
            for (ApiKeyPo key : authorizedKeys) {

                // 此处不需要再次检查status和series，因为JPQL查询已经过滤了
                ApiKeyAuthorizationPo auth = apiKeyAuthorizationRepository.findByApiKeyIdAndAuthorizedPlayerId(key.getId(), playerId);

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
                vo.setOwnerPlayerName(key.getPlayer().getName());
                result.add(vo);
            }
        }

        return result;
    }

} 