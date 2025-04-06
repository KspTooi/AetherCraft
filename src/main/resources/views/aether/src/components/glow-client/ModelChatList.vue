<template>
  <div class="model-chat-list-wrapper">
    <!-- 移动端悬浮按钮 -->
    <GlowButton
      v-if="isMobile"
      @click="toggleMobileMenu"
      class="mobile-menu-btn"
    >
      <i class="bi bi-list"></i>
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

      <!-- 新建会话按钮 -->
      <div class="create-chat-btn-wrapper">
        <GlowButton
          @click="handleCreateNewThread"
          class="create-chat-btn"
          :corners="['bottom-right','bottom-left']"
        >
          新建会话
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
          <i class="bi bi-chat-square-text"></i>
          <div class="empty-text">还没有会话记录</div>
          <GlowButton
            @click="handleCreateNewThread"
            class="empty-create-btn"
          >
            创建新会话
          </GlowButton>
        </div>
        
        <!-- 会话列表 -->
        <div v-else class="thread-list">
          <div 
            v-for="thread in threads" 
            :key="thread.id"
            @click="handleThreadClick(thread.id)"
            :class="['thread-item', { active: thread.id === activeThreadId }]"
          >
            <div class="thread-content">
              <div class="thread-title">{{ thread.title }}</div>
              <div class="thread-model">{{ thread.modelCode }}</div>
            </div>
            
            <div class="thread-actions">
              <button 
                class="thread-action-btn edit"
                @click.stop="handleEditThread(thread.id)"
                title="编辑标题"
              >
                <i class="bi bi-pencil"></i>
              </button>
              <button 
                class="thread-action-btn delete"
                @click.stop="handleDeleteThread(thread.id)"
                title="删除会话"
              >
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </GlowDiv>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, watch, onBeforeUnmount } from 'vue'
import axios from 'axios'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue"
import GlowButton from "@/components/glow-ui/GlowButton.vue"
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'

// 获取 glow 主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 事件定义
const emit = defineEmits<{
  (e: 'select-thread', threadId: string): void;
  (e: 'delete-thread', threadId: string): void;
  (e: 'update-title', threadId: string): void;
  (e: 'create-thread'): void;
}>()

// 状态
const loading = ref(false)
const activeThreadId = ref<string>('')
const threads = ref<Array<{
  id: string,
  title: string,
  modelCode: string,
  checked: number,
  updateTime?: string
}>>([])

// 移动端相关状态
const isMobile = ref(window.innerWidth <= 768)
const mobileMenuOpen = ref(false)

// 加载会话列表
const loadThreadList = async () => {
  loading.value = true
  try {
    const response = await axios.post('/model/chat/getThreadList')
    if (response.data.code === 0) {
      threads.value = response.data.data || []
      
      // 检查是否有默认选中的项
      const defaultThread = threads.value.find(t => t.checked === 1)
      if (defaultThread) {
        selectThread(defaultThread.id)
      } else if (threads.value.length > 0) {
        // 如果没有默认选中项但有会话，选中第一个
        selectThread(threads.value[0].id)
      }
      
      return threads.value
    } else {
      console.error('加载会话列表失败:', response.data.message)
      return []
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    return []
  } finally {
    loading.value = false
  }
}

// 选择会话
const selectThread = (threadId: string) => {
  // 更新内部activeThreadId
  activeThreadId.value = threadId
  
  // 更新checked状态
  threads.value.forEach(thread => {
    thread.checked = thread.id === threadId ? 1 : 0
  })
  
  // 通知父组件
  emit('select-thread', threadId)
}

// 处理点击会话
const handleThreadClick = (threadId: string) => {
  selectThread(threadId)
}

// 处理编辑会话标题
const handleEditThread = (threadId: string) => {
  emit('update-title', threadId)
}

// 处理删除会话
const handleDeleteThread = async (threadId: string) => {
  try {
    // 通知父组件删除
    emit('delete-thread', threadId)
    
    // 如果删除的是当前选中的会话
    if (threadId === activeThreadId.value) {
      activeThreadId.value = ''
    }
    
    // 重新加载列表
    await loadThreadList()
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

// 处理创建新会话
const handleCreateNewThread = () => {
  emit('create-thread')
}

// 外部调用设置活动会话
const setActiveThread = (threadId: string) => {
  selectThread(threadId)
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
  mobileMenuOpen.value = false
}

// 组件挂载时加载数据
onMounted(() => {
  loadThreadList()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

// 暴露方法给父组件
defineExpose({
  loadThreadList,
  setActiveThread,
  getActiveThreadId: () => activeThreadId.value,
  threads,
  closeMobileMenu
})
</script>

<style scoped>
.model-chat-list-wrapper {
  position: relative;
  height: 100%;
}

.mobile-menu-btn {
  position: fixed;
  top: 100px;
  left: 10px;
  z-index: 1000;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0;
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

.create-chat-btn-wrapper {
  padding: 12px;
  flex-shrink: 0;
}

.create-chat-btn {
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
  font-size: 32px;
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
  align-items: flex-start;
  padding: 10px 12px;
  margin: 4px 0;
  cursor: pointer;
  transition: background-color 0.2s ease, border-left-color 0.2s ease;
  border-left: 3px solid transparent;
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
}

.thread-title {
  font-size: 14px;
  color: v-bind('theme.boxTextColorNoActive');
  margin-bottom: 4px;
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

.thread-model {
  font-size: 12px;
  color: v-bind('theme.boxTextColorNoActive');
  opacity: 0.7;
}

/* 操作按钮 */
.thread-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.thread-item:hover .thread-actions {
  opacity: 1;
}

.thread-action-btn {
  background: transparent;
  border: none;
  color: v-bind('theme.boxTextColorNoActive');
  width: 24px;
  height: 24px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.thread-action-btn:hover {
  background-color: v-bind('theme.boxAccentColorHover');
  color: v-bind('theme.boxTextColor');
}

.thread-action-btn.delete:hover {
  color: #ff6b6b;
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
  }
  
  .chat-list-container.mobile-open {
    transform: translateX(240px);
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
  }
}
</style>