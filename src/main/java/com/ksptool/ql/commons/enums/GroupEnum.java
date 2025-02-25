package com.ksptool.ql.commons.enums;
import lombok.Getter;

/**
 * 默认用户组枚举
 */
@Getter
public enum GroupEnum {

    /**
     * 管理员组
     */
    ADMIN("admin", "管理员组"),
    
    /**
     * 普通用户组
     */
    USER("user", "普通用户组"),
    
    /**
     * 访客组
     */
    GUEST("guest", "访客组");
    
    /**
     * 组标识
     */
    private final String code;
    
    /**
     * 组名称
     */
    private final String name;
    
    GroupEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据组标识获取默认组
     */
    public static GroupEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (GroupEnum group : values()) {
            if (group.getCode().equals(code)) {
                return group;
            }
        }
        return null;
    }
} 