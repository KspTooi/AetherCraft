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
    status: number;    // 状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除
    remainingRemoveTime: number; // 剩余删除时间(小时), -1表示非等待删除状态或无请求时间, 0表示已可删除
}

export interface CreatePlayerDto {
    name: string;              // (明文)人物角色名称
    gender: number;            // 性别 0:男 1:女 2:不愿透露 4:自定义(男性) 5:自定义(女性) 6:自定义(其他)
    genderData?: string;       // (密文)自定义性别种类 gender为4 5 6时必填
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
    avatarUrl: string | null; // 当前玩家头像路径
}

export interface CheckPlayerNameDto {
    name: string; // 人物角色名称
}

export interface EditAttachPlayerDetailsDto {
    id: string;                 // 人物ID (根据Java @NotNull，设为必须)
    avatarUrl?: string;         // 头像路径
    gender: number;             // 性别 0:男 1:女 2:不愿透露 4:自定义(男性) 5:自定义(女性) 6:自定义(其他)
    genderData?: string;        // (密文)自定义性别种类 gender为4 5 6时必填
    publicInfo?: string;        // (明文)个人信息
    description?: string;       // (密文)人物角色描述
    language: string;           // 语言 如 中文,English,zh-CN,en-US
    era?: string;               // 年代 如 古代,中世纪,现代,未来,赛博朋克,80S,90S,2025-01-01
    contentFilterLevel: number; // 内容过滤等级
}

export interface GetAttachPlayerDetailsVo {
    id: string;                 // 玩家ID
    name: string;               // (明文)人物角色名称
    avatarUrl?: string;        // 头像路径
    gender: number;            // 性别 0:男 1:女 2:不愿透露 4:自定义(男性) 5:自定义(女性) 6:自定义(其他)
    genderData?: string;       // (密文)自定义性别种类 gender为4 5 6时必填
    publicInfo?: string;       // (明文)个人信息
    description?: string;      // (密文)人物角色描述
    balance: string;            // 钱包余额(CU)
    language: string;          // 语言
    era?: string;              // 年代
    contentFilterLevel: number; // 内容过滤等级
    status: number;            // 状态: 0:正在使用 1:不活跃 2:等待删除 3:已删除
    createTime: string;         // 诞生日期 (Date -> string)
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
    checkPlayerName: async (dto: CheckPlayerNameDto): Promise<boolean> => {
        try{
            await Http.postEntity<string>('/player/checkName', dto);
            return true;
        }catch (Exception) {
            return false;
        }
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
    },

    /**
     * 获取当前登录的人物详细信息
     */
    getAttachPlayerDetails: async (): Promise<GetAttachPlayerDetailsVo> => {
        return await Http.postEntity<GetAttachPlayerDetailsVo>('/player/getAttachPlayerDetails', {});
    },

    /**
     * 编辑当前登录的人物详细信息
     */
    editAttachPlayerDetails: async (dto: EditAttachPlayerDetailsDto): Promise<string> => {
        return await Http.postEntity<string>('/player/editAttachPlayerDetails', dto);
    },

    /**
     * 发送删除人物请求
     */
    sendRemoveRequest: async (dto: CommonIdDto): Promise<string> => {
        // 后端返回 Result<String>, Http.postEntity 应该处理解包或直接返回 message
        // 假设 Http.postEntity 在成功时返回 data (即 string), 失败时抛出异常
        return await Http.postEntity<string>('/player/sendRemoveRequest', dto);
    },

    /**
     * 确认删除人物
     */
    removePlayer: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/player/removePlayer', dto);
    },

    /**
     * 取消删除人物请求
     */
    cancelRemovePlayer: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/player/cancelRemovePlayer', dto);
    },
}
