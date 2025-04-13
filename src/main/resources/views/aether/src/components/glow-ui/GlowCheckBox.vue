<template>
  <div class="glow-check-box-wrapper" 
       @click="toggleCheck" 
       @mousedown="isActive = true" 
       @mouseup="isActive = false" 
       @mouseleave="isActive = false"
       @mouseover="handleMouseOver"
       @mouseout="handleMouseOut"
       ref="wrapperRef">
    <div class="glow-check-box" :class="{ 'active': isActive, 'hovered': isHovered }">
      <i v-if="modelValue" class="bi bi-x"></i>
    </div>
    <div class="content-slot">
      <slot></slot>
    </div>
  </div>
  <Teleport to="body" v-if="tip && isHovered">
    <div class="glow-tip" ref="tipRef" :style="tipStyleObject">{{ tip }}</div>
  </Teleport>
</template>

<script setup lang="ts">
import {inject, ref, computed, nextTick, onMounted, onUnmounted, watch} from "vue";
import {defaultTheme, GLOW_THEME_INJECTION_KEY, type GlowThemeColors} from "@/components/glow-ui/GlowTheme.ts";

const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  tip: {
    type: String,
    default: ''
  }
})

const isActive = ref(false)
const isHovered = ref(false)
const tipRef = ref<HTMLElement | null>(null)
const wrapperRef = ref<HTMLElement | null>(null)
const tipPosition = ref({ x: 0, y: 0 })

const emit = defineEmits(['update:modelValue'])

const toggleCheck = () => {
  emit('update:modelValue', !props.modelValue)
}

const handleMouseOver = () => {
  isHovered.value = true
  nextTick(() => {
    updateTipPosition()
  })
}

const handleMouseOut = () => {
  isHovered.value = false
}

// 添加一个安全边距，防止提示框靠近屏幕边缘
const SAFE_MARGIN = 10

const updateTipPosition = () => {
  if (!tipRef.value || !wrapperRef.value) return
  
  const tipElement = tipRef.value
  const wrapperRect = wrapperRef.value.getBoundingClientRect()
  
  // 获取提示框尺寸
  const tipRect = tipElement.getBoundingClientRect()
  
  // 计算最佳位置
  let x = wrapperRect.left + window.scrollX
  let y = wrapperRect.bottom + window.scrollY + 5 // 默认在下方
  
  // 检查右侧边界
  if (x + tipRect.width > window.innerWidth - SAFE_MARGIN) {
    // 如果超出右侧，将提示框右对齐
    x = wrapperRect.right + window.scrollX - tipRect.width
    
    // 如果仍然超出左侧，则将提示框放在最左侧
    if (x < SAFE_MARGIN) {
      x = SAFE_MARGIN
    }
  }
  
  // 检查下方边界
  if (y + tipRect.height > window.innerHeight + window.scrollY - SAFE_MARGIN) {
    // 如果超出下方，将提示框放在元素上方
    y = wrapperRect.top + window.scrollY - tipRect.height - 5
    
    // 如果上方也没有足够空间，优先保证提示框可见
    if (y < window.scrollY + SAFE_MARGIN) {
      y = window.scrollY + SAFE_MARGIN
    }
  }
  
  tipPosition.value = { x, y }
}

// 当窗口大小变化时重新计算位置
const onWindowResize = () => {
  if (isHovered.value) {
    updateTipPosition()
  }
}

// 当页面滚动时重新计算位置
const onWindowScroll = () => {
  if (isHovered.value) {
    updateTipPosition()
  }
}

onMounted(() => {
  window.addEventListener('resize', onWindowResize)
  window.addEventListener('scroll', onWindowScroll)
})

onUnmounted(() => {
  window.removeEventListener('resize', onWindowResize)
  window.removeEventListener('scroll', onWindowScroll)
})

// 监听hover状态变化
watch(() => isHovered.value, (newVal) => {
  if (newVal) {
    nextTick(() => {
      updateTipPosition()
    })
  }
})

// 计算提示框样式对象
const tipStyleObject = computed(() => ({
  position: 'absolute' as const,
  zIndex: 9999,
  top: `${tipPosition.value.y}px`,
  left: `${tipPosition.value.x}px`,
  backgroundColor: theme.boxSecondColor,
  color: theme.boxTextColor,
  padding: '5px 8px',
  borderRadius: '0',
  fontSize: '12px',
  whiteSpace: 'nowrap' as const,
  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.2)',
  pointerEvents: 'none' as const,
  maxWidth: '300px',
  overflowWrap: 'break-word' as const
}))
</script>

<style scoped>
.glow-check-box-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  position: relative;
}

.glow-check-box {
  width: 14px;
  height: 14px;
  border: 1px solid v-bind('theme.boxBorderColor');
  background-color: transparent;
  margin-right: 8px;
  position: relative;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 0 transparent;
}

.glow-check-box:hover,
.glow-check-box.hovered {
  border-color: v-bind('theme.mainBorderColor');
  box-shadow: 0 0 v-bind('theme.boxBlurHover + "px"') v-bind('theme.boxGlowColor');
}

.glow-check-box.active {
  background-color: v-bind('theme.mainColorActive');
  border-color: v-bind('theme.boxGlowColor');
}

.glow-check-box i {
  color: v-bind('theme.boxGlowColor');
  font-size: 18px;
  font-weight: bold;
  line-height: 1;
  position: absolute;
  top: 45%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.content-slot {
  color: v-bind('theme.boxTextColor');
  font-size: 14px;
}

.glow-tip {
  background-color: v-bind('theme.boxSecondColor');
  color: v-bind('theme.boxTextColor');
  padding: 5px 8px;
  border-radius: 0;
  font-size: 12px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  pointer-events: none;
  max-width: 300px;
  overflow-wrap: break-word;
}
</style>