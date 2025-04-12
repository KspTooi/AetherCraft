package com.ksptool.ql.biz.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
public class ThemeValuesDto {

    // 普通色
    @NotBlank
    private String boxColor;

    // 普通色Hover效果
    @NotBlank
    private String boxColorHover;

    // 普通色Active效果
    @NotBlank
    private String boxColorActive;
    
    // 强调色
    @NotBlank
    private String boxAccentColor;

    // 强调色Hover效果
    @NotBlank
    private String boxAccentColorHover;

    // 辉光色
    @NotBlank
    private String boxGlowColor;

    // 边框色
    @NotBlank
    private String boxBorderColor;

    // 边框色Hover
    @NotBlank
    private String boxBorderColorHover;

    // 普通模糊
    @NotNull
    private Integer boxBlur;

    // 聚焦模糊
    @NotNull
    private Integer boxBlurHover;

    // 激活模糊
    @NotNull
    private Integer boxBlurActive;

    // 文字色
    @NotBlank
    private String boxTextColor;

    // 文字色NoActive
    @NotBlank
    private String boxTextColorNoActive;

    // 次级色
    @NotBlank
    private String boxSecondColor;

    // 次级色Hover效果
    @NotBlank
    private String boxSecondColorHover;

    // 元素色
    @NotBlank
    private String mainColor;

    // 元素文字色
    @NotBlank
    private String mainTextColor;

    // 元素色边框
    @NotBlank
    private String mainBorderColor;

    // 元素色边框Hover效果
    @NotBlank
    private String mainBorderColorHover;

    // 元素色Hover效果
    @NotBlank
    private String mainColorHover;

    // 元素色Active效果
    @NotBlank
    private String mainColorActive;

    // 元素色文字Active效果
    @NotBlank
    private String mainTextColorActive;

    // 元素色边框Active效果
    @NotBlank
    private String mainBorderColorActive;

    // 危险色
    @NotBlank
    private String dangerColor;

    // 危险色文字
    @NotBlank
    private String dangerTextColor;

    // 危险色边框
    @NotBlank
    private String dangerBorderColor;

    // 危险色边框Hover效果
    @NotBlank
    private String dangerBorderColorHover;

    // 危险色Hover效果
    @NotBlank
    private String dangerColorHover;

    // 危险色Active效果
    @NotBlank
    private String dangerColorActive;

    // 危险色文字Active效果
    @NotBlank
    private String dangerTextColorActive;

    // 危险色边框Active效果
    @NotBlank
    private String dangerBorderColorActive;

    // 禁用色
    @NotBlank
    private String disabledColor;

    // 禁用色边框
    @NotBlank
    private String disabledBorderColor;
}
