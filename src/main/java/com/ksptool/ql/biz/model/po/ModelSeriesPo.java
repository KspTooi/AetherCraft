package com.ksptool.ql.biz.model.po;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Table(name = "model_series")
@Data
public class ModelSeriesPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("模型ID")
    private Long id;

    @Column(name = "code", nullable = false, length = 128,unique = true)
    @Comment("模型代码")
    private String code;

    @Column(name = "name",nullable = false, length = 128)
    @Comment("模型名称")
    private String name;

    @Column(name = "series",nullable = false,length = 128)
    @Comment("模型系列、厂商")
    private String series;

    @Column(name = "thinking")
    @Comment("思考能力 0:无 1:有")
    private Integer thinking;

    @Column(name = "scale")
    @Comment("规模 0:小型 1:中型 2:大型")
    private Integer scale;

    @Column(name = "speed")
    @Comment("速度 0:慢速 1:中速 2:快速 3:极快")
    private Integer speed;

    @Column(name = "intelligence")
    @Comment("智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子")
    private Integer intelligence;

    @Column(name = "enabled", nullable = false)
    @Comment("是否启用 0:禁用 1:启用")
    private Integer enabled;

    @Column(name = "create_time",nullable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time",nullable = false)
    @Comment("更新时间")
    private Date updateTime;

}
