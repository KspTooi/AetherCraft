<template>
  <div class="glow-input-area-wrapper">
    <div v-if="title" class="input-title">{{ title }}</div>
    <div class="input-container">
      <textarea 
        ref="textareaRef"
        :value="modelValue"
        @input="updateValue"
        @wheel="handleWheel"
        class="glow-input-area"
        :class="{ 
          'glow-input-area-active': isFocused,
          'no-resize': props.autoResize || props.noResize,
          'auto-resize': props.autoResize,
          'glow-input-area-error': notBlank && isEmpty
        }"
        :disabled="disabled"
        :maxlength="maxLength > 0 ? maxLength : undefined"
        @focus="isFocused = true"
        @blur="handleBlur"
        :rows="rows"
        :style="{ paddingRight: props.showLength && props.maxLength > 0 ? '50px' : '10px' }"
        v-bind="$attrs"
      ></textarea>
      <div v-if="showLength && maxLength > 0" class="length-indicator">
        {{ currentLength }}/{{ maxLength }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

// 获取 glow 主题
import {inject, ref, computed, watch, onMounted, onBeforeUnmount, nextTick} from "vue";
import {defaultTheme, GLOW_THEME_INJECTION_KEY, type GlowThemeColors} from "@/components/glow-ui/GlowTheme.ts";

const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

const props = defineProps({
  maxLength: {
    type: Number,
    default: -1
  },
  minLength: {
    type: Number,
    default: -1
  },
  showLength: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  modelValue: {
    type: [String, Number],
    default: ''
  },
  title: {
    type: String,
    default: ''
  },
  rows: {
    type: Number,
    default: 3
  },
  autoResize: {
    type: Boolean,
    default: false
  },
  noResize: {
    type: Boolean,
    default: false
  },
  notBlank: {
    type: Boolean,
    default: false
  },
  typingDelay: {
    type: Number,
    default: 500 // 停止输入后500ms触发onTypeDone
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'typeDone'])

const isFocused = ref(false)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
let typingTimer: number | null = null

// 判断输入是否为空
const isEmpty = computed(() => {
  return String(props.modelValue).trim() === ''
})

const updateValue = (event: Event) => {
  const target = event.target as HTMLTextAreaElement
  const value = target.value
  
  if (props.minLength > 0 && value.length < props.minLength) {
    // 可以在这里添加验证逻辑
  }
  
  emit('update:modelValue', value)
  emit('change', value)
  
  if (props.autoResize) {
    // 确保我们使用的是实际的DOM元素
    adjustHeight(target)
  }
  
  // 清除之前的计时器
  if (typingTimer !== null) {
    window.clearTimeout(typingTimer)
    typingTimer = null
  }
  
  // 如果不是notBlank或内容不为空，设置新的计时器
  if (!props.notBlank || !isEmpty.value) {
    typingTimer = window.setTimeout(() => {
      emit('typeDone', value)
      typingTimer = null
    }, props.typingDelay)
  }
}

const handleBlur = (event: FocusEvent) => {
  isFocused.value = false
  
  // 清除计时器
  if (typingTimer !== null) {
    window.clearTimeout(typingTimer)
    typingTimer = null
  }
  
  // 离开输入框时如果内容不为空，立即触发typeDone
  if (!props.notBlank || !isEmpty.value) {
    emit('typeDone', props.modelValue)
  }
}

const adjustHeight = (element: HTMLTextAreaElement) => {
  // 保存当前滚动位置
  const scrollTop = window.scrollY

  // 设置高度为自动，这样才能获取实际内容高度
  element.style.height = 'auto'
  
  // 设置高度为实际内容高度
  element.style.height = `${element.scrollHeight}px`
  
  // 恢复滚动位置，防止页面跳动
  window.scrollTo(window.scrollX, scrollTop)
}

const currentLength = computed(() => {
  return String(props.modelValue).length
})

// 改进监听值变化的逻辑，以确保正确调整高度
watch(() => props.modelValue, () => {
  if (props.autoResize) {
    nextTick(() => {
      const textarea = textareaRef.value
      if (textarea) {
        adjustHeight(textarea)
      }
    })
  }
}, { flush: 'post' })

// 初始化时也需要调整高度
onMounted(() => {
  if (props.autoResize) {
    nextTick(() => {
      const textarea = textareaRef.value
      if (textarea) {
        adjustHeight(textarea)
      }
    })
  }
  
  // 添加触摸事件监听器，用于移动设备
  const textarea = textareaRef.value
  if (textarea) {
    textarea.addEventListener('touchstart', handleTouchStart, { passive: false })
  }
})

// 组件卸载前清理事件监听器
onBeforeUnmount(() => {
  if (typingTimer !== null) {
    window.clearTimeout(typingTimer)
    typingTimer = null
  }
  
  const textarea = textareaRef.value
  if (textarea) {
    textarea.removeEventListener('touchstart', handleTouchStart)
  }
})

// 处理触摸开始事件
const handleTouchStart = (event: TouchEvent) => {
  if (!isFocused.value) return
  
  const textarea = event.currentTarget as HTMLTextAreaElement
  
  // 添加一次性的touchmove事件监听
  const handleTouchMove = (moveEvent: TouchEvent) => {
    const scrollTop = textarea.scrollTop
    const scrollHeight = textarea.scrollHeight
    const height = textarea.clientHeight
    
    // 检测滚动方向和位置
    if ((scrollTop <= 0 && document.body.scrollTop > 0) || 
        (scrollTop + height >= scrollHeight && document.body.scrollTop < document.body.scrollHeight)) {
      // 阻止默认行为，防止页面滚动
      moveEvent.preventDefault()
    }
  }
  
  // 添加监听器并在触摸结束时移除
  textarea.addEventListener('touchmove', handleTouchMove, { passive: false })
  
  const removeTouchMoveListener = () => {
    textarea.removeEventListener('touchmove', handleTouchMove)
    textarea.removeEventListener('touchend', removeTouchMoveListener)
    textarea.removeEventListener('touchcancel', removeTouchMoveListener)
  }
  
  textarea.addEventListener('touchend', removeTouchMoveListener, { once: true })
  textarea.addEventListener('touchcancel', removeTouchMoveListener, { once: true })
}

// 防止滚动传播到父元素
const handleWheel = (event: WheelEvent) => {
  const textarea = event.currentTarget as HTMLTextAreaElement
  
  // 只在textarea有焦点时处理
  if (!isFocused.value) return
  
  const scrollTop = textarea.scrollTop
  const scrollHeight = textarea.scrollHeight
  const height = textarea.clientHeight
  const delta = event.deltaY
  
  // 检查是否达到顶部或底部边界
  const isAtTop = scrollTop === 0 && delta < 0
  const isAtBottom = scrollTop + height >= scrollHeight && delta > 0
  
  // 如果滚动已经到达边界，并且继续朝那个方向滚动，阻止事件冒泡
  if (isAtTop || isAtBottom) {
    event.preventDefault()
  }
}

defineExpose({
  focus: () => {
    const textarea = textareaRef.value
    if (textarea) {
      textarea.focus()
    }
  }
})
</script>

<style scoped>
.glow-input-area-wrapper {
  position: relative;
  width: 100%;
  margin-bottom: 6px;
}

.input-title {
  font-family: 'Chakra Petch', sans-serif;
  font-size: 12px;
  color: v-bind('theme.boxTextColor');
  margin-bottom: 4px;
  opacity: 0.8;
  font-weight: 500;
}

.input-container {
  position: relative;
  width: 100%;
}

.glow-input-area {
  font-family: 'Chakra Petch', sans-serif;
  width: 100%;
  padding: 6px 10px;
  font-size: 13px;
  background-color: v-bind('theme.boxSecondColor');
  color: v-bind('theme.boxTextColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0;
  transition: all 0.3s ease;
  outline: none;
  box-sizing: border-box;
  backdrop-filter: blur(v-bind('theme.boxBlur') + 'px');
  line-height: 1.5;
  min-height: 60px;
  overflow: auto;
  resize: vertical; /* 默认可垂直调整 */
  overscroll-behavior: contain; /* 防止滚动传播 */
}

/* 自定义滚动条样式 */
.glow-input-area::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.glow-input-area::-webkit-scrollbar-track {
  background: transparent;
}

.glow-input-area::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.glow-input-area::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.glow-input-area::placeholder {
  font-family: 'Chakra Petch', sans-serif;
}

.glow-input-area:hover:not(:disabled) {
  background-color: v-bind('theme.boxSecondColor');
  border-color: v-bind('theme.boxSecondColorHover');
  backdrop-filter: blur(v-bind('theme.boxBlurHover') + 'px');
}

.glow-input-area-active:not(:disabled) {
  background-color: v-bind('theme.boxSecondColor');
  border-color: v-bind('theme.boxSecondColorHover');
  backdrop-filter: blur(v-bind('theme.boxBlurActive') + 'px');
}

.glow-input-area:disabled {
  background-color: v-bind('theme.disabledColor');
  border-color: v-bind('theme.disabledBorderColor');
  color: v-bind('theme.boxTextColorNoActive');
  cursor: not-allowed;
}

.length-indicator {
  font-family: 'Chakra Petch', sans-serif;
  position: absolute;
  right: 8px;
  bottom: 8px;
  font-size: 11px;
  color: v-bind('theme.boxTextColorNoActive');
  pointer-events: none;
}

/* 样式：禁止调整大小 */
.no-resize {
  resize: none !important;
}

/* 样式：自动调整大小模式 */
.auto-resize {
  overflow: hidden !important;
}

.glow-input-area-error {
  border-color: v-bind('theme.dangerBorderColor') !important;
}
</style>