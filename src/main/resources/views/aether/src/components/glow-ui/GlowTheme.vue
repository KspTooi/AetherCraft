<script setup lang="ts">
import { provide, reactive, readonly } from 'vue'
import type { GlowThemeColors } from './GlowTheme.ts'
import { defaultTheme, GLOW_THEME_INJECTION_KEY } from './GlowTheme.ts'

const props = withDefaults(defineProps<{
  theme?: Partial<GlowThemeColors>
}>(), {
  theme: () => ({})
})

const emit = defineEmits<{
  (e:"onThemeUpdate"):void
}>()

const mergedTheme = reactive<GlowThemeColors>({
  ...defaultTheme,
  ...props.theme
})

// 提供主题函数供子组件调用
const notifyThemeUpdate = () => {
  emit('onThemeUpdate')
}

// 提供主题对象给子组件
provide(GLOW_THEME_INJECTION_KEY, readonly(mergedTheme))
// 提供通知主题更新函数给子组件
provide('notifyThemeUpdate', notifyThemeUpdate)
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