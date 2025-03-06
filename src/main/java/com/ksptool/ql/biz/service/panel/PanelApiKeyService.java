package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.ApiKeyRepository;
import com.ksptool.ql.biz.model.dto.ListApiKeyDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyDto;
import com.ksptool.ql.biz.model.po.ApiKeyPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.ListApiKeyVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.SimpleExample;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ksptool.entities.Entities.assign;

@Service
@RequiredArgsConstructor
public class PanelApiKeyService {
    
    private final ApiKeyRepository apiKeyRepository;
    
    public PageableView<ListApiKeyVo> getListView(ListApiKeyDto dto) {
        // 构建查询条件
        var query = SimpleExample.of(new ApiKeyPo())
            .assign(dto)
            .like("keyName", "keyType")
            .orderByDesc("updateTime");

        // 执行查询并返回分页视图
        Page<ApiKeyPo> ret = apiKeyRepository.findAll(query.get(), dto.pageRequest().withSort(query.getSort()));
        return new PageableView<>(ret, ListApiKeyVo.class);
    }

    /**
     * 保存API密钥
     * @param dto 保存请求
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveApiKey(SaveApiKeyDto dto) throws BizException {
        
        // 检查名称是否重复
        if (apiKeyRepository.existsByKeyNameAndUserId(dto.getKeyName(), AuthService.getCurrentUserId(), dto.getId())) {
            throw new BizException("密钥名称已存在");
        }

        // 保存密钥
        ApiKeyPo po = new ApiKeyPo();
        assign(dto, po);
        
        if (dto.getId() == null) {
            po.setUsageCount(0L);
            UserPo user = new UserPo();
            user.setId(AuthService.getCurrentUserId());
            po.setUser(user);
        }
        
        apiKeyRepository.save(po);
    }

} 