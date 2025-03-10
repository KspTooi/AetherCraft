package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * AI聊天消息片段实体类
 * 用于存储AI返回但尚未被前端接收的消息片段
 */
@Data
@Entity
@Table(name = "model_rp_chat_segment")
public class ModelRpSegmentPo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;
    
    @Comment("用户ID")
    @Column(nullable = false)
    private Long userId;
    
    @Comment("关联的RP会话")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ModelRpThreadPo thread;
    
    @Comment("关联的历史记录ID")
    @Column(name = "history_id")
    private Long historyId;
    
    @Comment("片段序号，用于排序")
    @Column(nullable = false)
    private Integer sequence;
    
    @Comment("片段内容")
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Comment("片段状态 0-未读取 1-已读取")
    @Column(nullable = false)
    private Integer status;
    
    @Comment("片段类型 0-开始 1-数据 2-结束 10-错误")
    @Column(nullable = false)
    private Integer type;
    
    @Comment("创建时间")
    @Column(nullable = false)
    private Date createTime;
    
    @Comment("更新时间")
    @Column(nullable = false)
    private Date updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
        if (status == null) {
            status = 0; // 默认未读取
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }
} 