<template>
  <div class="model-chat-list-wrapper">
    <!-- 移动端悬浮按钮 -->
    <GlowButton
      v-if="isMobile"
      @click="toggleMobileMenu"
      class="mobile-menu-btn"
      :corners="[`top-left`]"
    >
      角色列表
    </GlowButton>

    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && mobileMenuOpen" 
      class="mobile-overlay"
      @click="closeMobileMenu"
    ></div>

    <GlowDiv 
      border="right" 
      class="chat-list-container"
      :class="{ 'mobile-open': isMobile && mobileMenuOpen, 'mobile': isMobile }"
    >

      <!-- 管理角色按钮 -->
      <div class="manage-role-btn-wrapper"> 
        <GlowButton
          @click="handleRoleManage" 
          class="manage-role-btn"
          :corners="['bottom-right','bottom-left']"
        >
          返回
        </GlowButton>
      </div>
      
      <!-- 会话列表内容 -->
      <div class="threads-wrapper">
        <!-- 加载中状态 -->
        <div v-if="loading" class="loading-indicator">
          <i class="bi bi-arrow-repeat spinning"></i> 
          <span>加载中...</span>
        </div>
        
        <!-- 空列表状态 -->
        <div v-else-if="threads.length === 0" class="empty-list">
          <i class="bi bi-person-plus"></i>
          <div class="empty-text">您还未拥有模型角色</div>
          <GlowButton
            @click="handleCreateRole($event)" 
            class="empty-create-btn"
          >
            创建新角色
          </GlowButton>
        </div>
        
        <!-- 会话列表 -->
        <div v-else class="thread-list">
          <!-- 新增角色按钮 -->
          <div 
            class="thread-item new-role-item"
            @click="handleCreateRole($event)"
          >
            <div class="role-avatar new-role-avatar">
              <i class="bi bi-plus-lg"></i>
            </div>
            <div class="thread-content">
              <div class="thread-title">创建新角色</div>
            </div>
          </div>
          
          <!-- 现有角色列表 -->
          <div 
            v-for="thread in threads" 
            :key="thread.id"
            @click="handleRoleClick(thread.id)"
            :class="['thread-item', { active: thread.id == activeThreadId }]"
          >
            <div class="role-avatar" :class="{ 'no-image': !thread.avatarPath }">
              <img v-if="thread.avatarPath" :src="thread.avatarPath" :alt="thread.name">
              <i v-else class="bi bi-person"></i>
            </div>
            <div class="thread-content">
              <div class="thread-title">{{ thread.name }}</div>
            </div>
          </div>
        </div>
      </div>
    </GlowDiv>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, watch, onBeforeUnmount, computed } from 'vue'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue"
import GlowButton from "@/components/glow-ui/GlowButton.vue"
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'
import type GetModelRoleListVo from '@/entity/GetModelRoleListVo'; // 导入 GetModelRoleListVo 类型
import { useRouter } from 'vue-router' // 导入 router
import { usePreferencesStore } from '@/stores/preferences' // 导入 preferences store

// 获取 glow 主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)
// 获取路由器
const router = useRouter()
// 获取偏好设置存储
const preferencesStore = usePreferencesStore()

// 事件定义
const emit = defineEmits<{
  (e: 'select-role', roleId: string): void;
  (e: 'create-role'): void; // 添加创建角色事件
}>()

const props = defineProps<{
  data: GetModelRoleListVo[] // 使用 GetModelRoleListVo 数组类型
  selected: string //当前选择的会话ID
  loading?: boolean
}>()

// 状态
const loading = computed(() => props.loading ?? false)
const threads = computed(() => props.data)

// 计算属性：活动会话ID
const activeThreadId = computed(() => props.selected)

// 移动端相关状态
const isMobile = ref(window.innerWidth <= 768)
const mobileMenuOpen = ref(false)

// 处理点击会话
const handleRoleClick = (roleId: string) => {
  if (roleId === activeThreadId.value) {
    closeMobileMenu(); // 点击当前选中项时也关闭移动菜单
    return; 
  }
  emit('select-role', roleId)
  closeMobileMenu()
}

// 处理管理角色按钮点击
const handleRoleManage = async () => {
  // 在跳转前先将clientRpPath设置为/rp-main，防止循环重定向
  await preferencesStore.saveClientRpPath('/rp')
  router.push('/rp')
  closeMobileMenu()
}

// 处理创建角色点击
const handleCreateRole = (event: Event) => {
  event.stopPropagation()
  emit('create-role')
  closeMobileMenu()
}

// 监听窗口大小变化
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    mobileMenuOpen.value = false
  }
}

// 移动端菜单控制
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  if (isMobile.value) {
    mobileMenuOpen.value = false
  }
}

// 组件挂载时设置窗口大小监听
onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

// 暴露方法给父组件
defineExpose({
  closeMobileMenu
})
</script>

<style scoped>
.model-chat-list-wrapper {
  position: relative;
  height: 100%;
  user-select: none; /* 禁止文本选择 */
  -webkit-user-select: none; /* 兼容性前缀 (Safari, Chrome) */
  -moz-user-select: none;    /* 兼容性前缀 (Firefox) */
  -ms-user-select: none;     /* 兼容性前缀 (IE/Edge) */
}

