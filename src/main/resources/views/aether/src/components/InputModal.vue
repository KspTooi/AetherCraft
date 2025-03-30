<!-- 通用输入模态框组件 -->
<template>
  <teleport to="body">
    <div v-if="visible" class="input-modal-overlay" @click.self="handleCancel">
      <div class="input-modal-container">
        <div class="modal-glow-border"></div>
        <div class="input-modal-header">
          <div class="input-icon">
            <i class="bi bi-pencil-square"></i>
          </div>
          <h3 class="input-modal-title">{{ title }}</h3>
        </div>
        <div class="input-modal-body">
          <div class="input-modal-content">{{ content }}</div>
          <div class="input-field-container">
            <template v-if="multiline">
              <textarea 
                ref="textareaRef"
                v-model="inputValue" 
                :placeholder="placeholder"
                class="input-textarea"
                @keyup.ctrl.enter="handleConfirm"></textarea>
            </template>
            <template v-else>
              <input 
                ref="inputRef"
                v-model="inputValue" 
                :type="inputType"
                :placeholder="placeholder"
                class="input-field" 
                @keyup.enter="handleConfirm" />
            </template>
          </div>
        </div>
        <div class="input-modal-footer">
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
import { ref, computed, nextTick } from 'vue'
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
const title = ref('请输入')
const content = ref('请在下方输入内容:')
const confirmText = ref('确定')
const cancelText = ref('取消')
const inputValue = ref('')
const placeholder = ref('请输入...')
const inputType = ref('text')
const multiline = ref(false)
const inputRef = ref<HTMLInputElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const resolvePromise = ref<((value: string | null) => void) | null>(null)

// 按钮样式
const confirmBtnBg = computed(() => primaryButton.value)
const confirmBtnBorder = computed(() => primaryButtonBorder.value)
const cancelBtnBg = computed(() => 'rgba(60, 60, 60, 0.5)') 
const cancelBtnBorder = computed(() => 'rgba(120, 120, 120, 0.5)')

// 处理确认
const handleConfirm = () => {
  visible.value = false
  if (resolvePromise.value) {
    resolvePromise.value(inputValue.value)
    resolvePromise.value = null
  }
}

// 处理取消
const handleCancel = () => {
  visible.value = false
  if (resolvePromise.value) {
    resolvePromise.value(null)
    resolvePromise.value = null
  }
}

/**
 * 显示输入对话框
 * @param options 可选配置项
 * @returns Promise，用户确认时返回输入的值，取消时返回null
 */
const showInput = (options?: {
  title?: string
  content?: string
  confirmText?: string
  cancelText?: string
  defaultValue?: string
  placeholder?: string
  inputType?: string
  multiline?: boolean
}): Promise<string | null> => {
  // 重置输入值
  inputValue.value = options?.defaultValue || ''
  
  // 更新选项
  if (options) {
    if (options.title) title.value = options.title
    if (options.content) content.value = options.content
    if (options.confirmText) confirmText.value = options.confirmText
    if (options.cancelText) cancelText.value = options.cancelText
    if (options.placeholder) placeholder.value = options.placeholder
    if (options.inputType) inputType.value = options.inputType
    multiline.value = options.multiline || false
  } else {
    multiline.value = false
  }
  
  // 显示模态框
  visible.value = true
  
  // 下一次DOM更新后聚焦输入框
  nextTick(() => {
    if (multiline.value && textareaRef.value) {
      textareaRef.value.focus()
      textareaRef.value.select()
    } else if (inputRef.value) {
      inputRef.value.focus()
      inputRef.value.select()
    }
  })
  
  // 返回Promise
  return new Promise((resolve) => {
    resolvePromise.value = resolve
  })
}

// 暴露方法
defineExpose({
  showInput
})
</script>

<style scoped>
.input-modal-overlay {
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

.input-modal-container {
  width: 400px;
  max-width: 90%;
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

.input-modal-header {
  padding: 16px 20px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  margin-bottom: 8px;
}

.input-icon {
  font-size: 24px;
  color: v-bind(modalActive);
  flex-shrink: 0;
}

.input-modal-title {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  font-weight: 500;
  letter-spacing: 0.5px;
  flex: 1;
}

.input-modal-body {
  padding: 0 20px 15px;
}

.input-modal-content {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
  white-space: pre-line;
  margin-bottom: 12px;
}

.input-field-container {
  margin-bottom: 5px;
  width: 100%;
  box-sizing: border-box;
}

.input-field {
  width: 100%;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
  border-radius: 4px;
  transition: all 0.3s;
  box-sizing: border-box;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.input-field:focus {
  outline: none;
  border-color: v-bind(modalActive);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 5px rgba(79, 172, 254, 0.3);
}

.input-field::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.input-textarea {
  width: 100%;
  min-height: 80px;
  resize: vertical;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
  border-radius: 4px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.input-textarea:focus {
  outline: none;
  border-color: v-bind(modalActive);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 5px rgba(79, 172, 254, 0.3);
}

.input-textarea::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.input-modal-footer {
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