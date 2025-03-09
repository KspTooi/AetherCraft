package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;
import java.util.List;

/**
 * AI模型世界实体类
 * 用于管理AI模型的世界/知识库信息
 */
@Entity
@Table(name = "model_worlds")
@Data
public class ModelWorldPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("世界ID")
    private Long id;

    @Column(nullable = false)
    @Comment("用户ID")
    private Long userId;

    @Column(nullable = false, length = 100)
    @Comment("世界名称")
    private String name;

    @Column(length = 1000)
    @Comment("世界描述")
    private String description;

    @Column(name = "cover_image_path", length = 255)
    @Comment("封面图片路径")
    private String coverImagePath;

    @Column(length = 50)
    @Comment("世界标签，多个标签用逗号分隔")
    private String tags;

    @Column(nullable = false)
    @Comment("排序号")
    private Integer sortOrder;

    @Column(nullable = false)
    @Comment("状态：0-禁用，1-启用")
    private Integer status;

    @Column(name = "is_public", nullable = false)
    @Comment("是否公开：0-私有，1-公开")
    private Integer isPublic;

    @OneToMany(mappedBy = "world", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ModelWorldEntryPo> entries;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;

    @PrePersist
    public void prePersist() {
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (status == null) {
            status = 1;
        }
        if (isPublic == null) {
            isPublic = 0; // 默认私有
        }
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
} 