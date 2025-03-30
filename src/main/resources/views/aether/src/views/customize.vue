<template>
  <div class="chat-container">
    <div class="chat-layout">
      <!-- 移动端菜单按钮 -->
      <LaserButton 
        class="mobile-menu-btn" 
        @click="toggleSidebar"
        :corners="['top-left', 'bottom-right']"
        corner-size="15px">
        设置菜单
      </LaserButton>
      
      <!-- 遮罩层 -->
      <div class="sidebar-mask" :class="{ show: isMobileMenuOpen }" @click="toggleSidebar"></div>
      
      <!-- 左侧菜单 -->
      <div class="thread-list" :class="{ show: isMobileMenuOpen }">
        <div class="thread-list-container">
          <div class="menu-header">
            <i class="bi bi-palette"></i>
            <span class="menu-title">个性化设置</span>
          </div>
          <div class="thread-items-wrapper">
            <div class="menu-items">
              <div
                v-for="item in menuItems"
                :key="item.id"
                class="thread-item"
                :class="{ active: activeSection === item.id }"
                @click="setActiveSection(item.id)"
              >
                <div class="menu-icon">
                  <i :class="item.icon"></i>
                </div>
                <div class="thread-content">
                  <div class="thread-title">{{ item.name }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="chat-main">
        <div v-if="activeSection === 'background'" class="section-content">
          <CustomizeWallpaper :confirmModal="confirmModal" />
        </div>

        <div v-if="activeSection === 'colors'" class="section-content">
          <CustomizeTheme />
        </div>

        <div v-if="activeSection === 'effects'" class="section-content">
          <div class="section-header">
            <h2>界面特效</h2>
            <p class="section-description">自定义界面动画和视觉效果</p>
          </div>
          <div class="settings-panel">
            <div class="coming-soon">
              <i class="bi bi-tools"></i>
              <span>功能开发中，敬请期待</span>
            </div>
          </div>
        </div>

        <div v-if="activeSection === 'fonts'" class="section-content">
          <div class="section-header">
            <h2>字体设置</h2>
            <p class="section-description">修改应用的字体和文本显示方式</p>
          </div>
          <div class="settings-panel">
            <div class="coming-soon">
              <i class="bi bi-tools"></i>
              <span>功能开发中，敬请期待</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 确认模态框 -->
    <ConfirmModal ref="confirmModal" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject } from 'vue'
import { useThemeStore } from '../stores/theme'
import LaserButton from '@/components/LaserButton.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import CustomizeWallpaper from '@/components/CustomizeWallpaper.vue'
import CustomizeTheme from '../components/CustomizeTheme.vue'

// 获取提前解除加载状态的方法
const finishLoading = inject<() => void>('finishLoading')

// 获取主题颜色
const themeStore = useThemeStore()
const primaryColor = computed(() => themeStore.primaryColor)
const primaryHover = computed(() => themeStore.primaryHover)
const activeColor = computed(() => themeStore.activeColor)
const primaryButton = computed(() => themeStore.primaryButton)
const primaryButtonBorder = computed(() => themeStore.primaryButtonBorder)
const menuActiveColor = computed(() => themeStore.menuActiveColor)
const sideBlur = computed(() => themeStore.sideBlur)
const mainBlur = computed(() => themeStore.mainBlur)
const menuBlur = computed(() => themeStore.menuBlur)

// 当前活动的章节
const activeSection = ref('background')
const isMobileMenuOpen = ref(false)

// 确认模态框引用
const confirmModal = ref()

// 菜单项定义
const menuItems = [
  { id: 'background', name: '背景设定', icon: 'bi bi-image' },
  { id: 'colors', name: '主题颜色', icon: 'bi bi-palette2' },
  { id: 'effects', name: '界面特效', icon: 'bi bi-stars' },
  { id: 'fonts', name: '字体设置', icon: 'bi bi-type' }
]

// 设置活动章节
const setActiveSection = (section: string) => {
  activeSection.value = section
  
  // 移动端模式下，切换章节后关闭菜单
  if (window.innerWidth <= 768 && isMobileMenuOpen.value) {
    toggleSidebar()
  }
}

// 切换侧边栏显示（移动端）
const toggleSidebar = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  color: #fff;
  min-height: 0;
  position: relative;
  border-radius: 0 !important;
}

.chat-layout {
  display: flex;
  height: calc(100vh - var(--nav-height));
  width: 100%;
  overflow: hidden;
  position: relative;
}

