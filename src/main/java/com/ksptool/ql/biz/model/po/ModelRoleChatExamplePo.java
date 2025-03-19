package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;

/**
 * AI模型对话示例实体类
 * 用于存储角色的对话示例，帮助AI理解角色特性
 */
@Entity
@Table(name = "model_role_chat_example")
@Data
public class ModelRoleChatExamplePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("示例ID")
    private Long id;

    @Column(name = "model_role_id", nullable = false)
    @Comment("关联的角色ID")
    private Long modelRoleId;

    @Column(name = "content", nullable = false, length = 3000)
    @Comment("(加密)对话内容")
    private String content;

    @Column(nullable = false)
    @Comment("排序号")
    private Integer sortOrder;

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
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
}