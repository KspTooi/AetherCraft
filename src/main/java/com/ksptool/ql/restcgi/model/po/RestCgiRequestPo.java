package com.ksptool.ql.restcgi.model.po;


import com.ksptool.entities.Any;
import com.ksptool.ql.biz.model.po.ChatMessagePo;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.po.UserPo;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

//@Data
//@Entity
//@Table(name = "rest_cgi_request")
public class RestCgiRequestPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "third_request_id", length = 320)
    @Comment("第三方请求ID")
    private String thirdRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("关联Thread")
    private ChatThreadPo thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("关联聊天记录")
    private ChatMessagePo message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("关联用户")
    private UserPo user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("关联玩家")
    private PlayerPo playerPo;

    @Column(name = "model_code", nullable = false, length = 128)
    @Comment("模型代码")
    private String modelCode;

    @Column(name = "model_series", nullable = false, length = 64)
    @Comment("模型系列/供应商")
    private String modelSeries;

    @Column(name = "status", nullable = false)
    @Comment("请求状态 0:发起 1:有效 2:失败")
    private Integer status;

    @Column(name = "token_input", nullable = false)
    @Comment("总TOKEN使用量(输入)")
    private Integer tokenInput;

    @Column(name = "token_output", nullable = false)
    @Comment("总TOKEN使用量(输出)")
    private Integer tokenOutput;

    @Column(name = "usage_calc_status", nullable = false)
    @Comment("使用量计算状态 0:未计算 1:已计算")
    private Integer usageCalcStatus;

    @Column(name = "create_time", nullable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "finish_time")
    @Comment("完成时间")
    private Date finishTime;

    public void setThreadId(Long threadId) {
        if(thread == null){
            thread = Any.of().val("id",threadId).as(ChatThreadPo.class);
        }
    }

    public void setMessageId(Long messageId) {
        if(message == null){
            message = Any.of().val("id",messageId).as(ChatMessagePo.class);
        }
    }

    public void setUserId(Long userId) {
        if(user == null){
            user = Any.of().val("id",userId).as(UserPo.class);
        }
    }

    public void setPlayerId(Long playerId) {
        if(playerPo == null){
            playerPo = Any.of().val("id",playerId).as(PlayerPo.class);
        }
    }

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        if(tokenInput == null){
            tokenInput = 0;
        }
        if(tokenOutput == null){
            tokenOutput = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        finishTime = new Date();
    }

}
