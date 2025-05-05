package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
@Data
public class UserPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("用户ID")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @Comment("用户名")
    private String username;

    @Column(nullable = false, length = 100)
    @Comment("密码")
    private String password;

    @Column(length = 100)
    @Comment("邮箱")
    private String email;

    @Column(length = 50)
    @Comment("昵称")
    private String nickname;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;

    @Column(name = "login_count", nullable = false)
    @Comment("登录次数")
    private Integer loginCount;

    @Column(name = "status", nullable = false)
    @Comment("用户状态 0:正常 1:封禁")
    private Integer status;

    @Column(name = "encrypted_dek", length = 512)
    @Comment("用户数据加密密钥(!已加密)")
    private String encryptedDek;

    @Column(name = "last_login_time")
    @Comment("最后登录时间")
    private Date lastLoginTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_group",
        joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
        inverseJoinColumns = @JoinColumn(name = "group_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Comment("用户所属的用户组")
    private Set<GroupPo> groups = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Comment("用户主题列表")
    private List<UserThemePo> themes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createTime = new Date();
        updateTime = new Date();
        if (loginCount == null) {
            loginCount = 0;
        }
        if (status == null) {
            status = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
}