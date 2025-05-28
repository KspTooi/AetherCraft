<template>
  <slot />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';

const emits = defineEmits<{
  (e: 'on-touch-move-right'): void;
  (e: 'on-touch-move-left'): void;
}>();

// 全局监听器标识
const GLOBAL_LISTENER_KEY = '__glow_mobile_support_listener__';

// 触摸事件相关变量
let startX = 0;
let startY = 0;
let startTime = 0;

// 滑动阈值配置
const SWIPE_THRESHOLD = 50; // 最小滑动距离
const SWIPE_TIME_THRESHOLD = 300; // 最大滑动时间(ms)
const VERTICAL_THRESHOLD = 100; // 垂直方向最大偏移

const handleTouchStart = (e: TouchEvent) => {
  if (!e.touches || e.touches.length === 0) return;
  
  const touch = e.touches[0];
  startX = touch.clientX;
  startY = touch.clientY;
  startTime = Date.now();
};

const handleTouchEnd = (e: TouchEvent) => {
  if (!e.changedTouches || e.changedTouches.length === 0) return;
  
  const touch = e.changedTouches[0];
  const endX = touch.clientX;
  const endY = touch.clientY;
  const endTime = Date.now();
  
  const deltaX = endX - startX;
  const deltaY = endY - startY;
  const deltaTime = endTime - startTime;
  
  // 检查是否为有效滑动
  if (deltaTime > SWIPE_TIME_THRESHOLD) return;
  if (Math.abs(deltaY) > VERTICAL_THRESHOLD) return;
  if (Math.abs(deltaX) < SWIPE_THRESHOLD) return;
  
  // 判断滑动方向并抛出事件
  if (deltaX > 0) {
    emits('on-touch-move-right');
  } else {
    emits('on-touch-move-left');
  }
};

onMounted(() => {
  // 检查是否已经注册了全局监听器
  if ((window as any)[GLOBAL_LISTENER_KEY]) {
    return;
  }
  
  // 注册全局触摸事件监听器
  document.addEventListener('touchstart', handleTouchStart, { passive: true });
  document.addEventListener('touchend', handleTouchEnd, { passive: true });
  
  // 标记已注册
  (window as any)[GLOBAL_LISTENER_KEY] = true;
});

onUnmounted(() => {
  // 移除全局监听器
  document.removeEventListener('touchstart', handleTouchStart);
  document.removeEventListener('touchend', handleTouchEnd);
  
  // 清除标记
  delete (window as any)[GLOBAL_LISTENER_KEY];
});
</script>

<style scoped>
/* 不添加任何样式，确保不影响插槽内容的布局 */
</style>