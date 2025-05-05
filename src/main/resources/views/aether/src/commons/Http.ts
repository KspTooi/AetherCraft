import type Result from "@/entity/Result.ts";
import axios from "axios";
// 不再需要单独导入 useRouter，因为我们直接使用 admin router
import router from '@/router/admin';

// 创建axios实例
const axiosInstance = axios.create({
    // 为所有请求添加自定义请求头
    headers: {
        'AE-Request-With': 'XHR'
    }
});

// 添加响应拦截器
axiosInstance.interceptors.response.use(
    response => response,
    error => {
        console.log("----------------- ERROR -----------------", error.response);

        // 检查是否为 401 Unauthorized 状态码
        if (error.response?.status === 401) {
            const data = error.response.data as Result<unknown>; // 类型断言
            
            // 检查是否为权限不足 (code = 2)
            if (data && data.code === 2 && data.message && data.message.includes('权限不足')) {
                console.log("捕获到 401 Unauthorized 且 code=2 (权限不足)，准备重定向到无权限页面");
                // 提取权限代码
                const permissionMatch = data.message.match(/权限不足：([\w:]+)/);
                const permissionCode = permissionMatch ? permissionMatch[1] : '';

                // 重定向到权限不足页面
                router.push({
                    name: 'no-permission',
                    query: {
                        message: data.message,
                        permissionCode: permissionCode
                    }
                });
                // 返回一个被拒绝的Promise，中断后续处理
                return Promise.reject(new Error(data.message)); 
            }
            
            // 如果不是权限不足的 401，则执行原来的重定向到登录页逻辑
            console.log("捕获到 401 Unauthorized 错误（非权限不足），准备重定向到登录页面");
            if (typeof window !== 'undefined') {
                const location = error.response.headers.location || error.response.headers.Location;
                if (location) {
                    window.location.href = location;
                } else {
                    window.location.href = '/login';
                }
            }
            // 返回一个被拒绝的Promise，中断后续处理
            return Promise.reject(error); 

        } else if (error.response?.status === 302 || error.response?.status === 301) {
             // 保留对 301/302 的处理
             const location = error.response.headers.location || error.response.headers.Location;
             if (location?.includes('/login')) {
                 if (typeof window !== 'undefined') {
                     window.location.href = '/login';
                 }
             }
             return Promise.reject(error); // 301/302也应中断
        }
        
        // 处理HTTP 400 (参数校验失败)
        if (error.response?.status === 400) {
            const data = error.response.data;
            if (data && typeof data === 'object' && data.code === 1 && typeof data.message === 'string') {
                // 直接抛出错误，让调用方处理
                throw new Error(`参数校验失败: \n${data.message}`);
            }
        }
        
        // 对于其他未处理的错误，继续传递
        return Promise.reject(error);
    }
);

export default {
    /**
     * 发送POST请求并返回Result包装的响应
     * @param url 请求URL
     * @param body 请求体
     * @returns Result包装的响应
     */
    async postRaw<T>(url:string, body:any): Promise<Result<T>> {
        try {
            const response = await axiosInstance.post<Result<T>>(url, body);
            return response.data;
        } catch (error) {
            // 拦截器已经处理了特定情况，这里只返回通用错误
            return {
                code: 2,
                data: null as T,
                message: error instanceof Error ? error.message : "请求失败"
            };
        }
    },

    /**
     * 发送POST请求并直接返回数据部分
     * @param url 请求URL
     * @param body 请求体
     * @returns 响应数据
     */
    async postEntity<T>(url:string, body:any): Promise<T> {
        const result = await this.postRaw<T>(url, body);
        
        // 拦截器已经处理了权限不足(code=2)和需要登录(401)的情况
        // 这里只需要处理 code 为 0 的成功情况和其他业务错误码
        if (result.code === 0) {
            // code 为 0 但 data 为 null 或 undefined 也可能是一种业务失败，但暂时按成功处理
            // 如果需要严格检查 data，可以在这里添加判断
            return result.data; 
        }
        
        // 抛出其他业务错误信息 (code != 0 且非权限不足)
        throw new Error(result.message || "请求失败或数据无效");
    }
}