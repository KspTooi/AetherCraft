<!-- 通用确认模态框组件 -->
<template>
  <teleport to="body">
    <div v-if="visible" class="confirm-modal-overlay" @click.self="handleCancel">
      <div class="confirm-modal-container">
        <div class="modal-glow-border"></div>
        <div class="confirm-modal-header">
          <div class="confirm-icon">
            <i class="bi bi-question-circle"></i>
          </div>
          <h3 class="confirm-modal-title">{{ title }}</h3>
        </div>
        <div class="confirm-modal-body">
          <div class="confirm-modal-content">{{ content }}</div>
        </div>
        <div class="confirm-modal-footer">
          <div class="button-container">
            <LaserButton
              class="confirm-btn"
              :background-color="confirmBtnBg"
              :border-color="confirmBtnBorder"
              :corners="['bottom-left']"
              corner-size="15px"
              @click="handleConfirm">
              {{ confirmText }}
            </LaserButton>
            <LaserButton
              class="cancel-btn"
              :background-color="cancelBtnBg"
              :border-color="cancelBtnBorder"
              :corners="['bottom-right']"
              corner-size="15px"
              :glow-intensity="'4px'"
              @click="handleCancel">
              {{ cancelText }}
            </LaserButton>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import LaserButton from './LaserButton.vue'
import { useThemeStore } from '../stores/theme'

// 获取主题颜色
const themeStore = useThemeStore()
const modalColor = computed(() => themeStore.modalColor)
const modalBlur = computed(() => themeStore.modalBlur)
const modalActive = computed(() => themeStore.modalActive)
const primaryButton = computed(() => themeStore.primaryButton)
const primaryButtonBorder = computed(() => themeStore.primaryButtonBorder)

// 状态
const visible = ref(false)
const title = ref('确认操作')
const content = ref('您确定要执行此操作吗？')
const confirmText = ref('确定')
const cancelText = ref('取消')
const resolvePromise = ref<((value: boolean) => void) | null>(null)

// 按钮样式
const confirmBtnBg = computed(() => primaryButton.value)
const confirmBtnBorder = computed(() => primaryButtonBorder.value)
const cancelBtnBg = computed(() => 'rgba(60, 60, 60, 0.5)') 
const cancelBtnBorder = computed(() => 'rgba(120, 120, 120, 0.5)')

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

// 暴露方法
defineExpose({
  showConfirm
})
</script>

<style scoped>
.confirm-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2147483646;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.confirm-modal-container {
  width: 400px;
  background: v-bind(modalColor);
  backdrop-filter: blur(v-bind(modalBlur));
  -webkit-backdrop-filter: blur(v-bind(modalBlur));
  border-radius: 0px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalIn 0.25s ease;
  position: relative;
}

@keyframes modalIn {
  from { 
    transform: translateY(-10px); 
    opacity: 0;
  }
  to { 
    transform: translateY(0); 
    opacity: 1;
  }
}

.modal-glow-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: v-bind(modalActive);
  box-shadow: 0 0 10px 1px v-bind(modalActive);
  z-index: 1;
}

.confirm-modal-header {
  padding: 16px 20px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  margin-bottom: 8px;
}

.confirm-icon {
  font-size: 24px;
  color: v-bind(modalActive);
  flex-shrink: 0;
}

.confirm-modal-title {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  font-weight: 500;
  letter-spacing: 0.5px;
  flex: 1;
}

.confirm-modal-body {
  padding: 0 20px 15px;
}

.confirm-modal-content {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
  white-space: pre-line;
  margin-bottom: 10px;
}

.confirm-modal-footer {
  padding: 8px 20px 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.button-container {
  display: flex;
  gap: 8px;
}

.confirm-btn, .cancel-btn {
  flex: 1;
  padding: 6px 0;
  border-radius: 0 !important;
  font-size: 13px;
  height: 34px;
}
</style> 