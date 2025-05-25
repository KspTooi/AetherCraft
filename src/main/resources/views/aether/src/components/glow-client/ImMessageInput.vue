<template>
  <div class="message-input-container" border="top">
    <div class="message-input-wrapper" :class="{ focused: isFocused, generating: props.isGenerating }">
      <!-- 顶部发光装饰条 -->
      <div class="top-glow-line" :class="{ active: isFocused }"></div>
      
      <!-- 左侧装饰线 -->
      <div class="side-accent-line left" :class="{ active: isFocused }"></div>
      
      <!-- 右侧装饰线 -->
      <div class="side-accent-line right" :class="{ active: isFocused }"></div>
      
      <!-- 底部发光装饰条 -->
      <div class="bottom-glow-line" :class="{ active: isFocused }"></div>
      
      <!-- 角落装饰点 -->
      <div class="corner-dot top-left" :class="{ active: isFocused }"></div>
      <div class="corner-dot top-right" :class="{ active: isFocused }"></div>
      <div class="corner-dot bottom-left" :class="{ active: isFocused }"></div>
      <div class="corner-dot bottom-right" :class="{ active: isFocused }"></div>
      
      <textarea 
        v-model="messageInput"
        ref="messageTextarea"
        :placeholder="props.disabled ? (props.placeholderDisabled || '无法发送消息') : (props.placeholder || '请输入消息...')"
        :disabled="props.disabled"
        @keydown="handleKeyPress"
        @input="adjustTextareaHeight"
        @focus="isFocused = true"
        @blur="isFocused = false"></textarea>
    </div>
    
    <GlowButton 
      v-if="!props.isGenerating" 
      :disabled="!messageInput.trim() || props.disabled"
      @click="handleSend"
      :corners="[`bottom-right`]"
      class="send-button">
      <i class="bi bi-send"></i>
      <span>发送</span>
    </GlowButton>
    
    <GlowButton 
      v-else
      @click="handleAbort"
      :corners="[`bottom-right`]"
      theme="danger"
      class="abort-button danger">
      <i class="bi bi-stop-circle"></i>
      <span>停止</span>
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

// 处理按键事件
const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    if (!props.isGenerating) {
      handleSend()
    }
  }
}

// 发送消息
const handleSend = () => {
  if (!messageInput.value.trim() || props.disabled || props.isGenerating) return
  
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
  gap: 12px;
  backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  border-top: 1px solid v-bind('theme.boxBorderColor');
  position: relative;
}

.message-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background: v-bind('theme.boxAccentColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  min-height: 42px;
  max-height: 150px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border-radius: 2px;
}

.message-input-wrapper:hover {
  border-color: v-bind('theme.boxBorderColorHover');
  box-shadow: 0 0 20px rgba(0, 150, 255, 0.1);
}

.message-input-wrapper.focused {
  border-color: v-bind('theme.mainBorderColorHover');
  box-shadow: 
    0 0 30px rgba(0, 150, 255, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.message-input-wrapper.generating {
  border-color: v-bind('theme.boxGlowColor');
  box-shadow: 
    0 0 25px v-bind('theme.boxGlowColor + "40"'),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

/* 顶部发光装饰条 */
.top-glow-line {
  position: absolute;
  top: -1px;
  left: -1px;
  right: -1px;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    v-bind('theme.mainBorderColorHover') 20%, 
    v-bind('theme.boxGlowColor') 50%, 
    v-bind('theme.mainBorderColorHover') 80%, 
    transparent 100%);
  opacity: 0;
  transform: scaleX(0);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 2;
}

.top-glow-line.active {
  opacity: 1;
  transform: scaleX(1);
}

/* 底部发光装饰条 */
.bottom-glow-line {
  position: absolute;
  bottom: -1px;
  left: -1px;
  right: -1px;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    v-bind('theme.mainBorderColorHover') 30%, 
    v-bind('theme.boxGlowColor') 50%, 
    v-bind('theme.mainBorderColorHover') 70%, 
    transparent 100%);
  opacity: 0;
  transform: scaleX(0);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1) 0.1s;
  z-index: 2;
}

.bottom-glow-line.active {
  opacity: 0.8;
  transform: scaleX(1);
}

/* 侧边装饰线 */
.side-accent-line {
  position: absolute;
  width: 1px;
  top: -1px;
  bottom: -1px;
  background: linear-gradient(180deg, 
    transparent 0%, 
    v-bind('theme.mainBorderColorHover') 30%, 
    v-bind('theme.boxGlowColor') 50%, 
    v-bind('theme.mainBorderColorHover') 70%, 
    transparent 100%);
  opacity: 0;
  transform: scaleY(0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) 0.2s;
  z-index: 2;
}

