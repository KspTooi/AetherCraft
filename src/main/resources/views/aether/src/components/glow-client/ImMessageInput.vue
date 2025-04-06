<template>
  <div class="message-input-container" border="top">
    <div class="message-input-wrapper">
      <textarea 
        v-model="messageInput"
        ref="messageTextarea"
        :placeholder="props.disabled ? (props.placeholderDisabled || '无法发送消息') : (props.placeholder || '请输入消息...')"
        :disabled="props.disabled || isGenerating"
        @keydown="handleKeyPress"
        @input="adjustTextareaHeight"
        @focus="isFocused = true"
        @blur="isFocused = false"></textarea>
      <div class="focus-border" :class="{ active: isFocused }"></div>
      <div class="corner-fill bottom-left" :class="{ active: isFocused }"></div>
    </div>
    
    <GlowButton 
      v-if="!isGenerating" 
      :disabled="!messageInput.trim() || props.disabled"
      @click="handleSend"
      :corners="[`bottom-right`]"
      class="send-button">
      发送
    </GlowButton>
    
    <GlowButton 
      v-else
      @click="handleAbort"
      :corners="[`bottom-right`]"
      class="abort-button danger">
      停止生成
    </GlowButton>

  </div>
</template>

<script setup lang="ts">
import { ref, inject, nextTick, onMounted } from 'vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue"
import GlowButton from "@/components/glow-ui/GlowButton.vue"

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 定义组件props
const props = defineProps<{
  disabled: boolean, //如果为true 则不可操作
  isGenerating?: boolean //是否正在生成回复
  placeholder?: string //自定义输入框提示文本（启用状态）
  placeholderDisabled?: string //自定义输入框提示文本（禁用状态）
}>()

// 事件定义
const emit = defineEmits<{
  (e: 'message-send', message: string): boolean;
  (e: 'abort-generate'): void;
}>()

// 响应数据
const messageInput = ref('')
const messageTextarea = ref<HTMLTextAreaElement | null>(null)
const isFocused = ref(false)
const isGenerating = ref(props.isGenerating || false)

// 处理按键事件
const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    if (!isGenerating.value) {
      handleSend()
    }
  }
}

// 发送消息
const handleSend = () => {
  if (!messageInput.value.trim() || props.disabled || isGenerating.value) return
  
  emit('message-send', messageInput.value.trim())
  
  messageInput.value = ''
  nextTick(() => {
    adjustTextareaHeight()
  })
}

// 中止生成
const handleAbort = () => {
  emit('abort-generate')
}

// 调整文本区域高度
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
.message-input-container {
  width: 100%;
  padding: 12px;
  display: flex;
  align-items: flex-end;
  gap: 10px;
  backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  border-top: 1px solid v-bind('theme.boxBorderColor');
}

.message-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background: v-bind('theme.boxColor');
  border: 1px solid v-bind('theme.boxBorderColorHover');
  min-height: 40px;
  max-height: 150px;
  transition: all 0.3s ease;
  overflow: hidden;
  background-color: v-bind('theme.boxAccentColor');
}

.message-input-wrapper:hover {
  border-color: v-bind('theme.boxBorderColorHover');
}

.message-input-wrapper:focus-within {
  border-color: v-bind('theme.mainBorderColorHover');
}

.message-input-wrapper textarea {
  flex: 1;
  width: 100%;
  min-height: 40px;
  padding: 10px 12px;
  background: transparent;
  border: none;
  color: v-bind('theme.boxTextColor');
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  transition: all 0.3s;
  overflow-y: hidden;
  max-height: 120px;
  display: block;
  margin: 0;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.message-input-wrapper textarea::placeholder {
  color: v-bind('theme.boxTextColorNoActive');
  opacity: 0.7;
}

.message-input-wrapper textarea:focus {
  outline: none;
}

/* 上边框焦点效果 */
.focus-border {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: transparent;
  transform: translateY(-100%);
  opacity: 0;
  transition: all 0.3s ease;
}

.focus-border.active {
  transform: translateY(0);
  opacity: 1;
  background: linear-gradient(15deg, transparent, v-bind('theme.mainBorderColorHover'), transparent);
}

/* 角落填充效果 */
.corner-fill {
  position: absolute;
  width: 0;
  height: 0;
  border-style: solid;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.corner-fill.bottom-left {
  left: 0;
  bottom: 0;
  border-width: 15px 0 0 15px;
  border-color: transparent transparent transparent v-bind('theme.mainBorderColorHover');
}

.corner-fill.active {
  opacity: 1;
}

.send-button, .abort-button {
  align-self: stretch;
  min-width: 80px;
  padding: 0 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .message-input-container {
    padding: 10px;
  }
  
  .send-button, .abort-button {
    min-width: 60px;
    padding: 0 10px;
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  .message-input-container {
    padding: 8px;
  }
  
  .message-input-wrapper textarea {
    font-size: 13px;
    padding: 8px 10px;
  }
  
  .send-button, .abort-button {
    min-width: 50px;
    padding: 0 8px;
    font-size: 13px;
  }
}
</style>