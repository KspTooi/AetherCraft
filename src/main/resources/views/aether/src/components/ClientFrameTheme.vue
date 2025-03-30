<template>
  <div class="client-frame">
    <!-- 背景层 -->
    <div class="background-layer"></div> 
    
    <!-- 内容层 -->
    <div class="content-wrapper">
      <TopNav />
      <main class="content-area">
        <!-- 加载特效层 -->
        <Transition name="fade">
          <div class="loading-overlay" v-if="isLoading">
            <div class="loading-animation">
              <div class="aether-loader">
                <div class="aether-line"></div>
                <div class="aether-line"></div>
                <div class="aether-line"></div>
              </div>
              <div class="loading-text">等待进程切换</div>
            </div>
          </div>
        </Transition>
        <slot></slot> <!-- 插槽用于渲染子路由内容 -->
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, provide } from 'vue'
import { useRouter } from 'vue-router'
import TopNav from '@/components/TopNav.vue'
import { useThemeStore } from '../stores/theme'

const router = useRouter()
const themeStore = useThemeStore()
const isLoading = ref(true)

// 定义提前解除加载状态的方法
const finishLoading = () => {
  isLoading.value = false
}

// 提供给子组件使用的方法
// 子组件可以通过 inject('finishLoading') 获取并调用此方法来提前结束加载动画
// 例如在数据加载完成后，不必等待预设的超时时间
// 用法示例：
// const finishLoading = inject<() => void>('finishLoading')
// if (finishLoading) finishLoading()
provide('finishLoading', finishLoading)

// 路由变化时显示加载动画
watch(() => router.currentRoute.value.path, () => {
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
  }, 600) // 设置加载时间，可以根据实际需求调整
})

// 组件加载时显示加载动画
onMounted(() => {
  setTimeout(() => {
    isLoading.value = false
  }, 800) // 初次加载时间略长
})
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

/* 加载特效样式 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 淡入淡出过渡效果 - 仅退出有动画 */
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-active {
  transition: none;
}

.fade-leave-to {
  opacity: 0;
}

.loading-animation {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.aether-loader {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.aether-line {
  width: 3px;
  height: 20px;
  background-color: v-bind('themeStore.primaryColor');
  border-radius: 1px;
  animation: aether-loader 1.2s ease-in-out infinite;
  box-shadow: 0 0 8px v-bind('themeStore.primaryColor');
}

.aether-line:nth-child(2) {
  animation-delay: 0.15s;
  background-color: v-bind('themeStore.activeColor');
  box-shadow: 0 0 8px v-bind('themeStore.activeColor');
}

.aether-line:nth-child(3) {
  animation-delay: 0.3s;
  background-color: v-bind('themeStore.brandColor');
  box-shadow: 0 0 8px v-bind('themeStore.brandColor');
}

@keyframes aether-loader {
  0%, 100% {
    transform: scaleY(0.4);
    opacity: 0.7;
  }
  50% {
    transform: scaleY(1.2);
    opacity: 1;
  }
}

.loading-text {
  color: white;
  font-size: 14px;
  letter-spacing: 2px;
  text-transform: uppercase;
  font-weight: 300;
  text-shadow: 0 0 6px v-bind('themeStore.primaryColor');
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}
</style> 