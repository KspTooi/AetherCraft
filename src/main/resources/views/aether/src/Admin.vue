<template>
  <div class="admin-container">
    <header class="admin-header">
      <h1>管理控制台</h1>
    </header>
    
    <main class="admin-content">
      <div v-if="isLoading" class="loading">
        加载中...
      </div>
      <router-view v-else />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoading = ref(true)

onMounted(() => {
  // 这里可以添加管理台初始化逻辑
  isLoading.value = false
})
</script>

<style scoped>
.admin-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

.admin-header {
  padding: 1rem;
  background-color: #333;
  color: white;
}

.admin-content {
  flex: 1;
  padding: 1rem;
  overflow: auto;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  font-size: 1.2rem;
}
</style> 