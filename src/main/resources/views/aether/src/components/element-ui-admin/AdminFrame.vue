<template>
  <div class="common-layout">
    <el-container>
      <!-- 桌面版侧边栏 -->
      <el-aside v-if="!isMobile" :width="isCollapse ? '64px' : '200px'" class="admin-sidebar">
        <admin-side-panel
          :items="menuItems"
          :active-item-id="activeMenuId"
          :is-collapse="isCollapse"
          @item-click="handleMenuClick"
          @action="handleMenuAction"
        />
      </el-aside>
      
      <!-- 移动端侧边栏抽屉 -->
      <el-drawer
        v-if="isMobile"
        v-model="drawerVisible"
        direction="ltr"
        size="80%"
        :with-header="false"
        class="mobile-sidebar-drawer"
      >
        <admin-side-panel
          :items="menuItems"
          :active-item-id="activeMenuId"
          :is-collapse="false"
          @item-click="handleMenuClick"
          @action="handleMenuAction"
        />
      </el-drawer>
      
      <el-container>
        <!-- 头部区域 -->
        <el-header class="admin-header" height="60px">
          <div class="header-left">
            <!-- 移动端显示菜单按钮 -->
            <el-icon class="menu-toggle" @click="toggleMobileMenu">
              <Menu v-if="isMobile" />
              <Expand v-else-if="isCollapse" />
              <Fold v-else />
            </el-icon>
            
            <div class="logo-wrapper">
              <img v-if="logo" :src="logo" class="logo-image" />
              <span class="logo-text">{{ title }}</span>
            </div>
          </div>
          <div class="header-right">
            <!-- 系统导航按钮区域 -->
            <div class="nav-buttons">
              <el-button 
                type="primary" 
                plain 
                size="small" 
                @click="navigateToUrl('/client-ui')"
                class="nav-button"
              >
                <el-icon><Back /></el-icon>
                <span class="button-text">返回客户端</span>
              </el-button>
              <el-button 
                type="info" 
                plain 
                size="small" 
                @click="navigateToUrl('/dashboard')"
                class="nav-button"
              >
                <el-icon><Menu /></el-icon>
                <span class="button-text">旧版管理台</span>
              </el-button>
            </div>
            
            <!-- 用户自定义操作区域 -->
            <slot name="header-actions"></slot>
            
            <!-- 用户信息和下拉菜单 -->
            <el-dropdown trigger="click" v-if="user">
              <div class="user-info">
                <el-avatar :size="32" :src="user.avatar">{{ user.name?.substring(0, 1) }}</el-avatar>
                <span class="username" v-if="!isMobile">{{ user.name }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <slot name="user-dropdown"></slot>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 面包屑导航，独立于内容区域 -->
        <div v-if="autoBreadcrumbs.length && !isMobile" class="breadcrumb-container">
          <el-breadcrumb separator="/" class="admin-breadcrumb">
            <el-breadcrumb-item v-for="(item, index) in autoBreadcrumbs" :key="index" :to="item.to">
              {{ item.text }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <!-- 内容区域 -->
        <el-main class="admin-content">
          <div class="content-wrapper">
            <!-- 路由视图 -->
            <router-view v-slot="{ Component }">
              <transition name="fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AdminSidePanel from './AdminSidePanel.vue'
import { 
  ElContainer, ElHeader, ElAside, ElMain, 
  ElBreadcrumb, ElBreadcrumbItem, ElIcon, 
  ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar,
  ElLoading, ElButton, ElDrawer
} from 'element-plus'
import { 
  Fold, Expand, Back, Menu, 
  User, Avatar, UserFilled, Lock, Setting, Key, 
  Grid, Briefcase, Tools, Monitor, Opportunity, 
  Collection, CollectionTag, Cpu, Trophy, List,
  HomeFilled, Refrigerator, Ticket, Management,
  SetUp, Operation
} from '@element-plus/icons-vue'

// 定义组件props
const props = defineProps<{
  title?: string,
  logo?: string,
  user?: {
    name: string,
    avatar?: string,
    [key: string]: any
  },
  defaultActiveMenuId?: string,
  breadcrumbs?: Array<{
    text: string,
    to?: string | object
  }>
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'menu-click', menuId: string): void
  (e: 'menu-action', action: string, menuId: string): void
  (e: 'logout'): void
}>()

const router = useRouter()
const route = useRoute()

// 导航到指定URL
const navigateToUrl = (url: string) => {
  window.location.href = url
}

// 内置菜单项定义，与旧版保持完全一致
const menuItems = ref([
  {
    id: 'user',
    title: '用户管理',
    icon: User,
    routerLink: '/admin/user'
  },
  {
    id: 'group',
    title: '用户组管理',
    icon: Lock,
    routerLink: '/admin/group'
  },
  {
    id: 'permission',
    title: '权限节点',
    icon: Key,
    routerLink: '/admin/permission'
  },
  {
    id: 'config',
    title: '配置项',
    icon: SetUp,
    routerLink: '/admin/config'
  },
  {
    id: 'model',
    title: 'AI模型配置',
    icon: Monitor,
    routerLink: '/admin/model'
  },
  {
    id: 'apikey',
    title: 'API密钥管理',
    icon: Key,
    routerLink: '/admin/model/apikey'
  },
  {
    id: 'maintain',
    title: '维护工具',
    icon: Tools,
    routerLink: '/admin/maintain'
  }
])

// 侧边栏折叠状态
const isCollapse = ref(false)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 移动端菜单状态
const isMobile = ref(false)
const drawerVisible = ref(false)

// 检测窗口大小变化
const checkMobileView = () => {
  isMobile.value = window.innerWidth <= 768
  // 在移动端默认折叠菜单
  if (isMobile.value) {
    isCollapse.value = true
  } else {
    // 在大屏幕上展开菜单，关闭抽屉
    drawerVisible.value = false
  }
}

// 移动端菜单切换
const toggleMobileMenu = () => {
  if (isMobile.value) {
    drawerVisible.value = !drawerVisible.value
  } else {
    toggleSidebar()
  }
}

// 菜单项点击事件，在移动端上自动关闭菜单
const handleMenuItemClick = (menuId: string) => {
  emit('menu-click', menuId)
  if (isMobile.value) {
    drawerVisible.value = false
  }
}

// 初始化和清理
onMounted(() => {
  checkMobileView()
  window.addEventListener('resize', checkMobileView)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobileView)
})

