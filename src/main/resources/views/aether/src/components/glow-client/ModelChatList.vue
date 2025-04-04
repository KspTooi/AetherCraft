<template>
  <GlowDiv border="right" class="chat-list-container">

    <!-- 新建会话按钮 -->
    <div class="create-chat-btn-wrapper">
      <GlowButton
        @click="handleCreateNewThread"
        class="create-chat-btn"
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
</template>

<script setup lang="ts">
import { ref, inject, onMounted } from 'vue'
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

// 组件挂载时加载数据
onMounted(() => {
  loadThreadList()
})

// 暴露方法给父组件
defineExpose({
  loadThreadList,
  setActiveThread,
  getActiveThreadId: () => activeThreadId.value,
  threads
})
</script>

<style scoped>
.chat-list-container {
  width: 240px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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
  padding: 4px 8px;
}

.thread-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 12px;
  margin: 4px 0;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s ease, border-left-color 0.2s ease;
  border-left: 3px solid transparent;
}

.thread-item:hover {
  background-color: v-bind('theme.boxAccentColor');
}

.thread-item.active {
  background-color: v-bind('theme.boxAccentColorHover');
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
  border-radius: 3px;
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
  border-radius: 1.5px;
}

.threads-wrapper::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.boxBorderColorHover');
}

/* 响应式样式 */
@media (max-width: 768px) {
  .chat-list-container {
    width: 100%;
    height: auto;
    max-height: 300px;
  }
}
</style>