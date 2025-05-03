package com.ksptool.ql.biz.service.admin;

import com.ksptool.ql.biz.mapper.ConfigRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.dto.GetConfigListDto;
import com.ksptool.ql.biz.model.dto.SaveConfigDto;
import com.ksptool.ql.biz.model.po.ConfigPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.GetConfigDetailsVo;
import com.ksptool.ql.biz.model.vo.GetConfigListVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AdminConfigService {

    @Autowired
    private ConfigRepository repository;
    @Autowired
    private UserRepository userRepository;

    public RestPageableView<GetConfigListVo> getConfigList(GetConfigListDto dto) {
        Long userId = AuthService.getCurrentUserId();

        if(AuthService.hasPermission("panel:config:view:global")){
            userId = null;
        }

        Pageable pageQuery = PageRequest.of(dto.getPage() - 1, dto.getPageSize(), Sort.Direction.DESC, "updateTime");
        Page<GetConfigListVo> pPos = repository.getConfigList(dto.getKeyword(),dto.getUsername(), userId, pageQuery);

        List<GetConfigListVo> vos = as(pPos.getContent(),GetConfigListVo.class);

        for(GetConfigListVo vo:vos){
            if(vo.getUserId() == -1){
                vo.setUsername("全局");
            }
        }

        return new RestPageableView<>(vos, pPos.getTotalElements());
    }

    public GetConfigDetailsVo getConfigDetails(Long id) throws BizException {

        Long userId = AuthService.getCurrentUserId();

        if(AuthService.hasPermission("panel:config:view:global")){
            userId = null;
        }

        ConfigPo po = repository.getByIdAndUserId(id, userId)
                .orElseThrow(()->new BizException("配置项不存在或无权访问"));

        GetConfigDetailsVo vo = as(po, GetConfigDetailsVo.class);

        if(po.getUserId() == -1){
            vo.setUsername("全局");
        }
        if(po.getUserId() != null && po.getUserId() != -1){
            UserPo userPo = userRepository.findById(po.getUserId()).orElse(null);
            if(userPo != null){
                vo.setUsername(userPo.getUsername());
            }
        }

        return vo;
    }

    @Transactional
    public void saveConfig(SaveConfigDto dto) throws BizException {

        //创建
        if (dto.getId() == null) {
            if (repository.existsByUserIdAndConfigKey(AuthService.getCurrentUserId(), dto.getConfigKey())) {
                throw new RuntimeException("配置键已存在");
            }
            ConfigPo config = new ConfigPo();
            assign(dto, config);
            config.setUserId(AuthService.getCurrentUserId());
            repository.save(config);
            return;
        }

        //编辑
        Long userId = AuthService.getCurrentUserId();

        if(AuthService.hasPermission("panel:config:view:global")){
            userId = null;
        }

        ConfigPo po = repository.getByIdAndUserId(dto.getId(), userId)
                .orElseThrow(()->new BizException("配置项不存在或无权访问"));

        po.setConfigValue(dto.getConfigValue());
        po.setDescription(dto.getDescription());
        repository.save(po);
    }

    @Transactional
    public void removeConfig(Long id) throws BizException {

        Long userId = AuthService.getCurrentUserId();

        if(AuthService.hasPermission("panel:config:view:global")){
            userId = null;
        }

        ConfigPo po = repository.getByIdAndUserId(id, userId)
                .orElseThrow(()->new BizException("配置项不存在或无权访问"));

        repository.delete(po);
    }
}
