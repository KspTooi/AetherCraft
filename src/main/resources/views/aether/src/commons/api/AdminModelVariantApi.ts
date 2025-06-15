import type RestPageableView from '@/entity/RestPageableView';
import Http from '@/commons/Http';
import type PageQuery from '@/entity/PageQuery';
import type CommonIdDto from '@/entity/dto/CommonIdDto';

// --- DTOs ---

export interface GetAdminModelVariantListDto extends PageQuery {
    keyword?: string | null; // 模糊筛选模型代码、名称、系列
    enabled?: number; // 启用状态 0:禁用 1:启用
}

export interface SaveAdminModelVariantDto {
    id?: string; // 模型ID，新增时为null，编辑时必填 (Long -> string)
    code: string; // 模型代码 (PO中nullable = false)
    name: string; // 模型名称 (PO中nullable = false)
    type: number; // 模型类型 0:文本 1:图形 2:多模态 (PO中nullable = false)
    series: string; // 模型系列、厂商 (PO中nullable = false)
    thinking?: number; // 思考能力 0:无 1:有 (PO中可为空)
    scale?: number; // 规模 0:小型 1:中型 2:大型 (PO中可为空)
    speed?: number; // 速度 0:慢速 1:中速 2:快速 3:极快 (PO中可为空)
    intelligence?: number; // 智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子 (PO中可为空)
    enabled: number; // 是否启用 0:禁用 1:启用 (PO中nullable = false)
    seq?: number; // 排序号 (前端允许为空，后端会自动设置)
}

export interface AdminToggleModelVariantDto {
    ids: string[]; // 模型变体ID列表 (Long[] -> string[])
    enabled: number; // 启用状态 0:禁用 1:启用
}

export interface ApplyModelVariantParamTemplateDto {
    templateId: string; // 模板ID，必填 (Long -> string)
    modelVariantIds: string[]; // 模型变体ID列表，支持批量 (Long[] -> string[])
    global: number; // 应用范围：0=个人参数, 1=全局参数
}

// --- VOs ---

export interface GetAdminModelVariantListVo {
    id: string; // 模型ID (Long -> string)
    code: string; // 模型代码
    name: string; // 模型名称
    type: number; // 模型类型 0:文本 1:图形 2:多模态
    series: string; // 模型系列、厂商
    thinking: number; // 思考能力 0:无 1:有
    scale: number; // 规模 0:小型 1:中型 2:大型
    speed: number; // 速度 0:慢速 1:中速 2:快速 3:极快
    intelligence: number; // 智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子
    enabled: number; // 是否启用 0:禁用 1:启用
    paramCount: number; // 参数数量
    createTime: string; // 创建时间 (Date -> string)
    updateTime: string; // 更新时间 (Date -> string)
}

export interface GetAdminModelVariantDetailsVo {
    id: string; // 模型ID (Long -> string)
    code: string; // 模型代码
    name: string; // 模型名称
    type: number; // 模型类型 0:文本 1:图形 2:多模态
    series: string; // 模型系列、厂商
    thinking: number; // 思考能力 0:无 1:有
    scale: number; // 规模 0:小型 1:中型 2:大型
    speed: number; // 速度 0:慢速 1:中速 2:快速 3:极快
    intelligence: number; // 智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子
    enabled: number; // 是否启用 0:禁用 1:启用
    seq: number | null; // 排序号，允许为空
    createTime: string; // 创建时间 (Date -> string)
    updateTime: string; // 更新时间 (Date -> string)
}

export default {

    /**
     * 获取所有支持的AI模型系列代码
     */
    getModelSeries: async (): Promise<string[]> => {
        return await Http.postEntity<string[]>('/admin/model/variant/getModelSeries', {});
    },

    /**
     * 获取模型变体列表
     */
    getModelVariantList: async (dto: GetAdminModelVariantListDto): Promise<RestPageableView<GetAdminModelVariantListVo>> => {
        return await Http.postEntity<RestPageableView<GetAdminModelVariantListVo>>('/admin/model/variant/getModelVariantList', dto);
    },

    /**
     * 获取模型变体详情
     */
    getModelVariantDetails: async (dto: CommonIdDto): Promise<GetAdminModelVariantDetailsVo> => {
        return await Http.postEntity<GetAdminModelVariantDetailsVo>('/admin/model/variant/getModelVariantDetails', dto);
    },

    /**
     * 保存模型变体
     */
    saveModelVariant: async (dto: SaveAdminModelVariantDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/saveModelVariant', dto);
    },

    /**
     * 批量切换模型变体启用状态
     */
    toggleModelVariant: async (dto: AdminToggleModelVariantDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/toggleModelVariant', dto);
    },

    /**
     * 删除模型变体
     */
    removeModelVariant: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/removeModelVariant', dto);
    },

    /**
     * 复制模型变体
     */
    copyModelVariant: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/copyModelVariant', dto);
    },

    /**
     * 应用参数模板到模型变体（支持批量应用和全局/个人参数选择）
     */
    applyModelVariantParamTemplate: async (dto: ApplyModelVariantParamTemplateDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/applyModelVariantParamTemplate', dto);
    }

};
