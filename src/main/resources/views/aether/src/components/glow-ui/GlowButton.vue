<template>
  <button 
    class="laser-button"
    :class="{ 
      disabled: disabled,
      'corner-tr': corners.includes('top-right'),
      'corner-tl': corners.includes('top-left'),
      'corner-br': corners.includes('bottom-right'),
      'corner-bl': corners.includes('bottom-left'),
      'multiple-corners': corners.length > 0,
      'danger-style': theme === 'danger'
    }"
    :disabled="disabled"
    :type="type"
    @mouseover="isHovered = true"
    @mouseleave="isHovered = false"
    @click="handleClick">
    
    <!-- 内发光效果 -->
    <div v-if="!disabled" class="inner-glow"></div>

    <!-- 按钮内容 -->
    <div class="button-content">
      <slot></slot>
    </div>
    
    <!-- 角落填充 -->
    <span v-if="corners.includes('top-right')" class="corner-fill corner-tr-fill"></span>
    <span v-if="corners.includes('top-left')" class="corner-fill corner-tl-fill"></span>
    <span v-if="corners.includes('bottom-right')" class="corner-fill corner-br-fill"></span>
    <span v-if="corners.includes('bottom-left')" class="corner-fill corner-bl-fill"></span>
  </button>
</template>

<script setup lang="ts">
import { inject, ref, computed } from 'vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from './GlowTheme.ts'
import type { GlowThemeColors } from './GlowTheme.ts'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },

  //填充角落 full top-right top-left bottom-right bottom-left
  corners: {
    type: Array as () => string[],
    default: () => []
  },
  
  // 按钮类型
  type: {
    type: String as () => 'button' | 'submit' | 'reset',
    default: 'button'
  },

  theme:{
    type: String as () => 'main' | 'danger',
    default: 'main'
  }
})

// 定义emit事件
const emit = defineEmits(['click'])

// 当组件单独运行时，如果没有注入主题，则使用默认主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 根据theme属性选择对应的颜色
const buttonColor = computed(() => props.theme === 'danger' ? theme.dangerColor : theme.mainColor)
const buttonColorHover = computed(() => props.theme === 'danger' ? theme.dangerColorHover : theme.mainColorHover)
const buttonColorActive = computed(() => props.theme === 'danger' ? theme.dangerColorActive : theme.mainColorActive)
const buttonBorderColor = computed(() => props.theme === 'danger' ? theme.dangerBorderColor : theme.mainBorderColor)
const buttonBorderColorHover = computed(() => props.theme === 'danger' ? theme.dangerBorderColorHover : theme.mainBorderColorHover)
const buttonBorderColorActive = computed(() => props.theme === 'danger' ? theme.dangerBorderColorActive : theme.mainBorderColorActive)
const buttonTextColor = computed(() => props.theme === 'danger' ? theme.dangerTextColor : theme.mainTextColor)
const buttonTextColorActive = computed(() => props.theme === 'danger' ? theme.dangerTextColorActive : theme.mainTextColorActive)

// 悬停状态
const isHovered = ref(false)

// 点击处理
const handleClick = (event: MouseEvent) => {
  if (!props.disabled) {
    emit('click', event)
  }
}
</script>

<style scoped>
.laser-button {
  position: relative;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: v-bind('buttonColor');
  color: v-bind('buttonTextColor');
  border: 1px solid v-bind('buttonBorderColor');
  box-shadow: 0 0 8px v-bind('buttonColor');
  transition: all 0.3s ease;
  min-height: 40px;
  line-height: 1.5;
  overflow: hidden;
}

.button-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.laser-button:hover {
  background-color: v-bind('buttonColorHover');
  border-color: v-bind('buttonBorderColorHover');
  box-shadow: 0 0 12px v-bind('buttonBorderColorHover');
}

.laser-button:active {
  background-color: v-bind('buttonColorActive');
  border-color: v-bind('buttonBorderColorActive');
  box-shadow: 0 0 15px v-bind('buttonBorderColorActive');
  color: v-bind('buttonTextColorActive');
}

