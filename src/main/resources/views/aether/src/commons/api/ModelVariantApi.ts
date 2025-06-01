import Http from "@/commons/Http.ts";

// 客户端模型变体数据接口
export interface ClientModelVariant {
    code: string;           // 模型代码
    name: string;           // 模型名称
    type: number;           // 模型类型 0:文本 1:图形 2:多模态
    series: string;         // 模型系列
    thinking: number;       // 思考能力 0:无 1:有
    scale: number;          // 规模 0:小型 1:中型 2:大型
    speed: number;          // 速度 0:慢速 1:中速 2:快速 3:极快
    intelligence: number;   // 智能程度 0:木质 1:石质 2:铁质 3:钻石 4:纳米 5:量子
}

export default {
    /**
     * 获取客户端可用的模型变体列表
     */
    getModelVariantList: async (): Promise<ClientModelVariant[]> => {
        return await Http.postEntity<ClientModelVariant[]>('/model/variant/getModelVariantList', {});
    }
}
