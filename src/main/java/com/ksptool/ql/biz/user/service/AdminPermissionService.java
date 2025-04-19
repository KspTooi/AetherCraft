package com.ksptool.ql.biz.user.service;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PermissionRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.user.model.dto.GetPermissionDetailsDto;
import com.ksptool.ql.biz.user.model.dto.GetPermissionListDto;
import com.ksptool.ql.biz.user.model.vo.GetPermissionDetailsVo;
import com.ksptool.ql.biz.user.model.vo.GetPermissionListVo;
import com.ksptool.ql.biz.user.model.vo.UserGroupVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AdminPermissionService {

    @Autowired
    private PermissionRepository repository;
    
    @Autowired
    private GroupRepository groupRepository;

    /**
     * 获取权限列表
     */
    public RestPageableView<GetPermissionListVo> getPermissionList(GetPermissionListDto dto) {
        // 创建分页对象，排序已在JPQL中指定
        Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getPageSize());
        
        // 使用Repository的getPermissionList方法查询PO对象
        Page<PermissionPo> pagePos = repository.getPermissionList(dto, pageable);
        
        // 将PO转换为VO
        List<GetPermissionListVo> vos = as(pagePos.getContent(), GetPermissionListVo.class);

        
        // 返回结果
        return new RestPageableView<>(vos, pagePos.getTotalElements());
    }

    /**
     * 获取权限详情
     */
    public GetPermissionDetailsVo getPermissionDetails(long id) throws BizException {

        PermissionPo permission = repository.findById(id)
                .orElseThrow(() -> new BizException("权限不存在"));
        
        GetPermissionDetailsVo vo = new GetPermissionDetailsVo();
        assign(permission, vo);
        return vo;
    }

    /**
     * 保存权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePermission(GetPermissionDetailsDto dto) throws BizException {
        if (StringUtils.isBlank(dto.getCode())) {
            throw new BizException("权限标识不能为空");
        }
        
        if (StringUtils.isBlank(dto.getName())) {
            throw new BizException("权限名称不能为空");
        }
        
        // 检查权限标识是否已存在
        PermissionPo query = new PermissionPo();
        query.setCode(dto.getCode());
        
        Example<PermissionPo> example = Example.of(query);
        List<PermissionPo> existingPerms = repository.findAll(example);
        PermissionPo existingPermByCode = existingPerms.isEmpty() ? null : existingPerms.get(0);
        
        if (existingPermByCode != null && (dto.getId() == null || !existingPermByCode.getId().equals(dto.getId()))) {
            throw new BizException("权限标识已存在");
        }
        
        PermissionPo permission;
        if (dto.getId() == null) {
            // 创建新权限
            permission = new PermissionPo();
            permission.setIsSystem(0); // 非系统权限
        } else {
            // 更新现有权限
            permission = repository.findById(dto.getId())
                    .orElseThrow(() -> new BizException("权限不存在"));
            
            // 如果是系统权限，不允许修改code和name
            if (permission.getIsSystem() != null && permission.getIsSystem() == 1) {
                dto.setCode(permission.getCode());
                dto.setName(permission.getName());
            }
        }
        
        // 设置排序顺序
        if (dto.getSortOrder() == null) {
            dto.setSortOrder(getNextSortOrder());
        }
        
        // 更新权限信息
        assign(dto, permission);
        repository.save(permission);
    }

    /**
     * 删除权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePermission(long id) throws BizException {
        PermissionPo permission = repository.findById(id)
                .orElseThrow(() -> new BizException("权限不存在"));
        
        // 检查是否为系统权限
        if (permission.getIsSystem() != null && permission.getIsSystem() == 1) {
            throw new BizException("系统权限不允许删除");
        }
        
        // 检查权限是否被用户组使用
        if (!permission.getGroups().isEmpty()) {
            throw new BizException("该权限已被用户组使用，无法删除");
        }
        
        repository.deleteById(id);
    }
    
    /**
     * 获取下一个可用的排序号
     */
    private Integer getNextSortOrder() {
        // 通过查询所有权限计算最大排序号
        List<PermissionPo> allPerms = repository.findAll();
        return allPerms.stream()
            .map(PermissionPo::getSortOrder)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0) + 1;
    }
}
