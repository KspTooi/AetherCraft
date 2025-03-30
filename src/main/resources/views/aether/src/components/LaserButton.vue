<!-- 激光按钮组件 -->
<template>
  <button 
    class="laser-button" 
    :class="{ 
      disabled: disabled,
      'corner-tr': corner === 'top-right' || corners.includes('top-right'),
      'corner-tl': corner === 'top-left' || corners.includes('top-left'),
      'corner-br': corner === 'bottom-right' || corners.includes('bottom-right'),
      'corner-bl': corner === 'bottom-left' || corners.includes('bottom-left'),
      'multiple-corners': corners.length > 0,
      'has-inner-glow': innerGlow,
      'has-particles': particles
    }"
    :style="buttonStyle"
    :disabled="disabled"
    @mouseover="isHovered = true"
    @mouseleave="isHovered = false"
    @click="handleClick">
    <!-- 内发光效果 -->
    <div v-if="innerGlow && !disabled" class="inner-glow" :style="innerGlowStyle"></div>
    
    <!-- 粒子效果 -->
    <div v-if="particles && !disabled && isHovered" class="particles-container">
      <div v-for="i in 15" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    
    <!-- 按钮内容 -->
    <div class="button-content">
      <slot></slot>
    </div>
    
    <!-- 角落填充 -->
    <span 
      v-if="!disabled && (corner === 'top-right' || corners.includes('top-right'))" 
      class="corner-fill corner-tr-fill" 
      :style="cornerStyle"></span>
    <span 
      v-if="!disabled && (corner === 'top-left' || corners.includes('top-left'))" 
      class="corner-fill corner-tl-fill" 
      :style="cornerStyle"></span>
    <span 
      v-if="!disabled && (corner === 'bottom-right' || corners.includes('bottom-right'))" 
      class="corner-fill corner-br-fill" 
      :style="cornerStyle"></span>
    <span 
      v-if="!disabled && (corner === 'bottom-left' || corners.includes('bottom-left'))" 
      class="corner-fill corner-bl-fill" 
      :style="cornerStyle"></span>
  </button>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

// 定义组件属性
const props = defineProps({
  // 基础样式属性
  backgroundColor: {
    type: String,
    default: 'rgba(61,138,168,0.24)' // 默认蓝色半透明背景
  },
  color: {
    type: String,
    default: 'white'
  },
  borderColor: {
    type: String,
    default: 'rgba(135, 206, 250, 0.7)' // 默认蓝色边框
  },
  width: {
    type: String,
    default: 'auto'
  },
  height: {
    type: String,
    default: 'auto'
  },
  fontSize: {
    type: String,
    default: '15px'
  },
  
  // 发光效果属性
  glowColor: {
    type: String,
    default: 'rgba(135, 206, 250, 0.5)' // 默认蓝色发光
  },
  glowIntensity: {
    type: String,
    default: '8px'
  },
  
  // 内发光效果
  innerGlow: {
    type: Boolean,
    default: true
  },
  innerGlowColor: {
    type: String,
    default: ''  // 默认与边框颜色相同
  },
  innerGlowSize: {
    type: String,
    default: '6px'
  },
  
  // 粒子效果
  particles: {
    type: Boolean,
    default: true
  },
  particleColor: {
    type: String,
    default: ''  // 默认与边框颜色相同
  },
  
  // 交互状态
  disabled: {
    type: Boolean,
    default: false
  },
  
  // 使用虚线边框
  dashed: {
    type: Boolean,
    default: false
  },
  
  // 角落填充（单个角落，向后兼容）
  corner: {
    type: String,
    default: '',
    validator: (value: string) => ['', 'top-right', 'top-left', 'bottom-right', 'bottom-left'].includes(value)
  },
  
  // 多角落填充
  corners: {
    type: Array as () => string[],
    default: () => []
  },
  
  // 角落大小
  cornerSize: {
    type: String,
    default: '20px'
  }
});

// 事件
const emit = defineEmits(['click']);

// 悬停状态
const isHovered = ref(false);

