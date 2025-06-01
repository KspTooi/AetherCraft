package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "model_variant_param", indexes = {
    @Index(name = "idx_model_variant_param_unique", 
           columnList = "model_variant_id, param_key, user_id, player_id", 
           unique = true),
    @Index(name = "idx_model_variant_param_model_user", 
           columnList = "model_variant_id, user_id, player_id"),
    @Index(name = "idx_model_variant_param_seq", 
           columnList = "seq")
})
@Entity
@Data
public class ModelVariantParamPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("参数ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_variant_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属模型变体")
    private ModelVariantPo modelVariant;

    @Column(name = "param_key", nullable = false, length = 128)
    @Comment("参数键")
    private String paramKey;

    @Column(name = "param_val", length = 1000, nullable = false)
    @Comment("参数值")
    private String paramVal;

    @Column(name = "type", nullable = false)
    @Comment("参数类型 0:string 1:int 2:boolean")
    private Integer type;

    @Column(name = "description", nullable = true)
    @Comment("参数描述")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属用户，为空表示全局默认参数")
    private UserPo user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属玩家，为空表示全局默认参数")
    private PlayerPo player;

    @Column(name = "seq", nullable = false)
    @Comment("排序号")
    private Integer seq;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false)
    @Comment("创建时间")
    private Date createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    @Comment("更新时间")
    private Date updateTime;

}