.thread-list {
  width: 240px;
  height: 100%;
  overflow: hidden;
  backdrop-filter: blur(v-bind(sideBlur));
  -webkit-backdrop-filter: blur(v-bind(sideBlur));
  background: rgba(0, 0, 0, 0.2);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  transition: transform 0.3s ease;
}

.thread-list-container {
  width: 240px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid v-bind('`rgba(${primaryColor.split("(")[1].split(")")[0]}, 0.1)`');
  border-radius: 0 !important;
  height: 100%;
  overflow: hidden;
}

.menu-header {
  padding: 20px 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu-header i {
  font-size: 22px;
  color: v-bind(activeColor);
}

.menu-title {
  font-size: 16px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 0.5px;
}

.manage-thread-btn {
  margin: 12px 12px 5px 12px;
  padding: 10px 15px;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.thread-items-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  border-top: none;
  min-height: 0;
  margin-top: 0;
  height: calc(100% - 60px);
}

.menu-items {
  display: flex;
  flex-direction: column;
  padding: 4px 0;
}

.thread-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.25s ease, border-left-color 0.25s ease, box-shadow 0.25s ease;
  border-left: 3px solid transparent;
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  overflow: hidden;
  margin: 2px 4px;
  border-radius: 2px;
}

.thread-item:first-child {
  margin-top: 5px;
}

.menu-icon {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
  transition: color 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.thread-item:hover .menu-icon {
  color: rgba(255, 255, 255, 0.95);
}

.thread-item.active .menu-icon {
  color: v-bind(activeColor);
}

.thread-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
  justify-content: center;
}

.thread-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.2px;
}

.thread-item:hover {
  background: v-bind(primaryHover);
}

.thread-item.active {
  background: rgba(40, 90, 130, 0.25);
  border-left-color: v-bind(activeColor);
  box-shadow: inset 3px 0 5px -2px v-bind(activeColor);
}

.thread-item.active .thread-title {
  color: rgba(255, 255, 255, 1);
  font-weight: 500;
}

.thread-items-wrapper::-webkit-scrollbar {
  width: 3px;
}

.thread-items-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.thread-items-wrapper::-webkit-scrollbar-thumb {
  background: v-bind(primaryHover);
  border-radius: 1.5px;
}

.thread-items-wrapper::-webkit-scrollbar-thumb:hover {
  background: v-bind(primaryColor);
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: transparent;
  overflow: hidden;
  border-radius: 0 !important;
  backdrop-filter: blur(v-bind(mainBlur));
  -webkit-backdrop-filter: blur(v-bind(mainBlur));
  padding: 20px;
  overflow-y: auto;
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
}

.section-header::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 80px;
  height: 2px;
  background-color: v-bind(activeColor);
}

.section-header h2 {
  font-size: 20px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-description {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin: 0;
}

.settings-panel {
  background-color: rgba(30, 40, 60, 0.3);
  border-radius: 0;
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 20px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.coming-soon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
}

.coming-soon i {
  font-size: 40px;
  margin-bottom: 15px;
  color: v-bind(activeColor);
  opacity: 0.7;
}

.coming-soon span {
  font-size: 16px;
  letter-spacing: 0.5px;
}

/* 移动端菜单按钮样式 */
.mobile-menu-btn {
  display: none;
  position: fixed;
  left: 12px;
  top: 8px;
  z-index: 1001;
  cursor: pointer;
  min-height: 32px;
  height: auto;
  width: auto;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
  padding: 5px 8px !important;
  font-size: 12px!important;
}

.mobile-menu-btn:hover {
  transform: translateY(0) !important;
}

.sidebar-mask {
  display: none;
  position: fixed;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.sidebar-mask.show {
  display: block;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .thread-list {
    position: fixed;
    left: -240px;
    top: 0;
    height: 100vh;
    width: 240px;
    z-index: 1001;
    transition: transform 0.3s ease;
    background-color: rgba(20, 30, 40, 0.9);
  }
  
  .thread-list.show {
    transform: translateX(240px);
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.3);
  }
  
  .mobile-menu-btn {
    display: flex !important;
  }
  
  .chat-main {
    width: 100%;
    margin-left: 0;
  }
  
  .thread-items-wrapper {
    flex: 1;
    overflow-y: auto;
    height: calc(100vh - 60px);
  }
}
</style> 