package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class GetApiKeyDetailsVo {

    //ID
    private Long id;

    //密钥名称
    private String keyName;

    //密钥系列
    private String keySeries;

    //密钥值(不回传给前端)
    //private String keyValue;

    //是否公开 0:私有 1:公开
    private Integer isShared;

    //使用次数
    private Long usageCount;

    //最后使用时间
    private Date lastUsedTime;

    //创建时间
    private Date createTime;

}
