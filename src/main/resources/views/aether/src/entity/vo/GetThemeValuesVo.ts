/**
 * 主题值详情
 */
export interface GetThemeValuesVo {
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
     * 是否激活
     */
    isActive: number;

    /**
     * 主题值列表
     */
    themeValues: GetThemeValuesVo;
} 