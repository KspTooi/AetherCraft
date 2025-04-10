import type ThemeValuesVo from "@/entity/vo/ThemeValuesVo.ts";

/**
 * 当前激活的主题
 */
export interface GetActiveThemeVo {
    /**
     * 主题ID
     */
    id: string;

    /**
     * 主题名称
     */
    themeName: string;

    /**
     * 主题描述
     */
    description: string;

    /**
     * 是否为系统主题
     */
    isSystem: number;

    /**
     * 主题值列表
     */
    themeValues: ThemeValuesVo;
} 