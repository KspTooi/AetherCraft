package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PermissionRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PanelGroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * 分页查询所有用户组
     */
    public List<GroupPo> findAll(int page, int size) {
        return groupRepository.findAllByOrderBySortOrderAsc(PageRequest.of(page - 1, size));
    }

    /**
     * 获取总页数
     */
    public int getTotalPages(int size) {
        long total = groupRepository.count();
        return (int) Math.ceil((double) total / size);
    }

    /**
     * 根据ID查询用户组
     */
    public GroupPo findById(Long id) {
        Optional<GroupPo> group = groupRepository.findById(id);
        return group.orElse(null);
    }

    /**
     * 保存或更新用户组
     */
    @Transactional
    public void save(GroupPo group, Long[] permissionIds) throws BizException {
        // 检查code是否已存在
        if (group.getId() == null) {
            if (groupRepository.existsByCode(group.getCode())) {
                throw new BizException("用户组标识已存在");
            }
        } else {
            GroupPo existingGroup = groupRepository.findById(group.getId())
                    .orElseThrow(() -> new BizException("用户组不存在"));
            if (!existingGroup.getCode().equals(group.getCode()) &&
                groupRepository.existsByCode(group.getCode())) {
                throw new BizException("用户组标识已存在");
            }
        }

        // 设置权限
        if (permissionIds != null) {
            Set<PermissionPo> permissions = Arrays.stream(permissionIds)
                .map(id -> permissionRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            group.setPermissions(permissions);
        } else {
            group.setPermissions(new HashSet<>());
        }

        // 设置默认值
        if (group.getStatus() == null) {
            group.setStatus(1); // 默认启用
        }
        if (group.getIsSystem() == null) {
            group.setIsSystem(false); // 默认非系统组
        }
        if (group.getSortOrder() == null) {
            group.setSortOrder(0); // 默认排序号
        }

        groupRepository.save(group);
    }

    /**
     * 删除用户组
     */
    @Transactional
    public void remove(Long id) throws BizException {
        GroupPo group = groupRepository.findById(id)
                .orElseThrow(() -> new BizException("用户组不存在"));
        
        if (group.getIsSystem()) {
            throw new BizException("系统用户组不能删除");
        }
        
        groupRepository.deleteById(id);
    }
} 