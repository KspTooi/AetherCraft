package com.ksptool.ql.biz.service.admin;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ConfigRepository;
import com.ksptool.ql.biz.model.dto.GetConfigListDto;
import com.ksptool.ql.biz.model.dto.SaveConfigDto;
import com.ksptool.ql.biz.model.po.ConfigPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.vo.GetConfigDetailsVo;
import com.ksptool.ql.biz.model.vo.GetConfigListVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AdminConfigService {

    @Autowired
    private ConfigRepository repository;

    public RestPageableView<GetConfigListVo> getConfigList(GetConfigListDto dto) {

        Long userId = AuthService.getCurrentUserId();

        if(AuthService.hasPermission("panel:config:view:global")){
            userId = null;
        }

        Pageable pageQuery = PageRequest.of(dto.getPage() - 1, dto.getPageSize(), Sort.Direction.DESC, "updateTime");
        Page<GetConfigListVo> pPos = repository.getConfigList(dto.getKeyword(),dto.getPlayerName(), userId, pageQuery);
        List<GetConfigListVo> vos = as(pPos.getContent(),GetConfigListVo.class);

        for(GetConfigListVo vo:vos){
            if(vo.getPlayerName() == null){
                vo.setPlayerName("全局");
            }
        }

        return new RestPageableView<>(vos, pPos.getTotalElements());
    }

    public GetConfigDetailsVo getConfigDetails(Long id) throws BizException {

        var query = new ConfigPo();
        query.setId(id);

        //无全局数据权限时仅查询当前玩家下的配置
        if(!AuthService.hasPermission("panel:config:view:global")){
            query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
        }

        ConfigPo po = repository.findOne(Example.of(query))
                .orElseThrow(()->new BizException("配置项不存在或无权访问"));

        GetConfigDetailsVo vo = as(po, GetConfigDetailsVo.class);

        if(po.getPlayer() == null){
            vo.setPlayerName("全局");
        }

        if(po.getPlayer() != null){
            vo.setPlayerName(po.getPlayer().getName());
        }

        return vo;
    }

    @Transactional
    public void saveConfig(SaveConfigDto dto) throws BizException {

        //创建
        if (dto.getId() == null) {
            if (repository.existsByPlayerIdAndConfigKey(AuthService.getCurrentPlayerId(), dto.getConfigKey())) {
                throw new RuntimeException("配置键已存在");
            }
            ConfigPo config = new ConfigPo();
            assign(dto, config);
            config.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
            repository.save(config);
            return;
        }

        //编辑
        var query = new ConfigPo();
        query.setId(dto.getId());

        //无全局数据权限时仅查询当前玩家下的配置
        if(!AuthService.hasPermission("panel:config:view:global")){
            query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
        }

        ConfigPo po = repository.findOne(Example.of(query))
                .orElseThrow(()->new BizException("配置项不存在或无权访问"));

        po.setConfigValue(dto.getConfigValue());
        po.setDescription(dto.getDescription());
        repository.save(po);
    }

    @Transactional
    public void removeConfig(Long id) throws BizException {

        var query = new ConfigPo();
        query.setId(id);

        //无全局数据权限时仅查询当前玩家下的配置
        if(!AuthService.hasPermission("panel:config:view:global")){
            query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
        }

        ConfigPo po = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("配置项不存在或无权访问"));

        repository.delete(po);
    }
}
