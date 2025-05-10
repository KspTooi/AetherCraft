<script setup lang="ts">
import { ref } from 'vue'
import MaintainApi from '@/commons/api/AdminMaintainApi.ts'
import { ElMessage } from 'element-plus'
import { 
    Lock, 
    User, 
    Setting, 
    UserFilled 
} from '@element-plus/icons-vue'

// 定义维护操作接口类型
interface MaintainOperation {
    title: string;
    description: string;
    icon: any;
    action: () => Promise<void>;
    buttonText: string;
    bgColor: string;
    iconColor: string;
}

// 维护操作状态
const operationLoading = ref<{ [key: string]: boolean }>({
    permissions: false,
    groups: false,
    users: false,
    configs: false,
    forceCreatePlayers: false
})

// 执行维护操作的通用方法
const executeMaintainOperation = async (
    operationKey: keyof typeof operationLoading.value, 
    apiMethod: () => Promise<string>
) => {
    try {
        operationLoading.value[operationKey] = true
        const result = await apiMethod()
        ElMessage.success({
            message: result || '校验完成',
            type: 'success'
        })
    } catch (error: any) {
        ElMessage.error({
            message: error.message || '校验失败',
            type: 'error'
        })
    } finally {
        operationLoading.value[operationKey] = false
    }
}

// 维护操作列表
const maintainOperations: MaintainOperation[] = [
    {
        title: '权限节点校验',
        description: '校验系统内置权限节点是否完整，如有缺失将自动补充。',
        icon: Lock,
        buttonText: '权限节点校验',
        bgColor: 'rgba(64, 158, 255, 0.1)',
        iconColor: '#409EFF',
        action: () => executeMaintainOperation('permissions', MaintainApi.validateSystemPermissions)
    },
    {
        title: '用户组校验',
        description: '校验系统内置用户组是否完整，如有缺失将自动补充。管理员组将被赋予所有权限。',
        icon: UserFilled,
        buttonText: '用户组校验',
        bgColor: 'rgba(103, 194, 58, 0.1)',
        iconColor: '#67C23A',
        action: () => executeMaintainOperation('groups', MaintainApi.validateSystemGroups)
    },
    {
        title: '用户校验',
        description: '校验系统内置用户是否完整，如有缺失将自动补充。管理员用户将被赋予所有用户组。',
        icon: User,
        buttonText: '用户校验',
        bgColor: 'rgba(230, 162, 60, 0.1)',
        iconColor: '#E6A23C',
        action: () => executeMaintainOperation('users', MaintainApi.validateSystemUsers)
    },
    {
        title: '全局配置校验',
        description: '校验系统全局配置项是否完整，如有缺失将自动补充默认配置。',
        icon: Setting,
        buttonText: '全局配置校验',
        bgColor: 'rgba(144, 147, 153, 0.1)',
        iconColor: '#909399',
        action: () => executeMaintainOperation('configs', MaintainApi.validateSystemConfigs)
    },
    {
        title: '强制创建Player',
        description: '为所有没有Player角色的用户强制创建Player角色。',
        icon: UserFilled,
        buttonText: '强制创建Player',
        bgColor: 'rgba(245, 108, 108, 0.1)',
        iconColor: '#F56C6C',
        action: () => executeMaintainOperation('forceCreatePlayers', MaintainApi.forceCreatePlayers)
    }
]
</script>

<template>
    <div class="application-maintain">
        <div class="page-header">
            <h1 class="page-title">系统维护工具</h1>
            <p class="page-description">提供各种系统维护功能，保障系统稳定运行</p>
        </div>
        
        <div class="maintain-cards">
            <div 
                v-for="(operation, index) in maintainOperations" 
                :key="index" 
                class="maintain-card"
                :style="{ '--card-bg-color': operation.bgColor }"
            >
                <div class="card-icon-wrapper" :style="{ backgroundColor: operation.bgColor }">
                    <el-icon :size="30" :color="operation.iconColor">
                        <component :is="operation.icon" />
                    </el-icon>
                </div>
                <div class="card-content">
                    <h3 class="card-title">{{ operation.title }}</h3>
                    <p class="card-description">{{ operation.description }}</p>
                    <el-button 
                        type="primary" 
                        :loading="operationLoading[
                            index === 0 ? 'permissions' : 
                            index === 1 ? 'groups' : 
                            index === 2 ? 'users' : 
                            index === 3 ? 'configs' : 
                            'forceCreatePlayers'
                        ]"
                        @click="operation.action"
                        class="card-button"
                    >
                        <el-icon class="button-icon"><component :is="operation.icon" /></el-icon>
                        {{ operation.buttonText }}
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.application-maintain {
    padding: 24px;
    max-width: 1200px;
    margin: 0 auto;
}

.page-header {
    margin-bottom: 32px;
    text-align: center;
}

.page-title {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
}

.page-description {
    font-size: 16px;
    color: #606266;
}

.maintain-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 24px;
}

.maintain-card {
    position: relative;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    transition: all 0.3s ease;
}

.maintain-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.maintain-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background-color: var(--card-bg-color, rgba(64, 158, 255, 0.1));
}

.card-icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    border-radius: 50%;
    margin: 24px auto 16px;
}

.card-content {
    padding: 0 24px 24px;
    text-align: center;
}

.card-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
}

.card-description {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 24px;
    height: 67px;
    overflow: hidden;
}

.card-button {
    width: 100%;
    border-radius: 4px;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
}

.button-icon {
    margin-right: 8px;
}

@media (max-width: 768px) {
    .maintain-cards {
        grid-template-columns: 1fr;
    }
    
    .application-maintain {
        padding: 16px;
    }
    
    .card-description {
        height: auto;
    }
}
</style>