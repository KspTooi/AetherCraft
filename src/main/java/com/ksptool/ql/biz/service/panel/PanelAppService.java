package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.AppRepository;
import com.ksptool.ql.biz.model.dto.ListPanelAppDto;
import com.ksptool.ql.biz.model.dto.SavePanelAppDto;
import com.ksptool.ql.biz.model.po.AppPo;
import com.ksptool.ql.biz.model.vo.ListPanelAppVo;
import com.ksptool.ql.biz.model.vo.SavePanelAppVo;
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
public class PanelAppService {

    @Autowired
    private AppRepository appRepository;

    /**
     * 获取应用列表视图
     */
    public PageableView<ListPanelAppVo> getListView(ListPanelAppDto dto) {
        // 创建分页对象
        Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getPageSize(), Sort.Direction.DESC, "updateTime");
        
        // 调用Repository查询
        Page<ListPanelAppVo> page = appRepository.getListView(dto.getNameOrCommand(), dto.getDescription(), pageable);
        
        // 返回分页视图
        return new PageableView<>(page.getContent(), page.getTotalElements(), dto.getPage(), dto.getPageSize());
    }

    /**
     * 获取创建视图
     */
    public SavePanelAppVo getCreateView() {
        return new SavePanelAppVo();
    }

    /**
     * 获取编辑视图
     */
    public SavePanelAppVo getEditView(Long id) throws BizException {
        AppPo app = appRepository.findById(id)
                .orElseThrow(() -> new BizException("应用不存在"));
        
        SavePanelAppVo vo = new SavePanelAppVo();
        assign(app, vo);
        return vo;
    }

    /**
     * 保存应用
     */
    @Transactional
    public void save(SavePanelAppDto dto) throws BizException {
        // 新增时检查快捷命令
        if (dto.getId() == null) {
            if (dto.getCommand() != null && !dto.getCommand().isEmpty()) {
                if (appRepository.existsByCommand(dto.getCommand())) {
                    throw new BizException("快捷命令已存在");
                }
            }

            AppPo app = new AppPo();
            assign(dto, app);
            
            Date now = new Date();
            app.setCreateTime(now);
            app.setUpdateTime(now);
            app.setLaunchCount(0);
            
            appRepository.save(app);
            return;
        }

        // 修改时检查快捷命令
        AppPo app = appRepository.findById(dto.getId())
                .orElseThrow(() -> new BizException("应用不存在"));
            
        if (dto.getCommand() != null && !dto.getCommand().isEmpty() && 
            !dto.getCommand().equals(app.getCommand())) {
            if (appRepository.existsByCommand(dto.getCommand())) {
                throw new BizException("快捷命令已存在");
            }
        }

        assign(dto, app);
        app.setUpdateTime(new Date());
        appRepository.save(app);
    }

    /**
     * 删除应用
     */
    @Transactional
    public void remove(Long id) throws BizException {
        AppPo app = appRepository.findById(id)
                .orElseThrow(() -> new BizException("应用不存在"));
        appRepository.delete(app);
    }
} 