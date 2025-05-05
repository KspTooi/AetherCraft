<template>
  <div class="glow-input-wrapper">
    <div v-if="title" class="input-title">{{ title }}</div>
    <div class="input-container">
      <input 
        :value="modelValue"
        @input="updateValue"
        class="glow-input"
        :disabled="disabled"
        :maxlength="maxLength > 0 ? maxLength : undefined"
        :class="{ 
          'glow-input-active': isFocused,
          'glow-input-error': (notBlank && isEmpty) || isInvalid 
        }"
        @focus="isFocused = true"
        @blur="handleBlur"
        v-bind="$attrs"
        :style="inputStyle"
      />
      <div v-if="showLength && maxLength > 0" class="length-indicator">
        {{ currentLength }}/{{ maxLength }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

// 获取 glow 主题
import {inject, ref, computed, watch, onUnmounted} from "vue";
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
  notBlank: {
    type: Boolean,
    default: false
  },
  typingDelay: {
    type: Number,
    default: 500 // 停止输入后500ms触发onTypeDone
  },
  isInvalid: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'typeDone'])

const isFocused = ref(false)
let typingTimer: number | null = null

// 判断输入是否为空
const isEmpty = computed(() => {
  return String(props.modelValue).trim() === ''
})

const updateValue = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = target.value
  
  if (props.minLength > 0 && value.length < props.minLength) {
    // 可以在这里添加验证逻辑
  }
  
  emit('update:modelValue', value)
  emit('change', value)
  
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

// 组件卸载时清理计时器
onUnmounted(() => {
  if (typingTimer !== null) {
    window.clearTimeout(typingTimer)
    typingTimer = null
  }
})

const currentLength = computed(() => {
  return String(props.modelValue).length
})

// 计算输入框样式
const inputStyle = computed(() => {
  return {
    paddingRight: props.showLength && props.maxLength > 0 ? '50px' : '10px'
  }
})

defineExpose({
  focus: () => {
    const input = document.querySelector('.glow-input') as HTMLInputElement
    input?.focus()
  }
})
</script>

<style scoped>
.glow-input-wrapper {
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

.glow-input {
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
  line-height: 1.2;
}

.glow-input::placeholder {
  font-family: 'Chakra Petch', sans-serif;
}

.glow-input:hover:not(:disabled) {
  background-color: v-bind('theme.boxSecondColor');
  border-color: v-bind('theme.boxSecondColorHover');
  backdrop-filter: blur(v-bind('theme.boxBlurHover') + 'px');
}

.glow-input-active:not(:disabled) {
  background-color: v-bind('theme.boxSecondColor');
  border-color: v-bind('theme.boxSecondColorHover');
  backdrop-filter: blur(v-bind('theme.boxBlurActive') + 'px');
}

.glow-input:disabled {
  background-color: v-bind('theme.disabledColor');
  border-color: v-bind('theme.disabledBorderColor');
  color: v-bind('theme.boxTextColorNoActive');
  cursor: not-allowed;
}

.length-indicator {
  font-family: 'Chakra Petch', sans-serif;
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 11px;
  color: v-bind('theme.boxTextColorNoActive');
  pointer-events: none;
}

.glow-input-error {
  border-color: v-bind('theme.dangerBorderColor') !important;
}

.glow-input-error-message {
    font-family: 'Chakra Petch', sans-serif;
    font-size: 11px;
    color: v-bind('theme.dangerColor');
    padding-left: 2px;
    margin-top: 2px;
    min-height: 14px;
}
</style>