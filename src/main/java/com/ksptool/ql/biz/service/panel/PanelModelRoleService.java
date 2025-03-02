package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.model.dto.ListModelRoleDto;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.vo.ListModelRoleItemVo;
import com.ksptool.ql.biz.model.vo.ListModelRoleVo;
import com.ksptool.ql.commons.web.PageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ksptool.entities.Entities.assign;

/**
 * 模型角色管理服务
 */
@Service
public class PanelModelRoleService {

    @Autowired
    private ModelRoleRepository modelRoleRepository;

    /**
     * 获取模型角色列表视图
     * 
     * @param dto 查询参数
     * @return 模型角色列表视图
     */
    @Transactional(readOnly = true)
    public ListModelRoleVo getListView(ListModelRoleDto dto) {
        // 创建视图对象
        ListModelRoleVo vo = new ListModelRoleVo();
        
        // 设置DTO中的参数回显
        vo.setId(dto.getId());
        vo.setKeyword(dto.getKeyword());
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(
            dto.getCurrentPage() - 1, 
            dto.getPageSize(), 
            Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.DESC, "updateTime"))
        );
        
        // 查询角色列表
        Page<ModelRolePo> rolePage = modelRoleRepository.findByKeyword(dto.getKeyword(), pageable);
        
        // 转换为VO列表
        List<ListModelRoleItemVo> roleItemList = new ArrayList<>();
        for (ModelRolePo po : rolePage.getContent()) {
            ListModelRoleItemVo itemVo = new ListModelRoleItemVo();
            assign(po, itemVo);
            roleItemList.add(itemVo);
        }
        
        // 创建分页视图
        PageableView<ListModelRoleItemVo> pageableView = new PageableView<>(
            roleItemList, 
            rolePage.getTotalElements(), 
            dto.getCurrentPage(), 
            dto.getPageSize()
        );
        
        // 设置分页信息
        vo.setRoleList(pageableView);
        
        // 如果有选中的角色ID，则查询角色详情
        if (dto.getId() != null) {
            ModelRolePo rolePo = modelRoleRepository.findById(dto.getId()).orElse(null);
            if (rolePo != null) {
                // 将角色详情映射到VO
                assign(rolePo, vo);
            }
        }
        
        return vo;
    }
    
    /**
     * 根据ID获取模型角色（包含对话示例）
     * 
     * @param id 角色ID
     * @return 模型角色
     */
    @Transactional(readOnly = true)
    public ModelRolePo getRoleWithTemplates(Long id) {
        return modelRoleRepository.findByIdWithTemplates(id);
    }
} 