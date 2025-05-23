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
            <router-link class="nav-link" to="/chat" @click="handleNavItemClick">聊天</router-link>
          </div>
          <div class="nav-item">
            <router-link class="nav-link" to="/rp" @click="handleNavItemClick">角色扮演</router-link>
          </div>
<!--           <div class="nav-item">
            <router-link class="nav-link" to="/agent" @click="handleNavItemClick">聊天(AGENT)</router-link>
          </div> -->
          <div class="nav-item">
            <router-link class="nav-link" to="/customize" @click="handleNavItemClick">个性化</router-link>
          </div>
          <div class="nav-item">
            <router-link class="nav-link" to="/effects-test" @click="handleNavItemClick">特效测试</router-link>
          </div>
          <div class="nav-item">
            <router-link class="nav-link" to="/laser-test" @click="handleNavItemClick">组件测试</router-link>
          </div>
        </div>
        <div class="nav-right d-flex align-items-center ms-auto">
          <div class="nav-item">
            <a class="nav-link" href="/model/chat/view" @click="handleNavItemClick">经典UI</a>
          </div>
          <div class="nav-item">
            <router-link class="nav-link" to="/dashboard" @click="handleNavItemClick">管理台</router-link>
          </div>
          <div class="nav-item">
            <a class="nav-link cursor-pointer" @click="showLogoutConfirm">注销</a>
          </div>
        </div>
      </div>
    </div>
  </nav>
  
  <ConfirmModal ref="logoutModal" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useThemeStore } from '../stores/theme'
import ConfirmModal from './ConfirmModal.vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const isExpanded = ref(false)
const brandName = ref('AetherCraft')
const logoutModal = ref<InstanceType<typeof ConfirmModal> | null>(null)

// 获取主题颜色
const themeStore = useThemeStore()
const brandColor = computed(() => themeStore.brandColor)
const primaryColor = computed(() => themeStore.primaryColor)
const activeColor = computed(() => themeStore.activeColor)
const primaryHover = computed(() => themeStore.primaryHover)
const navBlur = computed(() => themeStore.navBlur)

// 处理品牌点击 - 小屏幕时切换导航栏显示
const handleBrandClick = () => {
  // 使用与 CSS 匹配的断点 (< 768px)
  if (window.innerWidth < 768) { 
    isExpanded.value = !isExpanded.value
  }
}

// 处理导航项点击 - 关闭导航栏
const handleNavItemClick = () => {
  if (window.innerWidth < 768) {
    isExpanded.value = false
  }
}

// 处理点击外部 - 关闭导航栏
const handleClickOutside = (event: MouseEvent) => {
  if (window.innerWidth < 768 && isExpanded.value) {
    const navbar = document.querySelector('.navbar')
    const target = event.target as HTMLElement
    
    // 如果点击的不是导航栏内的元素，则关闭导航栏
    if (navbar && !navbar.contains(target)) {
      isExpanded.value = false
    }
  }
}

// 显示注销确认对话框
const showLogoutConfirm = async () => {
  if (!logoutModal.value) return
  
  const confirmed = await logoutModal.value.showConfirm({
    title: '确认注销',
    content: '您确定要注销登录吗？',
    confirmText: '确认注销',
    cancelText: '取消'
  })
  
  if (confirmed) {
    handleLogout()
  }
  
  handleNavItemClick() // 关闭导航栏
}

// 执行注销操作
const handleLogout = async () => {
  try {
    // 调用注销接口
    await axios.get('/logout')
    // 注销成功后重定向到登录页面
    window.location.href = '/login'
  } catch (error) {
    console.error('注销失败', error)
    // 如果请求失败，仍然跳转到登录页
    window.location.href = '/login'
  }
}

