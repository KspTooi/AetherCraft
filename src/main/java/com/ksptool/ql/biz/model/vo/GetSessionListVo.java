package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class GetSessionListVo {

    public GetSessionListVo(Long id, String username, String playerName, Date createTime, Date expiresAt) {
        this.id = id;
        this.username = username;
        this.playerName = playerName;
        this.createTime = createTime;
        this.expiresAt = expiresAt;
    }

    //会话ID
    private Long id;

    //用户名
    private String username;

    //当前登录人物名
    private String playerName;

    //权限节点数量
    private Integer permissionCount;

    //登入时间
    private Date createTime;

    //过期时间
    private Date expiresAt;

}
