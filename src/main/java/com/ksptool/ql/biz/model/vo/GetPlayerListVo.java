package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetPlayerListVo{

    //人物ID
    private Long id;

    //头像路径
    private String avatarUrl;

    //(明文)人物角色名称
    private String name;

    //钱包余额(CU)
    private BigDecimal balance;

    //状态 0:正在使用 1:不活跃 2:等待删除
    private Integer status;

    //剩余删除等待期
    private Integer remainingRemoveTime;

}
