<template>
  <div class="chat-container">
    <div class="chat-layout">
      <!-- 使用 GlowSidePanel 替换左侧菜单 -->
      <GlowSidePanel
        class="side-panel"
        :items="menuItemsForPanel"
        :active-item-id="activeSection"
        title="个性化设置"
        @item-click="setActiveSection"
      />

      <!-- 右侧内容区 -->
      <div class="chat-main">

        <div v-if="activeSection === 'background'" class="section-content">
          <CustomizeWallpaper />
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
import ConfirmModal from '@/components/ConfirmModal.vue'
import CustomizeWallpaper from '@/components/client-customize/CustomizeWallpaper.vue'
import CustomizeTheme from '../components/client-customize/CustomizeTheme.vue'
import GlowSidePanel from '@/components/glow-ui/GlowSidePanel.vue'

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

// 确认模态框引用
const confirmModal = ref()

// 菜单项定义
const menuItems = [
  { id: 'background', name: '背景设定', icon: 'bi bi-image' },
  { id: 'colors', name: '主题颜色', icon: 'bi bi-palette2' },
  { id: 'effects', name: '界面特效', icon: 'bi bi-stars' },
  { id: 'fonts', name: '字体设置', icon: 'bi bi-type' }
]

// 为 GlowSidePanel 准备数据，将 name 映射到 title
const menuItemsForPanel = computed(() => {
  return menuItems.map(item => ({
    id: item.id,
    title: item.name,
    icon: item.icon
  }))
})

// 设置活动章节
const setActiveSection = (sectionId: string) => {
  activeSection.value = sectionId
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

/* GlowSidePanel 容器样式 (如果需要微调) */
.side-panel {
  height: 100%;
  flex-shrink: 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .chat-main {
    width: 100%;
    margin-left: 0;
  }
}
</style> 