<template>
  <div class="client-frame">
    <!-- 背景层 -->
    <div class="background-layer"></div> 
    
    <!-- 内容层 -->
    <div class="content-wrapper">
      <TopNav />
      <main class="content-area">
        <slot></slot> <!-- 插槽用于渲染子路由内容 -->
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import TopNav from '@/components/TopNav.vue'
</script>

<style scoped>
.client-frame {
  position: relative; /* 使内部绝对/固定定位相对于此元素 */
  min-height: 100vh;
  width: 100%;
  overflow: hidden; /* 防止背景溢出可能导致滚动条 */
}

.background-layer {
  position: fixed; /* 固定定位，覆盖整个视口 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('/customize/wallpaper'); /* 使用 API 端点 */
  background-size: cover; /* 覆盖整个区域 */
  background-position: center; /* 居中显示 */
  background-repeat: no-repeat; /* 不重复 */
  z-index: -1; /* 置于内容下方 */
}

.content-wrapper {
  position: relative; /* 确保内容在背景之上 */
  z-index: 1; /* 明确层级 */
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  /* 可以添加一个轻微的半透明背景提高内容可读性 */
  /* background-color: rgba(255, 255, 255, 0.1); */ 
}

.content-area {
  flex-grow: 1; 
  /*padding: 1rem;*/
  /* 确保内容区域有背景或定位，避免被背景层覆盖 */
  position: relative; 
}
</style> 