<template>
  <slot />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, getCurrentInstance, onBeforeUnmount, watch } from 'vue';
import { useRoute } from 'vue-router';

// 定义props来接收事件处理函数
const props = defineProps<{
  onTouchMoveRight?: () => boolean | void;
  onTouchMoveLeft?: () => boolean | void;
  layer?: number; // 层级，数值越大优先级越高，默认为0
}>();

// 获取当前路由
const route = useRoute();

// 获取当前组件实例ID，用于区分不同的组件实例
const instance = getCurrentInstance();
const instanceId = instance?.uid || Math.random().toString(36).substr(2, 9);
const componentLayer = props.layer ?? 0; // 使用默认值0

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
  
  // 获取所有注册的实例，按layer层级倒序处理（层级高的先处理）
  const instances = (window as any)[INSTANCES_KEY] || [];
  // 过滤掉无效的实例（组件已卸载但未清理的）
  const validInstances = instances.filter((inst: any) => {
    try {
      // 检查实例是否仍然有效
      return inst && typeof inst.handleRight === 'function' && typeof inst.handleLeft === 'function';
    } catch {
      return false;
    }
  });
  
  // 更新实例列表，移除无效实例
  if (validInstances.length !== instances.length) {
    (window as any)[INSTANCES_KEY] = validInstances;
  }
  
  const sortedInstances = [...validInstances].sort((a, b) => b.layer - a.layer);
  
  // 判断滑动方向并依次触发事件，直到有组件拦截
  const isRightSwipe = deltaX > 0;
  
  // 调试信息
  if (import.meta.env.DEV) {
    console.log(`GlowMobileSupport: ${isRightSwipe ? '右滑' : '左滑'}, 实例数量: ${sortedInstances.length}, 层级: [${sortedInstances.map(i => i.layer).join(', ')}]`);
  }
  
  for (const inst of sortedInstances) {
    try {
      let result: boolean | void;
      if (isRightSwipe) {
        result = inst.handleRight();
      } else {
        result = inst.handleLeft();
      }
      
      // 调试信息
      if (import.meta.env.DEV) {
        console.log(`GlowMobileSupport: 层级 ${inst.layer} 处理结果: ${result}`);
      }
      
      // 如果返回true，表示事件被拦截，停止传播
      if (result === true) {
        if (import.meta.env.DEV) {
          console.log(`GlowMobileSupport: 事件被层级 ${inst.layer} 拦截`);
        }
        break;
      }
    } catch (error) {
      console.warn('GlowMobileSupport事件处理出错:', error);
      // 移除出错的实例
      const errorIndex = validInstances.findIndex((i: any) => i === inst);
      if (errorIndex !== -1) {
        validInstances.splice(errorIndex, 1);
        (window as any)[INSTANCES_KEY] = validInstances;
      }
    }
  }
};

// 创建事件处理器函数
const createHandler = () => {
  return {
    instanceId,
    layer: componentLayer, // 添加层级信息
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

// 清理当前实例的函数
const cleanupInstance = () => {
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

// 在组件卸载前清理实例
onBeforeUnmount(() => {
  cleanupInstance();
});

// 在组件卸载时也清理实例（双重保险）
onUnmounted(() => {
  cleanupInstance();
});

// 监听路由变化，清理所有实例
watch(() => route.path, () => {
  // 路由变化时，清理所有实例并重新初始化
  if ((window as any)[INSTANCES_KEY]) {
    (window as any)[INSTANCES_KEY] = [];
  }
  if ((window as any)[GLOBAL_LISTENER_KEY]) {
    document.removeEventListener('touchstart', handleTouchStart);
    document.removeEventListener('touchend', handleTouchEnd);
    delete (window as any)[GLOBAL_LISTENER_KEY];
  }
}, { immediate: false });
</script>

<style scoped>
/* 不添加任何样式，确保不影响插槽内容的布局 */
</style>