// 计算样式
const buttonStyle = computed(() => ({
  backgroundColor: props.backgroundColor,
  color: props.color,
  borderColor: props.borderColor,
  width: props.width,
  height: props.height,
  fontSize: props.fontSize,
  boxShadow: `0 0 ${props.glowIntensity} ${props.glowColor}`,
  borderStyle: props.dashed ? 'dashed' : 'solid'
}));

// 角落样式
const cornerStyle = computed(() => {
  const size = props.cornerSize;
  return {
    '--corner-size': size,
    '--corner-color': props.borderColor,
  };
});

// 内发光样式
const innerGlowStyle = computed(() => {
  const color = props.innerGlowColor || props.borderColor;
  return {
    '--inner-glow-color': color,
    '--inner-glow-size': props.innerGlowSize
  };
});

// 粒子样式生成函数
const getParticleStyle = (index: number) => {
  const color = props.particleColor || props.borderColor;
  
  // 随机位置、大小和动画延迟
  const size = Math.random() * 3 + 2; // 2-5px
  const x = Math.random() * 100;
  const delay = Math.random() * 2;
  const duration = Math.random() * 1 + 2; // 2-3s
  
  return {
    '--particle-color': color,
    width: `${size}px`,
    height: `${size}px`,
    left: `${x}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  };
};

// 点击处理
const handleClick = (event: MouseEvent) => {
  if (!props.disabled) {
    emit('click', event);
  }
};
</script>

<style scoped>
.laser-button {
  padding: 10px 15px;
  border-width: 1px;
  /* 默认为实线边框，可通过dashed属性切换为虚线 */
  /* 背景色、文字颜色、边框颜色通过props传入 */
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  position: relative;
  outline: none;
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
  /* 悬停状态 - 背景和边框更亮，发光更强 */
  background-color: rgba(96, 165, 250, 0.5) !important;
  border-color: rgba(135, 206, 250, 0.9) !important;
  box-shadow: 0 0 12px rgba(135, 206, 250, 0.7) !important;
}

.laser-button:active {
  /* 点击状态 - 减少发光，稍微调暗背景 */
  background-color: rgba(96, 165, 250, 0.4) !important;
  box-shadow: 0 0 6px rgba(135, 206, 250, 0.4) !important;
}

.laser-button.disabled {
  /* 禁用状态 */
  opacity: 0.5;
  cursor: not-allowed;
  background: rgba(108, 117, 125, 0.3) !important;
  border-color: rgba(108, 117, 125, 0.5) !important;
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
  box-shadow: inset 0 0 var(--inner-glow-size) var(--inner-glow-color);
  opacity: 0.5;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.laser-button:hover .inner-glow {
  opacity: 0.8;
}

/* 粒子效果 */
.particles-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 1;
}

.particle {
  position: absolute;
  background-color: var(--particle-color);
  border-radius: 50%;
  pointer-events: none;
  opacity: 0;
  bottom: 0;
  animation: particle-rise linear forwards;
}

@keyframes particle-rise {
  0% {
    transform: translateY(0) scale(1);
    opacity: 0.8;
  }
  100% {
    transform: translateY(-100px) scale(0);
    opacity: 0;
  }
}

/* 角落填充效果 */
.corner-fill {
  position: absolute;
  width: 0;
  height: 0;
  border-style: solid;
  pointer-events: none; /* 确保不影响点击事件 */
  z-index: 3; /* 确保显示在粒子和内发光上面 */
}

/* 右下角 */
.corner-br-fill {
  right: 0;
  bottom: 0;
  border-width: 0 0 var(--corner-size) var(--corner-size);
  border-color: transparent transparent var(--corner-color) transparent;
}

/* 左下角 */
.corner-bl-fill {
  left: 0;
  bottom: 0;
  border-width: var(--corner-size) 0 0 var(--corner-size);
  border-color: transparent transparent transparent var(--corner-color);
}

/* 右上角 */
.corner-tr-fill {
  right: 0;
  top: 0;
  border-width: 0 var(--corner-size) var(--corner-size) 0;
  border-color: transparent var(--corner-color) transparent transparent;
}

/* 左上角 */
.corner-tl-fill {
  left: 0;
  top: 0;
  border-width: var(--corner-size) var(--corner-size) 0 0;
  border-color: var(--corner-color) transparent transparent transparent;
}
</style> 