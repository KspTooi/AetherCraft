package com.ksptool.ql.biz.model.dto;

import lombok.Data;

@Data
public class ListPanelAppDto {
    // 页码
    private int page = 1;
    // 每页大小
    private int pageSize = 10;
    // 应用名称或快捷命令
    private String nameOrCommand;
    // 描述
    private String description;
} 