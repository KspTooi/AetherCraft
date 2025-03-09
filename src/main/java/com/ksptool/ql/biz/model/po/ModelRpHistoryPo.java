package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Data
@Entity
@Table(name = "model_rp_history")
public class ModelRpHistoryPo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;
    
    @Comment("会话存档ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ModelRpThreadPo thread;

    @Comment("消息内容-原始消息(展示给用户看的消息,不含系统Prompt等)")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawContent;

    @Comment("消息内容-经RpHandler处理后的消息(携带完整Prompt)")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String rpContent;

    @Comment("消息序号")
    @Column(nullable = false)
    private Integer sequence;

    @Comment("消息类型：0-用户消息，1-AI消息")
    @Column(nullable = false)
    private Integer type;
    
    @Comment("创建时间")
    @Column(nullable = false)
    private Date createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = new Date();
    }
} 