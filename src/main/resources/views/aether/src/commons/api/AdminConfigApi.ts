import type RestPageableView from "@/entity/RestPageableView.ts";
import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";

export interface GetConfigListDto extends PageQuery {
    keyword: string | null;     // 配置键/值/描述
    playerName: string | null;    // 所有者名称
}

export interface GetConfigListVo {
    id: string;             // 配置ID
    playerName: string;       // 所有者名称
    configKey: string;      // 配置键
    configValue: string;    // 配置值
    description: string;    // 配置描述
    createTime: string;     // 创建时间
    updateTime: string;     // 更新时间
}

export interface GetConfigDetailsVo {
    id: string;             // 配置ID
    userId: string;         // 用户ID
    playerName: string;       // 所有者名称
    configKey: string;      // 配置键
    configValue: string;    // 配置值
    description: string;    // 配置描述
    createTime: string;     // 创建时间
    updateTime: string;     // 更新时间
}

export interface SaveConfigDto {
    id?: string;            // 配置ID
    configKey: string;      // 配置键
    configValue: string;    // 配置值
    description?: string;   // 配置描述
}

export default {
    /**
     * 获取配置列表
     */
    getConfigList: async (dto: GetConfigListDto): Promise<RestPageableView<GetConfigListVo>> => {
        return await Http.postEntity<RestPageableView<GetConfigListVo>>('/admin/config/getConfigList', dto);
    },

    /**
     * 获取配置详情
     */
    getConfigDetails: async (dto: CommonIdDto): Promise<GetConfigDetailsVo> => {
        return await Http.postEntity<GetConfigDetailsVo>('/admin/config/getConfigDetails', dto);
    },

    /**
     * 保存配置
     */
    saveConfig: async (dto: SaveConfigDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/config/saveConfig', dto);
    },

    /**
     * 删除配置
     */
    removeConfig: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/config/removeConfig', dto);
    }
}
