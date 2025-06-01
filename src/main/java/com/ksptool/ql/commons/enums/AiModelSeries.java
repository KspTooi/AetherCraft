package com.ksptool.ql.commons.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.List;

/**
 * AI模型系列枚举
 * 用于分类和管理不同厂商的AI模型
 */
@Getter
public enum AiModelSeries {

    /**
     * Google Gemini系列
     * Google开发的多模态大语言模型
     */
    GEMINI("Gemini", "Google Gemini", "Google"),

    /**
     * xAI Grok系列  
     * Elon Musk的xAI公司开发的大语言模型
     */
    GROK("Grok", "xAI Grok", "xAI"),

    /**
     * OpenAI ChatGPT系列
     * OpenAI开发的GPT系列对话模型
     */
    CHATGPT("ChatGPT", "OpenAI ChatGPT", "OpenAI"),

    /**
     * Anthropic Claude系列
     * Anthropic开发的安全性优化的大语言模型  
     */
    CLAUDE("Claude", "Anthropic Claude", "Anthropic"),

    /**
     * Mistral AI系列
     * 法国Mistral AI公司开发的开源大语言模型
     */
    MISTRAL("Mistral", "Mistral AI", "Mistral AI"),

    /**
     * DeepSeek系列
     * 中国深度求索公司开发的大语言模型
     */
    DEEPSEEK("DeepSeek", "DeepSeek", "DeepSeek");

    /**
     * 系列代码，用于程序内部识别
     */
    private final String code;

    /**
     * 系列显示名称，用于用户界面展示
     */
    private final String displayName;

    /**
     * 开发厂商名称
     */
    private final String provider;

    AiModelSeries(String code, String displayName, String provider) {
        this.code = code;
        this.displayName = displayName;
        this.provider = provider;
    }

    /**
     * 根据代码获取枚举值
     * 
     * @param code 系列代码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static AiModelSeries getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (AiModelSeries series : values()) {
            if (series.getCode().equals(code)) {
                return series;
            }
        }
        return null;
    }

    /**
     * 根据厂商名称获取对应的模型系列
     * 
     * @param vendor 厂商名称
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static AiModelSeries getByVendor(String vendor) {
        if (vendor == null) {
            return null;
        }
        for (AiModelSeries series : values()) {
            if (series.getProvider().equals(vendor)) {
                return series;
            }
        }
        return null;
    }

    /**
     * 检查指定代码的模型系列是否存在
     * 
     * @param code 系列代码
     * @return 如果存在返回true，否则返回false
     */
    public static boolean exists(String code) {
        return getByCode(code) != null;
    }

    /**
     * 获取所有支持的模型系列代码
     * 
     * @return 模型系列代码列表
     */
    public static List<String> getAllCodes() {
        AiModelSeries[] series = values();
        String[] codes = new String[series.length];
        for (int i = 0; i < series.length; i++) {
            codes[i] = series[i].getCode();
        }
        return Arrays.asList(codes);
    }

    /**
     * 获取所有支持的厂商名称
     * 
     * @return 厂商名称数组（去重）
     */
    public static String[] getAllProvider() {
        return java.util.Arrays.stream(values())
                .map(AiModelSeries::getProvider)
                .distinct()
                .toArray(String[]::new);
    }

}
