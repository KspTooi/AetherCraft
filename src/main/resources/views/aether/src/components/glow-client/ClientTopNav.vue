<template>
  <glow-div border="bottom" class="navbar" @click="handleNavbarClick">
    <div class="container-fluid">
      <!-- 品牌名称 - 居中显示 -->
      <div class="navbar-brand brand-gradient">
        {{ brand }}
      </div>

      <!-- 导航内容 - 仅在桌面端显示 -->
      <div
        class="navbar-collapse"
        id="navbarContent"
        @click.stop
      >
        <!-- 左侧导航项 -->
        <ul class="navbar-nav nav-left">
          <li class="nav-item" v-for="(item, index) in leftItems" :key="index">
            <router-link v-if="item.routerLink" class="nav-link" :to="item.routerLink" @click="handleNavItemClick">{{ item.name }}</router-link>
            <a v-else-if="item.href" class="nav-link" :href="item.href" @click="handleNavItemClick">{{ item.name }}</a>
            <a v-else-if="item.action" class="nav-link cursor-pointer" @click="handleAction(item.action)">{{ item.name }}</a>
          </li>
        </ul>
        
        <!-- 右侧导航项 -->
        <ul class="navbar-nav nav-right">
          <li class="nav-item" v-for="(item, index) in rightItems" :key="'right-' + index">
            <router-link v-if="item.routerLink" class="nav-link" :to="item.routerLink" @click="handleNavItemClick">{{ item.name }}</router-link>
            <a v-else-if="item.href" class="nav-link" :href="item.href" @click="handleNavItemClick">{{ item.name }}</a>
            <a v-else-if="item.action" class="nav-link cursor-pointer" @click="handleAction(item.action)">{{ item.name }}</a>
          </li>
          <!-- Player Info Item & Dropdown Trigger -->
          <li
            class="nav-item player-info-container"
            v-if="!playerStore.isLoading"
            v-click-outside="closePlayerDropdown"
            @mouseenter="openPlayerDropdown" 
            @mouseleave="closePlayerDropdown" 
          >
            <div
              class="player-info-item"
              :class="{ 'disconnected-info': !playerStore.currentPlayer }"
            >
              <!-- Avatar/Icon -->
              <div class="player-avatar" :class="{ 'disconnected-icon': !playerStore.currentPlayer }">
                <img v-if="playerStore.currentPlayer" :src="getAvatarUrl(playerStore.currentPlayer.avatarUrl)" alt="Player Avatar">
                <i v-else class="bi bi-person"></i>
              </div>
              <!-- Name/Status -->
              <span class="player-name nav-link-like">{{ playerStore.currentPlayer?.name || '未连接' }}</span>
               <!-- Dropdown Arrow -->
              <div class="select-arrow player-dropdown-arrow">
                 <i class="bi bi-chevron-down" :class="{ 'rotated': isPlayerDropdownOpen }"></i>
              </div>
            </div>

            <!-- Player Context Dropdown Menu -->
            <transition name="dropdown">
              <div class="dropdown-menu player-dropdown-menu" v-show="isPlayerDropdownOpen">
                 <div
                   class="dropdown-item"
                   v-for="(item, index) in playerContextItems"
                   :key="'ctx-' + index"
                   @click="handlePlayerAction(item.action)"
                 >
                   {{ item.name }}
                 </div>
                 <div v-if="!playerContextItems || playerContextItems.length === 0" class="dropdown-item disabled">
                   (无选项)
                 </div>
              </div>
            </transition>
          </li>
          <!-- Loading Placeholder -->
          <li class="nav-item player-info-item loading-placeholder" v-else>
            <span>...</span>
          </li>
        </ul>
      </div>
    </div>
    
    <!-- 高亮线 -->
    <div class="nav-highlight-container">
      <div class="nav-highlight-line" :style="highlightStyle"></div>
    </div>
  </glow-div>


  <glow-div border="none" class="mobile-menu" :class="{ 'menu-expanded': isExpanded }" @click.stop>
    <!-- Mobile Player Info Header -->
    <div class="mobile-player-header" v-if="!playerStore.isLoading">
        <div v-if="playerStore.currentPlayer" class="player-info-item">
            <div class="player-avatar">
                <img :src="getAvatarUrl(playerStore.currentPlayer.avatarUrl)" alt="Player Avatar">
            </div>
            <span class="player-name">{{ playerStore.currentPlayer.name }}</span>
        </div>
        <div v-else class="player-info-item disconnected-info">
            <div class="player-avatar disconnected-icon">
                <i class="bi bi-person"></i>
            </div>
            <span class="player-name">未连接</span>
        </div>
    </div>
    <div class="mobile-player-header loading-placeholder" v-else>
        <span>加载中...</span>
    </div>

    <!-- Mobile Navigation Items -->
    <ul class="mobile-nav-left">
      <li class="mobile-nav-item" v-for="(item, index) in leftItems" :key="index">
        <router-link v-if="item.routerLink" class="mobile-nav-link" :to="item.routerLink" @click="handleNavItemClick">{{ item.name }}</router-link>
        <a v-else-if="item.href" class="mobile-nav-link" :href="item.href" @click="handleNavItemClick">{{ item.name }}</a>
        <a v-else-if="item.action" class="mobile-nav-link cursor-pointer" @click="handleAction(item.action)">{{ item.name }}</a>
      </li>
    </ul>
    <ul class="mobile-nav-right">
      <li class="mobile-nav-item" v-for="(item, index) in rightItems" :key="index">
        <router-link v-if="item.routerLink" class="mobile-nav-link" :to="item.routerLink" @click="handleNavItemClick">{{ item.name }}</router-link>
        <a v-else-if="item.href" class="mobile-nav-link" :href="item.href" @click="handleNavItemClick">{{ item.name }}</a>
        <a v-else-if="item.action" class="mobile-nav-link cursor-pointer" @click="handleAction(item.action)">{{ item.name }}</a>
      </li>
    </ul>
    <!-- Mobile Player Context Actions -->
    <ul class="mobile-nav-context" v-if="playerStore.currentPlayer">
        <li class="mobile-nav-item context-divider"></li> <!-- Optional Divider -->
        <li class="mobile-nav-item" v-for="(item, index) in playerContextItems" :key="'mobile-ctx-' + index">
             <a v-if="item.action" class="mobile-nav-link cursor-pointer" @click="handlePlayerAction(item.action)">{{ item.name }}</a>
        </li>
    </ul>
  </glow-div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'
