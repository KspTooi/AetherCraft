import type RestPageableView from "@/entity/RestPageableView.ts";
import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";

export interface GetApiKeyListDto extends PageQuery {
    keyName: string | null;    // 密钥名称
    keySeries: string | null;  // 密钥系列
    status: number | null;     // 状态：0-禁用，1-启用
}

export interface GetApiKeyListVo {
    id: string;             // 密钥ID
    keyName: string;        // 密钥名称
    keySeries: string;      // 密钥系列
    isShared: number;       // 是否共享：0-不共享，1-共享
    usageCount: string;     // 使用次数
    lastUsedTime: string;   // 最后使用时间
    status: number;         // 状态：0-禁用，1-启用
}

export interface GetApiKeyDetailsVo {
    id: string;             // 密钥ID
    keyName: string;        // 密钥名称
    keySeries: string;      // 密钥系列
    keyValue: string;       // 密钥值
    isShared: number;       // 是否公开：0-私有，1-公开
    usageCount: string;     // 使用次数
    lastUsedTime: string;   // 最后使用时间
    createTime: string;     // 创建时间
    status: number;         // 状态：0-禁用，1-启用
}

export interface SaveApiKeyDto {
    id?: string | null;            // 密钥ID（新增时为null）
    keyName: string;        // 密钥名称
    keySeries: string;      // 密钥系列，如OpenAI、Azure等
    keyValue: string;       // 密钥值
    isShared: number;       // 是否共享：0-不共享，1-共享
    status: number;         // 状态：0-禁用，1-启用
}

export default {
    /**
     * 获取API密钥列表
     */
    getApiKeyList: async (dto: GetApiKeyListDto): Promise<RestPageableView<GetApiKeyListVo>> => {
        return await Http.postEntity<RestPageableView<GetApiKeyListVo>>('/admin/apikey/getApiKeyList', dto);
    },

    /**
     * 获取API密钥详情
     */
    getApiKeyDetails: async (dto: CommonIdDto): Promise<GetApiKeyDetailsVo> => {
        return await Http.postEntity<GetApiKeyDetailsVo>('/admin/apikey/getApiKeyDetails', dto);
    },

    /**
     * 保存API密钥
     */
    saveApiKey: async (dto: SaveApiKeyDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/apikey/saveApiKey', dto);
    },

    /**
     * 删除API密钥
     */
    removeApiKey: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/apikey/removeApiKey', dto);
    }
}
