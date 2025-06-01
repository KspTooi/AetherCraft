import type RestPageableView from '@/entity/RestPageableView';
import Http from '@/commons/Http';
import type PageQuery from '@/entity/PageQuery';
import type CommonIdDto from '@/entity/dto/CommonIdDto';

// --- DTOs ---

export interface GetModelVariantParamTemplateListDto extends PageQuery {
    keyword?: string | null; // 模糊筛选模板名称
}

export interface GetModelVariantParamTemplateDetailsDto {
    templateId: string; // 模板ID，必填 (Long -> string)
}

export interface SaveModelVariantParamTemplateDto {
    templateId?: string; // 模板ID，新增时为null，编辑时必填 (Long -> string)
    name: string; // 模板名称，必填
}

export interface ApplyModelVariantParamTemplateToGlobalDto {
    templateId: string; // 模板ID，必填 (Long -> string)
    modelVariantId: string; // 模型变体ID，必填 (Long -> string)
}

export interface ApplyModelVariantParamTemplateToPersonalDto {
    templateId: string; // 模板ID，必填 (Long -> string)
    modelVariantId: string; // 模型变体ID，必填 (Long -> string)
}

// --- VOs ---

export interface GetModelVariantParamTemplateListVo {
    id: string; // 模板ID (Long -> string)
    name: string; // 模板名称
    valueCount: number; // 模板包含的参数数量 (原paramCount)
    createTime: string; // 创建时间 (Date -> string，后端自动格式化)
    updateTime: string; // 更新时间 (Date -> string，后端自动格式化)
}

export interface GetModelVariantParamTemplateDetailsVo {
    id: string; // 模板ID (Long -> string)
    name: string; // 模板名称
    userId: string; // 所属用户ID (Long -> string)
    playerId: string; // 所属玩家ID (Long -> string)
    createTime: string; // 创建时间 (Date -> string，后端自动格式化)
    updateTime: string; // 更新时间 (Date -> string，后端自动格式化)
}

export default {

    /**
     * 查询参数模板列表，包含所有用户的模板（不包含模板值）
     */
    getModelVariantParamTemplateList: async (dto: GetModelVariantParamTemplateListDto): Promise<RestPageableView<GetModelVariantParamTemplateListVo>> => {
        return await Http.postEntity<RestPageableView<GetModelVariantParamTemplateListVo>>('/admin/model/variant/param/template/getModelVariantParamTemplateList', dto);
    },

    /**
     * 查询模板详情（不包含模板值，模板值由另一个控制器管理）
     */
    getModelVariantParamTemplateDetails: async (dto: GetModelVariantParamTemplateDetailsDto): Promise<GetModelVariantParamTemplateDetailsVo> => {
        return await Http.postEntity<GetModelVariantParamTemplateDetailsVo>('/admin/model/variant/param/template/getModelVariantParamTemplateDetails', dto);
    },

    /**
     * 保存参数模板（仅模板基本信息，不包含模板值）
     */
    saveModelVariantParamTemplate: async (dto: SaveModelVariantParamTemplateDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/param/template/saveModelVariantParamTemplate', dto);
    },

    /**
     * 删除参数模板
     */
    removeModelVariantParamTemplate: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/param/template/removeModelVariantParamTemplate', dto);
    },

    /**
     * 应用模板为全局默认参数
     */
    applyModelVariantParamTemplateToGlobal: async (dto: ApplyModelVariantParamTemplateToGlobalDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/param/template/applyModelVariantParamTemplateToGlobal', dto);
    },

    /**
     * 应用模板为个人参数
     */
    applyModelVariantParamTemplateToPersonal: async (dto: ApplyModelVariantParamTemplateToPersonalDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/param/template/applyModelVariantParamTemplateToPersonal', dto);
    }

}; 