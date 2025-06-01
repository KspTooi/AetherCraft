package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAdminModelVariantListVo {

    // 模型ID
    private Long id;

    // 模型代码
    private String code;

    // 模型名称
    private String name;

    // 模型类型 0:文本 1:图形 2:多模态
    private Integer type;

    // 模型系列、厂商
    private String series;

    // 思考能力 0:无 1:有
    private Integer thinking;

    // 规模 0:小型 1:中型 2:大型
    private Integer scale;

    // 速度 0:慢速 1:中速 2:快速 3:极快
    private Integer speed;

    // 智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子
    private Integer intelligence;

    // 是否启用 0:禁用 1:启用
    private Integer enabled;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

}
