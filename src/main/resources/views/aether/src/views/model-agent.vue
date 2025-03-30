<template>
  <div class="agent-container">
    <div class="coming-soon-section">
      <div class="content-wrapper">
        <div class="icon-container">
          <i class="bi bi-robot"></i>
        </div>
        <h1 class="title">Agent功能正在开发中</h1>
        <p class="description">
          我们正在努力为您打造全新的智能Agent体验，敬请期待！<br>
          Agent将能够帮助您执行复杂任务并管理工作流程。
        </p>
        <div class="features">
          <div class="feature-item">
            <i class="bi bi-code-square"></i>
            <span>代码生成与优化</span>
          </div>
          <div class="feature-item">
            <i class="bi bi-search"></i>
            <span>智能搜索与分析</span>
          </div>
          <div class="feature-item">
            <i class="bi bi-file-earmark-text"></i>
            <span>文档生成与处理</span>
          </div>
          <div class="feature-item">
            <i class="bi bi-diagram-3"></i>
            <span>工作流自动化</span>
          </div>
        </div>
        <div class="progress-section">
          <div class="progress-bar">
            <div class="progress-indicator" :style="{ width: developmentProgress + '%' }"></div>
          </div>
          <div class="progress-text">开发进度: {{ developmentProgress }}%</div>
        </div>
        <div class="action-buttons">
          <button class="back-button" @click="goToChat">
            <i class="bi bi-chat-dots"></i>
            返回聊天
          </button>
          <button class="back-button" @click="goToRp">
            <i class="bi bi-people"></i>
            角色扮演
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '../stores/theme'

// 获取路由器
const router = useRouter()

// 获取主题设置
const themeStore = useThemeStore()

// 开发进度（模拟）
const developmentProgress = ref(68)

// 导航函数
const goToChat = () => {
  router.push('/chat')
}

const goToRp = () => {
  router.push('/rp')
}

// 加载完成处理
const finishLoading = inject<() => void>('finishLoading')
onMounted(() => {
  if (finishLoading) {
    finishLoading()
  }
})
</script>

<style scoped>
.agent-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(v-bind('themeStore.mainBlur'));
  -webkit-backdrop-filter: blur(v-bind('themeStore.mainBlur'));
}

.coming-soon-section {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.content-wrapper {
  background: rgba(20, 30, 40, 0.6);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  padding: 40px;
  max-width: 800px;
  width: 100%;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.icon-container {
  font-size: 64px;
  margin-bottom: 20px;
  color: v-bind('themeStore.primaryColor');
  animation: pulse 2s infinite ease-in-out;
}

.title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 16px;
  background: linear-gradient(45deg, v-bind('themeStore.primaryColor'), v-bind('themeStore.activeColor'));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.description {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 32px;
  color: rgba(255, 255, 255, 0.8);
}

.features {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
  margin-bottom: 40px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(0, 0, 0, 0.2);
  padding: 20px;
  border-radius: 8px;
  width: 150px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  background: rgba(0, 0, 0, 0.4);
}

.feature-item i {
  font-size: 32px;
  margin-bottom: 12px;
  color: v-bind('themeStore.activeColor');
}

.feature-item span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.progress-section {
  margin-bottom: 30px;
}

.progress-bar {
  height: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-indicator {
  height: 100%;
  background: linear-gradient(90deg, v-bind('themeStore.primaryColor'), v-bind('themeStore.activeColor'));
  border-radius: 5px;
  transition: width 1s ease;
}

.progress-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.back-button {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-button:hover {
  background: rgba(0, 0, 0, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.back-button i {
  font-size: 16px;
}

@keyframes pulse {
  0% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
  100% {
    opacity: 0.6;
    transform: scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-wrapper {
    padding: 30px 20px;
  }
  
  .title {
    font-size: 24px;
  }
  
  .description {
    font-size: 14px;
  }
  
  .features {
    gap: 10px;
  }
  
  .feature-item {
    width: calc(50% - 20px);
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .title {
    font-size: 20px;
  }
  
  .icon-container {
    font-size: 48px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }
  
  .back-button {
    width: 100%;
    justify-content: center;
  }
  
  .feature-item {
    width: 100%;
  }
}
</style> 