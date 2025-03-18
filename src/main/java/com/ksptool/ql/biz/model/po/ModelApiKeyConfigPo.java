package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;

/**
 * 模型API密钥配置实体类
 * 用于配置模型与API密钥的关联关系，指定调用特定模型时使用的API密钥
 */
@Entity
@Table(name = "model_api_key_configs")
@Data
public class ModelApiKeyConfigPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("配置ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("用户ID")
    private Long userId;

    @Column(name = "model_code", nullable = false, length = 50)
    @Comment("模型代码")
    private String modelCode;

    @Column(name = "api_key_id", nullable = false)
    @Comment("关联的API密钥ID")
    private Long apiKeyId;

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