<script setup lang="ts">
import { RouterView } from 'vue-router'
import ClientFrameTheme from '@/components/ClientFrameTheme.vue'
</script>

<template>
  <ClientFrameTheme>
    <RouterView />
  </ClientFrameTheme>
</template>

<style>

/* 重置 body 边距 */
body {
  margin: 0;
  padding: 0;
  box-sizing: border-box; /* 推荐添加，使得 padding 和 border 不会增加元素总宽度 */
}

/* 可选：重置 html 元素的边距和内边距 */
html {
  margin: 0;
  padding: 0;
}

/* 如果需要，可以把之前 #app 的一些基础样式加回来，但移除宽度/边距限制 */
#app {
  font-weight: normal;
  /* 其他需要的全局字体、颜色等样式 */
}


</style>
