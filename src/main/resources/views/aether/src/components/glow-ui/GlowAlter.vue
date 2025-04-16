<script setup lang="ts">
import { ref, computed } from 'vue'
import GlowModal from './GlowModal.vue'
import GlowButton from './GlowButton.vue'
import { inject } from 'vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from './GlowTheme.ts'
import type { GlowThemeColors } from './GlowTheme.ts'

// 当组件单独运行时，如果没有注入主题，则使用默认主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

console.log(theme.boxBorderColor)

// 状态
const visible = ref(false)
const title = ref('确认操作')
const content = ref('您确定要执行此操作吗？')
const closeText = ref('关闭')
const resolvePromise = ref<((value: boolean) => void) | null>(null)

// 按钮样式
const closeBtnBg = computed(() => theme.boxAccentColorHover) 
const closeBtnBorder = computed(() => theme.boxBorderColorHover)

// 处理关闭
const handleClose = () => {
  visible.value = false
  if (resolvePromise.value) {
    resolvePromise.value(false)
    resolvePromise.value = null
  }
}

/**
 * 显示确认对话框
 * @param options 可选配置项
 * @returns Promise，关闭时返回false
 */
const showConfirm = (options?: {
  title?: string
  content?: string
  closeText?: string
}): Promise<boolean> => {
  // 更新选项
  if (options) {
    if (options.title) title.value = options.title
    if (options.content) content.value = options.content
    if (options.closeText) closeText.value = options.closeText
  }

  // 显示模态框
  visible.value = true

  // 返回Promise
  return new Promise((resolve) => {
    resolvePromise.value = resolve
  })
}

// 暴露方法
defineExpose({
  showConfirm
})
</script>

<template>
  <GlowModal v-model="visible" :width="'320px'" :close-on-click-overlay="false" :show-header="false">
    <div class="laser-confirm-container">
      <!-- 自定义头部 -->
      <div class="laser-confirm-header">
        <div class="confirm-icon">
          <i class="bi bi-exclamation-circle"></i>
        </div>
        <h3 class="confirm-title">{{ title }}</h3>
      </div>

      <!-- 确认内容 -->
      <div class="laser-confirm-content">
        {{ content }}
      </div>

      <!-- 按钮区域 -->
      <div class="laser-confirm-buttons">
        <GlowButton
            class="close-btn"
            :corners="[`bottom-right`]"
            :background-color="closeBtnBg"
            :border-color="closeBtnBorder"
            @click="handleClose">
          {{ closeText }}
        </GlowButton>
      </div>
    </div>
  </GlowModal>
</template>

<style scoped>
.laser-confirm-container {
  padding: 0 0 0 0;
  display: flex;
  flex-direction: column;
}

.laser-confirm-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.confirm-icon {
  font-size: 24px;
  color: v-bind('theme.boxGlowColor');
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-title {
  font-size: 16px;
  color: v-bind('theme.boxTextColor');
  margin: 0;
  font-weight: 500;
}

.laser-confirm-content {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
  margin-bottom: 20px;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow-wrap: break-word;
  padding-left: 10px; /* 与图标对齐 */
  max-height: 200px;
  overflow-y: auto;
}

/* 自定义滚动条样式 - Chrome, Edge, Safari */
.laser-confirm-content::-webkit-scrollbar {
  width: 3px;
}

.laser-confirm-content::-webkit-scrollbar-track {
  background: transparent;
}

.laser-confirm-content::-webkit-scrollbar-thumb {
  background-color: v-bind('theme.boxBorderColor');
}

.laser-confirm-content::-webkit-scrollbar-thumb:hover {
  background-color: v-bind('theme.boxBorderColorHover');
}

/* 自定义滚动条样式 - Firefox */
.laser-confirm-content {
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.1) transparent;
}

.laser-confirm-buttons {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.close-btn {
  height: 32px;
  min-height: 32px;
  font-size: 14px;
  width: 120px;
}
</style>