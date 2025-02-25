package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.enums.UserEnum;
import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    public UserPo getUser(String username) throws BizException {
        UserPo user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BizException("该用户不存在");
        }
        return user;
    }

    public UserPo register(String username, String password) throws BizException {
        // 检查是否已存在同名用户
        if (userRepository.findByUsername(username) != null) {
            throw new BizException("用户名已存在");
        }

        // 使用用户名作为盐，加密密码：password + username
        String salted = password + username;
        String hashedPassword = hashSHA256(salted);

        UserPo newUser = new UserPo();
        newUser.setUsername(username);
        newUser.setPassword(hashedPassword);
        // 根据需要，可设置其它字段（如邮箱、昵称等）

        return userRepository.save(newUser);
    }

    private String hashSHA256(String input) throws BizException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BizException("密码加密失败", e);
        }
    }

    /**
     * 校验系统内置用户
     * 检查数据库中是否存在所有系统内置用户，如果不存在则自动创建
     * 对于admin用户，会赋予所有现有用户组
     * @return 校验结果消息
     */
    @Transactional(rollbackFor = Exception.class)
    public String validateSystemUsers() {
        // 获取所有系统内置用户枚举
        UserEnum[] userEnums = UserEnum.values();
        
        // 记录已存在和新增的用户数量
        int existCount = 0;
        int addedCount = 0;
        List<String> addedUsers = new ArrayList<>();
        
        // 遍历所有系统内置用户
        for (UserEnum userEnum : userEnums) {
            String username = userEnum.getUsername();
            
            // 检查用户是否已存在
            UserPo existingUser = userRepository.findByUsername(username);
            if (existingUser != null) {
                existCount++;
                // 如果是admin用户，更新其用户组
                if (userEnum == UserEnum.ADMIN) {
                    updateAdminGroups(existingUser);
                }
                continue;
            }
            
            try {
                // 创建新用户，密码与用户名相同
                UserPo newUser = register(username, username);
                newUser.setNickname(userEnum.getNickname());
                
                // 如果是admin用户，赋予所有用户组
                if (userEnum == UserEnum.ADMIN) {
                    updateAdminGroups(newUser);
                }
                
                addedCount++;
                addedUsers.add(username);
            } catch (BizException e) {
                // 注册失败的情况已经在register方法中处理过了
                continue;
            }
        }
        
        // 返回结果消息
        if (addedCount > 0) {
            return String.format("校验完成，已添加 %d 个缺失的用户（%s），已存在 %d 个用户", 
                addedCount, String.join("、", addedUsers), existCount);
        }
        
        return String.format("校验完成，所有 %d 个系统用户均已存在", existCount);
    }

    /**
     * 更新admin用户的用户组，确保拥有所有现有用户组
     */
    private void updateAdminGroups(UserPo adminUser) {
        // 获取所有用户组
        List<GroupPo> allGroups = groupRepository.findAll();
        
        // 更新admin的用户组
        adminUser.getGroups().clear();
        adminUser.getGroups().addAll(allGroups);
        
        // 保存更改
        userRepository.save(adminUser);
    }
}
