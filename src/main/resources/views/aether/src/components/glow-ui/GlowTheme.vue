<script setup lang="ts">
import { provide, reactive, readonly } from 'vue'
import type { GlowThemeColors } from './GlowTheme.ts'
import { defaultTheme, GLOW_THEME_INJECTION_KEY } from './GlowTheme.ts'

const props = withDefaults(defineProps<{
  theme?: Partial<GlowThemeColors>
}>(), {
  theme: () => ({})
})

const mergedTheme = reactive<GlowThemeColors>({
  ...defaultTheme,
  ...props.theme
})

// 提供主题对象给子组件
provide(GLOW_THEME_INJECTION_KEY, readonly(mergedTheme))
</script>

<template>
  <div class="laser-theme">
    <slot></slot>
  </div>
</template>

<style scoped>
.laser-theme {
  width: 100%;
  height: 100%;
}
</style>