import { inject } from 'vue'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue"
import PlayerApi, { type GetCurrentPlayerVo } from "@/commons/api/PlayerApi.ts"; // Import Player API
import { usePlayerStore } from "@/stores/player"; // Import the player store

// 定义组件props
const props = defineProps<{
  leftItems: Array<{
    name: string,
    routerLink?: string,
    href?: string,
    action?: string
  }>,
  rightItems: Array<{
    name: string,
    routerLink?: string,
    href?: string,
    action?: string
  }>,
  playerContextItems?: Array<{
    name: string,
    action?: string
  }>,
  brand: string
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'action', action: string): void
  (e: 'player-action', action: string): void
}>()

const router = useRouter()
const route = useRoute()
const isExpanded = ref(false)
const navbarHeight = ref(56) // 默认导航栏高度

// 获取 glow 主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// Instantiate the player store
const playerStore = usePlayerStore();

// 记录当前激活的导航项索引
const activeNavIndex = ref(-1)
const defaultAvatarUrl = '/imgs/default-avatar.png'; // Default avatar path
const isPlayerDropdownOpen = ref(false); // State for player dropdown

// 路由映射表 - 将一些特殊路由映射到导航项
const routeMapping: Record<string, string> = {
  '/model-role-manager': '/rp', // 将角色管理页面映射到角色扮演导航项
  // 可以根据需要添加更多映射
}

// 获取头像URL，处理null或相对路径
const getAvatarUrl = (url: string | undefined | null): string => {
  if (!url) {
    return defaultAvatarUrl;
  }
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/')) {
    return url;
  }
  return `/res/${url}`; // Add /res/ prefix for relative paths
};

// 更新高亮线位置
const updateHighlightPosition = () => {
  // 获取当前路由路径
  let currentPath = route.path
  
  // 检查当前路由是否需要映射到其他导航项
  if (routeMapping[currentPath]) {
    currentPath = routeMapping[currentPath]
  }
  
  // 检查哪个导航项匹配当前路由
  for (let i = 0; i < props.leftItems.length; i++) {
    const item = props.leftItems[i]
    if ((item.routerLink && (currentPath === item.routerLink || currentPath.startsWith(item.routerLink)))) {
      activeNavIndex.value = i
      return
    }
  }
  
  // 如果没有匹配项，设为默认值
  activeNavIndex.value = -1
}

