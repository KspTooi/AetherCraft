package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;

@Data
@Entity
@Table(name = "model_chat_history")
public class ModelChatHistoryPo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("玩家人物ID")
    private PlayerPo player;

    @Comment("关联的会话")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ModelChatThreadPo thread;

    @Comment("角色 0-用户 1-AI助手")
    @Column(nullable = false)
    private Integer role;

    @Comment("(加密)消息内容")
    @Column(columnDefinition = "TEXT")
    private String content;

    @Comment("消息序号")
    @Column(nullable = false)
    private Integer sequence;


    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }
} 