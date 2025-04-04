<script setup lang="ts">
import { inject, ref, watch, computed } from 'vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from './GlowTheme.ts'
import type { GlowThemeColors } from './GlowTheme.ts'

const props = defineProps({
  // 控制模态框显示/隐藏
  modelValue: {
    type: Boolean,
    default: false
  },
  
  // 模态框标题
  title: {
    type: String,
    default: '标题'
  },
  
  // 是否显示头部
  showHeader: {
    type: Boolean,
    default: true
  },
  
  // 点击遮罩层是否关闭模态框
  closeOnClickOverlay: {
    type: Boolean,
    default: true
  },
  
  // 模态框宽度
  width: {
    type: String,
    default: '500px'
  },
  
  // 模态框最大高度
  maxHeight: {
    type: String,
    default: '80vh'
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

// 当组件单独运行时，如果没有注入主题，则使用默认主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 模态框是否可见
const visible = ref(props.modelValue)

// 监听props.modelValue变化
watch(() => props.modelValue, (val) => {
  visible.value = val
})

// 关闭模态框
const closeModal = () => {
  visible.value = false
  emit('update:modelValue', false)
  emit('close')
}

// 点击遮罩层
const handleOverlayClick = () => {
  if (props.closeOnClickOverlay) {
    closeModal()
  }
}

// 计算模态框样式
const modalStyle = computed(() => ({
  width: props.width,
  maxHeight: props.maxHeight
}))
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="laser-modal-overlay" @click="handleOverlayClick">
      <div class="laser-modal-container" :style="modalStyle" @click.stop>
        <!-- 顶部发光线条 -->
        <div class="modal-glow-border"></div>
        
        <!-- 模态框头部 -->
        <div v-if="showHeader" class="laser-modal-header">
          <h3 class="laser-modal-title">{{ title }}</h3>
          <button class="laser-modal-close" @click="closeModal">×</button>
        </div>
        
        <!-- 模态框内容 -->
        <div class="laser-modal-body" :class="{ 'no-header': !showHeader }">
          <slot></slot>
        </div>
        
        <!-- 模态框底部 -->
        <div class="laser-modal-footer" v-if="$slots.footer">
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.laser-modal-overlay {
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

.laser-modal-container {
  border-radius: 0; /* 直角风格 */
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalIn 0.25s ease;
  position: relative;
  padding: 0;
  background-color: v-bind('theme.boxAccentColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  transition: background-color 0.3s ease, backdrop-filter 0.3s ease;
  color: v-bind('theme.boxTextColor');
  backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
}

.laser-modal-container:hover {
  background-color: v-bind('theme.boxAccentColorHover');
  backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
}

@keyframes modalIn {
  from { 
    opacity: 0;
  }
  to { 
    opacity: 1;
  }
}

.modal-glow-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 10px 1px v-bind('theme.boxGlowColor');
  z-index: 1;
}

.laser-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
  position: relative;
}

.laser-modal-title {
  font-size: 14px;
  color: v-bind('theme.boxTextColor');
  margin: 0;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.laser-modal-close {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 20px;
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0;
  transition: all 0.2s;
  margin-left: 10px;
}

.laser-modal-close:hover {
  background: v-bind('theme.boxColorHover');
  color: v-bind('theme.boxTextColor');
}

.laser-modal-body {
  overflow-y: auto;
  max-height: calc(80vh - 100px); /* 调整为更小的头部高度 */
  padding: 15px;
}

.laser-modal-body.no-header {
  /*padding-top: 25px;*/
}

.laser-modal-footer {
  padding: 1px 15px 15px 15px;
  border-top: none;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 自定义滚动条 */
.laser-modal-body::-webkit-scrollbar {
  width: 4px;
}

.laser-modal-body::-webkit-scrollbar-track {
  background: transparent;
}

.laser-modal-body::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 0;
}

.laser-modal-body::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.boxBorderColor');
}
</style>