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
@Table(name = "npc_chat_example")
@Data
public class NpcChatExamplePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("示例ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("模型角色ID")
    private NpcPo npc;

    @Column(name = "content", nullable = false, length = 3000)
    @Comment("(加密)对话内容")
    private String content;

    @Column(name = "sort_order", nullable = false)
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