.side-accent-line.left {
  left: -1px;
}

.side-accent-line.right {
  right: -1px;
}

.side-accent-line.active {
  opacity: 0.6;
  transform: scaleY(1);
}

/* 角落装饰点 */
.corner-dot {
  position: absolute;
  width: 2px;
  height: 2px;
  background: v-bind('theme.boxGlowColor');
  border-radius: 50%;
  opacity: 0;
  transform: scale(0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 3;
}

.corner-dot.top-left {
  top: -1px;
  left: -1px;
  transition-delay: 0.3s;
}

.corner-dot.top-right {
  top: -1px;
  right: -1px;
  transition-delay: 0.35s;
}

.corner-dot.bottom-left {
  bottom: -1px;
  left: -1px;
  transition-delay: 0.4s;
}

.corner-dot.bottom-right {
  bottom: -1px;
  right: -1px;
  transition-delay: 0.45s;
}

.corner-dot.active {
  opacity: 1;
  transform: scale(1);
  box-shadow: 0 0 6px v-bind('theme.boxGlowColor');
}

.message-input-wrapper textarea {
  flex: 1;
  width: 100%;
  min-height: 40px;
  padding: 11px 16px;
  background: transparent;
  border: none;
  color: v-bind('theme.boxTextColor');
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow-y: hidden;
  max-height: 120px;
  display: block;
  margin: 0;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", "PingFang SC", "Microsoft YaHei", sans-serif;
  z-index: 1;
  position: relative;
}

.message-input-wrapper textarea::placeholder {
  color: v-bind('theme.boxTextColorNoActive');
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.message-input-wrapper.focused textarea::placeholder {
  opacity: 0.5;
}

.message-input-wrapper textarea:focus {
  outline: none;
}

/* 自定义滚动条样式 */
.message-input-wrapper textarea::-webkit-scrollbar {
  width: 6px;
}

.message-input-wrapper textarea::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.message-input-wrapper textarea::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColorHover');
  border-radius: 3px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.message-input-wrapper textarea::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.mainBorderColorHover');
  box-shadow: 0 0 8px v-bind('theme.boxGlowColor + "60"');
}

.message-input-wrapper.focused textarea::-webkit-scrollbar-thumb {
  background: v-bind('theme.mainBorderColorHover');
  box-shadow: 0 0 6px v-bind('theme.boxGlowColor + "40"');
}

.message-input-wrapper.generating textarea::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 10px v-bind('theme.boxGlowColor + "80"');
  animation: scrollbarPulse 2s ease-in-out infinite;
}

@keyframes scrollbarPulse {
  0%, 100% { 
    opacity: 0.8;
    box-shadow: 0 0 6px v-bind('theme.boxGlowColor + "40"');
  }
  50% { 
    opacity: 1;
    box-shadow: 0 0 12px v-bind('theme.boxGlowColor + "80"');
  }
}

/* Firefox滚动条样式 */
.message-input-wrapper textarea {
  scrollbar-width: thin;
  scrollbar-color: v-bind('theme.boxBorderColorHover') transparent;
}

.send-button, .abort-button {
  align-self: stretch;
  min-width: 90px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.send-button i, .abort-button i {
  font-size: 14px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.send-button:hover i {
  transform: translateX(2px);
}

.abort-button:hover i {
  transform: rotate(90deg);
}

.send-button span, .abort-button span {
  font-size: 14px;
  letter-spacing: 0.5px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .message-input-container {
    padding: 10px;
    gap: 10px;
  }
  
  .send-button, .abort-button {
    min-width: 70px;
    padding: 0 12px;
    gap: 4px;
  }
  
  .send-button span, .abort-button span {
    font-size: 13px;
  }
  
  .send-button i, .abort-button i {
    font-size: 13px;
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  .message-input-container {
    padding: 8px;
    gap: 8px;
  }
  
  .message-input-wrapper textarea {
    font-size: 13px;
    padding: 10px 14px;
  }
  
  .send-button, .abort-button {
    min-width: 60px;
    padding: 0 10px;
    gap: 3px;
  }
  
  .send-button span, .abort-button span {
    font-size: 12px;
  }
  
  .send-button i, .abort-button i {
    font-size: 12px;
  }
  
  /* 在超小屏幕上隐藏文字，只显示图标 */
  .send-button span, .abort-button span {
    display: none;
  }
  
  .send-button, .abort-button {
    min-width: 44px;
  }
}
</style>