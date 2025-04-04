<script setup lang="ts">
import { ref, inject, computed } from 'vue'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue"
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'

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
}

// 是否展示标题
const showTitle = computed(() => !!props.title)
</script>

<template>
  <GlowDiv border="right" class="side-panel">
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
</template>

<style scoped>
.side-panel {
  width: 240px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-title {
  padding: 15px;
  font-size: 16px;
  font-weight: 500;
  color: v-bind('theme.boxTextColor');
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
  text-align: center;
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
  padding: 10px 15px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin: 2px 6px;
  border-radius: 2px;
  border-left: 3px solid transparent;
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
  .side-panel {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.2);
  }
  
  .side-panel.mobile-visible {
    transform: translateX(0);
  }
}
</style>