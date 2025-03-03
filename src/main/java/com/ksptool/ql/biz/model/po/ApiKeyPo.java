package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;

/**
 * API密钥实体类
 * 用于存储用户的API密钥信息
 */
@Entity
@Table(name = "api_keys")
@Data
public class ApiKeyPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("API密钥ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属用户")
    private UserPo user;

    @Column(name = "key_name", nullable = false, length = 50)
    @Comment("密钥名称")
    private String keyName;

    @Column(name = "key_type", nullable = false, length = 30)
    @Comment("密钥类型，如OpenAI、Azure等")
    private String keyType;

    @Column(name = "key_value", nullable = false, length = 500)
    @Comment("密钥值（加密存储）")
    private String keyValue;

    @Column(name = "is_shared", nullable = false)
    @Comment("是否共享：0-不共享，1-共享")
    private Integer isShared;

    @Column(name = "usage_count", nullable = false)
    @Comment("使用次数")
    private Long usageCount;

    @Column(name = "last_used_time")
    @Comment("最后使用时间")
    private Date lastUsedTime;

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
        if (isShared == null) {
            isShared = 0;
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