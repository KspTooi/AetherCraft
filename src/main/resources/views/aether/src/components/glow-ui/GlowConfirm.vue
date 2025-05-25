<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import GlowModal from './GlowModal.vue'
import GlowButton from './GlowButton.vue'
import { inject } from 'vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from './GlowTheme.ts'
import type { GlowThemeColors } from './GlowTheme.ts'

// 当组件单独运行时，如果没有注入主题，则使用默认主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 状态
const visible = ref(false)
const title = ref('确认操作')
const content = ref('您确定要执行此操作吗？')
const confirmText = ref('确定')
const cancelText = ref('取消')
const resolvePromise = ref<((value: boolean) => void) | null>(null)

// 按钮样式
const confirmBtnBg = computed(() => theme.boxAccentColorHover)
const confirmBtnBorder = computed(() => theme.boxBorderColorHover)
const cancelBtnBg = computed(() => 'rgba(60, 60, 60, 0.5)')
const cancelBtnBorder = computed(() => 'rgba(120, 120, 120, 0.5)')

// 键盘事件处理
const handleKeydown = (event: KeyboardEvent) => {
  if (!visible.value) return
  
  if (event.key === 'Enter') {
    event.preventDefault()
    handleConfirm()
  } else if (event.key === 'Escape') {
    event.preventDefault()
    handleCancel()
  }
}

// 处理确认
const handleConfirm = () => {
  visible.value = false
  if (resolvePromise.value) {
    resolvePromise.value(true)
    resolvePromise.value = null
  }
}

// 处理取消
const handleCancel = () => {
  visible.value = false
  if (resolvePromise.value) {
    resolvePromise.value(false)
    resolvePromise.value = null
  }
}

/**
 * 显示确认对话框
 * @param options 可选配置项
 * @returns Promise，用户确认时返回true，取消时返回false
 */
const showConfirm = (options?: {
  title?: string
  content?: string
  confirmText?: string
  cancelText?: string
}): Promise<boolean> => {
  // 更新选项
  if (options) {
    if (options.title) title.value = options.title
    if (options.content) content.value = options.content
    if (options.confirmText) confirmText.value = options.confirmText
    if (options.cancelText) cancelText.value = options.cancelText
  }

  // 显示模态框
  visible.value = true

  // 返回Promise
  return new Promise((resolve) => {
    resolvePromise.value = resolve
  })
}

// 组件挂载时添加键盘事件监听
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

// 组件卸载时移除键盘事件监听
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

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
            class="confirm-btn"
            :background-color="confirmBtnBg"
            :border-color="confirmBtnBorder"
            @click="handleConfirm">
          {{ confirmText }}
        </GlowButton>
        <GlowButton
            :corners="[`bottom-right`]"
            class="cancel-btn"
            :background-color="cancelBtnBg"
            :border-color="cancelBtnBorder"
            @click="handleCancel">
          {{ cancelText }}
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
  max-height: none;
  overflow: visible;
}

.laser-confirm-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 10px;
}

.confirm-btn, .cancel-btn {
  flex: 1;
  height: 32px;
  min-height: 32px;
  font-size: 14px;
  max-width: 120px;
}
</style>