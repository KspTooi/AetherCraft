<template>
  <div class="glow-input-wrapper">
    <div class="input-container">
      <input 
        :value="modelValue"
        @input="updateValue"
        class="glow-input"
        :disabled="disabled"
        :maxlength="maxLength > 0 ? maxLength : undefined"
        :class="{ 'glow-input-active': isFocused }"
        @focus="isFocused = true"
        @blur="isFocused = false"
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
import {inject, ref, computed} from "vue";
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
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isFocused = ref(false)

const updateValue = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = target.value
  
  if (props.minLength > 0 && value.length < props.minLength) {
    // 可以在这里添加验证逻辑
  }
  
  emit('update:modelValue', value)
  emit('change', value)
}

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

.input-container {
  position: relative;
  width: 100%;
}

.glow-input {
  width: 100%;
  padding: 6px 10px;
  font-size: 14px;
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
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 11px;
  color: v-bind('theme.boxTextColorNoActive');
  pointer-events: none;
}
</style>