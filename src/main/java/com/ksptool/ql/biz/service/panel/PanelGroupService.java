package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PermissionRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.vo.PanelGroupVo;
import com.ksptool.ql.biz.model.vo.EditPanelGroupVo;
import com.ksptool.ql.biz.model.vo.EditPanelGroupPermissionVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class PanelGroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * 获取用户组详细信息
     */
    public EditPanelGroupVo getGroupDetails(Long id) throws BizException {
        // 获取用户组信息
        GroupPo groupPo = groupRepository.getGroupDetailsById(id);

        if(groupPo == null){
            throw new BizException("用户组不存在!");
        }

        // 创建并填充基本信息
        EditPanelGroupVo vo = new EditPanelGroupVo();
        assign(groupPo, vo);

        // 获取所有权限
        List<PermissionPo> allPermissions = permissionRepository.findAll(
            Sort.by(Sort.Direction.ASC, "sortOrder")
        );

        // 获取用户组已有权限的ID集合
        Set<Long> gPermIds = new HashSet<>();
        for (PermissionPo permission : groupPo.getPermissions()) {
            gPermIds.add(permission.getId());
        }

        // 转换权限列表
        List<EditPanelGroupPermissionVo> permissions = new ArrayList<>();
        for (PermissionPo permission : allPermissions) {
            EditPanelGroupPermissionVo pVo = new EditPanelGroupPermissionVo();
            assign(permission, pVo);
            pVo.setHasPermission(gPermIds.contains(permission.getId()));
            permissions.add(pVo);
        }
        vo.setPermissions(permissions);
        return vo;
    }

    /**
     * 分页查询所有用户组
     */
    public PageableView<PanelGroupVo> findAll(int page, int size) {
        // 使用Repository的分页查询
        List<GroupPo> pos = groupRepository.findAllByOrderBySortOrderAsc(PageRequest.of(page - 1, size));
        List<PanelGroupVo> vos = as(pos,PanelGroupVo.class);
        long total = groupRepository.count();
        return new PageableView<>(vos, total, page, size);
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