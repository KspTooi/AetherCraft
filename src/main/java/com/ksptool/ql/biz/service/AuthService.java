package com.ksptool.ql.biz.service;

import com.google.gson.Gson;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.mapper.UserSessionRepository;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.po.UserSessionPo;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.vo.UserSessionVo;
import com.ksptool.ql.commons.WebUtils;
import com.ksptool.ql.commons.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class AuthService {

    private static final String SESSION_ATTRIBUTE = "CURRENT_USER_SESSION";

    private final Gson gson = new Gson();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Value("${session.expires}")
    private long expiresInSeconds;

    /**
     * 设置当前请求的用户会话
     * @param session 用户会话信息
     */
    public static void setCurrentUserSession(UserSessionVo session) {
        RequestContextHolder.currentRequestAttributes()
            .setAttribute(SESSION_ATTRIBUTE, session, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 获取当前请求的用户会话
     * @return 用户会话信息，如果未设置则返回null
     */
    public static UserSessionVo getCurrentUserSession() {
        return (UserSessionVo) RequestContextHolder.currentRequestAttributes()
            .getAttribute(SESSION_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 获取当前用户ID
     * @return 当前用户ID，如果未登录则返回null
     */
    public static Long getCurrentUserId() {
        UserSessionVo session = getCurrentUserSession();
        return session != null ? session.getUserId() : null;
    }

    /**
     * 检查当前用户是否拥有指定权限
     * @param permission 权限标识，如：system:user:view
     * @return 如果用户拥有该权限返回true，否则返回false
     */
    public static boolean hasPermission(String permission) {

        //return true;

        UserSessionVo session = getCurrentUserSession();
        if (session == null || session.getPermissions() == null) {
            return false;
        }
        return session.getPermissions().contains(permission);
    }

    public UserPo verifyUser(HttpServletRequest hsr){

        String token = WebUtils.getCookieValue(hsr, "token");

        if (token == null) {
            return null; // Cookie中没有token
        }

        Long userId = verifyToken(token);

        if (userId == null) {
            return null; // Token无效
        }

        UserPo user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null; // 用户不存在
        }

        return user; // Token有效，返回用户实例
    }


    public String loginByPassword(String username, String password) throws BizException {
        // 根据用户名查询用户
        UserPo user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BizException("用户名或密码错误");
        }
        // 使用用户名作为盐，对密码进行加密：password + username
        String salted = password + username;
        String hashedPassword = hashSHA256(salted);
        if (!hashedPassword.equals(user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        // 登录成功，创建或返回 token
        return createUserSession(user.getId()).getToken();
    }

    public Long verifyToken(String token) {
        UserSessionPo userSession = userSessionRepository.findByToken(token);
        if (userSession == null || userSession.getExpiresAt().before(new Date())) {
            return null; // Token无效
        } else {
            return userSession.getUserId();
        }
    }

    public void destroyToken(String token) {
        userSessionRepository.deleteByToken(token);
    }

    /**
     * 根据token获取用户会话信息
     * @param token 会话token
     * @return 会话信息，如果token无效或过期则返回null
     */
    public UserSessionVo getUserSessionByToken(String token) {
        UserSessionPo session = userSessionRepository.findByToken(token);
        if (session == null || session.getExpiresAt().before(new Date())) {
            return null;
        }
        return new UserSessionVo(session);
    }

    /**
     * 从HTTP请求中获取用户会话信息
     * @param hsr HTTP请求
     * @return 会话信息，如果未登录或会话无效则返回null
     */
    public UserSessionVo getUserSessionByHSR(HttpServletRequest hsr) {
        String token = WebUtils.getCookieValue(hsr, "token");
        if (token == null) {
            return null;
        }
        return getUserSessionByToken(token);
    }

    /**
     * 用户注销，清除数据库中的 session
     * @param user 当前用户
     */
    public void logout(UserPo user) {
        // 清除用户的 session
        UserSessionPo query = new UserSessionPo();
        query.setUserId(user.getId());
        Example<UserSessionPo> example = Example.of(query);
        userSessionRepository.deleteAll(userSessionRepository.findAll(example));
    }

    public UserSessionVo createUserSession(Long userId) {
        Date now = new Date();
        // 查询当前用户是否已有会话
        UserSessionPo existingSession = userSessionRepository.findByUserId(userId);
        if (existingSession != null) {
            // 如果 token 未过期，则直接返回当前 token
            if (existingSession.getExpiresAt().after(now)) {
                return new UserSessionVo(existingSession);
            } else {
                // 已过期，删除旧记录
                userSessionRepository.delete(existingSession);
            }
        }

        // 获取用户的所有权限
        List<PermissionPo> permissions = userRepository.findUserPermissions(userId);
        Set<String> permissionCodes = new HashSet<>();
        for (PermissionPo permission : permissions) {
            permissionCodes.add(permission.getCode());
        }

        // 创建新的 token 和会话
        String token = UUID.randomUUID().toString();
        UserSessionPo newSession = new UserSessionPo();
        newSession.setUserId(userId);
        newSession.setToken(token);
        newSession.setPermissions(gson.toJson(permissionCodes));
        newSession.setExpiresAt(new Date(System.currentTimeMillis() + expiresInSeconds * 1000));
        userSessionRepository.save(newSession);
        return new UserSessionVo(newSession);
    }

    /**
     * 刷新用户会话的权限和过期时间
     * @param userId 用户ID
     * @return 更新后的会话信息，如果会话不存在或已过期则返回null
     */
    public UserSessionVo refreshUserSession(Long userId) {
        // 查询当前用户是否已有会话
        UserSessionPo existingSession = userSessionRepository.findByUserId(userId);
        if (existingSession == null) {
            return null; // 没有会话时直接返回
        }

        // 检查会话是否过期
        if (existingSession.getExpiresAt().before(new Date())) {
            return null; // 会话已过期，直接返回
        }

        // 获取用户的所有权限
        List<PermissionPo> permissions = userRepository.findUserPermissions(userId);
        Set<String> permissionCodes = new HashSet<>();
        for (PermissionPo permission : permissions) {
            permissionCodes.add(permission.getCode());
        }

        // 更新会话信息
        existingSession.setPermissions(gson.toJson(permissionCodes));
        existingSession.setExpiresAt(new Date(System.currentTimeMillis() + expiresInSeconds * 1000));
        userSessionRepository.save(existingSession);
        return new UserSessionVo(existingSession);
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