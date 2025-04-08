/**
 * 用户主题列表项
 */
export interface GetUserThemeListVo {
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

} 