package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Index;

import java.util.Date;

/**
 * 应用实例实体类
 * 记录用户启动的应用程序实例信息和运行状态
 */
@Data
@Entity
@Table(name = "app_instance", indexes = {
    @jakarta.persistence.Index(name = "idx_user_app", columnList = "userId,appId"),
    @jakarta.persistence.Index(name = "idx_pid", columnList = "pid"),
    @jakarta.persistence.Index(name = "idx_status", columnList = "status"),
    @jakarta.persistence.Index(name = "idx_create_time", columnList = "createTime")
})
public class AppInstancePo {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     * 关联 user 表的主键
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 应用ID
     * 关联 app 表的主键
     */
    @Column(nullable = false)
    private Long appId;

    /**
     * 进程ID
     * Windows系统中的进程标识符
     */
    @Column(nullable = false)
    private Long pid;

    /**
     * 应用启动时间
     */
    @Column(nullable = false)
    private Date startTime;

    /**
     * 应用运行状态
     * 0: 运行中
     * 1: 已终止
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 应用结束时间
     * 仅在应用终止时有值
     */
    @Column(nullable = true)
    private Date endTime;

    /**
     * 应用退出码
     * 仅在应用终止时有值
     * 0 表示正常退出，其他值表示异常退出
     */
    @Column(nullable = true)
    private Integer exitCode;

    /**
     * 记录创建时间
     */
    @Column(nullable = false)
    private Date createTime;

    /**
     * 记录更新时间
     */
    @Column(nullable = false)
    private Date updateTime;
} 