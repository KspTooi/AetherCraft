package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;
import java.util.List;

/**
 * AI模型角色实体类
 * 用于管理AI模型的角色信息和对话设置
 */
@Entity
@Table(name = "model_roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"}, name = "uk_user_role_name")
})
@Data
public class ModelRolePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("角色ID")
    private Long id;

    @Column(nullable = false)
    @Comment("用户ID")
    private Long userId;

    @Column(nullable = false, length = 50)
    @Comment("(明文)角色名称")
    private String name;
    
    @Column(name = "avatar_path", length = 255)
    @Comment("(加密)头像路径")
    private String avatarPath;

    @Column(length = 50000)
    @Comment("(加密)角色描述")
    private String description;

    @Column(name = "role_summary", length = 50000)
    @Comment("(加密)角色设定摘要")
    private String roleSummary;

    @Column(name = "scenario", length = 50000)
    @Comment("(加密)情景")
    private String scenario;

    @Column(name = "first_message", length = 50000)
    @Comment("(加密)首次对话内容")
    private String firstMessage;

    @Column(length = 50)
    @Comment("(加密)角色标签，多个标签用逗号分隔")
    private String tags;

    @Column(nullable = false)
    @Comment("排序号")
    private Integer sortOrder;

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
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (status == null) {
            status = 1;
        }
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
} 