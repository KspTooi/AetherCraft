package com.ksptool.ql.biz.user.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class GetUserDetailsVo {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户邮箱
     */
    private String email;
    
    /**
     * 用户状态
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 最后登录时间
     */
    private String lastLoginTime;
    
    /**
     * 用户组列表
     */
    private List<UserGroupVo> groups;
    
    /**
     * 用户权限列表
     */
    private List<UserPermissionVo> permissions;
}
