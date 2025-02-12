package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.PanelUserVo;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

@Service
public class PanelUserService {

    @Autowired
    private UserRepository userRepository;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取用户列表
     */
    public Page<PanelUserVo> getUserList(Pageable pageable) {
        Page<UserPo> userPoPage = userRepository.findAll(pageable);
        return userPoPage.map(this::convertToVo);
    }

    /**
     * 保存用户
     * @throws BizException 业务异常
     */
    public void saveUser(UserPo user) throws BizException {
        // 如果是新用户或密码有更新，则加密密码
        if (user.getId() == null || user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(encryptPassword(user.getPassword(), user.getUsername()));
        } else {
            // 如果是编辑用户且密码为空，则保持原密码不变
            UserPo existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new BizException("用户不存在"));
            user.setPassword(existingUser.getPassword());
        }
        userRepository.save(user);
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

    /**
     * 获取用户信息（VO）
     * @throws BizException 用户不存在时抛出异常
     */
    public PanelUserVo getUser(Long id) throws BizException {
        UserPo userPo = getUserPo(id);
        return convertToVo(userPo);
    }

    /**
     * 获取用户信息（PO）
     * @throws BizException 用户不存在时抛出异常
     */
    public UserPo getUserPo(Long id) throws BizException {
        return userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));
    }

    /**
     * 将UserPo转换为PanelUserVo
     */
    private PanelUserVo convertToVo(UserPo po) {
        if (po == null) {
            return null;
        }
        PanelUserVo vo = new PanelUserVo();
        vo.setId(po.getId());
        vo.setUsername(po.getUsername());
        vo.setCreateTime(dateFormat.format(po.getCreateTime()));
        return vo;
    }
} 