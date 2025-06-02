<template>
  <div class="common-layout">
    <el-container>
      <!-- 桌面版侧边栏 -->
      <el-aside v-if="!isMobile" :width="isCollapse ? '64px' : '200px'" class="admin-sidebar">
        <admin-side-panel
          :items="menuItems"
          :active-item-id="activeMenuId"
          :is-collapse="isCollapse"
          title="管理控制台"
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
          title="管理控制台"
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
            
            <!-- 面包屑导航，放在头部区域 -->
            <el-breadcrumb v-if="autoBreadcrumbs.length && !isMobile" separator="/" class="admin-breadcrumb">
              <el-breadcrumb-item v-for="(item, index) in autoBreadcrumbs" :key="index" :to="item.to">
                {{ item.text }}
              </el-breadcrumb-item>
            </el-breadcrumb>
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
        
        <!-- 内容区域 -->
        <el-main class="admin-content">
          <div class="content-wrapper">
            <!-- 路由视图 -->
            <router-view v-slot="{ Component, route }">
              <transition name="fade" mode="out-in">
                <div :key="route.fullPath"> 
                  <keep-alive v-if="route.meta.keepAlive">
                    <component :is="Component" />
                  </keep-alive>
                  <component :is="Component" v-if="!route.meta.keepAlive" />
                </div>
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
  SetUp, Operation, DocumentCopy
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
    id: 'player',
    title: '玩家管理',
    icon: User,
    routerLink: '/admin/player'
  },
  {
    id: 'session',
    title: '会话管理',
    icon: User,
    routerLink: '/admin/session'
  },
  {
    id: 'player_default_group',
    title: '玩家默认访问组',
    icon: Lock,
    routerLink: '/admin/player/default/group'
  },
  {
    id: 'group',
    title: '访问组管理',
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
    title: 'AI模型参数',
    icon: Monitor,
    routerLink: '/admin/model'
  },
  {
    id: 'model_series',
    title: '模型变体管理',
    icon: Cpu,
    routerLink: '/admin/model/variant'
  },
  {
    id: 'model_param',
    title: '模型参数管理',
    icon: Setting,
    routerLink: '/admin/model/variant/param'
  },
  {
    id: 'param_template',
    title: '参数模板管理',
    icon: DocumentCopy,
    routerLink: '/admin/model/variant/param/template'
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
  // 如果 props 中提供了 breadcrumbs，则优先使用
  if (props.breadcrumbs?.length) return props.breadcrumbs;

  const revisedBreadcrumbs: Array<{ text: string; to?: string | object }> = [];
  
  // 尝试添加首页/根路径面包屑
  const homeRouteConfig = router.options.routes.find(r => r.path === '/');
  // Use optional chaining and type checking for safer access
  const homeBreadcrumbMeta = homeRouteConfig?.meta?.breadcrumb as { title?: string, hidden?: boolean } | undefined;
  if (homeBreadcrumbMeta?.title && route.path !== '/') {
      revisedBreadcrumbs.push({
          text: homeBreadcrumbMeta.title,
          to: '/'
      });
  }

  // 遍历匹配的路由记录
  route.matched.forEach((record, index) => {
      // 如果已经添加了首页，并且当前记录是根路径，则跳过
      if (record.path === '/' && revisedBreadcrumbs.length > 0 && revisedBreadcrumbs[0].to === '/') return;

      const meta = record.meta;
      let title = '';
      let hidden = false;
      const path = record.path; // 使用匹配路由的路径

      // 检查 meta.breadcrumb 配置
      // Use type assertion for breadcrumb meta structure
      const breadcrumbMeta = meta?.breadcrumb as { title?: string, hidden?: boolean } | undefined;
      if (breadcrumbMeta) {
          if (breadcrumbMeta.title) {
              title = breadcrumbMeta.title;
          }
          hidden = breadcrumbMeta.hidden === true;
      }

      // 如果 breadcrumb 中没有 title，尝试使用 meta.title
      // Check if meta.title exists and is a string
      if (!title && meta?.title && typeof meta.title === 'string') {
          title = meta.title;
      }

      // 如果有标题且不隐藏，则添加到面包屑数组中
      if (title && !hidden) {
           // 检查是否已存在（基于路径）
           const alreadyExists = revisedBreadcrumbs.some(b => b.to === path);
           if(!alreadyExists){
                revisedBreadcrumbs.push({
                    text: title,
                    // 最后一个面包屑（当前页面）不设置链接
                    to: index === route.matched.length - 1 ? undefined : path
                });
           }
      }
  });

  return revisedBreadcrumbs;
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(to bottom, #f0f2f5, #ffffff);
  border-right: none;
  height: 100%;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 10;
}

.admin-breadcrumb {
  font-size: 13px;
  line-height: 1;
  margin-left: 15px;
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

/* 侧边栏内部元素样式增强 */
:deep(.el-menu) {
  border-right: none;
  padding: 4px;
}

:deep(.el-menu-item), :deep(.el-sub-menu__title) {
  border-radius: 4px;
  margin: 1px 0;
  height: 44px;
  line-height: 44px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-menu-item.is-active) {
  position: relative;
  overflow: hidden;
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

:deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: var(--el-color-primary);
  transition: opacity 0.3s;
  border-radius: 0 2px 2px 0;
}

:deep(.el-menu-item:hover), :deep(.el-sub-menu__title:hover) {
  background-color: rgba(64, 158, 255, 0.08);
}

:deep(.el-menu--collapse .el-menu-item), :deep(.el-menu--collapse .el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
}

:deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  height: 40px;
  line-height: 40px;
}

:deep(.el-menu-item .el-icon), :deep(.el-sub-menu__title .el-icon) {
  margin-right: 8px;
  font-size: 16px;
}

:deep(.el-menu--collapse .el-menu-item .el-icon), 
:deep(.el-menu--collapse .el-sub-menu__title .el-icon) {
  margin: 0;
}

/* 移动端样式适配 */
.mobile-sidebar-drawer :deep(.el-drawer__body) {
  padding: 0;
  overflow: hidden;
  background: linear-gradient(to bottom, #f0f2f5, #ffffff);
}

.mobile-sidebar-drawer {
  --el-drawer-bg-color: transparent;
}

/* 移动端侧边栏滚动区域优化 */
.mobile-sidebar-drawer :deep(.admin-side-panel) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.mobile-sidebar-drawer :deep(.el-menu) {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
}

.mobile-sidebar-drawer :deep(.el-menu::-webkit-scrollbar) {
  width: 4px;
}

.mobile-sidebar-drawer :deep(.el-menu::-webkit-scrollbar-thumb) {
  background: rgba(144, 147, 153, 0.3);
  border-radius: 10px;
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
  
  .admin-breadcrumb {
    display: none; /* 在移动端隐藏面包屑 */
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