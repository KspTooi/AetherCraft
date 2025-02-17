package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.dto.ListPanelUserDto;
import com.ksptool.ql.biz.model.dto.SavePanelUserDto;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.ListPanelUserVo;
import com.ksptool.ql.biz.model.vo.SavePanelUserGroupVo;
import com.ksptool.ql.biz.model.vo.SavePanelUserPermissionVo;
import com.ksptool.ql.biz.model.vo.SavePanelUserVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
public class PanelUserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupRepository groupRepository;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 获取用户列表视图
     */
    public PageableView<ListPanelUserVo> getListView(ListPanelUserDto dto) {

        // 创建分页对象
        Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getSize(), Sort.Direction.DESC, "updateTime");

        // 创建查询条件
        UserPo query = new UserPo();
        assign(dto, query);

        // 创建Example查询对象
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("username", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnoreCase()
                .withIgnoreNullValues();

        // 查询数据
        Page<UserPo> userPage = userRepository.findAll(Example.of(query, matcher), pageable);

        // 转换为VO列表
        List<ListPanelUserVo> voList = new ArrayList<>();
        for (UserPo po : userPage.getContent()) {
            ListPanelUserVo vo = new ListPanelUserVo();
            assign(po, vo);
            vo.setCreateTime(dateFormat.format(po.getCreateTime()));
            voList.add(vo);
        }

        // 返回分页视图
        return new PageableView<>(voList, userPage.getTotalElements(), dto.getPage(), dto.getSize());
    }

    /**
     * 获取创建视图
     */
    public SavePanelUserVo getCreateView() {
        SavePanelUserVo vo = new SavePanelUserVo();
        
        // 获取所有用户组
        List<GroupPo> allGroups = groupRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
        List<SavePanelUserGroupVo> groupVos = new ArrayList<>();
        
        for (GroupPo group : allGroups) {
            SavePanelUserGroupVo groupVo = new SavePanelUserGroupVo();
            assign(group, groupVo);
            groupVo.setHasGroup(false);
            groupVos.add(groupVo);
        }
        
        vo.setGroups(groupVos);
        return vo;
    }

    /**
     * 获取编辑视图
     */
    public SavePanelUserVo getEditView(Long id) throws BizException {
        UserPo user = userRepository.findById(id).orElseThrow(() -> new BizException("用户不存在"));
        SavePanelUserVo vo = new SavePanelUserVo();
        assign(user, vo);

        // 获取所有组并标记用户所属的组
        List<GroupPo> allGroups = groupRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
        List<SavePanelUserGroupVo> groups = new ArrayList<>();
        HashSet<Long> userGroupIds = new HashSet<>();
        for (GroupPo group : user.getGroups()) {
            userGroupIds.add(group.getId());
        }

        for (GroupPo group : allGroups) {
            SavePanelUserGroupVo groupVo = new SavePanelUserGroupVo();
            assign(group, groupVo);
            groupVo.setHasGroup(userGroupIds.contains(group.getId()));
            groups.add(groupVo);
        }
        vo.setGroups(groups);

        // 获取用户的所有权限
        List<PermissionPo> userPermissions = userRepository.findUserPermissions(id);
        vo.setPermissions(as(userPermissions,SavePanelUserPermissionVo.class));
        return vo;
    }

    /**
     * 保存用户
     * @throws BizException 业务异常
     */
    public void saveUser(SavePanelUserDto dto) throws BizException {
        // 检查用户名是否已存在
        UserPo existingUserByName = userRepository.findByUsername(dto.getUsername());
        if (existingUserByName != null && (dto.getId() == null || !existingUserByName.getId().equals(dto.getId()))) {
            throw new BizException("用户名 '" + dto.getUsername() + "' 已被使用");
        }

        // 处理新建用户
        if (dto.getId() == null) {
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                throw new BizException("新建用户时密码不能为空");
            }
            UserPo user = new UserPo();
            assign(dto, user);
            dto.setPassword(encryptPassword(dto.getPassword(), dto.getUsername()));
            user.setGroups(getGroupSet(dto.getGroupIds()));
            userRepository.save(user);
            return;
        }

        // 处理编辑用户
        UserPo user = userRepository.findById(dto.getId()).orElseThrow(()->new BizException("用户不存在"));

        // 处理密码
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            dto.setPassword(user.getPassword());
        }
        if (!user.getPassword().equals(dto.getPassword())) {
            dto.setPassword(encryptPassword(dto.getPassword(), dto.getUsername()));
        }

        // 更新用户信息
        assign(dto, user);
        user.setGroups(getGroupSet(dto.getGroupIds()));
        userRepository.save(user);
    }

    private HashSet<GroupPo> getGroupSet(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(groupRepository.findAllById(groupIds));
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
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