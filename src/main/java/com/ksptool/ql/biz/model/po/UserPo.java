package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

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