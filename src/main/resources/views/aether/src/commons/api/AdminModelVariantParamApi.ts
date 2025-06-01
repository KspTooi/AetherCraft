import type RestPageableView from '@/entity/RestPageableView';
import Http from '@/commons/Http';
import type PageQuery from '@/entity/PageQuery';
import type CommonIdDto from '@/entity/dto/CommonIdDto';

// --- DTOs ---

export interface GetModelVariantParamListDto extends PageQuery {
    modelVariantId: string; // 模型变体ID，必填 (Long -> string)
    keyword?: string | null; // 模糊筛选参数键或描述
}

export interface GetModelVariantParamDetailsDto {
    modelVariantId: string; // 模型变体ID，必填 (Long -> string)
    paramKey: string; // 参数键，必填
    global: number; // 是否为全局默认参数，必填 0:否 1:是
}

export interface SaveModelVariantParamDto {
    modelVariantId: string; // 模型变体ID，必填 (Long -> string)
    paramKey: string; // 参数键，必填
    paramVal: string; // 参数值，必填
    type: number; // 参数类型，必填 0:string, 1:int, 2:boolean等
    description?: string; // 参数描述
    global: number; // 是否为全局默认参数，必填 0:否 1:是 (Integer)
    seq?: number; // 排序号
}

export interface RemoveModelVariantParamDto {
    modelVariantId: string; // 模型变体ID，必填 (Long -> string)
    paramKey: string; // 参数键，必填
    global: number; // 是否为全局默认参数，必填 0:否 1:是
}

// --- VOs ---

export interface GetModelVariantParamListVo {
    paramKey: string; // 参数键
    globalVal: string | null; // 全局默认值
    userVal: string | null; // 我的值（个人配置值）
    type: number; // 参数类型 0:string, 1:int, 2:boolean等
    description: string | null; // 参数描述
    seq: number; // 排序号
    createTime: string; // 创建时间 (Date -> string，后端自动格式化)
    updateTime: string; // 更新时间 (Date -> string，后端自动格式化)
}

export interface GetModelVariantParamDetailsVo {
    id: string; // 参数ID (Long -> string)
    modelVariantId: string; // 模型变体ID (Long -> string)
    paramKey: string; // 参数键
    paramVal: string; // 参数值
    type: number; // 参数类型 0:string, 1:int, 2:boolean等
    description: string | null; // 参数描述
    global: number; // 是否为全局默认参数 0:否 1:是
    userId: string | null; // 用户ID，为空表示全局默认参数 (Long -> string)
    playerId: string | null; // 玩家ID，为空表示全局默认参数 (Long -> string)
    seq: number; // 排序号
    createTime: string; // 创建时间 (Date -> string，后端自动格式化)
    updateTime: string; // 更新时间 (Date -> string，后端自动格式化)
}

export default {

    /**
     * 查询模型变体参数列表，VO中包含全局值和我的值
     */
    getModelVariantParamList: async (dto: GetModelVariantParamListDto): Promise<RestPageableView<GetModelVariantParamListVo>> => {
        return await Http.postEntity<RestPageableView<GetModelVariantParamListVo>>('/admin/model/variant/param/getModelVariantParamList', dto);
    },

    /**
     * 查询参数详情
     */
    getModelVariantParamDetails: async (dto: GetModelVariantParamDetailsDto): Promise<GetModelVariantParamDetailsVo> => {
        return await Http.postEntity<GetModelVariantParamDetailsVo>('/admin/model/variant/param/getModelVariantParamDetails', dto);
    },

    /**
     * 保存参数配置，通过三要素自动判断新增还是编辑
     */
    saveModelVariantParam: async (dto: SaveModelVariantParamDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/param/saveModelVariantParam', dto);
    },

    /**
     * 删除参数配置
     */
    removeModelVariantParam: async (dto: RemoveModelVariantParamDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/variant/param/removeModelVariantParam', dto);
    }

}; 