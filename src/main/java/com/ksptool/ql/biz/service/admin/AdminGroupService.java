package com.ksptool.ql.biz.service.admin;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PermissionRepository;
import com.ksptool.ql.biz.mapper.UserSessionRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.po.UserSessionPo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.model.dto.GetGroupListDto;
import com.ksptool.ql.biz.model.dto.SaveGroupDto;
import com.ksptool.ql.biz.model.vo.GetGroupDefinitionsVo;
import com.ksptool.ql.biz.model.vo.GetGroupDetailsVo;
import com.ksptool.ql.biz.model.vo.GetGroupListVo;
import com.ksptool.ql.biz.model.vo.GroupPermissionDefinitionVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ksptool.entities.Entities.as;

@Service
public class AdminGroupService {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private GroupRepository repository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AuthService authService;


    public List<GetGroupDefinitionsVo> getGroupDefinitions(){
        List<GroupPo> pos = repository.findAll();
        return as(pos,GetGroupDefinitionsVo.class);
    }

    public RestPageableView<GetGroupListVo> getGroupList(GetGroupListDto dto) {
        Page<GetGroupListVo> pagePos = repository.getGroupList(dto, dto.pageRequest());
        return new RestPageableView<>(pagePos);
    }

    public GetGroupDetailsVo getGroupDetails(long id) throws BizException {

        GroupPo po = repository.findById(id).orElseThrow(() -> new BizException("用户组不存在"));

        List<PermissionPo> allPermPos = permissionRepository.findAll();
        Set<PermissionPo> hasPermPos = po.getPermissions();

        GetGroupDetailsVo vo = as(po, GetGroupDetailsVo.class);
        List<GroupPermissionDefinitionVo> defVos = new ArrayList<>();

        for(var permission : allPermPos) {

            var defVo = as(permission, GroupPermissionDefinitionVo.class);
            defVo.setHas(1);

            for(var hasPermPo : hasPermPos) {
                if(permission.getId().equals(hasPermPo.getId())) {
                    defVo.setHas(0);
                }
            }

            defVos.add(defVo);
        }

        vo.setPermissions(defVos);
        return vo;
    }

    @Transactional
    public void saveGroup(SaveGroupDto dto) throws BizException {

        GroupPo group = null;
        boolean isUpdate = false;

        //创建新用户组
        if (dto.getId() == null) {
            group = new GroupPo();
            if (repository.existsByCode(dto.getCode())) {
                throw new BizException("用户组标识已存在");
            }
        }

        //更新现有用户组
        if (dto.getId() != null) {
            group = repository.findById(dto.getId()).orElseThrow(() -> new BizException("用户组不存在"));

            //判断是否修改了用户组Code 如果修改了Code判断是否重名
            if (!group.getCode().equals(dto.getCode())) {
                if (repository.existsByCode(dto.getCode())) {
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
        repository.save(group);

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

    @Transactional
    public void removeGroup(long id) throws BizException {

        GroupPo group = repository.getGroupWithUserAndPermission(id);

        if (group == null) {
            throw new BizException("用户组不存在");
        }

        if (group.getIsSystem()) {
            throw new BizException("系统用户组不能删除");
        }

        // 清空用户组与用户的关联关系
        if (!group.getUsers().isEmpty()) {
            for (UserPo user : new HashSet<>(group.getUsers())) {
                user.getGroups().remove(group);
            }
            group.getUsers().clear();
        }

        // 清空用户组与权限的关联关系
        if (group.getPermissions() != null) {
            group.getPermissions().clear();
        }

        // 保存更改并刷新
        repository.save(group);
        repository.flush();

        String groupName = group.getName();
        repository.delete(group);
    }
}
