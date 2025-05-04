package com.ksptool.ql.biz.service.admin;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ApiKeyAuthorizationRepository;
import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.mapper.ModelApiKeyConfigRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.ListApiKeyVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.SimpleExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.ksptool.ql.biz.model.dto.GetApiKeyListDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyDto;
import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.vo.GetApiKeyListVo;
import com.ksptool.ql.biz.model.vo.GetApiKeyDetailsVo;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.transaction.annotation.Transactional;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AdminApiKeyService {

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

    /**
     * 获取API密钥分页列表
     */
    public RestPageableView<GetApiKeyListVo> getApiKeyList(GetApiKeyListDto dto) {

        var probe = new ApiKeyPo();
        probe.setUser(Any.of().val("id", AuthService.getCurrentUserId()).as(UserPo.class));

        // 构建查询条件
        var query = SimpleExample.of(probe)
                .assign(dto)
                .like("keyName", "keyType")
                .orderByDesc("updateTime");

        Page<ApiKeyPo> pPos = repository.findAll(query.get(), dto.pageRequest().withSort(query.getSort()));
        return new RestPageableView<>(pPos, GetApiKeyListVo.class);
    }

    /**
     * 获取API密钥详情
     */
    public GetApiKeyDetailsVo getApiKeyDetails(CommonIdDto dto) throws BizException {
        var query = new ApiKeyPo();
        query.setId(dto.getId());
        query.setUser(Any.of().val("id", AuthService.getCurrentUserId()).as(UserPo.class));
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

        var query = new ApiKeyPo();
        query.setId(dto.getId());
        query.setUser(Any.of().val("id", AuthService.getCurrentUserId()).as(UserPo.class));

        ApiKeyPo po = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("Api密钥不存在"));
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
        query.setUser(Any.of().val("id", AuthService.getCurrentUserId()).as(UserPo.class));

        ApiKeyPo po = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("Api密钥不存在"));

        // 删除相关的授权记录
        authRepository.deleteByApiKeyId(dto.getId());

        // 删除相关的模型配置
        modelApiKeyConfigRepository.deleteByApiKey(dto.getId());

        // 删除API密钥
        repository.delete(po);
    }

}
