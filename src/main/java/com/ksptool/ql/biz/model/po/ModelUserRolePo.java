package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

/**
 * 用户角色实体类
 * 用于管理用户的角色信息
 */
@Entity
@Table(name = "model_user_roles")
@Data
@DynamicUpdate
public class ModelUserRolePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("用户角色ID")
    private Long id;

    @Column(nullable = false)
    @Comment("用户ID")
    private Long userId;

    @Column(name = "avatar_path", length = 255)
    @Comment("头像路径")
    private String avatarPath;

    @Column(nullable = false, length = 50)
    @Comment("角色名称")
    private String name;

    @Column(length = 1000)
    @Comment("角色描述")
    private String description;

    @Column(nullable = false)
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "is_default", nullable = false)
    @Comment("是否默认角色：0-否，1-是")
    private Integer isDefault;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;

    @PrePersist
    public void prePersist() {
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (isDefault == null) {
            isDefault = 0; // 默认非默认角色
        }
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
} 