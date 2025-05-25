package com.ksptool.ql.commons.enums;

import com.ksptool.ql.commons.exception.BizException;
import lombok.Getter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI模型枚举
 */
@Getter
public enum AIModelEnum {

    /*正式模型*/
    GEMINI_2_5_FLASH_PREVIEW_0520("gemini-2.5-flash-preview-05-20", "Gemini 2.5 Flash Preview 2025-05-20", "Gemini", "大型", "中速", "精英", 1),
    GEMINI_2_5_FLASH_PREVIEW_0417("gemini-2.5-flash-preview-04-17", "Gemini 2.5 Flash Preview 2025-04-17", "Gemini", "大型", "中速", "精英", 1),
    GEMINI_2_FLASH("gemini-2.0-flash", "Gemini 2.0 Flash", "Gemini", "中型", "极速", "铂金", 0),
    GEMINI_2_5_PRO_PREVIEW_0325("gemini-2.5-pro-preview-03-25", "Gemini 2.5 Pro Preview 2025-03-25", "Gemini", "大型", "慢速", "精英", 1),
    GEMINI_2_5_EXP_0325("gemini-2.5-pro-exp-03-25", "Gemini 2.5 Pro Exp 2025-03-25", "Gemini", "大型", "慢速", "精英", 1),

    GEMINI_2_FLASH_LITE("gemini-2.0-flash-lite", "Gemini 2.0 Flash-Lite", "Gemini", "小型", "极速", "白银", 0),
    GEMINI_15_FLASH("gemini-1.5-flash", "Gemini 1.5 Flash", "Gemini", "中型", "中速", "铂金", 0),
    GEMINI_15_FLASH_8B("gemini-1.5-flash-8b", "Gemini 1.5 Flash 8B", "Gemini", "小型", "极速", "木制", 0),
    GEMINI_15_PRO("gemini-1.5-pro", "Gemini 1.5 Pro", "Gemini", "中型", "中速", "铂金", 0),

    /*实验模型*/

    GEMINI_2_PRO_EXP("gemini-2.0-pro-exp", "Gemini 2.0 Pro Exp", "Gemini", "大型", "中速", "铂金", 0),
    GEMINI_2_PRO_EXP_0205("gemini-2.0-pro-exp-02-05", "Gemini 2.0 Pro Exp 2025-02-05", "Gemini", "大型", "中速", "铂金", 0),
    GEMINI_2_FLASH_THINKING_EXP_0121("gemini-2.0-flash-thinking-exp-01-21", "Gemini 2.0 Flash Thinking Exp 2025-01-21", "Gemini", "大型", "慢速", "铂金", 1),
    GEMINI_2_FLASH_THINKING_EXP_1219("gemini-2.0-flash-thinking-exp-1219", "Gemini 2.0 Flash Thinking Exp 2024-12-19", "Gemini", "大型", "慢速", "铂金", 1),
    GEMINI_2_FLASH_EXP("gemini-2.0-flash-exp", "Gemini 2.0 Flash Exp", "Gemini", "中型", "极速", "铂金", 0),

    GEMINI_2_EXP_1206("gemini-exp-1206", "Gemini 2024-12-06", "Gemini", "中型", "中速", "铂金", 0),
    GEMINI_2_EXP_1121("gemini-exp-1121", "Gemini 2024-11-21", "Gemini", "中型", "中速", "铂金", 0),
    GEMINI_2_EXP_1114("gemini-exp-1114", "Gemini 2024-11-14", "Gemini", "中型", "中速", "铂金", 0),

    GEMINI_15_PRO_EXP_0827("gemini-1.5-pro-exp-0827", "Gemini 1.5 Pro Exp 2024-08-27", "Gemini", "中型", "中速", "白银", 0),
    GEMINI_15_PRO_EXP_0801("gemini-1.5-pro-exp-0801", "Gemini 1.5 Pro Exp 2024-08-01", "Gemini", "中型", "中速", "白银", 0),

