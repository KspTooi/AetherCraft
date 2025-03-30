<script setup lang="ts">
import { RouterView } from 'vue-router'
import ClientFrameTheme from '@/components/ClientFrameTheme.vue'
</script>

<template>
  <ClientFrameTheme>
    <RouterView />
  </ClientFrameTheme>
</template>

<style scoped>
/* 移除原有的header和nav样式，TopNav组件自带样式 */
/* 这里的样式可以根据全局需要添加，或者保持为空 */
</style>
