<template>
  <div class="chat-input">
    <div class="chat-input-wrapper">
      <textarea 
        v-model="messageInput"
        ref="messageTextarea"
        :placeholder="isDisabled ? '请先选择一个角色' : '在这里输入您的消息...'"
        :disabled="isLoading || isDisabled"
        @keydown="handleKeyPress"
        @input="adjustTextareaHeight"
        @focus="isFocused = true"
        @blur="isFocused = false"></textarea>
      <div class="focus-border" :class="{ active: isFocused }"></div>
      <span class="corner-fill corner-bl-fill" :class="{ active: isFocused }" :style="cornerStyle"></span>
    </div>
    <LaserButton 
      v-if="!isLoading" 
      :disabled="!messageInput.trim() || isDisabled"
      corner="bottom-right"
      corner-size="15px"
      :background-color="primaryButton"
      :border-color="primaryButtonBorder"
      :glow-color="primaryHover"
      :innerGlow="true"
      :particles="true"
      @click="handleSend">
      发送
    </LaserButton>
    <LaserButton 
      v-else
      background-color="rgba(254, 79, 79, 0.3)"
      border-color="rgba(254, 79, 79, 0.6)"
      glow-color="rgba(254, 79, 79, 0.5)"
      corner="bottom-right"
      corner-size="15px"
      :innerGlow="true"
      @click="handleStop">
      停止生成
    </LaserButton>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, computed } from 'vue'
import LaserButton from './LaserButton.vue'
import { useThemeStore } from '../stores/theme'

// 获取主题颜色
const themeStore = useThemeStore()
const primaryColor = computed(() => themeStore.primaryColor)
const activeColor = computed(() => themeStore.activeColor)
const primaryHover = computed(() => themeStore.primaryHover)
const primaryButton = computed(() => themeStore.primaryButton)
const primaryButtonBorder = computed(() => themeStore.primaryButtonBorder)
const textareaColor = computed(() => themeStore.textareaColor)
const textareaActive = computed(() => themeStore.textareaActive)
const textareaBorder = computed(() => themeStore.textareaBorder)

// 角落样式
const cornerStyle = computed(() => {
  return {
    '--corner-size': '15px',
    '--corner-color': primaryButtonBorder.value,
  };
});

// Props
const props = defineProps({
  isLoading: {
    type: Boolean,
    default: false
  },
  isDisabled: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits<{
  (e: 'send', message: string): void
  (e: 'stop'): void
}>()

// Refs
const messageInput = ref('')
const messageTextarea = ref<HTMLTextAreaElement | null>(null)
const isFocused = ref(false)

// Methods
const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}

const handleSend = () => {
  if (!messageInput.value.trim() || props.isLoading || props.isDisabled) return
  emit('send', messageInput.value.trim())
  messageInput.value = ''
  nextTick(() => {
    adjustTextareaHeight()
  })
}

const handleStop = () => {
  emit('stop')
}

const adjustTextareaHeight = () => {
  const textarea = messageTextarea.value
  if (!textarea) return
  
  textarea.style.height = '40px'
  const scrollHeight = textarea.scrollHeight

  if (scrollHeight <= 40 || !messageInput.value.trim()) {
    textarea.style.height = '40px'
    textarea.style.overflowY = 'hidden'
    return
  }

  if (scrollHeight > 120) {
    textarea.style.height = '120px'
    textarea.style.overflowY = 'auto'
  } else {
    textarea.style.height = scrollHeight + 'px'
    textarea.style.overflowY = 'hidden'
  }
}

// 生命周期钩子
onMounted(() => {
  nextTick(() => {
    adjustTextareaHeight()
  })
})
</script>

<style scoped>
/* 输入区域样式 */
.chat-input {
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  gap: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background: v-bind(textareaColor);
  border: 1px solid v-bind(textareaBorder);
  border-radius: 0;
  min-height: 40px;
  max-height: 150px;
  padding: 2px 4px;
  transition: all 0.3s ease;
  overflow: hidden;
}

/* 角落填充效果 */
.corner-fill {
  position: absolute;
  width: 0;
  height: 0;
  border-style: solid;
  pointer-events: none; /* 确保不影响点击事件 */
  z-index: 3; /* 确保显示在其他元素上面 */
  transition: all 0.3s ease;
  opacity: 0; /* 默认隐藏 */
}

/* 左下角 */
.corner-bl-fill {
  left: 0;
  bottom: 0;
  border-width: var(--corner-size) 0 0 var(--corner-size);
  border-color: transparent transparent transparent var(--corner-color);
}

.corner-fill.active {
  opacity: 1; /* 激活时显示 */
}

.chat-input textarea {
  flex: 1;
  width: 100%;
  min-height: 40px;
  padding: 8px 12px;
  background: transparent;
  border: none;
  color: #fff;
  resize: none;
  font-size: 15px;
  line-height: 1.5;
  transition: all 0.3s;
  overflow-y: hidden;
  max-height: 120px;
  display: block;
  margin: 0;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", "PingFang SC", "Microsoft YaHei", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.chat-input textarea::placeholder {
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.5;
  padding: 0;
  margin: 0;
  font-family: inherit;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.chat-input textarea:focus {
  outline: none;
}

.chat-input-wrapper:focus-within {
  background: v-bind(textareaActive);
  border-color: v-bind(primaryColor);
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
}

/* 上边框焦点效果 */
.focus-border {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, v-bind(textareaBorder), transparent);
  transform: translateY(-100%);
  opacity: 0;
  transition: all 0.3s ease;
}

.focus-border.active {
  transform: translateY(0);
  opacity: 1;
  background: linear-gradient(90deg, transparent, v-bind(activeColor), transparent);
  box-shadow: 0 0 4px v-bind(textareaBorder);
}

/* 移除过度的内发光效果 */
.chat-input-wrapper:focus-within::after {
  content: none;
}
</style> 