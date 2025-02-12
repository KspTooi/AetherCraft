package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.PermissionRepository;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<PermissionPo> getPermissionList(Pageable pageable) {
        return permissionRepository.findAll(pageable);
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
} 