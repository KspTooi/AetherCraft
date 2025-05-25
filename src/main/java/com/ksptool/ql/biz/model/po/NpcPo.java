package com.ksptool.ql.biz.model.po;

import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.AuthException;
import jakarta.persistence.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Comment;
import java.util.Date;

/**
 * AI模型角色实体类
 * 用于管理AI模型的角色信息和对话设置
 */
@Entity
@Table(name = "npc", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "name"}, name = "uk_player_role_name")
})
@Data
public class NpcPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("NPC ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("玩家人物ID 为空表示全局配置")
    private PlayerPo player;

    @Column(nullable = false, length = 50)
    @Comment("(明文)NPC名称")
    private String name;
    
    @Column(name = "avatar_url", length = 255)
    @Comment("(加密)头像路径")
    private String avatarUrl;

    @Column(length = 50000)
    @Comment("(加密)NPC描述")
    private String description;

    @Column(name = "role_summary", length = 50000)
    @Comment("(加密)NPC角色设定摘要")
    private String roleSummary;

    @Column(name = "scenario", length = 50000)
    @Comment("(加密)情景")
    private String scenario;

    @Column(name = "first_message", length = 50000)
    @Comment("(加密)首次对话内容")
    private String firstMessage;

    @Column(name = "tags", length = 50)
    @Comment("(加密)NPC标签，多个标签用逗号分隔")
    private String tags;

    @Column(name = "seq", nullable = false)
    @Comment("排序号")
    private Integer seq;

    @Column(name = "status", nullable = false)
    @Comment("状态：0-禁用，1-启用")
    private Integer status;

    @Column(name = "active", nullable = false)
    @Comment("当前是否激活 0:否 1:是")
    private Integer active;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    @Comment("修改时间")
    private Date updateTime;

    @PrePersist
    public void prePersist() {
        if (seq == null) {
            seq = 0;
        }
        if (status == null) {
            status = 1;
        }
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }

    public String getAvatarUrlPt(ContentSecurityService css) {

        String urlPt = css.decryptForCurUser(avatarUrl);

        if(StringUtils.isBlank(urlPt)){
            return "";
        }

        return "/res/"+urlPt;
    }


} 