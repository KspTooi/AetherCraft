<template>
  <teleport to="body">
    <div class="menu-teleport-container" v-if="isVisible">
      <div class="global-menu-overlay" @click="hide"></div>
      <div class="global-menu" :style="menuPosition">
        <div class="menu-top-border"></div>
        <div v-if="header" class="menu-title">{{ menuTitle }}</div>
        <div v-if="header" class="menu-divider"></div>
        <div 
          v-for="(item, index) in actions" 
          :key="index" 
          class="menu-item" 
          :class="{ 'disabled': item.disabled }"
          @click="handleItemClick(contextData, item.action)"
        >
          {{ item.name }}
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed, inject } from 'vue'
import { GLOW_THEME_INJECTION_KEY, type GlowThemeColors, defaultTheme } from './GlowTheme'

const props = defineProps<{
  header: boolean, //是否展示菜单头
  menuTitle?: string, //菜单标题
  actions: {      //可选的操作
    name: string
    action: string
    disabled?: boolean //默认false
  }[]
}>()

const emit = defineEmits<{
  (e: 'click', context: string, action: string): void
}>()

// 注入主题
const theme = inject(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 状态管理
const isVisible = ref(false)
const menuPosition = ref({
  top: '0px',
  left: '0px'
})
const contextData = ref('')
const menuTitle = ref('')

// 显示菜单
const show = (context: string, event?: MouseEvent, title?: string) => {
  contextData.value = context
  
  if (title) {
    menuTitle.value = title
  }
  
  isVisible.value = true
  
  // 如果提供了事件，根据事件位置定位菜单
  if (event) {
    positionMenu(event)
  }
  
  // 添加全局点击监听来关闭菜单
  document.addEventListener('click', closeMenuOnClickOutside)
}

// 隐藏菜单
const hide = () => {
  isVisible.value = false
  document.removeEventListener('click', closeMenuOnClickOutside)
}

// 菜单位置计算
const positionMenu = (event: MouseEvent) => {
  // 获取窗口尺寸和鼠标事件的位置
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight
  const mouseX = event.pageX
  const mouseY = event.pageY
  
  // 菜单尺寸估计
  const menuWidth = 150
  const menuHeight = props.actions.length * 30 + (props.header ? 40 : 0)
  
  // 检查边界
  const isNearRightEdge = mouseX + menuWidth + 5 > windowWidth
  const isNearBottomEdge = mouseY + menuHeight + 5 > windowHeight
  
  // 设置菜单位置
  let topPosition = mouseY
  let leftPosition = mouseX
  
  // 边界调整
  if (isNearRightEdge) {
    leftPosition = mouseX - menuWidth
  }
  
  if (isNearBottomEdge) {
    topPosition = mouseY - menuHeight
  }
  
  // 确保不超出左边界
  if (leftPosition < 10) {
    leftPosition = 10
  }
  
  // 更新菜单位置
  menuPosition.value = {
    top: `${topPosition}px`,
    left: `${leftPosition}px`
  }
}

// 点击菜单项处理
const handleItemClick = (context: string, action: string) => {
  // 检查是否有禁用项
  const actionItem = props.actions.find(item => item.action === action)
  if (actionItem && actionItem.disabled) {
    return
  }
  
  emit('click', context, action)
  hide()
}

// 点击菜单外部关闭菜单
const closeMenuOnClickOutside = (event: MouseEvent) => {
  hide()
}

// 向父组件暴露方法
defineExpose({
  show,
  hide
})
</script>

<style scoped>
/* 传送到body的菜单容器 */
.menu-teleport-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 2147483647; /* 最大可能z-index值 */
}

/* 菜单遮罩层 */
.global-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: transparent;
  pointer-events: auto;
  z-index: 2147483646;
}

/* 下拉菜单 */
.global-menu {
  position: fixed;
  background: v-bind('theme.boxSecondColor');
  backdrop-filter: blur(v-bind('theme.boxBlur') + 'px');
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlur') + 'px');
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 2px;
  width: 150px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  z-index: 2147483647;
  overflow: hidden;
  animation: menuFadeIn 0.15s ease-out;
  transform-origin: top right;
  padding: 1px 0;
  transition: all 0.15s ease-out;
  pointer-events: auto;
}

.menu-top-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 6px v-bind('theme.boxGlowColor');
}

.menu-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.08);
  margin: 1px 0;
}

@keyframes menuFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.menu-item {
  padding: 6px 10px;
  display: flex;
  align-items: center;
  color: v-bind('theme.boxTextColor');
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
  font-size: 12px;
  border-left: 2px solid transparent;
  letter-spacing: 0.3px;
  line-height: 1.3;
  pointer-events: auto !important;
  user-select: none;
}

.menu-item:hover {
  background: v-bind('theme.boxSecondColorHover');
  color: v-bind('theme.boxTextColor');
  border-left: 2px solid v-bind('theme.boxGlowColor');
}

.menu-item:active {
  background: v-bind('theme.boxColorActive');
}

.menu-item.disabled {
  color: v-bind('theme.boxTextColorNoActive');
  cursor: not-allowed;
  opacity: 0.6;
}

.menu-item.disabled:hover {
  background: transparent;
  border-left: 2px solid transparent;
}

/* 菜单标题样式 */
.menu-title {
  padding: 6px 8px;
  color: v-bind('theme.boxTextColor');
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
  user-select: none;
  background: v-bind('theme.boxGlowColor + "30"');
  position: relative;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

/* 移除标题后的分隔线，用标题自带的底部边框代替 */
.menu-title + .menu-divider {
  display: none;
}
</style>