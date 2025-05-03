import type RestPageableView from "@/entity/RestPageableView.ts";
import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts";

export interface GetAvailableModelVo {
    modelName: string;    // 模型名称
    modelCode: string;    // 模型代码
}

export interface GetModelConfigDto {
    modelCode: string;    // 模型代码
}

export interface AvailableApiKeyVo {
    id: string;          // API密钥ID
    apiKey: string;      // API密钥
    apiHost: string;     // API地址
    proxyHost: string;   // 代理地址
    createTime: string;  // 创建时间
    updateTime: string;  // 更新时间
}

export interface GetAdminModelConfigVo {
    modelCode: string;           // 模型代码
    modelName: string;           // 模型名称
    globalProxyConfig: string;   // 全局代理配置
    userProxyConfig: string;     // 用户代理配置
    temperature: number;         // 温度值
    topP: number;               // 采样值
    topK: number;               // Top K值
    maxOutputTokens: number;    // 最大输出长度
    apiKeys: AvailableApiKeyVo[]; // 可用的API密钥列表
    currentApiKeyId: string;    // 当前使用的API密钥ID
}

export interface SaveModelConfigDto {
    id?: string;         // 配置ID
    modelCode: string;   // 模型代码
    apiKey: string;      // API密钥
    apiHost?: string;    // API地址
    proxyHost?: string;  // 代理地址
}

export interface TestModelConnectionDto {
    modelCode: string;   // 模型代码
    apiKey: string;      // API密钥
    apiHost?: string;    // API地址
    proxyHost?: string;  // 代理地址
}

export default {
    /**
     * 获取可用的模型列表
     */
    getAvailableModels: async (): Promise<GetAvailableModelVo[]> => {
        return await Http.postEntity<GetAvailableModelVo[]>('/admin/model/getAvailableModels', {});
    },

    /**
     * 获取模型配置
     */
    getModelConfig: async (dto: GetModelConfigDto): Promise<GetAdminModelConfigVo> => {
        return await Http.postEntity<GetAdminModelConfigVo>('/admin/model/getModelConfig', dto);
    },

    /**
     * 保存模型配置
     */
    saveModelConfig: async (dto: SaveModelConfigDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/saveModelConfig', dto);
    },

    /**
     * 测试模型连接
     */
    testModelConnection: async (dto: TestModelConnectionDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/testModelConnection', dto);
    }
}
