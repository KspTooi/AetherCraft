package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "model_chat_thread")
public class ModelChatThreadPo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;
    
    @Comment("用户ID")
    @Column(nullable = false)
    private Long userId;
    
    @Comment("(加密)会话标题")
    @Column(length = 100)
    private String title;
    
    @Comment("AI模型代码")
    @Column(length = 50, nullable = false)
    private String modelCode;
    
    @Comment("是否已生成过标题 0-未生成 1-已生成")
    @Column(nullable = false)
    private Integer titleGenerated;
    
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ModelChatHistoryPo> histories;
    
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
        if (titleGenerated == null) {
            titleGenerated = 0; // 默认未生成
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }
} 