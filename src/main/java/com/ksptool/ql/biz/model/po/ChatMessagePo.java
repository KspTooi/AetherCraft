package com.ksptool.ql.biz.model.po;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Data
@Entity
@Table(name = "chat_message")
public class ChatMessagePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("会话ThreadID")
    private ChatThreadPo thread;

    @Column(name = "sender_role", nullable = false)
    @Comment("发送人角色 0:Player 1:Model")
    private Integer senderRole;

    @Column(name = "sender_name", nullable = false)
    @Comment("发送人名称")
    private String senderName;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    @Comment("(加密)消息内容")
    private String content;

    @Column(name = "seq", nullable = false)
    @Comment("消息序号")
    private Integer seq;

    @Column(name = "token_input", nullable = false)
    @Comment("TOKEN使用量(用户输入)")
    private Long tokenInput;

    @Column(name = "token_output", nullable = false)
    @Comment("TOKEN使用量(模型输出)")
    private Long tokenOutput;

    @Column(name = "token_thoughts", nullable = false)
    @Comment("TOKEN使用量(模型思考)")
    private Long tokenThoughts;

    @Column(name = "create_time", nullable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("编辑时间")
    private Date updateTime;


    @PrePersist
    protected void onCreate() {
        createTime = new Date();

        if(tokenInput == null){
            tokenInput = 0L;
        }
        if(tokenOutput == null){
            tokenOutput = 0L;
        }
        if(tokenThoughts == null){
            tokenThoughts = 0L;
        }

    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }

}