.laser-button.disabled {
  opacity: 0.8;
  cursor: not-allowed;
  background: v-bind('theme.disabledColor') !important;
  border-color: v-bind('theme.disabledBorderColor') !important;
  border-style: dashed !important;
  box-shadow: none !important;
  color: rgba(255, 255, 255, 0.5) !important;
}

/* 内发光效果 */
.inner-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  background: transparent;
  box-shadow: inset 0 0 6px v-bind('buttonColor');
  opacity: 0.5;
  transition: opacity 0.3s ease, box-shadow 0.3s ease;
  z-index: 1;
}

.laser-button:hover .inner-glow {
  opacity: 0.8;
  box-shadow: inset 0 0 10px v-bind('buttonColorHover');
}

.laser-button:active .inner-glow {
  opacity: 0.9;
  box-shadow: inset 0 0 12px v-bind('buttonColorActive');
}

/* 角落填充效果 */
.corner-fill {
  position: absolute;
  width: 0;
  height: 0;
  border-style: solid;
  pointer-events: none;
  z-index: 3;
  transition: all 0.3s ease;
}

/* 右下角 */
.corner-br-fill {
  right: 0;
  bottom: 0;
  border-width: 0 0 20px 20px;
  border-color: transparent transparent v-bind('buttonBorderColor') transparent;
}

/* 左下角 */
.corner-bl-fill {
  left: 0;
  bottom: 0;
  border-width: 20px 0 0 20px;
  border-color: transparent transparent transparent v-bind('buttonBorderColor');
}

/* 右上角 */
.corner-tr-fill {
  right: 0;
  top: 0;
  border-width: 0 20px 20px 0;
  border-color: transparent v-bind('buttonBorderColor') transparent transparent;
}

/* 左上角 */
.corner-tl-fill {
  left: 0;
  top: 0;
  border-width: 20px 20px 0 0;
  border-color: v-bind('buttonBorderColor') transparent transparent transparent;
}

/* 禁用状态下的角落样式 */
.laser-button.disabled .corner-fill {
  border-style: dashed;
}

.laser-button.disabled .corner-br-fill {
  border-color: transparent transparent v-bind('theme.disabledBorderColor') transparent;
}

.laser-button.disabled .corner-bl-fill {
  border-color: transparent transparent transparent v-bind('theme.disabledBorderColor');
}

.laser-button.disabled .corner-tr-fill {
  border-color: transparent v-bind('theme.disabledBorderColor') transparent transparent;
}

.laser-button.disabled .corner-tl-fill {
  border-color: v-bind('theme.disabledBorderColor') transparent transparent transparent;
}

/* 悬停时角落颜色变化 */
.laser-button:not(.disabled):hover .corner-fill {
  border-color: transparent transparent v-bind('buttonBorderColorHover') transparent;
  transform: scale(1.05);
}

.laser-button:not(.disabled):hover .corner-tr-fill {
  border-color: transparent v-bind('buttonBorderColorHover') transparent transparent;
}

.laser-button:not(.disabled):hover .corner-tl-fill {
  border-color: v-bind('buttonBorderColorHover') transparent transparent transparent;
}

.laser-button:not(.disabled):hover .corner-bl-fill {
  border-color: transparent transparent transparent v-bind('buttonBorderColorHover');
}

/* active状态下角落颜色变化 */
.laser-button:not(.disabled):active .corner-fill {
  border-color: transparent transparent v-bind('buttonBorderColorActive') transparent;
  transform: scale(1.1);
}

.laser-button:not(.disabled):active .corner-tr-fill {
  border-color: transparent v-bind('buttonBorderColorActive') transparent transparent;
}

.laser-button:not(.disabled):active .corner-tl-fill {
  border-color: v-bind('buttonBorderColorActive') transparent transparent transparent;
}

.laser-button:not(.disabled):active .corner-bl-fill {
  border-color: transparent transparent transparent v-bind('buttonBorderColorActive');
}
</style>