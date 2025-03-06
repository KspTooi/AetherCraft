package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;

/**
 * API密钥授权实体类
 * 用于存储API密钥的授权信息，实现用户可以共享他们的APIKEY给其他用户使用但不能查看
 */
@Entity
@Table(name = "api_key_authorizations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"api_key_id", "authorized_user_id"}))
@Data
public class ApiKeyAuthorizationPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("授权ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("被授权的API密钥")
    private ApiKeyPo apiKey;

    @Column(name = "authorizer_user_id", nullable = false)
    @Comment("授权者用户ID")
    private Long authorizerUserId;

    @Column(name = "authorized_user_id", nullable = false)
    @Comment("被授权的用户ID")
    private Long authorizedUserId;

    @Column(name = "usage_limit")
    @Comment("使用次数限制，null表示无限制")
    private Long usageLimit;

    @Column(name = "usage_count", nullable = false)
    @Comment("已使用次数")
    private Long usageCount;

    @Column(name = "expire_time")
    @Comment("过期时间，null表示永不过期")
    private Date expireTime;

    @Column(nullable = false)
    @Comment("状态：0-禁用，1-启用")
    private Integer status;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = 1;
        }
        if (usageCount == null) {
            usageCount = 0L;
        }
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
} 