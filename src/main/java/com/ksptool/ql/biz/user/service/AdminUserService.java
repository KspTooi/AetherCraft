package com.ksptool.ql.biz.user.service;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PermissionRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.user.model.dto.GetUserListDto;
import com.ksptool.ql.biz.user.model.dto.SaveUserDto;
import com.ksptool.ql.biz.user.model.vo.GetUserDetailsVo;
import com.ksptool.ql.biz.user.model.vo.GetUserListVo;
import com.ksptool.ql.biz.user.model.vo.UserGroupVo;
import com.ksptool.ql.biz.user.model.vo.UserPermissionVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.RestPageableView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository repository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private AuthService authService;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RestPageableView<GetUserListVo> getUserList(GetUserListDto dto) {
        // 创建分页对象
        Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getPageSize(), Sort.Direction.DESC, "updateTime");

        // 创建查询条件
        UserPo query = new UserPo();
        if (StringUtils.isNotBlank(dto.getUsername())) {
            query.setUsername(dto.getUsername());
        }

        // 创建Example查询对象
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("username", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnoreCase()
                .withIgnoreNullValues();

        // 查询数据
        Page<UserPo> userPage = repository.findAll(Example.of(query, matcher), pageable);

        // 转换为VO列表
        List<GetUserListVo> voList = new ArrayList<>();
        for (UserPo po : userPage.getContent()) {
            GetUserListVo vo = new GetUserListVo();
            assign(po, vo);
            if (po.getCreateTime() != null) {
                vo.setCreateTime(dateFormat.format(po.getCreateTime()));
            }
            if (po.getLastLoginTime() != null) {
                vo.setLastLoginTime(dateFormat.format(po.getLastLoginTime()));
            }
            voList.add(vo);
        }

        // 返回分页视图
        return new RestPageableView<>(voList, userPage.getTotalElements());
    }

    public GetUserDetailsVo getUserDetails(long id) throws BizException{
        UserPo user = repository.findById(id).orElseThrow(() -> new BizException("用户不存在"));
        GetUserDetailsVo vo = new GetUserDetailsVo();
        assign(user, vo);
        
        if (user.getCreateTime() != null) {
            vo.setCreateTime(dateFormat.format(user.getCreateTime()));
        }
        if (user.getLastLoginTime() != null) {
            vo.setLastLoginTime(dateFormat.format(user.getLastLoginTime()));
        }

        // 获取用户组信息
        List<UserGroupVo> groupVos = new ArrayList<>();
        List<GroupPo> allGroups = groupRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
        HashSet<Long> userGroupIds = new HashSet<>();
        for (GroupPo group : user.getGroups()) {
            userGroupIds.add(group.getId());
        }

        for (GroupPo group : allGroups) {
            UserGroupVo groupVo = new UserGroupVo();
            assign(group, groupVo);
            groupVo.setHasGroup(userGroupIds.contains(group.getId()));
            groupVos.add(groupVo);
        }
        vo.setGroups(groupVos);

        // 获取用户权限信息
        List<PermissionPo> userPermissions = repository.findUserPermissions(id);
        vo.setPermissions(as(userPermissions, UserPermissionVo.class));
        
        return vo;
    }

    public void saveUser(SaveUserDto dto) throws BizException {
        // 检查用户名是否已存在
        if (StringUtils.isBlank(dto.getUsername())) {
            throw new BizException("用户名不能为空");
        }
        
        UserPo existingUserByName = repository.findByUsername(dto.getUsername());
        if (existingUserByName != null && (dto.getId() == null || !existingUserByName.getId().equals(dto.getId()))) {
            throw new BizException("用户名 '" + dto.getUsername() + "' 已被使用");
        }

        // 处理新建用户
        if (dto.getId() == null) {
            if (StringUtils.isBlank(dto.getPassword())) {
                throw new BizException("新建用户时密码不能为空");
            }
            UserPo user = new UserPo();
            assign(dto, user);
            user.setPassword(encryptPassword(dto.getPassword(), dto.getUsername()));
            user.setGroups(getGroupSet(dto.getGroupIds()));
            repository.save(user);
            return;
        }

        // 处理编辑用户
        UserPo user = repository.findById(dto.getId()).orElseThrow(() -> new BizException("用户不存在"));

        // 处理密码(dto密码非空 代表用户修改了密码)
        if (StringUtils.isNotBlank(dto.getPassword())) {
            dto.setPassword(encryptPassword(dto.getPassword(), dto.getUsername()));
        }
        if (StringUtils.isBlank(dto.getPassword())) {
            dto.setPassword(user.getPassword());
        }

        // 更新用户信息
        assign(dto, user);
        user.setGroups(getGroupSet(dto.getGroupIds()));
        repository.save(user);

        // 刷新用户的会话(如果在线)
        authService.refreshUserSession(user.getId());
    }

    public void removeUser(long id) throws BizException {
        if (!repository.existsById(id)) {
            throw new BizException("用户不存在");
        }
        repository.deleteById(id);
    }
    
    private HashSet<GroupPo> getGroupSet(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(groupRepository.findAllById(groupIds));
    }

    /**
     * 密码加密
     * @param password 密码
     * @param username 用户名（用作盐值）
     * @throws BizException 加密失败时抛出异常
     */
    private String encryptPassword(String password, String username) throws BizException {
        try {
            // 使用用户名作为盐，加密密码：password + username
            String salted = password + username;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BizException("密码加密失败", e);
        }
    }
}
