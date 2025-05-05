<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useThemeStore } from '../../stores/theme'
import GlowConfirm from '../glow-ui/GlowConfirm.vue'
import axios from 'axios'
import ClientTopNav from './ClientTopNav.vue'

const brandName = ref('AetherCraft')
const logoutModal = ref<InstanceType<typeof GlowConfirm> | null>(null)

// 获取主题颜色
const themeStore = useThemeStore()

// 计算内容区域高度
const contentHeight = ref('100vh')
const updateContentHeight = () => {
  // 简化高度计算，使用flex布局自动填充
  contentHeight.value = 'auto'
  
  // 确保移动端视口高度正确
  document.documentElement.style.setProperty('--vh', `${window.innerHeight * 0.01}px`)
}

// 导航项配置
const leftNavItems = [
  { name: '聊天', routerLink: '/chat' },
  { name: '角色扮演', routerLink: '/rp' },
  { name: '个性化', routerLink: '/customize' },
  { name: '特效测试', routerLink: '/effects-test' },
  { name: '组件测试', routerLink: '/laser-test' },
  { name: '侧边栏示例', routerLink: '/side-panel-test' },
]

// 右侧导航项
const rightNavItems = [
  { name: '经典UI', href: '/model/chat/view' },
  { name: '管理台', href: '/admin-ui' },
  { name: '注销', action: 'logout' }
]

// 处理导航栏动作
const handleNavAction = (action: string) => {
  if (action === 'logout') {
    showLogoutConfirm()
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

// 挂载时初始化
onMounted(() => {
  // 计算内容高度
  updateContentHeight()
  window.addEventListener('resize', updateContentHeight)
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  window.removeEventListener('resize', updateContentHeight)
})
</script>

<template>
  <div class="client-frame">
    <div class="background-layer"></div>
    
    <div class="content-wrapper">
      <!-- 导航栏 -->
      <client-top-nav 
        :left-items="leftNavItems"
        :right-items="rightNavItems"
        :brand="brandName"
        @action="handleNavAction"
      />
      
      <!-- 主内容区 -->
      <div class="frame-content">
        <slot></slot>
      </div>
    </div>
    
    <GlowConfirm ref="logoutModal" />
  </div>
</template>

<style>
/* 全局样式，确保导航栏充满整个宽度 */
:root {
  --nav-height: 40px;
  --vh: 1vh;
}

body, html {
  margin: 0;
  padding: 0;
  overflow: hidden;
  height: 100%;
  width: 100%;
  position: fixed;
}

#app {
  width: 100%;
  height: 100%;
  overflow: hidden;
}
</style>

<style scoped>
.client-frame {
  position: relative;
  height: 100%;
  width: 100%;
  overflow: hidden;
  height: calc(var(--vh, 1vh) * 100);
}

.background-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('/customize/getWallpaper');
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
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.frame-content {
  flex: 1;
  overflow: auto;
  position: relative;
  -webkit-overflow-scrolling: touch;
}
</style>