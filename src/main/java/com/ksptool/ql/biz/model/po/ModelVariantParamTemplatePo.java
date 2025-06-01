package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "model_variant_param_template", indexes = {
    @Index(name = "idx_template_user_name_unique", 
           columnList = "user_id, name", 
           unique = true),
    @Index(name = "idx_template_player", 
           columnList = "player_id"),
    @Index(name = "idx_template_user", 
           columnList = "user_id")
})
@Entity
@Data
public class ModelVariantParamTemplatePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("字段代码")
    private Long id;

    @Column(name = "name", nullable = false, length = 128)
    @Comment("模板名称")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属玩家ID")
    private PlayerPo player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属用户ID")
    private UserPo user;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false)
    @Comment("创建时间")
    private Date createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    @Comment("更新时间")
    private Date updateTime;

} 