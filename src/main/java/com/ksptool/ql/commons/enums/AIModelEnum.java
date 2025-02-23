package com.ksptool.ql.commons.enums;

import lombok.Getter;

/**
 * AI模型枚举
 */
@Getter
public enum AIModelEnum {
    
    GEMINI_2_FLASH("gemini-2.0-flash", "Gemini 2.0 Flash"),
    GEMINI_2_FLASH_LITE_PREVIEW("gemini-2.0-flash-lite-preview-02-05", "Gemini 2.0 Flash Lite Preview"),
    GEMINI_15_FLASH("gemini-1.5-flash", "Gemini 1.5 Flash"),
    GEMINI_15_FLASH_8B("gemini-1.5-flash-8b", "Gemini 1.5 Flash 8B"),
    GEMINI_15_PRO("gemini-1.5-pro", "Gemini 1.5 Pro"),
    GEMINI_2_PRO_EXP("gemini-2.0-pro-exp-02-05", "Gemini 2.0 Pro Experimental"),
    GEMINI_2_FLASH_THINKING_EXP("gemini-2.0-flash-thinking-exp-01-21", "Gemini 2.0 Flash Thinking Experimental"),
    GEMINI_EXP_1206("gemini-exp-1206", "Gemini Experimental 1206");
    
    /**
     * 模型代码
     */
    private final String code;
    
    /**
     * 模型名称
     */
    private final String name;
    
    AIModelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据代码获取枚举
     */
    public static AIModelEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (AIModelEnum model : values()) {
            if (model.getCode().equals(code)) {
                return model;
            }
        }
        return null;
    }
} 