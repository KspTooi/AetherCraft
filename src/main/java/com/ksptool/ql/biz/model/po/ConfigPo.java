package com.ksptool.ql.biz.model.po;

import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.Comment;
import java.util.Date;

@Data
@Entity
@Table(name = "config", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "config_key"}, name = "uk_player_config")
})
public class ConfigPo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("玩家人物ID 为空表示全局配置")
    private PlayerPo player;

    @Column(name = "config_key", nullable = false, length = 100)
    @Comment("配置键")
    private String configKey;

    @Column(name = "config_value", length = 500)
    @Comment("配置值")
    private String configValue;

    @Column(name = "description", length = 200)
    @Comment("配置描述")
    private String description;

    @Column(name = "create_time",nullable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time",nullable = false)
    @Comment("更新时间")
    private Date updateTime;

    @PrePersist
    public void prePersist() {
        createTime = new Date();
        updateTime = new Date();
    }
} 