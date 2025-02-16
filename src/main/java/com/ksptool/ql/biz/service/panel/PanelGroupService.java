package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PermissionRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.vo.ListPanelGroupVo;
import com.ksptool.ql.biz.model.vo.SavePanelGroupVo;
import com.ksptool.ql.biz.model.vo.SavePanelGroupPermissionVo;
import com.ksptool.ql.biz.model.dto.SavePanelGroupDto;
import com.ksptool.ql.biz.model.dto.ListPanelGroupDto;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    public SavePanelGroupVo getEditView(Long id) throws BizException {
        // 获取用户组信息
        GroupPo groupPo = groupRepository.getGroupDetailsById(id);

        if(groupPo == null){
            throw new BizException("用户组不存在!");
        }

        // 创建并填充基本信息
        SavePanelGroupVo vo = new SavePanelGroupVo();
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
        List<SavePanelGroupPermissionVo> permissions = new ArrayList<>();
        for (PermissionPo permission : allPermissions) {
            SavePanelGroupPermissionVo pVo = new SavePanelGroupPermissionVo();
            assign(permission, pVo);
            pVo.setHasPermission(gPermIds.contains(permission.getId()));
            permissions.add(pVo);
        }
        vo.setPermissions(permissions);
        
        // 编辑模式下不需要设置nextOrder
        vo.setNextOrder(null);
        
        return vo;
    }

    /**
     * 根据ID查询用户组
     */
    public GroupPo findById(Long id) {
        Optional<GroupPo> group = groupRepository.findById(id);
        return group.orElse(null);
    }

    /**
     * 保存用户组信息
     * 采用三步式保存：
     * 1. 更新基础信息
     * 2. 处理权限关联
     * 3. 保存所有更改
     */
    @Transactional
    public void savePanelGroup(SavePanelGroupDto dto) throws BizException {

        // 1. 更新基础信息
        GroupPo group;
        if (dto.getId() == null) {
            // 创建新用户组
            group = new GroupPo();
            if (groupRepository.existsByCode(dto.getCode())) {
                throw new BizException("用户组标识已存在");
            }
        } else {
            // 更新现有用户组
            group = groupRepository.findById(dto.getId())
                    .orElseThrow(() -> new BizException("用户组不存在"));
            if (!group.getCode().equals(dto.getCode()) &&
                groupRepository.existsByCode(dto.getCode())) {
                throw new BizException("用户组标识已存在");
            }
        }

        // 设置基础信息
        group.setCode(dto.getCode());
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setStatus(dto.getStatus());
        group.setSortOrder(dto.getSortOrder());
        
        // 2. 处理权限关联
        group.getPermissions().clear();
        
        // 3. 建立新的权限关联
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            Set<PermissionPo> newPermissions = new HashSet<>(
                permissionRepository.findAllById(dto.getPermissionIds())
            );
            group.getPermissions().addAll(newPermissions);
        }
        
        // 4. 保存所有更改
        groupRepository.save(group);
    }

    /**
     * 删除用户组
     * @param id 用户组ID
     * @return 被删除的用户组名称
     */
    @Transactional
    public String removeGroup(Long id) throws BizException {
        GroupPo group = groupRepository.findById(id)
                .orElseThrow(() -> new BizException("用户组不存在"));
        
        if (group.getIsSystem()) {
            throw new BizException("系统用户组不能删除");
        }
        
        String groupName = group.getName();
        groupRepository.deleteById(id);
        return groupName;
    }

    /**
     * 获取下一个排序号
     * @return 当前最大排序号+1
     */
    public int getNextSortOrder() {
        return groupRepository.findMaxSortOrder() + 1;
    }

    /**
     * 获取创建用户组视图数据
     */
    public SavePanelGroupVo getCreateView() {
        SavePanelGroupVo vo = new SavePanelGroupVo();
        // 设置基本信息默认值
        vo.setId(null);
        vo.setCode("");
        vo.setName("");
        vo.setDescription("");
        vo.setStatus(1);
        vo.setIsSystem(false);
        
        // 设置排序号
        int nextOrder = getNextSortOrder();
        vo.setNextOrder(nextOrder);
        vo.setSortOrder(nextOrder);
        
        // 获取所有权限列表
        List<PermissionPo> allPermissions = permissionRepository.findAll(
            Sort.by(Sort.Direction.ASC, "sortOrder")
        );
        
        // 转换权限列表
        List<SavePanelGroupPermissionVo> permissions = new ArrayList<>();
        for (PermissionPo permission : allPermissions) {
            SavePanelGroupPermissionVo pVo = new SavePanelGroupPermissionVo();
            assign(permission, pVo);
            pVo.setHasPermission(false);
            permissions.add(pVo);
        }
        vo.setPermissions(permissions);
        
        return vo;
    }

    /**
     * 获取用户组列表视图数据
     */
    public PageableView<ListPanelGroupVo> getListView(ListPanelGroupDto dto) {
        // 创建分页和排序
        PageRequest pr = PageRequest.of(dto.getPage() - 1, dto.getPageSize());

        Page<ListPanelGroupVo> pageResult = groupRepository.getListView(dto,pr);

        return new PageableView<>(
            pageResult.getContent(),
            pageResult.getTotalElements(),
            dto.getPage(),
            dto.getPageSize()
        );
    }
} 