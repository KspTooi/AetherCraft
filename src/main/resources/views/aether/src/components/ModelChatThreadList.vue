<template>
  <div class="thread-list-container" :class="{ show: isMobileMenuOpen }">
    <a href="#" class="manage-thread-btn" @click.prevent="handleCreateNewThread">
      <i class="bi bi-plus-circle"></i>
      新建会话
    </a>
    <div class="thread-items-wrapper">
      <div v-if="loading" class="loading-indicator">
        <i class="bi bi-arrow-repeat spinning"></i> 加载中...
      </div>
      <div v-else-if="threads.length === 0" class="empty-thread-tip">
        <i class="bi bi-chat-square-text"></i>
        <div class="tip-text">还没有会话记录</div>
        <a href="#" class="create-thread-btn" @click.prevent="handleCreateNewThread">
          创建新会话
        </a>
      </div>
      <div v-else class="thread-items">
        <div v-for="thread in threads" 
             :key="thread.id"
             @click="handleThreadClick(thread.id)"
             :class="['thread-item', { active: String(thread.id) === String(currentThreadId) }]"
             :title="thread.title">
          <div class="thread-content">
            <div class="thread-title">{{ thread.title }}</div>
            <div class="thread-time">{{ formatTime(thread.updateTime) }}</div>
            <div class="thread-message" v-if="thread.lastMessage">{{ thread.lastMessage }}</div>
          </div>
          <div class="thread-actions">
            <button class="thread-action-btn" 
                    @click.stop="handleEditThread(thread)"
                    title="编辑标题">
              <i class="bi bi-pencil"></i>
            </button>
            <button class="thread-action-btn" 
                    @click.stop="handleDeleteThread(thread.id)"
                    title="删除会话">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'

// Props
const props = defineProps<{
  currentThreadId: string | null
  isMobileMenuOpen: boolean // 控制移动端显示
}>()

// Emits
const emit = defineEmits<{
  (e: 'threadChecked', threadId: string): void
  (e: 'threadEdit', thread: any): void
  (e: 'threadRemove', threadId: string): void
  (e: 'createNewThread'): void // 通知父组件创建新会话
}>()

// State
const threads = ref<any[]>([])
const loading = ref(false)

// Methods
const loadThreadList = async () => {
  loading.value = true
  try {
    const response = await axios.post('/model/chat/getThreadList')
    if (response.data.code === 0) {
      threads.value = response.data.data || []
      // 检查是否有默认选中的项，并通知父组件
      const defaultThread = threads.value.find(t => t.checked === 1)
      if (defaultThread) {
        emit('threadChecked', defaultThread.id)
      }
    } else {
      // 可以选择性地向父组件抛出错误或显示本地提示
      console.error('加载会话列表失败:', response.data.message)
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

const formatTime = (timestamp: string) => {
  if (!timestamp) return ''
  try {
    const date = new Date(timestamp)
    if (isNaN(date.getTime())) return ''
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    // const seconds = date.getSeconds().toString().padStart(2, '0') // 通常列表不需要秒
    return `${year}年${month}月${day}日 ${hours}:${minutes}`
  } catch (e) {
    console.error('时间格式化错误:', e)
    return ''
  }
}

// Event Handlers that emit to parent
const handleThreadClick = (threadId: string) => {
  emit('threadChecked', threadId)
}

const handleEditThread = (thread: any) => {
  emit('threadEdit', thread)
}

const handleDeleteThread = (threadId: string) => {
  emit('threadRemove', threadId)
}

const handleCreateNewThread = () => {
  emit('createNewThread')
}

// Expose loadThreadList function to parent
defineExpose({
  loadThreadList
})
</script>

<style scoped>
.thread-list-container {
  width: 240px;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 0 !important;
  transition: transform 0.3s ease;
}

.manage-thread-btn {
  margin: 12px;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: rgba(79, 172, 254, 0.2);
  color: white;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  text-decoration: none;
  flex-shrink: 0; /* 防止按钮被压缩 */
}

.manage-thread-btn i {
  font-size: 16px;
}

.manage-thread-btn:hover {
  background: rgba(79, 172, 254, 0.3);
  transform: translateY(-1px);
}

.manage-thread-btn:active {
  transform: translateY(0);
}

.thread-items-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  min-height: 0; /* 重要，确保flex布局正常 */
}

.loading-indicator {
  padding: 20px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-thread-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  flex: 1; /* 占据剩余空间 */
}

.empty-thread-tip i {
  font-size: 36px;
  margin-bottom: 12px;
}

.empty-thread-tip .tip-text {
  margin-bottom: 16px;
  font-size: 14px;
}

.create-thread-btn {
  padding: 8px 16px;
  background: rgba(79, 172, 254, 0.3);
  color: white;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.create-thread-btn:hover {
  background: rgba(79, 172, 254, 0.5);
  transform: translateY(-2px);
}

.thread-items {
  display: flex;
  flex-direction: column;
}

.thread-item {
  padding: 10px 16px; /* 微调内边距 */
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.thread-item .thread-content {
  flex: 1;
  min-width: 0;
}

.thread-item .thread-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 2px; /* 减小间距 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.thread-item .thread-time {
  font-size: 11px; /* 减小字体 */
  color: rgba(255, 255, 255, 0.4);
  margin-bottom: 2px;
}

.thread-item .thread-message {
  font-size: 12px; /* 减小字体 */
  color: rgba(255, 255, 255, 0.6);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1; /* 最多显示一行 */
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.thread-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.thread-item.active {
  background: rgba(79, 172, 254, 0.15);
  border-left-color: #4facfe;
}

.thread-actions {
  display: none;
  gap: 4px;
  align-items: center; /* 垂直居中按钮 */
}

.thread-item:hover .thread-actions {
  display: flex;
}

.thread-action-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.4);
  width: 24px; /* 减小按钮大小 */
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.thread-action-btn i {
  font-size: 14px; /* 减小图标大小 */
}

.thread-action-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.9);
}

/* 自定义滚动条 */
.thread-items-wrapper::-webkit-scrollbar {
  width: 4px;
}

.thread-items-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.thread-items-wrapper::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.thread-items-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .thread-list-container {
    position: absolute;
    left: -240px;
    top: 0;
    bottom: 0;
    z-index: 100;
    background: rgba(0, 0, 0, 0.85); /* 加深背景以便看清 */
    backdrop-filter: blur(5px);
  }
  
  .thread-list-container.show {
    transform: translateX(240px);
  }
}
</style> 