package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.util.Date;

/**
 * API密钥使用日志实体类
 * 用于记录API密钥的使用情况，便于审计和统计
 */
@Entity
@Table(name = "api_key_usage_logs")
@Data
public class ApiKeyUsageLogPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("日志ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id", nullable = false)
    @Comment("使用的API密钥")
    private ApiKeyPo apiKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("使用者")
    private UserPo user;

    @Column(name = "model_name", length = 50)
    @Comment("使用的模型名称")
    private String modelName;

    @Column(name = "request_type", length = 30)
    @Comment("请求类型，如chat、completion等")
    private String requestType;

    @Column(name = "token_count")
    @Comment("消耗的token数量")
    private Integer tokenCount;

    @Column(name = "request_content", length = 1000)
    @Comment("请求内容摘要")
    private String requestContent;

    @Column(name = "response_status", length = 20)
    @Comment("响应状态，如success、failed等")
    private String responseStatus;

    @Column(name = "error_message", length = 500)
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "request_time", nullable = false)
    @Comment("请求时间")
    private Date requestTime;

    @Column(name = "response_time")
    @Comment("响应时间")
    private Date responseTime;

    @Column(name = "duration")
    @Comment("请求持续时间（毫秒）")
    private Long duration;

    @Column(name = "ip_address", length = 50)
    @Comment("请求IP地址")
    private String ipAddress;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @PrePersist
    public void prePersist() {
        createTime = new Date();
        if (requestTime == null) {
            requestTime = new Date();
        }
    }
} 