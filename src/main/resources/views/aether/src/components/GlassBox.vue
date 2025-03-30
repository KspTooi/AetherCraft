<template>
  <div class="glass-box" :style="glassStyle">
    <div class="glass-box-content">
      <slot></slot> <!-- 内容将通过插槽传入 -->
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed } from 'vue';

const props = defineProps({
  // 背景基础色 (RGB格式，无 alpha)
  backgroundColor: {
    type: String,
    default: '255, 255, 255' // 默认为白色
  },
  // 背景透明度
  backgroundOpacity: {
    type: Number,
    default: 0.25
  },
  // 背景模糊程度 (px)
  blurAmount: {
    type: Number,
    default: 12
  }
});

// 计算最终的样式对象
const glassStyle = computed(() => ({
  background: `rgba(${props.backgroundColor}, ${props.backgroundOpacity})`,
  backdropFilter: `blur(${props.blurAmount}px)`,
  webkitBackdropFilter: `blur(${props.blurAmount}px)` // 兼容 Webkit
}));

</script>

<style scoped>
/* 玻璃盒子基础样式 - 无边框、无内外边距 */
.glass-box {
  box-sizing: border-box;
  /* background 和 backdrop-filter 移至 :style 绑定 */
  /* border: 1px solid rgba(255, 255, 255, 0.3); */ /* 移除边框 */
  display: flex;
  flex-direction: column;
  /* padding: 20px; */ /* 移除内边距 */
  overflow: hidden; /* 主盒子隐藏溢出 */
  margin: 0; /* 明确设置外边距为 0 */
  padding: 0; /* 明确设置内边距为 0 */
}

/* 内容区域样式 */
.glass-box-content {
  flex: 1; /* 占据剩余空间 */
  overflow-y: auto; /* 内容溢出时显示滚动条 */
  min-height: 0; /* 配合 flex: 1 使用 */
  position: relative;
  /* 如果需要在内容区域内部添加边距，可以在这里设置 padding */
  /* padding: 10px; */
}

/* 自定义滚动条样式 - Chrome, Edge, Safari */
.glass-box-content::-webkit-scrollbar {
  width: 8px;
}

.glass-box-content::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

.glass-box-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

.glass-box-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.5);
}

/* 自定义滚动条样式 - Firefox */
.glass-box-content {
  scrollbar-width: thin;
  scrollbar-color: rgba(0,0,0,0.3) rgba(255,255,255,0.2);
}

/* 响应式布局调整 - 已移除，因为无边框/边距组件通常会填充其容器 */
</style> 