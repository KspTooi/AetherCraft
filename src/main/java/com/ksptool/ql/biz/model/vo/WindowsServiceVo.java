package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class WindowsServiceVo {
    private String serviceName;    // 服务名称
    private String displayName;    // 显示名称
    private int state;            // 服务状态
    private String stateDesc;     // 状态描述

    public WindowsServiceVo(String serviceName, String displayName, int state) {
        this.serviceName = serviceName;
        this.displayName = displayName;
        this.state = state;
        this.stateDesc = convertStateToDesc(state);
    }

    private String convertStateToDesc(int state) {
        // 根据Windows服务状态码转换为描述文本
        return switch (state) {
            case 1 -> "已停止";
            case 2 -> "正在启动";
            case 3 -> "正在停止";
            case 4 -> "正在运行";
            case 5 -> "继续挂起";
            case 6 -> "暂停挂起";
            case 7 -> "已暂停";
            default -> "未知状态";
        };
    }
} 