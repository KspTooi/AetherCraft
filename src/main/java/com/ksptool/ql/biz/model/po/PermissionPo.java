package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.Set;

/**
 * 权限实体类
 * 用于管理系统中的权限节点信息
 * 采用平级设计，权限标识使用 : 分隔表示层级
 * 例如：system:user:view, system:user:edit
 */
@Entity
@Table(name = "permissions")
@Data
public class PermissionPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("权限ID")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @Comment("权限标识，如：system:user:view")
    private String code;

    @Column(nullable = false, length = 50)
    @Comment("权限名称，如：查看用户")
    private String name;

    @Column(length = 200)
    @Comment("权限描述")
    private String description;

    @Column(nullable = false)
    @Comment("排序号")
    private Integer sortOrder;

    @Column(nullable = false)
    @Comment("是否系统权限 0:非系统权限 1:系统权限")
    private Integer isSystem = 0;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Comment("拥有此权限的用户组")
    private Set<GroupPo> groups;

    @PrePersist
    public void prePersist() {
        if (sortOrder == null) {
            sortOrder = 0;
        }
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
} 