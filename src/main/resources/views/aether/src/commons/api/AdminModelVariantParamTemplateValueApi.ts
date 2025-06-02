import type RestPageableView from '@/entity/RestPageableView';
import Http from '@/commons/Http';
import type PageQuery from '@/entity/PageQuery';
import type CommonIdDto from '@/entity/dto/CommonIdDto';

// --- DTOs ---

export interface GetModelVariantParamTemplateValueListDto extends PageQuery {
    templateId: string; // 模板ID，必填 (Long -> string)
    keyword?: string | null; // 模糊筛选参数键或描述
}

export interface SaveModelVariantParamTemplateValueDto {
    id?: string; // 模板值ID，新增时为null，编辑时必填 (Long -> string)
    templateId: string; // 所属模板ID，必填 (Long -> string)
    paramKey: string; // 参数键，必填
    paramVal: string; // 参数值，必填
    type: number; // 参数类型，必填 0:string, 1:int, 2:boolean, 3:float
    description?: string; // 参数描述
    seq?: number; // 排序号
}

// --- VOs ---

export interface GetModelVariantParamTemplateValueVo {
    id: string; // 模板值ID (Long -> string)
    templateId: string; // 所属模板ID (Long -> string)
    templateName: string; // 所属模板名称
    paramKey: string; // 参数键
    paramVal: string; // 参数值
    type: number; // 参数类型 0:string, 1:int, 2:boolean, 3:float
    typeName: string; // 参数类型名称
    description: string; // 参数描述
    seq: number; // 排序号
    createTime: string; // 创建时间 (Date -> string，后端自动格式化)
    updateTime: string; // 更新时间 (Date -> string，后端自动格式化)
}

export default {

    /**
     * 查询指定模板的参数值列表（分页）
     */
    getModelVariantParamTemplateValueList: async (dto: GetModelVariantParamTemplateValueListDto): Promise<RestPageableView<GetModelVariantParamTemplateValueVo>> => {
        return await Http.postEntity<RestPageableView<GetModelVariantParamTemplateValueVo>>('/admin/model/variant/param/template/value/getModelVariantParamTemplateValueList', dto);
    },

    /**
     * 查询模板参数值详情
     */
    getModelVariantParamTemplateValueDetails: async (dto: CommonIdDto): Promise<GetModelVariantParamTemplateValueVo> => {
        return await Http.postEntity<GetModelVariantParamTemplateValueVo>('/admin/model/variant/param/template/value/getModelVariantParamTemplateValueDetails', dto);
    },

    /**
     * 新增或编辑单个模板参数值
     */
    saveModelVariantParamTemplateValue: async (dto: SaveModelVariantParamTemplateValueDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/param/template/value/saveModelVariantParamTemplateValue', dto);
    },

    /**
     * 删除单个模板参数值
     */
    removeModelVariantParamTemplateValue: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/param/template/value/removeModelVariantParamTemplateValue', dto);
    }

}; 