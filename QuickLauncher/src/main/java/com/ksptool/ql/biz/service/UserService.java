package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}
