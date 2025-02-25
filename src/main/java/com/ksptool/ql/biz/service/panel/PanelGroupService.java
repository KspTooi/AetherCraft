package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PermissionRepository;
import com.ksptool.ql.biz.mapper.UserSessionRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.po.UserSessionPo;
import com.ksptool.ql.biz.model.vo.ListPanelGroupVo;
import com.ksptool.ql.biz.model.vo.SavePanelGroupVo;
import com.ksptool.ql.biz.model.vo.SavePanelGroupPermissionVo;
import com.ksptool.ql.biz.model.dto.SavePanelGroupDto;
import com.ksptool.ql.biz.model.dto.ListPanelGroupDto;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static com.ksptool.entities.Entities.assign;

@Service
public class PanelGroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private AuthService authService;

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
        int nextOrder = groupRepository.findMaxSortOrder() + 1;
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
     * 获取用户组编辑视图
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
     * 保存用户组信息
     * 采用三步式保存：
     * 1. 更新基础信息
     * 2. 处理权限关联
     * 3. 保存所有更改
     */
    @Transactional
    public void savePanelGroup(SavePanelGroupDto dto) throws BizException {

        GroupPo group = null;
        boolean isUpdate = false;
        
        //创建新用户组
        if (dto.getId() == null) {
            group = new GroupPo();
            if (groupRepository.existsByCode(dto.getCode())) {
                throw new BizException("用户组标识已存在");
            }
        }
        
        //更新现有用户组
        if (dto.getId() != null) {
            group = groupRepository.findById(dto.getId()).orElseThrow(() -> new BizException("用户组不存在"));

            //判断是否修改了用户组Code 如果修改了Code判断是否重名
            if (!group.getCode().equals(dto.getCode())) {
                if (groupRepository.existsByCode(dto.getCode())) {
                    throw new BizException("用户组标识重复");
                }
            }

            isUpdate = true;
        }

        //设置基础信息
        group.setCode(dto.getCode());
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setStatus(dto.getStatus());
        group.setSortOrder(dto.getSortOrder());
        
        //处理权限关联
        group.getPermissions().clear();
        
        //建立新的权限关联
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            Set<PermissionPo> newPermissions = new HashSet<>(
                permissionRepository.findAllById(dto.getPermissionIds())
            );
            group.getPermissions().addAll(newPermissions);
        }
        
        //保存所有更改
        groupRepository.save(group);
        
        //如果是更新用户组，刷新该组下所有在线用户的会话
        if (isUpdate) {
            // 获取该用户组下所有在线用户的会话
            List<UserSessionPo> activeSessions = userSessionRepository.getUserSessionByGroupId(group.getId());
            
            // 刷新每个用户的会话
            for (UserSessionPo session : activeSessions) {
                authService.refreshUserSession(session.getUserId());
            }
        }
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

} 