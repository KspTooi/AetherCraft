package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.ConfigRepository;
import com.ksptool.ql.biz.model.dto.ListPanelConfigDto;
import com.ksptool.ql.biz.model.vo.ListPanelConfigVo;
import com.ksptool.ql.commons.web.PageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
} 