// 页面加载时设置事件监听
onMounted(() => {
  // 在移动端点击品牌名称切换导航栏
  const brandElement = document.querySelector('.navbar-brand')
  if (brandElement) {
    brandElement.addEventListener('click', handleBrandClick)
  }
  
  // 添加全局点击事件监听
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  const brandElement = document.querySelector('.navbar-brand')
  if (brandElement) {
    brandElement.removeEventListener('click', handleBrandClick)
  }
  
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style>
/* 全局样式，确保导航栏充满整个宽度 */
:root {
  --nav-height: 40px;
}

body, html {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  height: 100%;
}

#app {
  width: 100%;
}
</style>

<style scoped>
.my-vista-taskbar {
  position: relative;
  z-index: 1000;
  width: 100%;
  height: var(--nav-height);
  backdrop-filter: blur(v-bind(navBlur));
  -webkit-backdrop-filter: blur(v-bind(navBlur));
  border-bottom: 1px solid rgba(66, 66, 66, 0.34);
  box-shadow: none;
  padding: 0.2rem 0;
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
  background: linear-gradient(90deg, 
    v-bind(brandColor) 0%, 
    v-bind(primaryColor) 70%
  );
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent !important;
  font-weight: 700;
  text-shadow: 0px 0px 4px v-bind(brandColor);
  letter-spacing: 0.8px;
}

.brand-gradient:hover {
  text-shadow: 0px 0px 8px v-bind(brandColor);
  transform: scale(1.02);
  transition: text-shadow 0.2s ease, transform 0.2s ease;
}

@keyframes gradient-shift {
  0% { background-position: 0% center; }
  100% { background-position: 200% center; }
}

.navbar-brand {
  font-size: 1.4rem;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0.3rem 0.8rem 0.3rem 0.6rem;
  transition: transform 0.2s ease, text-shadow 0.2s ease;
  margin-right: 0.5rem;
}

.navbar-brand:hover {
  transform: scale(1.03);
  text-shadow: 0px 0px 12px v-bind(brandColor);
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
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  text-decoration: none;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
  padding: 0.5rem 0.6rem;
  border-radius: 4px;
  transition: color 0.3s ease, background-color 0.3s ease, text-shadow 0.3s ease, border-color 0.3s ease;
  position: relative;
  border-bottom: 2px solid transparent;
}

.nav-link:hover {
  background-color: v-bind(primaryHover);
  color: #ffffff;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.5);
}

.nav-link.router-link-active {
  color: #ffffff;
  font-weight: 500;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.4);
  border-bottom-color: transparent;
}

/* 修改激活状态下的发光效果，使用主题颜色 */
.nav-link.router-link-active::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -1px;
  width: 100%;
  height: 1px;
  background: v-bind(activeColor);
  box-shadow: 0 0 3px v-bind(activeColor);
  opacity: 1;
}

.nav-link::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 100%;
  height: 2px;
  background: transparent;
  transition: all 0.3s ease;
  opacity: 0;
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
    flex-direction: column;
    padding-left: 0;
    padding-right: 0;
    min-height: 30px;
  }

  /* 在小屏幕下隐藏 Toggler 按钮 */
  .navbar-toggler {
    display: none !important;
  }

  /* 品牌样式调整 */
  .navbar-brand {
    display: block;
    width: 100%;
    text-align: center;
    margin: 0;
    padding: 0.3rem 0;
    cursor: pointer;
  }
  
  /* 默认隐藏导航内容，准备动画 */
  .navbar-collapse {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: linear-gradient(180deg, 
      rgba(20, 30, 50, 0.9) 0%, 
      v-bind(primaryColor) 100%
    );
    backdrop-filter: blur(v-bind(navBlur));
    -webkit-backdrop-filter: blur(v-bind(navBlur));
    border-bottom: none;
    box-shadow: none;
    transform-origin: top;
    transform: scaleY(0);
    opacity: 0;
    visibility: hidden;
    pointer-events: none;
    transition: all 0.2s ease-out;
  }

  /* 为下拉菜单添加底部渐变过渡 */
  .navbar-collapse::after {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 6px;
    background: linear-gradient(to bottom, 
      rgba(0, 0, 0, 0.3),
      rgba(0, 0, 0, 0)
    );
    pointer-events: none;
  }

  /* 展开状态 */
  .navbar-collapse.show {
    transform: scaleY(1);
    opacity: 1;
    visibility: visible;
    pointer-events: auto;
  }

  /* 调整内部导航组在小屏幕下的布局 */
  .nav-left, .nav-right {
    width: 100%;
    display: flex;
    justify-content: flex-start; /* 改为左对齐 */
    flex-direction: column; /* 垂直排列 */
    padding: 0; /* 移除内边距 */
  }
  
  .nav-item {
    margin: 0; /* 移除外边距 */
    padding: 0;
    width: 100%;
    text-align: left; /* 文字左对齐 */
  }

  .nav-link {
    display: block;
    padding: 0.8rem 1.2rem; /* 增加垂直内边距，保持水平内边距 */
    width: 100%;
    text-align: left;
  }

  /* 添加hover效果 */
  .nav-link:hover {
    background-color: v-bind(primaryHover);
  }
}

.cursor-pointer {
  cursor: pointer;
}
</style> 