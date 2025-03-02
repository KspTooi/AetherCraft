package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.model.dto.ListModelRoleDto;
import com.ksptool.ql.biz.model.dto.SaveModelRoleDto;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.vo.ListModelRoleItemVo;
import com.ksptool.ql.biz.model.vo.ListModelRoleVo;
import com.ksptool.ql.biz.model.vo.SaveModelRoleVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
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

    /**
     * 保存模型角色
     * 
     * @param dto 保存参数
     * @return 保存结果
     * @throws BizException 业务异常
     */
    @Transactional
    public SaveModelRoleVo saveModelRole(SaveModelRoleDto dto) throws BizException {
        SaveModelRoleVo vo = new SaveModelRoleVo();
        
        try {
            // 获取当前用户ID
            Long currentUserId = AuthService.getCurrentUserId();
            
            // 声明角色对象
            ModelRolePo rolePo = null;
            
            // 新增角色
            if (dto.getId() == null) {
                // 新增时检查名称是否已存在
                if (modelRoleRepository.existsByName(dto.getName())) {
                    throw new BizException("角色名称已存在");
                }
                
                rolePo = new ModelRolePo();
                // 设置当前用户ID
                rolePo.setUserId(currentUserId);
            }
            
            // 更新角色
            if (dto.getId() != null) {
                rolePo = modelRoleRepository.findById(dto.getId()).orElse(null);
                if (rolePo == null) {
                    throw new BizException("角色不存在");
                }
                
                // 检查是否有权限修改该角色
                if (!rolePo.getUserId().equals(currentUserId)) {
                    throw new BizException("无权修改该角色");
                }
                
                // 更新时检查名称是否与其他角色重复
                if (modelRoleRepository.existsByNameAndIdNot(dto.getName(), dto.getId())) {
                    throw new BizException("角色名称已存在");
                }
            }
            
            // 确保rolePo不为null
            if (rolePo == null) {
                throw new BizException("角色对象创建失败");
            }
            
            // 将DTO数据映射到PO
            assign(dto, rolePo);
            
            // 保存角色
            rolePo = modelRoleRepository.save(rolePo);
            
            // 设置返回结果
            vo.setSuccess(true);
            vo.setId(rolePo.getId());
            vo.setName(rolePo.getName());
            vo.setMessage("保存成功");
            
            return vo;
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            throw new BizException("保存角色失败：" + e.getMessage(), e);
        }
    }
} 