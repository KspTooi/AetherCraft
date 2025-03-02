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
@Table(name = "model_chat_templates")
@Data
public class ModelChatTemplatePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("示例ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_role_id", nullable = false)
    @Comment("关联的角色")
    private ModelRolePo modelRole;

    @Column(name = "user_message", nullable = false, length = 1000)
    @Comment("用户消息内容")
    private String userMessage;

    @Column(name = "assistant_message", nullable = false, length = 2000)
    @Comment("助手回复内容")
    private String assistantMessage;

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