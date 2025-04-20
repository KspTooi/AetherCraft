import type PageableView from "@/entity/PageableView.ts";
import Http from "@/commons/Http.ts";
import type PageQuery from "@/entity/PageQuery.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";

export interface UserGroupVo {
    id: string;           // 用户组ID
    name: string;         // 用户组名称
    description: string;  // 用户组描述
    sortOrder: number;    // 排序顺序
    isSystem: boolean;    // 是否为系统内置组
    hasGroup: boolean;    // 用户是否属于此组
}

export interface UserPermissionVo {
    id: string;           // 权限ID
    permKey: string;      // 权限键
    name: string;         // 权限名称
    description: string;  // 权限描述
    isSystem: boolean;    // 是否为系统内置权限
}

export interface GetUserListDto extends PageQuery {
    username?: string;    // 用户名查询
}

export interface GetUserListVo {
    id: string;           // 用户ID
    username: string;     // 用户名
    nickname: string;     // 用户昵称
    email: string;        // 用户邮箱
    createTime: string;   // 创建时间
    lastLoginTime: string;// 最后登录时间
    status: number;       // 用户状态
}

export interface GetUserDetailsVo {
    id: string;           // 用户ID
    username: string;     // 用户名
    nickname: string;     // 用户昵称
    email: string;        // 用户邮箱
    status: number;       // 用户状态
    createTime: string;   // 创建时间
    lastLoginTime: string;// 最后登录时间
    groups: UserGroupVo[];       // 用户组列表
    permissions: UserPermissionVo[]; // 用户权限列表
}

export interface SaveUserDto {
    id?: string;          // 用户ID，创建时为空
    username: string;     // 用户名
    password?: string;    // 用户密码，创建时必填，编辑时可选
    nickname?: string;    // 用户昵称
    email?: string;       // 用户邮箱
    status?: number;      // 用户状态：0-禁用，1-启用
    groupIds?: string[];  // 用户组ID列表
}

export default {
    /**
     * 获取用户列表
     */
    getUserList: async (dto: GetUserListDto): Promise<PageableView<GetUserListVo>> => {
        return await Http.postEntity<PageableView<GetUserListVo>>('/admin/user/getUserList', dto);
    },

    /**
     * 获取用户详情
     */
    getUserDetails: async (dto: CommonIdDto): Promise<GetUserDetailsVo> => {
        return await Http.postEntity<GetUserDetailsVo>('/admin/user/getUserDetails', dto);
    },

    /**
     * 保存用户
     */
    saveUser: async (dto: SaveUserDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/user/saveUser', dto);
    },

    /**
     * 删除用户
     */
    removeUser: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/user/removeUser', dto);
    }
}