    GEMINI_15_PRO_001("gemini-1.5-pro-001", "Gemini 1.5 Pro #001", "Gemini", "中型", "中速", "青铜", 0),
    GEMINI_15_PRO_002("gemini-1.5-pro-002", "Gemini 1.5 Pro #002", "Gemini", "中型", "中速", "青铜", 0),
    GEMINI_15_FLASH_001("gemini-1.5-flash-001", "Gemini 1.5 Flash #001", "Gemini", "中型", "中速", "青铜", 0),
    GEMINI_15_FLASH_002("gemini-1.5-flash-002", "Gemini 1.5 Flash #002", "Gemini", "中型", "中速", "青铜", 0),


    GROK_3("grok-3", "Grok-3", "Grok", "大型", "中速", "精英", 0),
    GROK_3_FAST("grok-3-fast", "Grok-3 Fast", "Grok", "大型", "快速", "精英", 0),
    GROK_3_MINI("grok-3-mini", "Grok-3 Mini", "Grok", "中型", "快速", "铂金", 1),
    GROK_3_MINI_FAST("grok-3-mini-fast", "Grok-3 Mini Fast", "Grok", "中型", "极速", "铂金", 1),


    GROK_3_BETA("grok-3-beta", "Grok 3 Beta", "Grok", "大型", "中速", "精英", 0),
    GROK_3_FAST_BETA("grok-3-fast-beta", "Grok 3 Fast Beta", "Grok", "大型", "快速", "精英", 0),
    GROK_3_MINI_BETA("grok-3-mini-beta", "Grok 3 Mini Beta", "Grok", "中型", "快速", "铂金", 1),
    GROK_3_MINI_FAST_BETA("grok-3-mini-fast-beta", "Grok 3 Mini Fast Beta", "Grok", "中型", "快速", "铂金", 1),
    GROK_2_1212("grok-2-1212", "Grok 2 #1212", "Grok", "大型", "中速", "白银", 0),
    //GROK_2_VISION_1212("grok-2-vision-1212", "Grok 2 Vision #1212", "Grok"),

    //GROK_BETA("grok-beta", "Grok Beta", "Grok"),
    //GROK_BETA_VISION("grok-vision-beta", "Grok Beta Vision", "Grok"),


    //目前不会计划支持DeepSeek
//    DEEPSEEK_CHAT("deepseek-chat", "DeepSeek", "DeepSeek"),
//    DEEPSEEK_REASONER("deepseek-reasoner", "DeepSeek-Reasoner(R1)", "DeepSeek"),

    ;

    /**
     * 模型代码
     */
    private final String code;
    
    /**
     * 模型名称
     */
    private final String name;

    /**
     * 模型系列
     */
    private final String series;

    /**
     * 模型规模
     */
    private final String size;

    /**
     * 模型速度
     */
    private final String speed;

    /**
     * 智能程度
     */
    private final String intelligence;

    /**
     * 思考能力
     */
    private final Integer thinking;
    
    AIModelEnum(String code, String name, String series, String size, String speed, String intelligence, Integer thinking) {
        this.code = code;
        this.name = name;
        this.series = series;
        this.size = size;
        this.speed = speed;
        this.intelligence = intelligence;
        this.thinking = thinking;
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

    /**
     * 根据系列名称获取枚举
     */
    public static AIModelEnum getBySeries(String series) {
        if (series == null) {
            return null;
        }
        for (AIModelEnum model : values()) {
            if (model.getSeries().equals(series)) {
                return model;
            }
        }
        return null;
    }

    public String getSeries() {
        return series;
    }

    /**
     * 获取所有不重复的模型系列列表
     * @return 模型系列列表
     */
    public static List<String> getSeriesList() {
        return Arrays.stream(values())
                .map(AIModelEnum::getSeries)
                .distinct()
                .collect(Collectors.toList());
    }

    public static AIModelEnum ensureModelCodeExists(String modelCode) throws BizException {
        // 获取并验证模型配置
        AIModelEnum modelEnum = AIModelEnum.getByCode(modelCode);
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }
        return modelEnum;
    }


} 