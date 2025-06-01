package com.ksptool.ql.commons.enums;

import lombok.Getter;

/**
 * AI模型变体枚举
 * 定义系统默认支持的AI模型变体，用于数据初始化
 */
@Getter
public enum AiModelVariantEnum {

    /*正式模型*/
    GEMINI_2_5_FLASH_PREVIEW_0520("gemini-2.5-flash-preview-05-20", "Gemini 2.5 Flash Preview 2025-05-20", "Gemini", 2, 2, 5, 1),
    GEMINI_2_5_FLASH_PREVIEW_0417("gemini-2.5-flash-preview-04-17", "Gemini 2.5 Flash Preview 2025-04-17", "Gemini", 2, 1, 5, 1),
    GEMINI_2_FLASH("gemini-2.0-flash", "Gemini 2.0 Flash", "Gemini", 1, 3, 3, 0),
    GEMINI_2_5_PRO_PREVIEW_0325("gemini-2.5-pro-preview-03-25", "Gemini 2.5 Pro Preview 2025-03-25", "Gemini", 2, 0, 5, 1),
    GEMINI_2_5_EXP_0325("gemini-2.5-pro-exp-03-25", "Gemini 2.5 Pro Exp 2025-03-25", "Gemini", 2, 0, 5, 1),

    GEMINI_2_FLASH_LITE("gemini-2.0-flash-lite", "Gemini 2.0 Flash-Lite", "Gemini", 0, 3, 2, 0),
    GEMINI_15_FLASH("gemini-1.5-flash", "Gemini 1.5 Flash", "Gemini", 1, 1, 3, 0),
    GEMINI_15_FLASH_8B("gemini-1.5-flash-8b", "Gemini 1.5 Flash 8B", "Gemini", 0, 3, 0, 0),
    GEMINI_15_PRO("gemini-1.5-pro", "Gemini 1.5 Pro", "Gemini", 1, 1, 3, 0),

    /*实验模型*/
    GEMINI_2_PRO_EXP("gemini-2.0-pro-exp", "Gemini 2.0 Pro Exp", "Gemini", 2, 1, 3, 0),
    GEMINI_2_PRO_EXP_0205("gemini-2.0-pro-exp-02-05", "Gemini 2.0 Pro Exp 2025-02-05", "Gemini", 2, 1, 3, 0),
    GEMINI_2_FLASH_THINKING_EXP_0121("gemini-2.0-flash-thinking-exp-01-21", "Gemini 2.0 Flash Thinking Exp 2025-01-21", "Gemini", 2, 0, 3, 1),
    GEMINI_2_FLASH_THINKING_EXP_1219("gemini-2.0-flash-thinking-exp-1219", "Gemini 2.0 Flash Thinking Exp 2024-12-19", "Gemini", 2, 0, 3, 1),
    GEMINI_2_FLASH_EXP("gemini-2.0-flash-exp", "Gemini 2.0 Flash Exp", "Gemini", 1, 3, 3, 0),

    GEMINI_2_EXP_1206("gemini-exp-1206", "Gemini 2024-12-06", "Gemini", 1, 1, 3, 0),
    GEMINI_2_EXP_1121("gemini-exp-1121", "Gemini 2024-11-21", "Gemini", 1, 1, 3, 0),
    GEMINI_2_EXP_1114("gemini-exp-1114", "Gemini 2024-11-14", "Gemini", 1, 1, 3, 0),

    GEMINI_15_PRO_EXP_0827("gemini-1.5-pro-exp-0827", "Gemini 1.5 Pro Exp 2024-08-27", "Gemini", 1, 1, 2, 0),
    GEMINI_15_PRO_EXP_0801("gemini-1.5-pro-exp-0801", "Gemini 1.5 Pro Exp 2024-08-01", "Gemini", 1, 1, 2, 0),