// 更新高亮线位置和宽度
const highlightStyle = computed(() => {
  if (activeNavIndex.value < 0) {
    return {
      left: '0px',
      width: '0px',
      opacity: 0
    }
  }
  
  // 由于DOM可能尚未挂载，通过setTimeout确保DOM已渲染
  setTimeout(() => {
    updateHighlightPosition();
  }, 200);
  
  // 获取所有导航链接项
  const navItemElements = document.querySelectorAll('.nav-left .nav-item');
  if (navItemElements.length === 0 || activeNavIndex.value >= navItemElements.length) {
    return {
      left: '0px',
      width: '0px',
      opacity: 0
    }
  }
  
  // 获取当前激活的导航项
  const activeItem = navItemElements[activeNavIndex.value] as HTMLElement;
  if (!activeItem) {
    return {
      left: '0px',
      width: '0px',
      opacity: 0
    }
  }
  
  // 获取导航栏与导航项的位置
  const navbar = document.querySelector('.navbar') as HTMLElement;
  const activeItemRect = activeItem.getBoundingClientRect();
  const navbarRect = navbar?.getBoundingClientRect();
  
  // 计算相对于导航栏的偏移，考虑品牌宽度
  const leftOffset = activeItemRect.left - (navbarRect?.left || 0);
  
  return {
    left: `${leftOffset}px`,
    width: `${activeItemRect.width}px`,
    opacity: 1
  };
})

// 处理整个导航栏点击 - 在移动端展开/关闭导航栏
const handleNavbarClick = () => {
  if (window.innerWidth < 768) { 
    isExpanded.value = !isExpanded.value
    console.log('导航栏点击 - isExpanded:', isExpanded.value)
  }
}

// 处理导航项点击 - 关闭导航栏
const handleNavItemClick = () => {
  if (window.innerWidth < 768) {
    isExpanded.value = false
  }
}

// 处理动作点击
const handleAction = (action: string) => {
  emit('action', action)
  handleNavItemClick() // Close mobile nav if open
}

// 处理点击外部 - 关闭导航栏
const handleClickOutside = (event: MouseEvent) => {
  if (window.innerWidth < 768 && isExpanded.value) {
    const navbar = document.querySelector('.navbar')
    const target = event.target as HTMLElement
    
    // 如果点击的不是导航栏内的元素，则关闭导航栏
    if (navbar && !navbar.contains(target)) {
      isExpanded.value = false
    }
  }
}

// Open player dropdown on hover
const openPlayerDropdown = () => {
  isPlayerDropdownOpen.value = true;
};

// Close player dropdown (used by mouseleave and click-outside)
const closePlayerDropdown = () => {
  isPlayerDropdownOpen.value = false;
};

// Handle actions from the player context menu
const handlePlayerAction = (action: string | undefined) => {
  if (action) {
    emit('player-action', action);
  }
  closePlayerDropdown(); // Close dropdown after action
  handleNavItemClick(); // Close mobile nav if open
}

// 挂载时初始化
onMounted(() => {
  // 添加全局点击事件监听
  document.addEventListener('click', handleClickOutside)
  
  // 初始化高亮位置
  updateHighlightPosition()
  
  // 监听路由变化
  router.afterEach(() => {
    updateHighlightPosition()
  })
  
  // 计算导航栏高度
  updateNavbarHeight()
  window.addEventListener('resize', updateNavbarHeight)
  
  // Load player info using the store action
  playerStore.updatePlayerInfo();
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('resize', updateNavbarHeight)
})

// 计算导航栏高度
const updateNavbarHeight = () => {
  nextTick(() => {
    const navbar = document.querySelector('.navbar') as HTMLElement
    if (navbar) {
      navbarHeight.value = navbar.offsetHeight
      
      // 更新移动菜单位置
      const mobileMenu = document.querySelector('.mobile-menu') as HTMLElement
      if (mobileMenu) {
        mobileMenu.style.top = `${navbarHeight.value}px`
      }
    }
  })
}
</script>

<style scoped>
/* 基本样式 */
.navbar {
  position: relative;
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  width: 100%;
  padding: 0;
  z-index: 100;
}

.container-fluid {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  padding: 0 0.8rem;
  margin: 0;
}

/* 品牌样式 */
.navbar-brand {
  display: flex;
  align-items: center;
  font-size: 1.4rem;
  font-weight: bold;
  cursor: pointer;
  padding: 0.5rem 0.8rem 0.5rem 0;
  margin-right: 1rem;
  white-space: nowrap;
  color: v-bind('theme.boxGlowColor') !important;
  letter-spacing: 0.8px;
  transition: color 0.2s ease;
}

.navbar-brand:hover {
  color: v-bind('theme.boxTextColor') !important;
}