.mobile-menu-btn {
  position: fixed;
  top: 8px;
  left: 10px;
  z-index: 1000;
  height: 32px;
  min-height: 32px;
  font-size: 12px;
  padding: 8px;
}

.mobile-menu-btn i {
  font-size: 24px;
}

.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.chat-list-container {
  width: 240px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1000;
}

.manage-role-btn-wrapper { 
  padding: 12px;
  flex-shrink: 0;
}

.manage-role-btn { 
  width: 100%;
  padding: 10px 0;
  font-size: 14px;
}

.threads-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* 加载中状态 */
.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: v-bind('theme.boxTextColorNoActive');
  gap: 8px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 空列表状态 */
.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px;
  flex: 1;
}

.empty-list i {
  font-size: 36px;
  margin-bottom: 16px;
  color: v-bind('theme.boxTextColorNoActive');
}

.empty-text {
  font-size: 14px;
  margin-bottom: 20px;
  color: v-bind('theme.boxTextColorNoActive');
}

.empty-create-btn {
  width: 80%;
  max-width: 160px;
}

/* 会话列表 */
.thread-list {
  padding: 1px 0;
}

.thread-item {
  display: flex;
  align-items: center; /* 垂直居中对齐 */
  padding: 10px 8px 10px 20px; /* 调整右边距给菜单按钮空间 */
  margin: 4px 0;
  cursor: pointer;
  transition: background-color 0.2s ease, border-left-color 0.2s ease;
  border-left: 3px solid transparent;
  position: relative; /* 为菜单按钮定位 */
}

.thread-item:hover {
  background-color: v-bind('theme.boxAccentColor');
}

.thread-item.active {
  background-color: v-bind('theme.boxColorActive');
  border-left-color: v-bind('theme.boxGlowColor');
  box-shadow: inset 5px 0 8px -2px v-bind('theme.boxGlowColor');
  backdrop-filter: blur(v-bind('theme.boxBlurActive') + 'px');
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlurActive') + 'px');
}

.thread-content {
  flex: 1;
  min-width: 0;
  /* align-self: center; */ /* 移除，因为父级已居中 */
  margin-right: 24px; /* 给菜单按钮留出空间 */
}

.thread-title {
  font-size: 14px;
  color: v-bind('theme.boxTextColorNoActive');
  margin-bottom: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s ease;
}

.thread-item:hover .thread-title,
.thread-item.active .thread-title {
  color: v-bind('theme.boxTextColor');
}

.thread-item.active .thread-title {
  font-weight: 500;
}

/* 新增 role-avatar 样式 */
.role-avatar {
  width: 36px;
  height: 36px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  margin-right: 10px; /* 添加右边距 */
  border-radius: 0; /* 直角 */
}

.role-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.role-avatar.no-image i {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
}

.thread-item.active .role-avatar {
  border-color: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 8px v-bind('theme.boxGlowColor');
}

/* 菜单按钮相关样式 */
.menu-wrapper {
  position: absolute; /* 绝对定位到 thread-item 右侧 */
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  opacity: 0; /* 默认隐藏 */
  transition: opacity 0.2s ease;
}

.thread-item:hover .menu-wrapper {
  opacity: 1; /* 悬浮时显示 */
}

.menu-btn {
  background: transparent;
  border: none;
  color: v-bind('theme.boxTextColorNoActive');
  padding: 4px 6px;
  border-radius: 2px;
  cursor: pointer;
  transition: color 0.2s ease, background-color 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-btn:hover {
  color: v-bind('theme.boxTextColor');
  background: v-bind('theme.boxAccentColorHover');
}

.menu-btn i {
  font-size: 16px;
}

/* 滚动条样式 */
.threads-wrapper::-webkit-scrollbar {
  width: 3px;
}

.threads-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.threads-wrapper::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
}

.threads-wrapper::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.boxBorderColorHover');
}

/* 响应式样式 */
@media (max-width: 768px) {
  .chat-list-container {
    position: fixed;
    top: 0;
    left: -240px;
    height: 100vh;
    transition: transform 0.3s ease;
    box-shadow: none;
    /* 移动端菜单总是显示 */
    .menu-wrapper {
      opacity: 1;
    }
  }
  
  .chat-list-container.mobile-open {
    transform: translateX(240px);
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
  }
}

/* 新角色按钮样式 */
.new-role-item {
  border-left: 3px solid v-bind('theme.mainColor');
  background-color: rgba(255, 255, 255, 0.05);
  margin: 4px 0; /* 与其他角色项目保持一致的边距 */
}

.new-role-item:hover {
  background-color: v-bind('theme.mainColorHover');
}

.new-role-avatar {
  background: v-bind('theme.mainColor');
  border-color: v-bind('theme.mainBorderColor');
}

.new-role-avatar i {
  font-size: 18px; /* 调整图标大小与其他角色头像图标一致 */
  color: v-bind('theme.mainTextColor');
}

.new-role-item .thread-title {
  color: v-bind('theme.boxTextColor');
}
</style>