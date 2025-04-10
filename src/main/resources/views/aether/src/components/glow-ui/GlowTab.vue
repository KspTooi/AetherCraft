<template>
  <div class="glow-tab">
    <div class="tab-header">
      <div 
        v-for="(item, index) in items" 
        :key="index" 
        class="tab-item"
        :class="{ 'active': item.action === activeTab }"
        @click="handleTabClick(index, item.action)"
      >
        {{ item.title }}
      </div>
    </div>
    
    <div class="tab-content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, watch } from 'vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from './GlowTheme'
import type { GlowThemeColors } from './GlowTheme'

const props = defineProps<{
  items: Array<{
    title: string,
    action: string
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

// 初始化时根据activeTab设置激活的索引
const updateActiveIndex = () => {
  const index = props.items.findIndex(item => item.action === props.activeTab)
  if (index !== -1) {
    activeIndex.value = index
  }
}

// 初始化激活索引
updateActiveIndex()

// 监听activeTab变化
watch(() => props.activeTab, () => {
  updateActiveIndex()
})

// 处理标签点击
const handleTabClick = (index: number, action: string) => {
  activeIndex.value = index
  emit('tab-change', action, index)
  emit('update:activeTab', action)
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

.tab-content {
}

/* 添加辉光效果 */
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 10px 2px v-bind('theme.boxGlowColor');
}

@media (max-width: 768px) {
  .tab-item {
    padding: 8px 12px;
    font-size: 13px;
  }
}
</style>