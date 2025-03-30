package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user_theme")
@Data
public class UserTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主题ID")
    private Long id;

    @Column(nullable = false)
    @Comment("用户ID")
    private Long userId;

    @Column(length = 50, nullable = false)
    @Comment("主题名称")
    private String themeName;

    @Column(length = 255)
    @Comment("主题描述")
    private String description;

    @Column(nullable = false)
    @Comment("是否为默认主题（0-否，1-是）")
    private Integer isActive = 0;

    @Column(nullable = false)
    @Comment("是否为系统预设主题（0-否，1-是）")
    private Integer isSystem = 0;
    
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("主题配置值列表")
    private List<UserThemeValues> themeValues;

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
