import type ThemeValuesDto from "@/entity/dto/ThemeValuesDto.ts";

export default interface SaveThemeDto {
    /**
     * 主题ID (可选，用于更新)
     */
    themeId?: string;

    /**
     * 主题名称 (可选)
     */
    themeName?: string;

    /**
     * 主题描述 (可选)
     */
    description?: string;

    /**
     * 主题值映射，key为属性名，value为属性值
     */
    themeValues: ThemeValuesDto;
} 