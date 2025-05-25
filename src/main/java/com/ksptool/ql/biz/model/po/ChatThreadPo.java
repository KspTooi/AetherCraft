package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "chat_thread")
public class ChatThreadPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "type",nullable = false)
    @Comment("Thread类型 0:标准会话 1:RP会话 2:标准增强会话")
    private Integer type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("Thread拥有方用户")
    private UserPo user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("Thread拥有方")
    private PlayerPo player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("聊天NPC 当type=2时必填")
    private NpcPo npc;

    @Column(name = "title",nullable = false, length = 240)
    @Comment("(明文)会话标题")
    private String title;

    @Column(name = "public_info", length = 1024)
    @Comment("(明文)会话公开信息")
    private String publicInfo;

    @Column(name = "description", length = 3750)
    @Comment("(加密)会话描述")
    private String description;

    @Column(name = "title_generated", nullable = false)
    @Comment("是否已生成过标题 0-未生成 1-已生成")
    private Integer titleGenerated;

    @Column(name = "model_code", length = 50, nullable = false)
    @Comment("AI模型代码")
    private String modelCode;

    @Column(name = "active", nullable = false)
    @Comment("是否为当前激活的对话 -1:旁路 0:存档 1:激活 type为1时需处理")
    private Integer active;

    @Column(name = "token_input", nullable = false)
    @Comment("总TOKEN使用量(用户输入)")
    private Long tokenInput;

    @Column(name = "token_output", nullable = false)
    @Comment("总TOKEN使用量(模型输出)")
    private Long tokenOutput;

    @Column(name = "token_thoughts", nullable = false)
    @Comment("总TOKEN使用量(模型思考)")
    private Long tokenThoughts;

    @Column(name = "coin_usage",nullable = false)
    @Comment("预估价值(以CU记)")
    private BigDecimal coinUsage;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("seq ASC")
    @Comment("消息列表")
    private List<ChatMessagePo> messages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("最后一条消息ID")
    private ChatMessagePo lastMessage;

    @Comment("创建时间")
    @Column(name = "create_time", nullable = false)
    private Date createTime;

    @Comment("更新时间")
    @Column(name = "update_time", nullable = false)
    private Date updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
        if(tokenInput == null){
            tokenInput = 0L;
        }
        if(tokenOutput == null){
            tokenOutput = 0L;
        }
        if(tokenThoughts == null){
            tokenThoughts = 0L;
        }
        if(coinUsage == null){
            coinUsage = BigDecimal.ZERO;
        }
        if(titleGenerated == null){
            titleGenerated = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }

    @Override
    public String toString() {
        return "ChatThreadPo{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", publicInfo='" + publicInfo + '\'' +
                ", modelCode='" + modelCode + '\'' +
                ", active=" + active +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

}
