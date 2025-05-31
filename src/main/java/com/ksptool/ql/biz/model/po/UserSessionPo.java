package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Table(name = "user_session")
@Data
public class UserSessionPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("会话ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("用户ID")
    private Long userId;

    @Column(name = "player_id")
    @Comment("当前激活的人物ID")
    private Long playerId;

    @Column(name = "player_name")
    @Comment("当前激活的人物名称")
    private String playerName;

    @Column(name = "player_avatar_url")
    @Comment("当前激活的人物头像路径")
    private String playerAvatarUrl;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    @Comment("Token")
    private String token;

    @Column(name = "permissions", nullable = false,columnDefinition = "TEXT")
    @Comment("用户权限JSON")
    private String permissions;

    @Column(name = "expires_at", nullable = false)
    @Comment("过期时间")
    private Date expiresAt;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;


    @PrePersist
    public void prePersist() {
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
}