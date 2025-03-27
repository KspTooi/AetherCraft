package com.ksptool.ql.biz.service.panel;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.mapper.ApiKeyAuthorizationRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.mapper.ModelApiKeyConfigRepository;
import com.ksptool.ql.biz.model.dto.ListApiKeyDto;
import com.ksptool.ql.biz.model.dto.ListApiKeyAuthDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyAuthDto;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.po.ApiKeyAuthorizationPo;
import com.ksptool.ql.biz.model.po.ModelApiKeyConfigPo;
import com.ksptool.ql.biz.model.vo.*;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.SimpleExample;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class PanelApiKeyService {

    @Autowired
    private ApiKeyRepository repository;

    @Autowired
    private ApiKeyAuthorizationRepository authRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelApiKeyConfigRepository modelApiKeyConfigRepository;

    @Autowired
    private ApiKeyAuthorizationRepository apiKeyAuthorizationRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    public PageableView<ListApiKeyVo> getListView(ListApiKeyDto dto) {

        var probe = new ApiKeyPo();
        probe.setUser(Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class));

        // 构建查询条件
        var query = SimpleExample.of(probe)
                .assign(dto)
                .like("keyName", "keyType")
                .orderByDesc("updateTime");

        // 执行查询并返回分页视图
        Page<ApiKeyPo> ret = repository.findAll(query.get(), dto.pageRequest().withSort(query.getSort()));
        return new PageableView<>(ret, ListApiKeyVo.class);
    }

    /**
     * 获取编辑视图数据
     * @param id API密钥ID
     * @return 编辑视图数据
     * @throws BizException 当API密钥不存在或无权访问时
     */
    public SaveApiKeyVo getEditView(Long id) throws BizException {
        // 查询API密钥
        ApiKeyPo po = repository.findById(id)
            .orElseThrow(() -> new BizException("API密钥不存在"));
            
        // 检查是否为当前用户的密钥
        if (!po.getUser().getId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权访问此API密钥");
        }
        
        // 转换为视图对象
        SaveApiKeyVo vo = new SaveApiKeyVo();
        assign(po, vo);

        // 设置系列列表到VO
        vo.setKeySeriesList(AIModelEnum.getSeriesList());
        return vo;
    }

    /**
     * 保存API密钥
     * @param dto 保存请求
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveApiKey(SaveApiKeyDto dto) throws BizException {

        var createMode = dto.getId() == null;

        // 检查名称是否重复
        if (repository.existsByKeyNameAndUserId(dto.getKeyName(), AuthService.getCurrentUserId(), dto.getId())) {
            throw new BizException("密钥名称已存在");
        }

        if(createMode){
            ApiKeyPo create = as(dto,ApiKeyPo.class);
            UserPo user = new UserPo();
            user.setId(AuthService.getCurrentUserId());
            create.setUser(user);
            repository.save(create);
            return;
        }

        ApiKeyPo po = repository.findById(dto.getId()).orElseThrow(() -> new BizException("Api密钥不存在"));
        assign(dto, po);
        repository.save(po);
    }

    /**
     * 获取API密钥授权列表视图
     * @param dto 查询条件
     * @return 分页视图
     * @throws BizException 当API密钥不存在或无权访问时
     */
    public PageableView<ListApiKeyAuthVo> getAuthListView(ListApiKeyAuthDto dto) throws BizException {
        // 检查API密钥是否存在且属于当前用户
        ApiKeyPo apiKey = repository.findById(dto.getApiKeyId())
            .orElseThrow(() -> new BizException("API密钥不存在"));
            
        if (!apiKey.getUser().getId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权访问此API密钥");
        }
        
        // 查询授权列表
        Page<ListApiKeyAuthVo> page = authRepository.findAuthList(
            dto.getApiKeyId(),
            dto.getAuthorizedUserName(),
            dto.pageRequest()
        );
        
        return new PageableView<>(page);
    }

    /**
     * 获取API密钥授权创建视图
     * @param apiKeyId API密钥ID
     * @return 创建视图数据
     * @throws BizException 当API密钥不存在或无权访问时
     */
    public SaveApiKeyAuthVo getAuthCreateView(Long apiKeyId) throws BizException {
        // 检查API密钥是否存在且属于当前用户
        ApiKeyPo apiKey = repository.findById(apiKeyId)
            .orElseThrow(() -> new BizException("API密钥不存在"));
            
        if (!apiKey.getUser().getId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权访问此API密钥");
        }
        
        SaveApiKeyAuthVo vo = new SaveApiKeyAuthVo();
        vo.setApiKeyId(apiKeyId);
        vo.setStatus(1);
        return vo;
    }

    /**
     * 获取API密钥授权编辑视图
     * @param id 授权ID
     * @return 编辑视图数据
     * @throws BizException 当授权不存在或无权访问时
     */
    public SaveApiKeyAuthVo getAuthEditView(Long id) throws BizException {

        var authPo = authRepository.findById(id)
            .orElseThrow(() -> new BizException("授权记录不存在"));
            
        // 检查API密钥是否存在且属于当前用户
        ApiKeyPo apiKey = repository.findById(authPo.getApiKey().getId())
            .orElseThrow(() -> new BizException("API密钥不存在"));
            
        if (!apiKey.getUser().getId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权访问此API密钥");
        }
        
        SaveApiKeyAuthVo vo = new SaveApiKeyAuthVo();
        assign(authPo, vo);
        vo.setApiKeyId(authPo.getApiKey().getId());
        
        // 查询并设置被授权用户名
        UserPo authorizedUser = userRepository.findById(authPo.getAuthorizedUserId())
            .orElseThrow(() -> new BizException("被授权用户不存在"));
        vo.setAuthorizedUserName(authorizedUser.getUsername());
        
        return vo;
    }

    /**
     * 保存API密钥授权
     * @param dto 保存请求
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAuth(SaveApiKeyAuthDto dto) throws BizException {
        var createMode = dto.getId() == null;

        // 检查API密钥是否存在且属于当前用户
        ApiKeyPo apiKey = repository.findById(dto.getApiKeyId())
            .orElseThrow(() -> new BizException("API密钥不存在"));
            
        if (!apiKey.getUser().getId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权访问此API密钥");
        }
        
        // 查找被授权用户
        UserPo authorizedUser = userRepository.findByUsername(dto.getAuthorizedUserName());
        if (authorizedUser == null) {
            throw new BizException("被授权用户不存在");
        }
        
        // 检查是否为自授权
        if (authorizedUser.getId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无需授权，您已经可以使用自己创建的API密钥");
        }
            
        // 检查是否已存在授权
        if (authRepository.existsByApiKeyIdAndAuthorizedUserId(
                dto.getApiKeyId(), authorizedUser.getId(), dto.getId())) {
            throw new BizException("该用户已被授权使用此密钥");
        }
        
        if (createMode) {
            var auth = as(dto, ApiKeyAuthorizationPo.class);
            auth.setAuthorizedUserId(authorizedUser.getId());
            auth.setAuthorizerUserId(AuthService.getCurrentUserId());
            auth.setApiKey(Any.of().val("id",apiKey.getId()).as(ApiKeyPo.class));
            auth.setUsageCount(0L);
            authRepository.save(auth);
            return;
        }
        
        var auth = authRepository.findById(dto.getId())
            .orElseThrow(() -> new BizException("授权记录不存在"));
            
        assign(dto, auth);
        auth.setAuthorizedUserId(authorizedUser.getId());
        authRepository.save(auth);
    }
    
    /**
     * 移除API密钥授权
     * @param id 授权ID
     * @throws BizException 当授权不存在或无权访问时
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAuth(Long id) throws BizException {
        // 查询授权记录
        var auth = authRepository.findById(id)
            .orElseThrow(() -> new BizException("授权记录不存在"));
            
        // 检查API密钥是否属于当前用户
        if (!auth.getApiKey().getUser().getId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权移除此授权");
        }

        // 删除相关的模型配置
        modelApiKeyConfigRepository.deleteByApiKey(auth.getApiKey().getId());
        
        // 删除授权记录
        authRepository.delete(auth);
    }

    /**
     * 判断当前用户是否有权限使用指定的API密钥
     * @param apiKeyId API密钥ID
     * @return 是否有权限使用
     * @throws BizException 当API密钥不存在时
     */
    public boolean hasApiKeyPermission(Long apiKeyId) throws BizException {
        // 查询API密钥
        ApiKeyPo apiKey = repository.findById(apiKeyId)
            .orElseThrow(() -> new BizException("API密钥不存在"));
            
        // 检查是否是自己的密钥
        Long currentUserId = AuthService.getCurrentUserId();
        if (apiKey.getUser().getId().equals(currentUserId)) {
            return true;
        }
        
        // 检查是否有授权记录
        return authRepository.countByAuthorized(currentUserId, apiKeyId, 1) > 0;
    }

    /**
     * 移除API密钥
     * @param id API密钥ID
     * @throws BizException 当API密钥不存在或无权访问时
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeApiKey(Long id) throws BizException {
        // 查询API密钥
        ApiKeyPo apiKey = repository.findById(id)
            .orElseThrow(() -> new BizException("API密钥不存在"));
            
        // 检查是否为当前用户的密钥
        if (!apiKey.getUser().getId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权移除此API密钥");
        }
        
        // 删除相关的授权记录
        authRepository.deleteByApiKeyId(id);
        
        // 删除相关的模型配置
        modelApiKeyConfigRepository.deleteByApiKey(id);
        
        // 删除API密钥
        repository.delete(apiKey);
    }

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