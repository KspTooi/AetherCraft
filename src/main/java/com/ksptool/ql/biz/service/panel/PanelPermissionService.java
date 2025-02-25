package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.PermissionRepository;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.vo.EditPanelPermissionVo;
import com.ksptool.ql.biz.model.vo.ListPanelPermissionVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.enums.PermissionEnum;
import com.ksptool.ql.biz.model.vo.ValidateSystemPermissionsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ksptool.entities.Entities.assign;

/**
 * 权限节点管理服务
 */
@Service
public class PanelPermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * 获取权限列表
     */
    public PageableView<ListPanelPermissionVo> getPermissionList(Pageable pageable) {
        Page<PermissionPo> page = permissionRepository.findAll(pageable);
        
        // 转换为VO
        List<ListPanelPermissionVo> voList = new ArrayList<>();
        for (PermissionPo po : page.getContent()) {
            ListPanelPermissionVo vo = new ListPanelPermissionVo();
            assign(po, vo);
            voList.add(vo);
        }
        
        return new PageableView<>(
            voList,
            page.getTotalElements(),
            pageable.getPageNumber() + 1,
            pageable.getPageSize()
        );
    }

    /**
     * 获取权限编辑信息
     * @throws BizException 权限不存在时抛出异常
     */
    public EditPanelPermissionVo getPermissionForEdit(Long id) throws BizException {
        PermissionPo po = permissionRepository.findById(id)
                .orElseThrow(() -> new BizException("权限不存在"));
        
        EditPanelPermissionVo vo = new EditPanelPermissionVo();
        assign(po, vo);
        return vo;
    }

    /**
     * 保存权限
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePermission(PermissionPo permission) throws BizException {
        // 检查权限标识是否已存在
        if (permission.getId() == null) {
            if (permissionRepository.existsByCode(permission.getCode())) {
                throw new BizException("权限标识已存在");
            }
        } else {
            PermissionPo existingPermission = permissionRepository.findById(permission.getId())
                    .orElseThrow(() -> new BizException("权限不存在"));
            if (!existingPermission.getCode().equals(permission.getCode()) &&
                permissionRepository.existsByCode(permission.getCode())) {
                throw new BizException("权限标识已存在");
            }
        }
        
        permissionRepository.save(permission);
    }

    /**
     * 删除权限
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) throws BizException {
        PermissionPo permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BizException("权限不存在"));
        
        // 检查是否为系统权限
        if (permission.getIsSystem() != null && permission.getIsSystem() == 1) {
            throw new BizException("系统权限不允许删除");
        }
        
        // 检查权限是否被使用
        if (!permission.getGroups().isEmpty()) {
            throw new BizException("权限已被用户组使用，无法删除");
        }
        
        permissionRepository.deleteById(id);
    }

    /**
     * 获取权限信息
     * @throws BizException 权限不存在时抛出异常
     */
    public PermissionPo getPermission(Long id) throws BizException {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new BizException("权限不存在"));
    }

    /**
     * 获取下一个可用的排序号
     * @return 下一个排序号
     */
    public Integer getNextSortOrder() {
        return permissionRepository.findMaxSortOrder() + 1;
    }
    
    /**
     * 校验系统内置权限节点
     * 检查数据库中是否存在所有系统内置权限，如果不存在则自动创建
     * @return 校验结果，包含新增的权限数量和已存在的权限数量
     */
    @Transactional(rollbackFor = Exception.class)
    public ValidateSystemPermissionsVo validateSystemPermissions() {
        ValidateSystemPermissionsVo result = new ValidateSystemPermissionsVo();
        
        // 获取所有系统内置权限枚举
        PermissionEnum[] permissionEnums = PermissionEnum.values();
        
        // 记录已存在和新增的权限数量
        int existCount = 0;
        int addedCount = 0;
        List<String> addedPermissions = new ArrayList<>();
        
        // 遍历所有系统内置权限
        for (PermissionEnum permEnum : permissionEnums) {
            String code = permEnum.getCode();
            
            // 检查权限是否已存在
            if (permissionRepository.existsByCode(code)) {
                existCount++;
            } else {
                // 创建新的权限
                PermissionPo permission = new PermissionPo();
                permission.setCode(code);
                permission.setName(permEnum.getDescription());
                permission.setDescription(permEnum.getDescription());
                permission.setIsSystem(1); // 标记为系统权限
                permission.setSortOrder(getNextSortOrder());
                
                // 保存权限
                permissionRepository.save(permission);
                
                addedCount++;
                addedPermissions.add(code);
            }
        }
        
        // 设置结果
        result.setExistCount(existCount);
        result.setAddedCount(addedCount);
        result.setAddedPermissions(addedPermissions);
        
        return result;
    }
} 