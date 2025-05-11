package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;
import java.util.List;

@Data
@Entity
// 一个模型角色只能有一个激活的对话
// 注意：需要在数据库层面添加部分唯一索引 WHERE active = 1
@Table(name = "npc_chat_thread",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"npc_id", "active"}, name = "uk_npc_thread_active")
})
public class NpcChatThreadPo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("玩家人物ID 为空表示全局配置")
    private PlayerPo player;
    
    @Comment("(加密)存档名称")
    @Column(length = 100)
    private String title;

    @Comment("(加密)存档描述")
    @Column(length = 500)
    private String description;
    
    @Comment("AI模型代码")
    @Column(length = 50, nullable = false)
    private String modelCode;

    @Comment("NPC角色")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private NpcPo npc;
    
    @Comment("是否为当前激活的对话 0-存档 1-激活")
    @Column(nullable = false)
    private Integer active;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NpcChatHistoryPo> histories;
    
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
        if (active == null) {
            active = 1; // 默认为激活状态
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        //updateTime = new Date();
    }
} 