/* 导航项样式 */
.navbar-nav {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-left {
  margin-right: auto;
}

.nav-right {
  margin-left: auto;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 0.3rem;
}

.nav-link {
  display: flex;
  align-items: center;
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 0.9rem;
  text-decoration: none;
  padding: 0.5rem 0.6rem;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.nav-link:hover {
  color: v-bind('theme.boxTextColor');
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.4);
}

.nav-link.router-link-active {
  color: #ffffff;
  font-weight: 500;
}

/* 高亮线样式 */
.nav-highlight-container {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  overflow: visible;
  pointer-events: none;
  z-index: 10;
}

.nav-highlight-line {
  position: absolute;
  bottom: 0;
  height: 1px;
  background: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 10px 2px v-bind('theme.boxGlowColor');
  transition: all 0.3s ease;
}

/* 移动菜单样式 */
.mobile-menu {
  display: none;
  position: fixed;
  top: 56px; /* 初始值，将通过JS动态更新 */
  left: 0;
  width: 100%;
  background-color: v-bind('theme.boxAccentColor');
  backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
  z-index: 999; /* 略低于导航栏 */
  padding: 0;
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.4s cubic-bezier(0, 1, 0, 1);
  border-top: 0 solid transparent;
  border-bottom: 0 solid transparent;
  box-shadow: none;
}

.menu-expanded {
  max-height: 80vh;
  transition: max-height 0.4s ease-in-out;
  border-top: 1px solid v-bind('theme.boxBorderColor');
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.mobile-nav-left, .mobile-nav-right {
  list-style: none;
  margin: 0;
  padding: 0;
  opacity: 0;
  transform: translateY(-10px);
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.menu-expanded .mobile-nav-left,
.menu-expanded .mobile-nav-right {
  opacity: 1;
  transform: translateY(0);
  transition-delay: 0.1s;
}

.mobile-nav-item {
  width: 100%;
  padding: 0;
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
}

.mobile-nav-item:last-child {
  border-bottom: none;
}

.mobile-nav-link {
  display: block;
  width: 100%;
  padding: 1rem 1.5rem;
  color: v-bind('theme.boxTextColor');
  text-decoration: none;
  text-align: left;
  transition: background-color 0.2s ease, color 0.2s ease;
  font-size: 1rem;
}

.mobile-nav-link:hover {
  background-color: v-bind('theme.mainColorHover');
  color: v-bind('theme.boxTextColor');
}

.mobile-nav-link.router-link-active {
  color: #ffffff;
  font-weight: 500;
  background-color: v-bind('theme.boxAccentColorHover');
}

/* 桌面端样式 */
@media (min-width: 768px) {
  .navbar-collapse {
    display: flex !important;
    flex-basis: auto;
    width: 100%;
  }
  
  .navbar-nav {
    flex-direction: row;
    align-items: center;
  }
  
  .mobile-menu {
    display: none !important;
  }
}

/* 移动端样式 */
@media (max-width: 767.98px) {
  .navbar {
    flex-wrap: wrap;
    cursor: pointer;
  }
  
  .container-fluid {
    justify-content: center;
    text-align: center;
  }
  
  .navbar-brand {
    margin: 0 auto;
    text-align: center;
    padding: 0.5rem 0;
  }
  
  .navbar-collapse {
    display: none !important;
  }
  
  .nav-highlight-container {
    display: none;
  }
  
  .mobile-menu {
    display: block;
  }
  /* Hide player info in mobile header (can be added to mobile menu if needed) */
  .player-info-item {
      display: none;
  }
}

.cursor-pointer {
  cursor: pointer;
}

/* Player Info Styles */
.player-info-container {
  position: relative; /* Needed for dropdown positioning */
  padding: 0; /* Remove default nav-item padding */
}

.player-info-item {
  display: flex;
  align-items: center;
  margin-left: 1rem; /* Space from previous item */
  padding: 6px 8px; /* Add some padding for click area */
  cursor: pointer;
  border-radius: 3px; /* Optional: slight rounding */
  transition: background-color 0.2s ease;
}

.player-info-item:hover {
  background-color: v-bind('theme.boxSecondColorHover');
}

.player-avatar {
  width: 26px;
  height: 26px;
  overflow: hidden;
  margin-right: 0.5rem;
  flex-shrink: 0;
  background-color: v-bind('theme.boxSecondColor');
  display: flex; /* Center icon */
  align-items: center;
  justify-content: center;
}

.player-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.player-name {
  /* color: v-bind('theme.boxTextColor'); Removed explicit color here, handled by nav-link-like */
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px; /* Adjust max-width */
  font-weight: 500;
  margin-right: 0.3rem; /* Space before arrow */
}

.player-dropdown-arrow {
  color: v-bind('theme.boxTextColorNoActive');
  transition: transform 0.3s ease, color 0.3s ease;
}

.player-dropdown-arrow i {
  font-size: 14px;
  display: block;
}

.player-dropdown-arrow i.rotated {
  transform: rotate(180deg);
}

.player-info-item:hover .player-dropdown-arrow {
  color: v-bind('theme.boxTextColor');
}

/* Player Dropdown Menu Styles */
.player-dropdown-menu {
  position: absolute;
  top: calc(100% + 4px); /* Position below the trigger */
  right: 0; /* Align to the right */
  min-width: 100px; /* Minimum width */
  background: v-bind('theme.boxSecondColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  z-index: 1010; /* Ensure it's above other content */
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  padding: 5px 0;
  backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
}

.dropdown-item {
  padding: 8px 15px;
  color: v-bind('theme.boxTextColor');
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.dropdown-item:hover {
  background: v-bind('theme.boxSecondColorHover');
}

.dropdown-item.disabled {
  color: v-bind('theme.boxTextColorNoActive');
  cursor: default;
  opacity: 0.6;
  background: transparent !important;
}

/* Dropdown animation (copied from ModelSelector) */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: top right;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-5px);
}

/* Apply nav-link like color behavior */
.nav-link-like {
    color: v-bind('theme.boxTextColorNoActive');
    transition: color 0.2s ease;
    /* Inherit other relevant styles if needed, like padding, but likely not needed here */
}

/* Hover effect for player info item */
.player-info-item:hover .nav-link-like {
    color: v-bind('theme.boxTextColor');
}

/* Adjust right nav alignment if needed */
.nav-right {
  align-items: center; /* Vertically align items including player info */
}

/* Disconnected State Styles */
.disconnected-info .player-avatar {
    /* Style placeholder icon container like the avatar */
    display: flex;
    align-items: center;
    justify-content: center;
}

.disconnected-icon i {
    font-size: 18px; /* Adjust icon size */
    color: v-bind('theme.boxTextColorNoActive');
}

/* Mobile Styles Adjustments */
@media (max-width: 767.98px) {
  /* Hide player info in the main top bar on mobile */
  .player-info-container {
      display: none;
  }
}

/* --- Mobile Menu Specific Styles --- */

.mobile-menu {
  /* ... (existing mobile-menu styles) ... */
  display: flex; /* Use flex for better layout */
  flex-direction: column;
}

.mobile-player-header {
    padding: 10px 1.5rem; /* Consistent padding with nav links */
    border-bottom: 1px solid v-bind('theme.boxBorderColor');
    background-color: rgba(0, 0, 0, 0.1); /* Slight background difference */
}

.mobile-player-header .player-info-item {
    margin-left: 0; /* Reset margin */
    padding: 0; /* Reset padding */
    cursor: default; /* Not clickable */
}

.mobile-player-header .player-info-item:hover {
    background-color: transparent; /* No hover background */
}

.mobile-player-header .player-avatar {
    width: 32px; /* Slightly larger avatar */
    height: 32px;
}

.mobile-player-header .player-name {
    max-width: none; /* Allow full name display */
    font-size: 1rem;
    color: v-bind('theme.boxTextColor'); /* Ensure name is visible */
}

.mobile-player-header.loading-placeholder {
    text-align: center;
    color: v-bind('theme.boxTextColorNoActive');
    font-style: italic;
    font-size: 0.9rem;
}

/* Ensure nav lists take up remaining space if needed, and enable scrolling within */
.mobile-nav-left,
.mobile-nav-right,
.mobile-nav-context {
    list-style: none;
    margin: 0;
    padding: 0;
    opacity: 0;
    transform: translateY(-10px);
    transition: opacity 0.3s ease, transform 0.3s ease;
    /* Remove flex: 1 if causing issues, manage height via max-height on .mobile-menu */
}

/* Optional divider style */
.context-divider {
    height: 1px;
    background-color: v-bind('theme.boxBorderColor');
    margin: 5px 0;
    padding: 0 !important; /* Override item padding */
}

.menu-expanded .mobile-nav-left,
.menu-expanded .mobile-nav-right,
.menu-expanded .mobile-nav-context { /* Apply transitions to context menu too */
  opacity: 1;
  transform: translateY(0);
  transition-delay: 0.1s;
}

.mobile-nav-context .mobile-nav-link {
    /* Add specific styling if needed, e.g., different color */
    /* color: v-bind('theme.mainColor'); */
}

.cursor-pointer {
  cursor: pointer;
}
</style>