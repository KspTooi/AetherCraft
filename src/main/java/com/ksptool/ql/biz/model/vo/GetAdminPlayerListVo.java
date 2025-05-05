package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.biz.model.po.UserPo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GetAdminPlayerListVo {

    //主键ID
    private Long id;

    //人物名称
    private String name;

    //所有者
    private String username;

    //余额
    private BigDecimal balance;

    //状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除
    private Integer status;

    //诞生日期
    private Date createTime;
}
