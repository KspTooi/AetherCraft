package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetThemeSettingsVo {

    // 品牌颜色 (e.g., "rgba(75, 227, 238, 0.62)")
    private String brandColor;

    // 主要主题颜色 (e.g., "rgba(135, 206, 250, 0.7)")
    private String primaryColor;

    // 消息悬浮背景色 - 用户 (e.g., "rgba(61, 138, 168, 0.12)")
    private String messageHoverUser;

    // 消息悬浮背景色 - 模型 (e.g., "rgba(61, 138, 168, 0.12)")
    private String messageHoverModel;

    // 激活状态的颜色 (e.g., "rgba(94, 203, 245, 0.85)")
    private String activeColor;

    // hover状态的颜色 (e.g., "rgba(135, 206, 250, 0.2)")
    private String primaryHover;

    // 按钮背景颜色 (e.g., "rgba(61, 138, 168, 0.24)")
    private String primaryButton;

    // 按钮边框颜色 (e.g., "rgba(135, 206, 250, 0.7)")
    private String primaryButtonBorder;

    // textarea背景颜色 (e.g., "rgba(2, 98, 136, 0.1)")
    private String textareaColor;

    // textarea激活颜色 (e.g., "rgba(61, 138, 168, 0.18)")
    private String textareaActive;

    // textarea边框颜色 (e.g., "rgba(135, 206, 250, 0.22)")
    private String textareaBorder;

    // 导航栏模糊程度 (e.g., "15px")
    private String navBlur;

    // 主要内容模糊程度 (e.g., "15px")
    private String mainBlur;

    // 次要内容模糊程度 (e.g., "15px")
    private String sideBlur;

    // 模态窗口背景颜色 (e.g., "rgba(2, 98, 136, 0.1)")
    private String modalColor;

    // 模态窗口模糊程度 (e.g., "10px")
    private String modalBlur;

    // 模态窗口激活颜色 (e.g., "rgba(0, 212, 240, 0.6)")
    private String modalActive;

    // 三点菜单背景颜色 (e.g., "rgba(22, 56, 66, 0.51)")
    private String menuColor;

    // 三点菜单激活颜色 (e.g., "rgb(51, 168, 184)")
    private String menuActiveColor;

    // 三点菜单模糊程度 (e.g., "6px")
    private String menuBlur;

    // 选择器背景颜色 (e.g., "rgba(25, 35, 60, 0.5)")
    private String selectorColor;

    // 选择器激活颜色 (e.g., "rgba(23, 140, 194, 0.5)")
    private String selectorActiveColor;

    // 选择器边框颜色 (e.g., "rgba(79, 172, 254, 0.15)")
    private String selectorBorderColor;

}