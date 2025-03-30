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
  position: relative;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.background-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('/customize/wallpaper');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: -1;
}

.content-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.content-area {
  flex: 1;
  position: relative;
  height: calc(100vh - var(--nav-height));
  overflow: hidden;
}
</style> 