package com.ksptool.ql.biz.model.po;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "player")
public class PlayerPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("关联用户ID")
    private UserPo user;

    @Column(name = "avatar_url", length = 320)
    @Comment("头像路径")
    private String avatarUrl;

    @Column(name = "name", nullable = false,unique = true, length = 64)
    @Comment("(明文)人物角色名称")
    private String name;

    @Column(name = "gender", nullable = true)
    @Comment("性别 0:自定义 1:男 2:女 3:不透露")
    private Integer gender;

    @Column(name = "gender_data",nullable = true)
    @Comment("(密文)自定义性别 gender为0时选填")
    private String genderData;

    @Column(length = 40000)
    @Comment("(明文)个人信息")
    private String publicInfo;

    @Column(length = 32000)
    @Comment("(密文)人物角色描述")
    private String description;

    @Column(name = "balance",nullable = false)
    @Comment("钱包余额(CU)")
    private BigDecimal balance;

    @Column(name = "language",nullable = false,length = 24)
    @Comment("语言 如 中文,English,zh-CN,en-US")
    private String language;

    @Column(name = "era",length = 32)
    @Comment("年代 如 古代,中世纪,现代,未来,赛博朋克,80S,90S,2025-01-01")
    private String era;

    @Column(name = "content_filter_level")
    @Comment("内容过滤等级 0:不过滤 1:普通 2:严格")
    private Integer contentFilterLevel;

    @Column(name = "status", nullable = false)
    @Comment("状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除")
    private Integer status;

    @Column(name = "removal_request_time")
    @Comment("移除申请提交时间(为空则未提交删除)")
    private Date removalRequestTime;

    @Column(name = "removedTime")
    @Comment("角色移除时间")
    private Date removedTime;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;

    @Column(name = "last_active_time")
    @Comment("最后激活时间")
    private Date lastActiveTime;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "player_group",
            joinColumns = @JoinColumn(name = "player_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "group_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Comment("玩家所属的访问组")
    private Set<GroupPo> groups = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        createTime = now;
        updateTime = now;
        // lastActiveTime 在 Service 中设置初始值，这里不再处理
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
        // lastActiveTime 的更新通常与具体业务操作相关，不在 PreUpdate 中处理
    }

}
