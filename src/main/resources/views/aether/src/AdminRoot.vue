<script setup lang="ts">
import { ref, reactive, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import AdminFrame from './components/element-ui-admin/AdminFrame.vue'
import { ElLoading, ElLoadingService, ElDropdownItem, ElIcon } from 'element-plus'
import { User, Setting, InfoFilled } from '@element-plus/icons-vue'

// 使用 markRaw 包装图标组件
const UserIcon = markRaw(User);
const SettingIcon = markRaw(Setting);
const InfoFilledIcon = markRaw(InfoFilled);

const router = useRouter()
const isLoading = ref(true)

// 模拟用户数据
const currentUser = reactive({
  name: '管理员',
  avatar: '', // 可以添加头像路径
  role: 'admin'
})

// 处理菜单点击
const handleMenuClick = (menuId: string) => {
  console.log(`菜单点击: ${menuId}`)
}

// 处理菜单操作
const handleMenuAction = (action: string, menuId: string) => {
  console.log(`执行操作: ${action}, 菜单ID: ${menuId}`)
}

// 处理登出
const handleLogout = () => {
  // 这里添加登出逻辑
  router.push('/login')
}

// 创建loading实例
let loadingInstance: ReturnType<typeof ElLoadingService> | null = null;

onMounted(() => {
  // 管理台初始化逻辑
  // 使用 ElLoadingService 而非组件形式
  loadingInstance = ElLoading.service({
    fullscreen: true,
    lock: true
  });
  
  setTimeout(() => {
    // 关闭loading
    if (loadingInstance) {
      loadingInstance.close();
      loadingInstance = null;
    }
    isLoading.value = false;
  }, 500)
})
</script>

<template>
  <admin-frame 
    v-if="!isLoading"
    title="管理控制台"
    :user="currentUser"
    @menu-click="handleMenuClick"
    @menu-action="handleMenuAction"
    @logout="handleLogout"
  >
    <!-- 用户下拉菜单额外项目插槽 -->
    <template #user-dropdown>
      <el-dropdown-item>
        <el-icon><component :is="UserIcon" /></el-icon>
        <span>个人信息</span>
      </el-dropdown-item>
      <el-dropdown-item>
        <el-icon><component :is="SettingIcon" /></el-icon>
        <span>系统设置</span>
      </el-dropdown-item>
      <el-dropdown-item divided>
        <el-icon><component :is="InfoFilledIcon" /></el-icon>
        <span>帮助</span>
      </el-dropdown-item>
    </template>
  </admin-frame>
</template>

<style>
/* 全局样式，禁止页面滚动 */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

/* 按钮内图标样式 */
.icon-in-button {
  margin-right: 4px;
}
</style>

<style scoped>
.loading-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>