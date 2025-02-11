package com.ksptool.ql.biz.model.po;

import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.Comment;
import java.util.Date;

@Data
@Entity
@Table(name = "config")
public class ConfigPo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(nullable = false)
    @Comment("用户ID，-1表示全局配置")
    private Long userId;

    @Column(name = "config_key", nullable = false, length = 100)
    @Comment("配置键")
    private String configKey;

    @Column(name = "config_value", length = 500)
    @Comment("配置值")
    private String configValue;

    @Column(name = "description", length = 200)
    @Comment("配置描述")
    private String description;

    @Column(name = "create_time")
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time")
    @Comment("更新时间")
    private Date updateTime;
} 