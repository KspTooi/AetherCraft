package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetModelSeriesVo {

    //模型代码
    private String modelCode;

    //模型名称(显示)
    private String modelName;

    //模型系列
    private String series;

}
