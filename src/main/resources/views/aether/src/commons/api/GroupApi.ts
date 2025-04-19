import type PageableView from "@/entity/PageableView.ts";
import type RestPageableView from "@/entity/RestPageableView.ts";
import Http from "@/commons/Http.ts";
import type GetGroupListDto from "@/entity/dto/GetGroupListDto.ts";
import type CommonIdDto from "@/entity/dto/CommonIdDto.ts";
import type SaveGroupDto from "@/entity/dto/SaveGroupDto.ts";
import type GetGroupListVo from "@/entity/vo/GetGroupListVo.ts";
import type GetGroupDetailsVo from "@/entity/vo/GetGroupDetailsVo.ts";
import type GetGroupDefinitionsVo from "@/entity/vo/GetGroupDefinitionsVo.ts";

export default {
    /**
     * 获取组定义列表
     */
    getGroupDefinitions: async (): Promise<GetGroupDefinitionsVo[]> => {
        return await Http.postEntity<GetGroupDefinitionsVo[]>('/admin/group/getGroupDefinitions', {});
    },

    /**
     * 获取组列表
     */
    getGroupList: async (dto: GetGroupListDto): Promise<RestPageableView<GetGroupListVo>> => {
        return await Http.postEntity<RestPageableView<GetGroupListVo>>('/admin/group/getGroupList', dto);
    },

    /**
     * 获取组详情
     */
    getGroupDetails: async (dto: CommonIdDto): Promise<GetGroupDetailsVo> => {
        return await Http.postEntity<GetGroupDetailsVo>('/admin/group/getGroupDetails', dto);
    },

    /**
     * 保存组
     */
    saveGroup: async (dto: SaveGroupDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/group/saveGroup', dto);
    },

    /**
     * 删除组
     */
    removeGroup: async (dto: CommonIdDto): Promise<string> => {
        return await Http.postEntity<string>('/admin/group/removeGroup', dto);
    }
}