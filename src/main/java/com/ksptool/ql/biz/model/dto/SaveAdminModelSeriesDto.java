package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveAdminModelSeriesDto {

    // 模型ID，新增时为null，编辑时必填
    private Long id;

    // 模型代码 (PO中nullable = false)
    @NotBlank(message = "模型代码不能为空")
    private String code;

    // 模型名称 (PO中nullable = false)
    @NotBlank(message = "模型名称不能为空")
    private String name;

    // 模型系列、厂商 (PO中nullable = false)
    @NotBlank(message = "模型系列不能为空")
    private String series;

    // 思考能力 0:无 1:有 (PO中可为空)
    private Integer thinking;

    // 规模 0:小型 1:中型 2:大型 (PO中可为空)
    private Integer scale;

    // 速度 0:慢速 1:中速 2:快速 3:极快 (PO中可为空)
    private Integer speed;

    // 智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子 (PO中可为空)
    private Integer intelligence;

    // 是否启用 0:禁用 1:启用 (PO中nullable = false)
    @NotNull(message = "启用状态不能为空")
    private Integer enabled;

    // 排序号 (可为空，后端会自动设置)
    private Integer seq;

} 