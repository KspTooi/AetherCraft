package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetModelSeriesVo {

    // 模型代码，用于API调用的唯一标识
    private String modelCode;

    // 模型名称，用户界面显示的友好名称
    private String modelName;

    // 模型系列，用于分组显示，如GPT、Claude、Gemini等
    private String series;

    // 模型规模，取值：大型 中型 小型
    private String size;

    // 模型速度，取值：极速 快速 中速 慢速 最慢
    private String speed;

    // 智能程度，取值：精英 钻石 铂金 白银 青铜 木制
    private String intelligence;

    // 思考能力，0:否 1:是，标识模型是否具备深度推理能力
    private Integer thinking;

}
