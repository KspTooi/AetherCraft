package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.commons.web.PageQuery;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetPlayerListVo{

    //头像路径
    private String avatarUrl;

    //(明文)人物角色名称
    private String name;

    //钱包余额(CU)
    private BigDecimal balance;

}
