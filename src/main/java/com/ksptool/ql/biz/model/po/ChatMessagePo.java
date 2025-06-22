package com.ksptool.ql.biz.model.po;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Data
@Entity
@Table(name = "chat_message", indexes = {
    @Index(name = "idx_chat_message_thread_id", columnList = "thread_id"),
    @Index(name = "idx_chat_message_thread_seq", columnList = "thread_id, seq"),
    @Index(name = "idx_chat_message_thread_role_seq", columnList = "thread_id, sender_role, seq DESC"),
    @Index(name = "idx_chat_message_seq_desc", columnList = "seq DESC"),
    @Index(name = "idx_chat_message_create_time", columnList = "create_time")
})
public class ChatMessagePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("会话ThreadID")
    private ChatThreadPo thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属用户ID")
    private UserPo user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属玩家ID")
    private PlayerPo player;

    @Column(name = "sender_role", nullable = false)
    @Comment("发送人角色 0:Player 1:Model")
    private Integer senderRole;

    @Column(name = "sender_name", nullable = false)
    @Comment("发送人名称")
    private String senderName;

    @Column(name = "model_code")
    @Comment("模型编码")
    private String modelCode;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    @Comment("(加密)消息内容")
    private String content;

    @Column(name = "content_thoughts", columnDefinition = "TEXT")
    @Comment("(加密)思考内容")
    private String contentThoughts;

    @Column(name = "seq", nullable = false)
    @Comment("消息序号")
    private Integer seq;

    @Column(name = "cost_token_input")
    @Comment("消息TOKEN使用总量(输入)")
    private Integer costTokenInput;

    @Column(name = "cost_token_output")
    @Comment("消息TOKEN使用总量(输出)")
    private Integer costTokenOutput;

    @Column(name = "token_input_text", nullable = false)
    @Comment("消息TOKEN使用量(输入文本)")
    private Integer tokenInputText;

    @Column(name = "token_input_image", nullable = false)
    @Comment("消息TOKEN使用量(输入图片)")
    private Integer tokenInputImage;

    @Column(name = "token_input_audio", nullable = false)
    @Comment("消息TOKEN使用量(输入音频)")
    private Integer tokenInputAudio;

    @Column(name = "token_output_text", nullable = false)
    @Comment("消息TOKEN使用量(输出文本)")
    private Integer tokenOutputText;

    @Column(name = "token_output_image", nullable = false)
    @Comment("消息TOKEN使用量(输出图片)")
    private Integer tokenOutputImage;

    @Column(name = "token_output_audio", nullable = false)
    @Comment("消息TOKEN使用量(输出音频)")
    private Integer tokenOutputAudio;

    @Column(name = "token_output_thoughts", nullable = false)
    @Comment("消息TOKEN使用量(输出思考)")
    private Integer tokenOutputThoughts;

    @Column(name = "create_time", nullable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("编辑时间")
    private Date updateTime;


    @PrePersist
    protected void onCreate() {
        if(createTime == null){
            createTime = new Date();
        }
        if(updateTime == null){
            updateTime = new Date();
        }
        if(costTokenInput == null){
            costTokenInput = 0;
        }
        if(costTokenOutput == null){
            costTokenOutput = 0;
        }
        if(tokenInputText == null){
            tokenInputText = 0;
        }
        if(tokenInputImage == null){
            tokenInputImage = 0;
        }
        if(tokenInputAudio == null){
            tokenInputAudio = 0;
        }
        if(tokenOutputText == null){
            tokenOutputText = 0;
        }
        if(tokenOutputImage == null){ 
            tokenOutputImage = 0;
        }
        if(tokenOutputAudio == null){
            tokenOutputAudio = 0;
        }
        if(tokenOutputThoughts == null){
            tokenOutputThoughts = 0;
        }  
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }


    @Override
    public String toString() {
        return "ChatMessagePo{" +
                "id=" + id +
                '}';
    }
}
