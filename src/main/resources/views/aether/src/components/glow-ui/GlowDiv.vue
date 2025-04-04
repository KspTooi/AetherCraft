<script setup lang="ts">
import { inject, computed } from 'vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from './GlowTheme.ts'
import type { GlowThemeColors } from './GlowTheme.ts'

// 定义props
const props = withDefaults(defineProps<{
  border?: 'full' | 'top' | 'right' | 'bottom' | 'left'
}>(), {
  border: 'full'
})

// 当组件单独运行时，如果没有注入主题，则使用默认主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 根据border属性计算边框样式
const borderStyle = computed(() => {
  switch (props.border) {
    case 'top':
      return {
        borderTop: `1px solid ${theme.boxBorderColor}`,
        borderRight: 'none',
        borderBottom: 'none',
        borderLeft: 'none'
      }
    case 'right':
      return {
        borderTop: 'none',
        borderRight: `1px solid ${theme.boxBorderColor}`,
        borderBottom: 'none',
        borderLeft: 'none'
      }
    case 'bottom':
      return {
        borderTop: 'none',
        borderRight: 'none',
        borderBottom: `1px solid ${theme.boxBorderColor}`,
        borderLeft: 'none'
      }
    case 'left':
      return {
        borderTop: 'none',
        borderRight: 'none',
        borderBottom: 'none',
        borderLeft: `1px solid ${theme.boxBorderColor}`
      }
    default: // 'full'
      return {
        border: `1px solid ${theme.boxBorderColor}`
      }
  }
})

// 悬停状态下的边框样式
const hoverBorderStyle = computed(() => {
  switch (props.border) {
    case 'top':
      return {
        borderTop: `1px solid ${theme.boxBorderColorHover}`
      }
    case 'right':
      return {
        borderRight: `1px solid ${theme.boxBorderColorHover}`
      }
    case 'bottom':
      return {
        borderBottom: `1px solid ${theme.boxBorderColorHover}`
      }
    case 'left':
      return {
        borderLeft: `1px solid ${theme.boxBorderColorHover}`
      }
    default: // 'full'
      return {
        border: `1px solid ${theme.boxBorderColorHover}`
      }
  }
})
</script>

<template>
  <div class="laser-div" :style="borderStyle">
    <slot></slot>
  </div>
</template>

<style scoped>
.laser-div {
  color: v-bind('theme.boxTextColor');
  background-color: v-bind('theme.boxColor');
  backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  transition: all 0.8s ease;
}

.laser-div:hover {
  background-color: v-bind('theme.boxColorHover');
  backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
  border-color: v-bind('theme.boxBorderColorHover') !important;
}
</style>