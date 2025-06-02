package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "model_variant_param_template_values", indexes = {
    @Index(name = "idx_template_value_param_unique", 
           columnList = "template_id, param_key", 
           unique = true),
    @Index(name = "idx_template_value_template", 
           columnList = "template_id"),
    @Index(name = "idx_template_value_seq", 
           columnList = "seq")
})
@Entity
@Data
public class ModelVariantParamTemplateValuePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("值ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("关联模板ID")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ModelVariantParamTemplatePo template;

    @Column(name = "param_key", nullable = false, length = 128)
    @Comment("参数键")
    private String paramKey;

    @Column(name = "param_val", nullable = false, length = 1000)
    @Comment("参数值")
    private String paramVal;

    @Column(name = "type", nullable = false)
    @Comment("参数类型 0:string 1:int 2:boolean")
    private Integer type;

    @Column(name = "description", nullable = true, length = 255)
    @Comment("参数说明")
    private String description;

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

    // 自定义hashCode方法，只使用id计算哈希
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // 自定义equals方法，只比较id
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ModelVariantParamTemplateValuePo that = (ModelVariantParamTemplateValuePo) obj;
        return id != null && id.equals(that.id);
    }
} 