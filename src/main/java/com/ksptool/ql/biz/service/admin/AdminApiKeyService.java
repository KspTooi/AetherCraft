package com.ksptool.ql.biz.service.admin;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.*;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.vo.*;
import com.ksptool.ql.biz.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.ksptool.ql.biz.model.dto.GetApiKeyListDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyDto;
import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.transaction.annotation.Transactional;
import com.ksptool.ql.biz.model.dto.GetApiKeyAuthorizationListDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyAuthorizationDto;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AdminApiKeyService {

    @Autowired
    private ApiKeyRepository repository;

    @Autowired
    private ApiKeyAuthorizationRepository authRepository;

    @Autowired
    private ModelApiKeyConfigRepository modelApiKeyConfigRepository;

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * 获取API密钥分页列表
     */
    public RestPageableView<GetApiKeyListVo> getApiKeyList(GetApiKeyListDto dto) {
        Long currentPlayerId = AuthService.getCurrentPlayerId();
        Page<GetApiKeyListVo> pVos = repository.getApiKeyList(dto, currentPlayerId, dto.pageRequest());
        return new RestPageableView<>(pVos);
    }

    /**
     * 获取API密钥详情
     */
    public GetApiKeyDetailsVo getApiKeyDetails(CommonIdDto dto) throws BizException {
        var query = new ApiKeyPo();
        query.setId(dto.getId());
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
        ApiKeyPo po = repository.findOne(Example.of(query)).orElseThrow(() -> new BizException("Apikey未找到或无权访问"));
        return as(po,GetApiKeyDetailsVo.class);
    }

    /**
     * 保存API密钥
     */
    @Transactional
    public void saveApiKey(SaveApiKeyDto dto) throws BizException {

        var createMode = dto.getId() == null;

        // 检查名称是否重复
        if (repository.existsByKeyNameAndPlayerId(dto.getKeyName(), AuthService.getCurrentPlayerId(), dto.getId())) {
            throw new BizException("密钥名称已存在");
        }

        if(createMode){
            ApiKeyPo create = as(dto,ApiKeyPo.class);
            PlayerPo player = new PlayerPo();
            player.setId(AuthService.getCurrentPlayerId());
            create.setPlayer(player);
            repository.save(create);
            return;
        }

        var query = new ApiKeyPo();
        query.setId(dto.getId());
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        ApiKeyPo po = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("Api密钥不存在"));

        //留空不修改密钥值
        if(StringUtils.isBlank(dto.getKeyValue())){
            dto.setKeyValue(po.getKeyValue());
        }

        assign(dto, po);
        repository.save(po);
    }

    /**
     * 删除API密钥
     */
    @Transactional
    public void removeApiKey(CommonIdDto dto) throws BizException {

        var query = new ApiKeyPo();
        query.setId(dto.getId());
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        ApiKeyPo po = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("Api密钥不存在"));

        // 删除相关的授权记录
        authRepository.deleteByApiKeyId(dto.getId());

        // 删除相关的模型配置
        modelApiKeyConfigRepository.deleteByApiKey(dto.getId());

        // 删除API密钥
        repository.delete(po);
    }

    /**
     * 获取某个Apikey的授权列表
     */
    public RestPageableView<GetApiKeyAuthorizationListVo> getAuthorizationList(GetApiKeyAuthorizationListDto dto) throws BizException {

        //检查API密钥是否存在且属于当前人物
        var apiKeyQuery = new ApiKeyPo();
        apiKeyQuery.setId(dto.getApiKeyId());
        apiKeyQuery.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        repository.findOne(Example.of(apiKeyQuery))
                .orElseThrow(() -> new BizException("API密钥不存在或无权访问"));

        // 查询授权列表
        Page<GetApiKeyAuthorizationListVo> page = authRepository.getApiKeyAuthorizationList(
                dto.getApiKeyId(),
                dto.getAuthorizedPlayerName(),
                dto.pageRequest()
        );

        return new RestPageableView<>(page);
    }

    /**
     * 查询授权详情
     */
    public GetApiKeyAuthorizationDetailsVo getApiKeyAuthorizationDetails(CommonIdDto dto) throws BizException {

        var query = new ApiKeyAuthorizationPo();
        query.setId(dto.getId());
        query.setAuthorizerPlayer(Any.of().val("id", AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        var authPo = authRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("授权记录不存在或无权访问"));

        var vo = as(authPo,GetApiKeyAuthorizationDetailsVo.class);
        vo.setAuthorizedPlayerName("----");

        //查询并设置被授权人物名
        PlayerPo player = authPo.getAuthorizedPlayer();

        if(player != null){
            vo.setAuthorizedPlayerName(player.getName());
        }

        return vo;
    }

    /**
     * 创建Apikey的授权
     */
    @Transactional
    public void saveAuth(SaveApiKeyAuthorizationDto dto) throws BizException {

        var createMode = dto.getId() == null;

        //查询API密钥
        var apiKeyQuery = new ApiKeyPo();
        apiKeyQuery.setId(dto.getApiKeyId());
        apiKeyQuery.setPlayer(Any.of().val("id", AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        ApiKeyPo apiKeyPo = repository.findOne(Example.of(apiKeyQuery))
                .orElseThrow(() -> new BizException("API密钥不存在或无权访问"));

        //查找被授权人物
        PlayerPo authorizedPlayer = playerRepository.findOneByName(dto.getAuthorizedPlayerName());

        if (authorizedPlayer == null) {
            throw new BizException("被授权人物不存在");
        }

        // 检查是否为自授权
        if (authorizedPlayer.getId().equals(AuthService.getCurrentPlayerId())) {
            throw new BizException("无需授权，您已经可以使用自己创建的API密钥");
        }

        // 检查是否已存在授权
        if (authRepository.existsByApiKeyIdAndAuthorizedPlayerId(
                dto.getApiKeyId(), authorizedPlayer.getId(), dto.getId())) {
            throw new BizException("该人物已被授权使用此密钥");
        }

        //创建
        if (createMode) {
            var auth = as(dto, ApiKeyAuthorizationPo.class);
            auth.setAuthorizedPlayer(authorizedPlayer);
            auth.setAuthorizerPlayer(Any.of().val("id", AuthService.getCurrentPlayerId()).as(PlayerPo.class));
            auth.setApiKey(Any.of().val("id",apiKeyPo.getId()).as(ApiKeyPo.class));
            auth.setUsageCount(0L);
            authRepository.save(auth);
            return;
        }

        //修改
        var query = new ApiKeyAuthorizationPo();
        query.setId(dto.getId());
        query.setAuthorizerPlayer(Any.of().val("id", AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        var edit = authRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("授权记录不存在或无权访问"));

        edit.setUsageLimit(dto.getUsageLimit());
        edit.setExpireTime(dto.getExpireTime());
        edit.setStatus(dto.getStatus());
        authRepository.save(edit);
    }

    /**
     * 移除授权关系
     */
    @Transactional
    public void removeAuthorization(CommonIdDto dto) throws BizException {

        //查询授权记录
        var query = new ApiKeyAuthorizationPo();
        query.setId(dto.getId());
        query.setAuthorizerPlayer(Any.of().val("id", AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        var po = authRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("授权记录不存在或无权访问"));

        // 删除相关的模型配置
        modelApiKeyConfigRepository.deleteByApiKey(po.getApiKey().getId());
        authRepository.delete(po);
    }

}
