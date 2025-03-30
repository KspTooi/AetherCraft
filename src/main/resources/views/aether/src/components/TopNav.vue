<template>
  <nav class="navbar my-vista-taskbar navbar-expand-md">
    <div class="container-fluid">
      <div class="navbar-brand brand-gradient">
        {{ brandName }}
      </div>
      <button
        class="navbar-toggler"
        type="button"
        @click="isExpanded = !isExpanded"
        :aria-expanded="isExpanded"
        aria-label="切换导航"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div
        class="collapse navbar-collapse"
        :class="{ show: isExpanded }"
        id="navbarContent"
      >
        <div class="nav-left d-flex align-items-center">
          <div class="nav-item">
            <router-link class="nav-link" to="/chat">聊天</router-link>
          </div>
          <div class="nav-item">
            <router-link class="nav-link" to="/chat-rp">聊天(RP)</router-link>
          </div>
          <div class="nav-item">
            <router-link class="nav-link" to="/chat-agent">聊天(AGENT)</router-link>
          </div>
          <div class="nav-item">
            <router-link class="nav-link" to="/customize">个性化</router-link>
          </div>
        </div>
        <div class="nav-right d-flex align-items-center ms-auto">
          <div class="nav-item">
            <router-link class="nav-link" to="/dashboard">管理台</router-link>
          </div>
          <div class="nav-item">
            <a class="nav-link" @click="handleLogout">注销</a>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isExpanded = ref(false)
const brandName = ref('AetherCraft')

// 处理品牌点击 - 小屏幕时切换导航栏显示
const handleBrandClick = () => {
  // 使用与 CSS 匹配的断点 (< 768px)
  if (window.innerWidth < 768) { 
    isExpanded.value = !isExpanded.value
  }
}

// 页面加载时设置当前活动导航项
onMounted(() => {
  // 在移动端点击品牌名称切换导航栏
  const brandElement = document.querySelector('.navbar-brand')
  if (brandElement) {
    brandElement.addEventListener('click', handleBrandClick)
  }
})

const handleLogout = () => {
  // 这里添加登出逻辑
  console.log('用户登出')
}
</script>

<style>
/* 全局样式，确保导航栏充满整个宽度 */
body, html {
  margin: 0;
  padding: 0;
  overflow-x: hidden; /* 防止水平滚动条 */
}

#app {
  width: 100%;
}
</style>

<style scoped>
.my-vista-taskbar {
  position: relative;
  z-index: 1000;
  width: 100%; /* 确保导航栏占满全宽 */
  background: linear-gradient(180deg, rgba(200,230,255,0.3) 0%, rgba(200,230,255,0.1) 100%);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255,255,255,0.3);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  padding: 0.2rem 0; /* 移除左右内边距 */
  transition: background 0.3s ease;
}

/* 移除 container-fluid 的内边距，并在外层 navbar 或此处添加 */
.container-fluid {
  display: flex; /* 确保 flex 布局 */
  align-items: center; /* 垂直居中对齐内部元素 */
  padding-left: 0.6rem; /* 稍微增加左边距 */
  padding-right: 0.6rem; /* 稍微增加右边距 */
  width: 100%; 
  margin: 0;
  box-sizing: border-box; /* 包含 padding 在宽度内 */
}

