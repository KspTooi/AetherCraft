<script setup lang="ts">
import { ref, inject, computed, onMounted, onBeforeUnmount } from 'vue'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue"
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from './GlowTheme.ts'
import GlowButton from "@/components/glow-ui/GlowButton.vue"

// 获取 glow 主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 定义组件props
const props = defineProps<{
  items: Array<{
    id: string,
    title: string,
    icon?: string,
    badge?: number | string,
    routerLink?: string,
    action?: string
  }>,
  activeItemId?: string,
  title?: string
}>()

// 禁用属性继承
defineOptions({
  inheritAttrs: false
})

// 定义事件
const emit = defineEmits<{
  (e: 'item-click', itemId: string): void
  (e: 'action', action: string, itemId: string): void
}>()

// 处理项目点击
const handleItemClick = (item: any) => {
  if (item.action) {
    emit('action', item.action, item.id)
  } else {
    emit('item-click', item.id)
  }
  // 点击项目后关闭移动菜单（如果处于移动模式）
  if (isMobile.value) {
    closeMobileMenu();
  }
}

// 是否展示标题
const showTitle = computed(() => !!props.title)

// 移动端相关状态
const isMobile = ref(window.innerWidth <= 768)
const mobileMenuOpen = ref(false)

// 监听窗口大小变化
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    mobileMenuOpen.value = false // 桌面端自动关闭菜单
  }
}

// 移动端菜单控制
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}

// 组件挂载和卸载时管理监听器
onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

// 暴露方法（如果需要）
defineExpose({
  closeMobileMenu
});
</script>

<template>
  <!-- 移动端悬浮按钮 -->
  <GlowButton
    v-if="isMobile && !mobileMenuOpen"
    @click="toggleMobileMenu"
    class="mobile-menu-btn"
    :corners="[`top-left`]"
  >
    展开
  </GlowButton>

  <!-- 移动端遮罩层 (移到 wrapper 外部) -->
  <div
    v-if="isMobile && mobileMenuOpen"
    class="mobile-overlay"
    @click="closeMobileMenu"
  ></div>

  <!-- 侧边栏容器 -->
  <div class="side-panel-wrapper" :class="[
    { 'mobile-open': isMobile && mobileMenuOpen, 'mobile': isMobile },
    $attrs.class
  ]">
    <GlowDiv
      border="right"
      class="side-panel"
    >
      <!-- 标题区域 -->
      <div v-if="showTitle" class="panel-title">
        {{ title }}
      </div>
      
      <!-- 内容区域 -->
      <div class="panel-content">
        <div class="panel-items">
          <div 
            v-for="item in items" 
            :key="item.id"
            @click="handleItemClick(item)"
            :class="['panel-item', { active: item.id === activeItemId }]"
          >
            <div class="item-icon" v-if="item.icon">
              <i :class="item.icon"></i>
            </div>
            <div class="item-content">
              <div class="item-title">{{ item.title }}</div>
            </div>
            <div class="item-badge" v-if="item.badge">
              {{ item.badge }}
            </div>
          </div>
        </div>
      </div>
    </GlowDiv>
  </div>
</template>

<style scoped>
.side-panel-wrapper {
  position: relative; /* 添加相对定位，以便按钮和遮罩层定位 */
  height: 100%;
  user-select: none; /* 禁止文本选择 */
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.mobile-menu-btn {
  position: fixed; /* 改为 fixed 定位 */
  top: 8px; /* 与 ModelChatList 一致 */
  left: 10px; /* 与 ModelChatList 一致 */
  z-index: 1003; /* Highest */
  height: 32px;
  min-height: 32px;
  font-size: 12px;
  padding: 8px;
}

.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000; /* Below opened sidebar, above page content */
}

.side-panel {
  width: 240px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  /* Ensure it has a background to cover content behind it when opened on mobile */
  background-color: v-bind('theme.boxColor'); /* Correct theme variable */
}

.panel-title {
  padding: 15px;
  font-size: 16px;
  font-weight: 500;
  color: v-bind('theme.boxTextColor');
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
  text-align: center;
  bottom: 0;
  z-index: 1000; /* Higher than overlay */
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.panel-items {
  display: flex;
  flex-direction: column;
}

.panel-item {
  display: flex;
  align-items: center;
  padding: 10px 15px 15px 15px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 2px;
  border-left: 3px solid transparent;
  margin-left: 2px;
  position: relative;
}

.panel-item:hover {
  background-color: v-bind('theme.boxAccentColor');
}

.panel-item.active {
  background-color: v-bind('theme.boxAccentColorHover');
  border-left-color: v-bind('theme.boxGlowColor');
  box-shadow: inset 3px 0 5px -2px v-bind('theme.boxGlowColor');
}

.panel-item.active .item-title {
  color: v-bind('theme.boxTextColor');
  font-weight: 500;
}

.item-icon {
  margin-right: 10px;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: v-bind('theme.boxTextColorNoActive');
}

.panel-item.active .item-icon,
.panel-item:hover .item-icon {
  color: v-bind('theme.boxTextColor');
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  color: v-bind('theme.boxTextColorNoActive');
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s ease;
}

.panel-item:hover .item-title {
  color: v-bind('theme.boxTextColor');
}

.item-badge {
  background-color: v-bind('theme.boxGlowColor');
  color: v-bind('theme.boxTextColor');
  font-size: 12px;
  padding: 1px 6px;
  border-radius: 10px;
  min-width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 滚动条样式 */
.panel-content::-webkit-scrollbar {
  width: 3px;
}

.panel-content::-webkit-scrollbar-track {
  background: transparent;
}

.panel-content::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
  border-radius: 1.5px;
}

.panel-content::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.boxBorderColorHover');
}

/* 移动端响应式设计 */
@media (max-width: 768px) {
  /* 让 wrapper 在移动端固定定位并控制显隐 */
  .side-panel-wrapper.mobile {
    position: fixed;
    left: -240px; /* 初始隐藏在左侧 */
    top: 0;
    bottom: 0;
    width: 240px; /* 固定宽度 */
    height: 100vh; /* 确保全屏高 */
    z-index: 1000; /* Higher than overlay */
    transition: transform 0.3s ease;
    /* GlowDiv 的背景和边框现在由内部 panel 实现，wrapper 不需要 */
  }

  .side-panel-wrapper.mobile.mobile-open {
    transform: translateX(240px); /* 使用 transform 来显示 */
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.2); /* 打开时显示阴影 */
    z-index: 1001; /* Ensure opened wrapper is above overlay */
  }

  /* 确保 side-panel 基础样式能填满其容器 */
  .side-panel {
    width: 100%;
    height: 100%;
    /* 可能需要 flex 布局来排列内部元素 */
    display: flex;
    flex-direction: column;
    overflow: hidden; /* 防止内容溢出破坏布局 */
  }

  /* Ensure inner panel content is definitely above overlay */
  .side-panel-wrapper.mobile.mobile-open .side-panel {
    position: relative; /* Create stacking context */
    z-index: 1002; /* Above wrapper and overlay */
  }
}
</style>