// 路由变化时在移动端关闭菜单
watch(() => route.path, () => {
  if (isMobile.value) {
    drawerVisible.value = false
  }
})

// 根据路由路径计算当前活动菜单ID
const findMenuIdByPath = (items: any[], path: string): string => {
  for (const item of items) {
    if (item.routerLink === path) {
      return item.id
    }
    if (item.children?.length) {
      const foundId = findMenuIdByPath(item.children, path)
      if (foundId) return foundId
    }
  }
  return ''
}

// 当前活动菜单
const activeMenuId = computed(() => {
  if (props.defaultActiveMenuId) return props.defaultActiveMenuId
  return findMenuIdByPath(menuItems.value, route.path)
})

// 处理菜单点击
const handleMenuClick = (menuId: string) => {
  handleMenuItemClick(menuId)
}

// 处理菜单动作
const handleMenuAction = (action: string, menuId: string) => {
  emit('menu-action', action, menuId)
  if (isMobile.value) {
    drawerVisible.value = false
  }
}

// 处理退出登录
const handleLogout = () => {
  emit('logout')
}

// 自动生成面包屑导航
const autoBreadcrumbs = computed(() => {
  if (props.breadcrumbs?.length) return props.breadcrumbs

  const paths = route.path.split('/').filter(Boolean)
  if (paths.length <= 1) return []
  
  const items = [{ text: '首页', to: '/admin' }]
  let currentPath = ''
  
  for (const path of paths.slice(1)) {
    currentPath += `/${path}`
    // 简化演示，直接使用路径名
    items.push({
      text: path.charAt(0).toUpperCase() + path.slice(1),
      to: currentPath
    })
  }
  
  return items
})
</script>

<style scoped>
.common-layout {
  height: 100%;
  width: 100%;
  overflow: hidden;
  display: flex;
}

.el-container {
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.admin-header {
  background-color: #fff;
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  height: 60px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.logo-image {
  height: 32px;
  margin-right: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

.menu-toggle {
  font-size: 20px;
  cursor: pointer;
  color: var(--el-text-color-secondary);
  margin-right: 15px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-buttons {
  display: flex;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.admin-sidebar {
  transition: width 0.3s;
  background-color: #fff;
  border-right: 1px solid var(--el-border-color-light);
  height: 100%;
  overflow: hidden;
}

/* 面包屑容器样式 */
.breadcrumb-container {
  background-color: #fff;
  border-bottom: 1px solid var(--el-border-color-light);
  padding: 8px 20px;
  flex-shrink: 0;
}

.admin-breadcrumb {
  font-size: 13px;
  line-height: 1;
}

.admin-content {
  background-color: var(--el-bg-color-page);
  padding: 0;
  height: 100%;
  overflow: auto;
  flex: 1;
}

.content-wrapper {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  min-height: 100%;
  padding: 0;
}

/* 移动端样式适配 */
.mobile-sidebar-drawer :deep(.el-drawer__body) {
  padding: 0;
  overflow: hidden;
}

.mobile-sidebar-drawer {
  --el-drawer-bg-color: var(--el-bg-color);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .admin-content {
    padding: 0;
  }
  
  .content-wrapper {
    padding: 0;
  }
  
  .nav-button .button-text {
    display: none;
  }
  
  .admin-header {
    padding: 0 10px;
  }
  
  .logo-text {
    font-size: 16px;
  }
  
  .header-right {
    gap: 8px;
  }
  
  .breadcrumb-container {
    padding: 6px 10px;
  }
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>