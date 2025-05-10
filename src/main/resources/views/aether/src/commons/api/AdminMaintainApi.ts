import Http from "@/commons/Http.ts";
import type Result from "@/entity/Result.ts";

export default {
    /**
     * 校验系统内置权限节点
     */
    validateSystemPermissions: async (): Promise<string> => {
        const result = await Http.postRaw<string>('/admin/maintain/validSystemPermission', {});
        return result.message || '权限节点校验完成';
    },

    /**
     * 校验系统内置用户组
     */
    validateSystemGroups: async (): Promise<string> => {
        const result = await Http.postRaw<string>('/admin/maintain/validSystemGroup', {});
        return result.message || '用户组校验完成';
    },

    /**
     * 校验系统内置用户
     */
    validateSystemUsers: async (): Promise<string> => {
        const result = await Http.postRaw<string>('/admin/maintain/validSystemUsers', {});
        return result.message || '系统用户校验完成';
    },

    /**
     * 校验系统全局配置项
     */
    validateSystemConfigs: async (): Promise<string> => {
        const result = await Http.postRaw<string>('/admin/maintain/validSystemConfigs', {});
        return result.message || '全局配置校验完成';
    },

    /**
     * 强制为所有没有Player的用户创建Player
     */
    forceCreatePlayers: async (): Promise<string> => {
        const result = await Http.postRaw<string>('/admin/maintain/forceCreatePlayers', {});
        return result.message || '强制创建Player完成';
    }
}
