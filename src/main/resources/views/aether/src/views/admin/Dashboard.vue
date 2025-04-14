<template>
  <div class="dashboard-container">
    <h2>管理控制台主页</h2>
    <div class="dashboard-stats">
      <div class="stat-card">
        <div class="stat-value">{{ userCount }}</div>
        <div class="stat-label">用户数量</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ modelCount }}</div>
        <div class="stat-label">模型数量</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ activeUsers }}</div>
        <div class="stat-label">在线用户</div>
      </div>
    </div>
    
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-buttons">
        <button @click="navigateTo('user-manage')">用户管理</button>
        <button @click="navigateTo('system-config')">系统配置</button>
        <button @click="navigateTo('log-viewer')">日志查看</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userCount = ref(0)
const modelCount = ref(0)
const activeUsers = ref(0)

onMounted(() => {
  // 这里可以添加获取实际数据的逻辑
  // 暂时使用模拟数据
  userCount.value = 128
  modelCount.value = 15
  activeUsers.value = 42
})

const navigateTo = (route: string) => {
  router.push({ name: route })
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-stats {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.stat-card {
  background-color: #2c3e50;
  color: white;
  padding: 20px;
  border-radius: 4px;
  flex: 1;
  min-width: 200px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 1rem;
  opacity: 0.8;
}

.quick-actions {
  margin-top: 40px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  flex-wrap: wrap;
}

.action-buttons button {
  padding: 10px 15px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.action-buttons button:hover {
  background-color: #2980b9;
}
</style> 