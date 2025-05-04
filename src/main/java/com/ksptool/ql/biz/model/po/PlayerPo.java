package com.ksptool.ql.biz.model.po;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

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

    @Column(name = "last_active_time", nullable = false)
    @Comment("最后激活时间")
    private Date lastActiveTime;

}
