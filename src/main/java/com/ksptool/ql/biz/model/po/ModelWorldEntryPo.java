package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * AI模型世界条目实体类
 * 用于管理AI模型世界中的知识条目/记录
 */
@Entity
@Table(name = "model_world_entries")
@Data
public class ModelWorldEntryPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("条目ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false)
    @Comment("所属世界ID")
    private ModelWorldPo world;

    @Column(nullable = false, length = 100)
    @Comment("条目标题")
    private String title;

    @Column(length = 5000)
    @Comment("条目内容")
    private String content;

    @Column(nullable = false)
    @Comment("条目类型：0-全局，1-关键字")
    private Integer type;

    @Column(length = 100)
    @Comment("关键字 多个逗号分隔")
    private String keyword;

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
        if (type == null) {
            type = 0; // 默认全局
        }
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
} 