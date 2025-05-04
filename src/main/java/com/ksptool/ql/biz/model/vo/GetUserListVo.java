package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetUserListVo {
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
     * 创建时间
     */
    private String createTime;
    
    /**
     * 最后登录时间
     */
    private String lastLoginTime;
    
    /**
     * 用户状态 0:正常 1:封禁
     */
    private Integer status;
}
