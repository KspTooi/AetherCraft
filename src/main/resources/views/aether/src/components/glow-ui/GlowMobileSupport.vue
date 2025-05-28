<template>
  <slot />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, getCurrentInstance } from 'vue';

// 定义props来接收事件处理函数
const props = defineProps<{
  onTouchMoveRight?: () => boolean | void;
  onTouchMoveLeft?: () => boolean | void;
}>();

// 获取当前组件实例ID，用于区分不同的组件实例
const instance = getCurrentInstance();
const instanceId = instance?.uid || Math.random().toString(36).substr(2, 9);

// 全局监听器标识和实例管理
const GLOBAL_LISTENER_KEY = '__glow_mobile_support_listener__';
const INSTANCES_KEY = '__glow_mobile_support_instances__';

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
  
  // 获取所有注册的实例，按注册顺序倒序处理（后注册的先处理，即内层优先）
  const instances = (window as any)[INSTANCES_KEY] || [];
  const sortedInstances = [...instances].reverse();
  
  // 判断滑动方向并依次触发事件，直到有组件拦截
  const isRightSwipe = deltaX > 0;
  
  for (const inst of sortedInstances) {
    try {
      let result: boolean | void;
      if (isRightSwipe) {
        result = inst.handleRight();
      } else {
        result = inst.handleLeft();
      }
      
      // 如果返回true，表示事件被拦截，停止传播
      if (result === true) {
        break;
      }
    } catch (error) {
      console.warn('GlowMobileSupport事件处理出错:', error);
    }
  }
};

// 创建事件处理器函数
const createHandler = () => {
  return {
    instanceId,
    handleRight: () => {
      if (props.onTouchMoveRight) {
        return props.onTouchMoveRight();
      }
    },
    handleLeft: () => {
      if (props.onTouchMoveLeft) {
        return props.onTouchMoveLeft();
      }
    }
  };
};

onMounted(() => {
  // 初始化全局实例数组
  if (!(window as any)[INSTANCES_KEY]) {
    (window as any)[INSTANCES_KEY] = [];
  }
  
  // 注册当前实例
  const instances = (window as any)[INSTANCES_KEY];
  const handler = createHandler();
  instances.push(handler);
  
  // 如果是第一个实例，注册全局监听器
  if (!(window as any)[GLOBAL_LISTENER_KEY]) {
    document.addEventListener('touchstart', handleTouchStart, { passive: true });
    document.addEventListener('touchend', handleTouchEnd, { passive: true });
    (window as any)[GLOBAL_LISTENER_KEY] = true;
  }
});

onUnmounted(() => {
  // 移除当前实例
  const instances = (window as any)[INSTANCES_KEY] || [];
  const index = instances.findIndex((inst: any) => inst.instanceId === instanceId);
  if (index !== -1) {
    instances.splice(index, 1);
  }
  
  // 如果没有实例了，移除全局监听器
  if (instances.length === 0) {
    document.removeEventListener('touchstart', handleTouchStart);
    document.removeEventListener('touchend', handleTouchEnd);
    delete (window as any)[GLOBAL_LISTENER_KEY];
    delete (window as any)[INSTANCES_KEY];
  }
});
</script>

<style scoped>
/* 不添加任何样式，确保不影响插槽内容的布局 */
</style>