<template>
  <div class="glow-tab">
    <div class="tab-header">
      <div 
        v-for="(item, index) in items" 
        :key="index" 
        class="tab-item"
        :class="{ 
          'active': item.action === activeTab,
          'disabled': item.disabled 
        }"
        @click="!item.disabled && handleTabClick(index, item.action)"
      >
        {{ item.title }}
      </div>
      
      <!-- 添加高亮线容器 -->
      <div class="tab-highlight-container">
        <div class="tab-highlight-line" :class="{ 'initialized': isInitialized }" :style="highlightStyle"></div>
      </div>
    </div>
    
    <div class="tab-content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, watch, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from './GlowTheme'
import type { GlowThemeColors } from './GlowTheme'

const props = defineProps<{
  items: Array<{
    title: string,
    action: string,
    disabled?: boolean
  }>,
  activeTab:string
}>()

// 注入主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 定义事件
const emit = defineEmits<{
  (e: 'tab-change', action: string, index: number): void
  (e: 'update:activeTab', value: string): void
}>()

// 当前激活的标签索引
const activeIndex = ref(0)

// 用于控制高亮线显示的标志
const isReady = ref(false)
// 用于控制是否启用过渡动画
const isInitialized = ref(false)

// 初始化时根据activeTab设置激活的索引
const updateActiveIndex = () => {
  const index = props.items.findIndex(item => item.action === props.activeTab)
  if (index !== -1) {
    activeIndex.value = index
  }
}

// 防抖函数
const debounce = (fn: Function, delay: number) => {
  let timer: number | null = null
  return (...args: any[]) => {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      fn(...args)
      timer = null
    }, delay) as unknown as number
  }
}

// 更新高亮线位置和宽度
const updateHighlightPosition = () => {
  nextTick(() => {
    // 获取所有标签项
    const tabItems = document.querySelectorAll('.tab-item')
    if (tabItems.length > 0 && activeIndex.value >= 0 && activeIndex.value < tabItems.length) {
      const activeItem = tabItems[activeIndex.value] as HTMLElement
      const tabHeader = document.querySelector('.tab-header') as HTMLElement
      
      if (activeItem && tabHeader) {
        const activeItemRect = activeItem.getBoundingClientRect()
        const tabHeaderRect = tabHeader.getBoundingClientRect()
        const leftOffset = activeItemRect.left - tabHeaderRect.left
        
        highlightPosition.value = {
          left: `${leftOffset}px`,
          width: `${activeItemRect.width}px`,
          opacity: 1
        }
        
        // 确保高亮线可见
        isReady.value = true
        
        // 延迟启用过渡动画
        if (!isInitialized.value) {
          setTimeout(() => {
            isInitialized.value = true
          }, 100)
        }
      }
    }
  })
}

// 存储高亮线位置信息
const highlightPosition = ref({
  left: '0px',
  width: '0px',
  opacity: 0
})

// 计算高亮线样式
const highlightStyle = computed(() => {
  if (!isReady.value) {
    return {
      left: '0px',
      width: '0px',
      opacity: 0
    }
  }
  return highlightPosition.value
})

// 监听activeTab变化
watch(() => props.activeTab, (newValue) => {
  const index = props.items.findIndex(item => item.action === newValue)
  if (index !== -1) {
    activeIndex.value = index
    // 触发tab-change事件
    emit('tab-change', newValue, index)
  }
  updateHighlightPosition()
})

// 监听窗口大小变化 - 使用防抖处理
const handleResize = debounce(() => {
  updateHighlightPosition()
}, 100)

// 监听orientation变化 - 移动设备旋转时重新计算
const handleOrientationChange = () => {
  // 延迟执行以确保布局已更新
  setTimeout(() => {
    updateHighlightPosition()
  }, 200)
}

// 组件挂载后初始化
onMounted(() => {
  updateActiveIndex()
  // 使用setTimeout确保DOM完全渲染
  setTimeout(() => {
    updateHighlightPosition()
  }, 100)
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
  
  // 添加设备方向变化监听
  window.addEventListener('orientationchange', handleOrientationChange)
  
  // 添加观察器以捕获父元素大小变化
  if (typeof ResizeObserver !== 'undefined') {
    const tabElement = document.querySelector('.glow-tab')
    if (tabElement) {
      const resizeObserver = new ResizeObserver(debounce(() => {
        updateHighlightPosition()
      }, 100))
      resizeObserver.observe(tabElement)
      
      // 保存观察器引用以便清理
      resizeObserverRef.value = resizeObserver
    }
  }
})

// 存储ResizeObserver引用
const resizeObserverRef = ref<ResizeObserver | null>(null)

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('orientationchange', handleOrientationChange)
  
  // 清理ResizeObserver
  if (resizeObserverRef.value) {
    resizeObserverRef.value.disconnect()
  }
})

// 处理标签点击
const handleTabClick = (index: number, action: string) => {
  activeIndex.value = index
  emit('tab-change', action, index)
  emit('update:activeTab', action)
  updateHighlightPosition()
}
</script>

<style scoped>
.glow-tab {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.tab-header {
  display: flex;
  flex-direction: row;
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
  position: relative;
}

.tab-item {
  padding: 10px 20px;
  cursor: pointer;
  font-size: 14px;
  color: v-bind('theme.boxTextColorNoActive');
  border-bottom: none;
  transition: all 0.3s ease;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.tab-item:hover {
  color: v-bind('theme.boxTextColor');
  background: v-bind('theme.boxColorHover');
  border-bottom-color: transparent;
}

.tab-item.active {
  color: v-bind('theme.boxTextColor');
  background: transparent;
  border-bottom-color: transparent;
}

.tab-item.disabled {
  cursor: not-allowed;
  opacity: 0.5;
  pointer-events: none;
}

.tab-item.disabled:hover {
  background: transparent;
  color: v-bind('theme.boxTextColorNoActive');
}

.tab-content {
}

/* 高亮线容器和线样式 */
.tab-highlight-container {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  overflow: visible;
  pointer-events: none;
  z-index: 10;
}

.tab-highlight-line {
  position: absolute;
  bottom: 0;
  height: 1px;
  background: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 10px 2px v-bind('theme.boxGlowColor');
}

/* 仅在初始化后启用过渡动画 */
.tab-highlight-line.initialized {
  transition: all 0.3s ease;
}

/* 移除原来的静态高亮效果 */
.tab-item.active::after {
  display: none;
}

@media (max-width: 768px) {
  .tab-item {
    padding: 8px 12px;
    font-size: 13px;
  }
}
</style>