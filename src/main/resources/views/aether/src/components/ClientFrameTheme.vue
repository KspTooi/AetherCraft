<template>
  <div class="client-frame">
    <TopNav />
    <main class="content-area">
      <slot></slot> <!-- 插槽用于渲染子路由内容 -->
    </main>
  </div>
</template>

<script setup lang="ts">
import TopNav from '@/components/TopNav.vue'
</script>

<style scoped>
.client-frame {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* 确保布局至少占满整个视口高度 */
  width: 100%; /* 确保宽度与浏览器一致 */
}

.content-area {
  flex-grow: 1; /* 让内容区域占据剩余空间 */
  padding: 1rem; /* 可以添加一些内边距 */
  /* 可以根据需要添加背景色或其他样式 */
}
</style> 