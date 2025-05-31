import type RestPageableView from '@/entity/RestPageableView';
import Http from '@/commons/Http';
import type PageQuery from '@/entity/PageQuery';
import type CommonIdDto from '@/entity/dto/CommonIdDto';

// --- DTOs ---

export interface GetAdminModelSeriesListDto extends PageQuery {
    keyword?: string; // 模糊筛选模型代码、名称、系列
    enabled?: number; // 启用状态 0:禁用 1:启用
}

export interface SaveAdminModelSeriesDto {
    id?: string; // 模型ID，新增时为null，编辑时必填 (Long -> string)
    code: string; // 模型代码 (PO中nullable = false)
    name: string; // 模型名称 (PO中nullable = false)
    series: string; // 模型系列、厂商 (PO中nullable = false)
    thinking?: number; // 思考能力 0:无 1:有 (PO中可为空)
    scale?: number; // 规模 0:小型 1:中型 2:大型 (PO中可为空)
    speed?: number; // 速度 0:慢速 1:中速 2:快速 3:极快 (PO中可为空)
    intelligence?: number; // 智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子 (PO中可为空)
    enabled: number; // 是否启用 0:禁用 1:启用 (PO中nullable = false)
}

// --- VOs ---

export interface GetAdminModelSeriesListVo {
    id: string; // 模型ID (Long -> string)
    code: string; // 模型代码
    name: string; // 模型名称
    series: string; // 模型系列、厂商
    thinking?: number; // 思考能力 0:无 1:有
    scale?: number; // 规模 0:小型 1:中型 2:大型
    speed?: number; // 速度 0:慢速 1:中速 2:快速 3:极快
    intelligence?: number; // 智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子
    enabled: number; // 是否启用 0:禁用 1:启用
    createTime: string; // 创建时间 (Date -> string)
    updateTime: string; // 更新时间 (Date -> string)
}

export interface GetAdminModelSeriesDetailsVo {
    id: string; // 模型ID (Long -> string)
    code: string; // 模型代码
    name: string; // 模型名称
    series: string; // 模型系列、厂商
    thinking?: number; // 思考能力 0:无 1:有
    scale?: number; // 规模 0:小型 1:中型 2:大型
    speed?: number; // 速度 0:慢速 1:中速 2:快速 3:极快
    intelligence?: number; // 智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子
    enabled: number; // 是否启用 0:禁用 1:启用
    createTime: string; // 创建时间 (Date -> string)
    updateTime: string; // 更新时间 (Date -> string)
}

export default {

    /**
     * 获取模型系列列表
     */
    getModelSeriesList: async (dto: GetAdminModelSeriesListDto): Promise<RestPageableView<GetAdminModelSeriesListVo>> => {
        return await Http.postEntity<RestPageableView<GetAdminModelSeriesListVo>>('/admin/model/series/getModelSeriesList', dto);
    },

    /**
     * 获取模型系列详情
     */
    getModelSeriesDetails: async (dto: CommonIdDto): Promise<GetAdminModelSeriesDetailsVo> => {
        return await Http.postEntity<GetAdminModelSeriesDetailsVo>('/admin/model/series/getModelSeriesDetails', dto);
    },

    /**
     * 保存模型系列
     */
    saveModelSeries: async (dto: SaveAdminModelSeriesDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/series/saveModelSeries', dto);
    },

    /**
     * 删除模型系列
     */
    removeModelSeries: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/model/series/removeModelSeries', dto);
    }

};
