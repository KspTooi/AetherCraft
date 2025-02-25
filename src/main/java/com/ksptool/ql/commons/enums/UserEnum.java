package com.ksptool.ql.commons.enums;
import lombok.Getter;

/**
 * 默认用户枚举
 */
@Getter
public enum UserEnum {

    /**
     * 管理员
     */
    ADMIN("admin", "系统管理员"),
    
    /**
     * 访客用户
     */
    GUEST("guest", "访客用户");
    
    /**
     * 用户名
     */
    private final String username;
    
    /**
     * 昵称
     */
    private final String nickname;
    
    UserEnum(String username, String nickname) {
        this.username = username;
        this.nickname = nickname;
    }
    
    /**
     * 根据用户名获取默认用户
     */
    public static UserEnum getByUsername(String username) {
        if (username == null) {
            return null;
        }
        
        for (UserEnum user : values()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
} 