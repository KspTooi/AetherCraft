<template>
  <div class="chat-container">
    <div class="chat-layout">
      <!-- 使用 GlowSidePanel 替换左侧菜单 -->
      <GlowSidePanel
        class="side-panel"
        :items="sidePanelItems"
        :active-item-id="sidePanelCurrentItem"
        title="个性化设置"
        @item-click="onSidePanelChange"
      />

      <!-- 右侧内容区 -->
      <GlowDiv class="chat-main">

        <div v-if="sidePanelCurrentItem === 'wallpaper'" class="section-content">
          <CustomizeWallpaper/>
        </div>

        <div v-if="sidePanelCurrentItem === 'theme'" class="section-content">
          <CustomizeTheme />
        </div>

        <div v-if="sidePanelCurrentItem === 'effects'" class="section-content">

        </div>

        <div v-if="sidePanelCurrentItem === 'fonts'" class="section-content">

        </div>
        
      </GlowDiv>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '../stores/theme'
import { usePreferencesStore } from '../stores/preferences'
import CustomizeWallpaper from '@/components/client-customize/CustomizeWallpaper.vue'
import CustomizeTheme from '../components/client-customize/CustomizeTheme.vue'
import GlowSidePanel from '@/components/glow-ui/GlowSidePanel.vue'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";

// 获取主题颜色
const themeStore = useThemeStore()
const activeColor = computed(() => themeStore.activeColor)

// 初始化preferences store
const preferencesStore = usePreferencesStore()
const { customizePathSide } = storeToRefs(preferencesStore)

// 当前活动的章节
const sidePanelCurrentItem = ref('wallpaper')

// 菜单项定义
const menuItems = [
  { id: 'wallpaper', name: '背景设定', icon: 'bi bi-image' },
  { id: 'theme', name: '主题设定', icon: 'bi bi-palette2' },
  { id: 'effects', name: '界面特效', icon: 'bi bi-stars' },
  { id: 'fonts', name: '字体设置', icon: 'bi bi-type' }
]

// 为 GlowSidePanel 准备数据，将 name 映射到 title
const sidePanelItems = computed(() => {
  return menuItems.map(item => ({
    id: item.id,
    title: item.name,
    icon: item.icon
  }))
})

// 处理边栏项目变化
const onSidePanelChange = async (action: string) => {
  sidePanelCurrentItem.value = action
  // 保存当前选中的边栏项目
  await preferencesStore.saveCustomizePathSide(action)
}

// 组件挂载时加载保存的边栏状态
onMounted(async () => {
  await preferencesStore.loadPreferences()
  if (customizePathSide.value) {
    sidePanelCurrentItem.value = customizePathSide.value
  }
})

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
  /*padding: 20px;*/
  overflow-y: auto;
  width: 100%;
  height: 100%;
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