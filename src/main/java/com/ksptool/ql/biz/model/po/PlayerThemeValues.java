package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Table(name = "player_theme_values")
@Data
public class PlayerThemeValues {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主题值ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属主题")
    private PlayerThemePo theme;

    @Column(name = "theme_key", length = 50, nullable = false)
    @Comment("设置项键名")
    private String themeKey;

    @Column(name = "theme_value", length = 100, nullable = false)
    @Comment("设置项值")
    private String themeValue;

    @Column(name = "display_name", length = 100)
    @Comment("设置项显示名称")
    private String displayName;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;
    
    @PrePersist
    public void prePersist() {
        createTime = new Date();
        updateTime = new Date();
    }
    
    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
}