.brand-gradient {
  background: linear-gradient(90deg, #ff9a9e 0%, #fad0c4 10%, #fad0c4 20%, #a18cd1 30%, #fbc2eb 40%, #8fd3f4 50%, #84fab0 60%, #8fd3f4 70%, #fbc2eb 80%, #a18cd1 90%, #ff9a9e 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent !important;
  font-weight: 600;
  text-shadow: 0px 0px 10px rgba(255, 255, 255, 0.2);
  animation: gradient-shift 8s linear infinite;
  letter-spacing: 0.5px;
}

.brand-gradient:hover {
  animation-duration: 3s;
  text-shadow: 0px 0px 15px rgba(255, 255, 255, 0.4);
}

@keyframes gradient-shift {
  0% { background-position: 0% center; }
  100% { background-position: 200% center; }
}

.navbar-brand {
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0.3rem 0.6rem;
}

.navbar-brand:hover {
  transform: scale(1.05);
  text-shadow: 0px 0px 15px rgba(255, 255, 255, 0.4);
}

.nav-left, .nav-right {
  display: flex;
  align-items: center;
  flex-wrap: nowrap; /* 防止内部导航项换行 */
}

.nav-item {
  padding: 0.2rem 0.3rem;
  transition: transform 0.3s ease;
}

.nav-item:hover {
  transform: scale(1.05);
}

.nav-item.active-item {
  transform: scale(1.02);
  background: rgba(255,255,255,0.03);
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(0,0,0,0.1) inset;
}

.nav-link {
  color: #ffffff;
  font-size: 0.9rem;
  text-decoration: none;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.5);
  padding: 0.2rem 0.3rem;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.nav-link:hover {
  background-color: rgba(255,255,255,0.1);
  text-shadow: 1px 1px 3px rgba(0,0,0,0.7);
  transform: translateY(-1px);
  box-shadow: 0 0 5px rgba(255,255,255,0.1);
}

.nav-link.router-link-active {
  background: rgba(255,255,255,0.15);
  box-shadow: 0 0 8px rgba(255,255,255,0.2);
  color: #ffffff;
  font-weight: 500;
  text-shadow: 1px 1px 3px rgba(0,0,0,0.7);
  position: relative;
  transform: translateY(-1px);
  animation: subtle-glow 2s ease-in-out infinite alternate;
}

@keyframes subtle-glow {
  from { box-shadow: 0 0 8px rgba(255,255,255,0.2); }
  to { box-shadow: 0 0 12px rgba(255,255,255,0.3); }
}

/* 响应式设计优化 */
@media (min-width: 768px) { /* Bootstrap 5 md breakpoint */
  .navbar-toggler {
    display: none !important; /* 在宽屏隐藏 Toggler */
  }
  .navbar-collapse {
    display: flex !important; /* 强制 flex 布局 */
    flex-basis: auto;
    flex-grow: 1; /* 占据剩余空间 */
    align-items: center; /* 垂直对齐内部导航组 */
    justify-content: space-between; /* 将左右导航组推向两端 */
  }
}

@media (max-width: 767.98px) {
  /* 调整容器布局为垂直堆叠 */
  .container-fluid {
    flex-direction: column; /* 强制垂直排列 */
    /* align-items: center; */ /* 在垂直布局下可能不需要或需要调整 */
    padding-left: 0; /* 移动端不需要左右内边距 */
    padding-right: 0;
  }

  /* 在小屏幕下隐藏 Toggler 按钮 */
  .navbar-toggler {
    display: none !important;
  }

  /* 品牌样式调整 */
  .navbar-brand {
    display: block;
    width: 100%; /* 占据整行 */
    text-align: center;
    margin-bottom: 0; /* 移除可能存在的底部边距 */
    padding: 0.3rem 0;
    /* 可以考虑增加最小高度确保可点击区域 */
    min-height: 30px; /* 示例值，根据实际导航栏高度调整 */
  }
  
  /* 默认隐藏导航内容，准备动画 */
  .navbar-collapse {
    max-height: 0; /* 初始高度为0 */
    overflow: hidden; /* 隐藏溢出内容 */
    transition: max-height 0.35s ease-in-out; /* 添加过渡动画 */
    width: 100%; /* 确保宽度一致 */
  }

  /* 当展开时 (有 .show 类)，设置最大高度以显示内容 */
  .navbar-collapse.show {
    max-height: 500px; /* 设置足够大的最大高度，根据内容调整 */
  }

  /* 调整内部导航组在小屏幕下的布局 */
  .nav-left, .nav-right {
    width: 100%;
    justify-content: center; /* 居中显示导航项 */
    flex-wrap: wrap; /* 允许换行 */
    padding-top: 0.5rem; /* 在品牌下方添加一些间距 */
  }
  
  .nav-item {
    margin: 0.1rem;
    padding: 0.2rem 0.3rem;
  }
}
</style> 