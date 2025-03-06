package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.model.dto.ListApiKeyDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyDto;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.ListApiKeyVo;
import com.ksptool.ql.biz.model.vo.SaveApiKeyVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.SimpleExample;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
@RequiredArgsConstructor
public class PanelApiKeyService {
    
    private final ApiKeyRepository repository;
    
    public PageableView<ListApiKeyVo> getListView(ListApiKeyDto dto) {
        // 构建查询条件
        var query = SimpleExample.of(new ApiKeyPo())
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
} 