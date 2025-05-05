import type RestPageableView from "@/entity/RestPageableView.ts";
import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";

export interface GetPlayerListDto extends PageQuery {
    keyword?: string; // 关键字查询
}

export interface GetPlayerListVo {
    id: string;       // 玩家ID (注意：根据ts-vue-rule，后端Long类型映射为string)
    avatarUrl?: string; // 头像路径
    name: string;       // (明文)人物角色名称
    balance: string;    // 钱包余额(CU) (注意：根据ts-vue-rule，后端BigDecimal类型映射为string)
}

export interface CreatePlayerDto {
    name: string;              // (明文)人物角色名称
    publicInfo?: string;       // (明文)个人信息
    description?: string;      // (密文)人物角色描述
    language: string;          // 语言 如 中文,English,zh-CN,en-US
    era?: string;              // 年代 如 古代,中世纪,现代,未来,赛博朋克,80S,90S,2025-01-01
    contentFilterLevel: number; // 内容过滤等级 0:不过滤 1:普通 2:严格
    avatarUrl?: string;        // 头像路径
}

export interface GetCurrentPlayerVo {
    id: string;   // 当前玩家ID
    name: string; // 当前玩家名称
}

export interface CheckPlayerNameDto {
    name: string; // 人物角色名称
}

export default {

    /**
     * 获取用户的人物列表
     */
    getPlayerList: async (dto: GetPlayerListDto): Promise<RestPageableView<GetPlayerListVo>> => {
        return await Http.postEntity<RestPageableView<GetPlayerListVo>>('/player/getPlayerList', dto);
    },

    /**
     * 创建人物
     * @return 创建成功后的人物ID
     */
    createPlayer: async (dto: CreatePlayerDto): Promise<string> => {
        return await Http.postEntity<string>('/player/createPlayer', dto);
    },

    /**
     * 检查人物名字是否可用
     */
    checkPlayerName: async (dto: CheckPlayerNameDto): Promise<string> => {
        // 注意：后端成功时返回 Result<String>("该名字可用")，失败时返回 Result.error("该名字不可用")
        // Http 工具类会自动处理 Result 包装，业务逻辑只需关注成功时的数据或失败时的错误信息
        // 这里我们直接返回后端 Result 中的 data 字段 (即 "该名字可用" 或 由Http抛出的错误中的 message "该名字不可用")
        return await Http.postEntity<string>('/player/checkName', dto);
    },

    /**
     * 获取当前登录的人物信息
     */
    getCurrentPlayer: async (): Promise<GetCurrentPlayerVo> => {
        return await Http.postEntity<GetCurrentPlayerVo>('/player/getCurrentPlayer', {});
    },

    /**
     * 用户选择一个人物进行登录
     */
    attachPlayer: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/player/attachPlayer', dto);
    },

    /**
     * 用户取消激活所有人物，退回到人物选择界面
     */
    detachPlayer: async (): Promise<string> => {
        return await Http.postEntity<string>('/player/detachPlayer', {});
    }
}
