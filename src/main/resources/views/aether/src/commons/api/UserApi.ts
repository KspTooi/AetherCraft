import type PageableView from "@/entity/PageableView.ts";
import Http from "@/commons/Http.ts";
import type GetUserListDto from "@/entity/dto/GetUserListDto.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";
import type SaveUserDto from "@/entity/dto/SaveUserDto.ts";
import type GetUserListVo from "@/entity/vo/GetUserListVo.ts";
import type GetUserDetailsVo from "@/entity/vo/GetUserDetailsVo.ts";










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