    GEMINI_15_PRO_001("gemini-1.5-pro-001", "Gemini 1.5 Pro #001", "Gemini", 1, 1, 1, 0),
    GEMINI_15_PRO_002("gemini-1.5-pro-002", "Gemini 1.5 Pro #002", "Gemini", 1, 1, 1, 0),
    GEMINI_15_FLASH_001("gemini-1.5-flash-001", "Gemini 1.5 Flash #001", "Gemini", 1, 1, 1, 0),
    GEMINI_15_FLASH_002("gemini-1.5-flash-002", "Gemini 1.5 Flash #002", "Gemini", 1, 1, 1, 0),


    GROK_3("grok-3", "Grok-3", "Grok", 2, 1, 5, 0),
    GROK_3_FAST("grok-3-fast", "Grok-3 Fast", "Grok", 2, 2, 5, 0),
    GROK_3_MINI("grok-3-mini", "Grok-3 Mini", "Grok", 1, 2, 3, 1),
    GROK_3_MINI_FAST("grok-3-mini-fast", "Grok-3 Mini Fast", "Grok", 1, 3, 3, 1),


    GROK_3_BETA("grok-3-beta", "Grok 3 Beta", "Grok", 2, 1, 5, 0),
    GROK_3_FAST_BETA("grok-3-fast-beta", "Grok 3 Fast Beta", "Grok", 2, 2, 5, 0),
    GROK_3_MINI_BETA("grok-3-mini-beta", "Grok 3 Mini Beta", "Grok", 1, 2, 3, 1),
    GROK_3_MINI_FAST_BETA("grok-3-mini-fast-beta", "Grok 3 Mini Fast Beta", "Grok", 1, 3, 3, 1),
    GROK_2_1212("grok-2-1212", "Grok 2 #1212", "Grok", 2, 1, 2, 0),

    //目前不会计划支持DeepSeek
    DEEPSEEK_CHAT("deepseek-chat", "DeepSeek", "DeepSeek", 2, 0, 2, 0),
    DEEPSEEK_REASONER("deepseek-reasoner", "DeepSeek-Reasoner(R1)", "DeepSeek", 2, 0, 3, 1);

    // 模型代码
    private final String code;
    
    // 模型名称
    private final String name;
    
    // 模型系列
    private final String series;
    
    // 模型规模 0:小型 1:中型 2:大型
    private final Integer scale;
    
    // 模型速度 0:慢速 1:中速 2:快速 3:极快
    private final Integer speed;
    
    // 智能程度 0:木制 1:青铜 2:白银 3:铂金 4:钻石 5:精英
    private final Integer intelligence;
    
    // 思考能力 0:无 1:有
    private final Integer thinking;

    AiModelVariantEnum(String code, String name, String series, Integer scale, Integer speed, 
                       Integer intelligence, Integer thinking) {
        this.code = code;
        this.name = name;
        this.series = series;
        this.scale = scale;
        this.speed = speed;
        this.intelligence = intelligence;
        this.thinking = thinking;
    }

    /**
     * 根据模型代码获取枚举值
     */
    public static AiModelVariantEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (AiModelVariantEnum variant : values()) {
            if (variant.getCode().equals(code)) {
                return variant;
            }
        }
        return null;
    }

    /**
     * 根据系列获取该系列的所有变体
     */
    public static AiModelVariantEnum[] getBySeries(String series) {
        if (series == null) {
            return new AiModelVariantEnum[0];
        }
        return java.util.Arrays.stream(values())
                .filter(variant -> variant.getSeries().equals(series))
                .toArray(AiModelVariantEnum[]::new);
    }

    /**
     * 检查指定代码的模型变体是否存在
     */
    public static boolean exists(String code) {
        return getByCode(code) != null;
    }

    /**
     * 获取所有模型代码
     */
    public static String[] getAllCodes() {
        return java.util.Arrays.stream(values())
                .map(AiModelVariantEnum::getCode)
                .toArray(String[]::new);
    }

}
