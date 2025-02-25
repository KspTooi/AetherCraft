package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.ConfigRepository;
import com.ksptool.ql.biz.model.dto.ListPanelConfigDto;
import com.ksptool.ql.biz.model.dto.SavePanelConfigDto;
import com.ksptool.ql.biz.model.po.ConfigPo;
import com.ksptool.ql.biz.model.vo.ListPanelConfigVo;
import com.ksptool.ql.biz.model.vo.SavePanelConfigVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.ksptool.entities.Entities.assign;

@Service
public class PanelConfigService {

    @Autowired
    private ConfigRepository configRepository;

    /**
     * 获取配置项列表视图
     */
    public PageableView<ListPanelConfigVo> getListView(ListPanelConfigDto dto) {
        // 创建分页对象
        Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getPageSize(), Sort.Direction.DESC, "updateTime");
        
        // 调用Repository查询
        Page<ListPanelConfigVo> page = configRepository.getListView(dto.getKeyOrValue(), dto.getDescription(), pageable);
        
        // 返回分页视图
        return new PageableView<>(page.getContent(), page.getTotalElements(), dto.getPage(), dto.getPageSize());
    }

    /**
     * 获取创建视图
     */
    public SavePanelConfigVo getCreateView() {
        return new SavePanelConfigVo();
    }

    /**
     * 获取编辑视图
     */
    public SavePanelConfigVo getEditView(Long id) throws BizException {
        ConfigPo config = configRepository.findById(id)
                .orElseThrow(() -> new BizException("配置项不存在"));
        
        SavePanelConfigVo vo = new SavePanelConfigVo();
        assign(config, vo);
        return vo;
    }

    /**
     * 保存配置项
     */
    @Transactional
    public void save(SavePanelConfigDto dto) throws BizException {
        // 新增时检查配置键
        if (dto.getId() == null) {

            if (dto.getUserId() == null) {
                if (configRepository.existsByConfigKey(dto.getConfigKey())) {
                    throw new BizException("配置键已存在");
                }
                dto.setUserId(-1L);
            }
            
            if (dto.getUserId() != null) {
                if (configRepository.existsByUserIdAndConfigKey(dto.getUserId(), dto.getConfigKey())) {
                    throw new BizException("该用户下的配置键已存在");
                }
            }

            ConfigPo config = new ConfigPo();
            assign(dto, config);

            Date now = new Date();
            config.setCreateTime(now);
            config.setUpdateTime(now);
            
            configRepository.save(config);
            return;
        }

        // 修改时检查配置键
        ConfigPo config = configRepository.findById(dto.getId())
                .orElseThrow(() -> new BizException("配置项不存在"));
            
        if (!config.getConfigKey().equals(dto.getConfigKey())) {
            if (dto.getUserId() == null) {
                if (configRepository.existsByConfigKey(dto.getConfigKey())) {
                    throw new BizException("配置键已存在");
                }
            }
            
            if (dto.getUserId() != null) {
                if (configRepository.existsByUserIdAndConfigKey(dto.getUserId(), dto.getConfigKey())) {
                    throw new BizException("该用户下的配置键已存在");
                }
            }
        }

        assign(dto, config);
        config.setUpdateTime(new Date());
        configRepository.save(config);
    }

    /**
     * 删除配置项
     */
    @Transactional
    public void remove(Long id) throws BizException {
        ConfigPo config = configRepository.findById(id)
                .orElseThrow(() -> new BizException("配置项不存在"));
        configRepository.delete(config);
    }
} 