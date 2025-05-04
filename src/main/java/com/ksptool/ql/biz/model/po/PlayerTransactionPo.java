package com.ksptool.ql.biz.model.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "player_transaction", indexes = {
        @Index(name = "idx_ptrans_player_time", columnList = "player_id, create_time"),
        @Index(name = "idx_ptrans_group_id", columnList = "transaction_group_id"),
        @Index(name = "idx_ptrans_uuid", columnList = "transaction_uuid", unique = true)
})
public class PlayerTransactionPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID 交易流水号")
    private Long id;

    @Column(name = "transaction_uuid", nullable = false, length = 36)
    @Comment("交易记录唯一标识符 (UUID用于幂等性校验)")
    private String transactionUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属用户ID")
    private UserPo user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id",nullable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属玩家ID")
    private PlayerPo player;

    @Column(name = "type", nullable = false)
    @Comment("交易类型")
    private Integer type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    @Comment("交易金额 正数为收入,负数为支出")
    private BigDecimal amount;

    @Column(name = "description",length = 128)
    @Comment("交易备注")
    private String description;

    @Column(name = "balance_before", precision = 19, scale = 4,nullable = false)
    @Comment("本次交易前的账户余额快照")
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 19, scale = 4,nullable = false)
    @Comment("本次交易后的账户余额快照")
    private BigDecimal balanceAfter;

    @Column(name = "transaction_group_id", nullable = false, length = 36)
    @Comment("交易组唯一标识符 (UUID) 同一笔原子交易产生的多条记录UUID需一致")
    private String transactionGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rel_user_id",foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("对手用户ID")
    private UserPo relUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rel_player_id",foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("对手玩家ID")
    private PlayerPo relPlayerId;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间(交易发生的时间)")
    private Date createTime;

}
