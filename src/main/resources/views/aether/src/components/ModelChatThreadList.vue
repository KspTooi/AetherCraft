<template>
  <div class="thread-list-container">
    <LaserButton
      class="manage-thread-btn"
      :corners="['bottom-left', 'bottom-right']"
      corner-size="15px"
      :background-color="primaryButton"
      :border-color="primaryButtonBorder"
      @click="handleCreateNewThread">
      新建会话
    </LaserButton>
    <div class="thread-items-wrapper">
      <div v-if="loading" class="loading-indicator">
        <i class="bi bi-arrow-repeat spinning"></i> 加载中...
      </div>
      <div v-else-if="threads.length === 0" class="empty-thread-tip">
        <i class="bi bi-chat-square-text"></i>
        <div class="tip-text">还没有会话记录</div>
        <LaserButton
          class="create-thread-btn"
          :corners="['bottom-left', 'bottom-right']"
          corner-size="15px"
          :background-color="primaryButton"
          :border-color="primaryButtonBorder"
          :glow-color="primaryHover"
          @click="handleCreateNewThread">
          创建新会话
        </LaserButton>
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
            <LaserButton 
              class="thread-action-btn"
              width="24px"
              height="24px"
              background-color="transparent"
              border-color="transparent"
              glowIntensity="0"
              @click.stop="handleEditThread(thread)"
              title="编辑标题">
              <i class="bi bi-pencil"></i>
            </LaserButton>
            <LaserButton 
              class="thread-action-btn"
              width="24px"
              height="24px"
              background-color="transparent"
              border-color="transparent"
              glowIntensity="0"
              @click.stop="handleDeleteThread(thread.id)"
              title="删除会话">
              <i class="bi bi-trash"></i>
            </LaserButton>
          </div>
        </div>
      </div>
    </div>
  </div>
  <ConfirmModal ref="confirmModal" />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import axios from 'axios'
import LaserButton from './LaserButton.vue'
import { useThemeStore } from '../stores/theme'
import ConfirmModal from './ConfirmModal.vue'

// 获取主题颜色
const themeStore = useThemeStore()
const primaryColor = computed(() => themeStore.primaryColor)
const activeColor = computed(() => themeStore.activeColor)
const primaryHover = computed(() => themeStore.primaryHover)
const primaryButton = computed(() => themeStore.primaryButton)
const primaryButtonBorder = computed(() => themeStore.primaryButtonBorder)
const sideBlur = computed(() => themeStore.sideBlur)

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
const confirmModal = ref<InstanceType<typeof ConfirmModal> | null>(null)

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

const handleDeleteThread = async (threadId: string) => {
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
  display: flex;
  flex-direction: column;
  border-right: 1px solid v-bind('`rgba(${primaryColor.split("(")[1].split(")")[0]}, 0.1)`');
  border-radius: 0 !important;
  height: 100%;
  overflow: hidden;
}

.manage-thread-btn {
  margin: 12px 12px 5px 12px;
  padding: 10px 15px;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.manage-thread-btn i {
  font-size: 16px;
}

.thread-items-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  border-top: none;
  min-height: 0;
  margin-top: 5px;
  height: calc(100% - 60px);
}

.loading-indicator {
  padding: 20px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 10px;
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
  padding: 20px 20px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  flex: 1;
  margin-top: 10px;
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
  font-size: 14px;
  background-color: v-bind(primaryButton) !important;
  border-color: v-bind(primaryButtonBorder) !important;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.thread-items {
  display: flex;
  flex-direction: column;
  padding: 4px 0;
}

.thread-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.25s ease, border-left-color 0.25s ease, box-shadow 0.25s ease;
  border-left: 3px solid transparent;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  position: relative;
  overflow: hidden;
  margin: 2px 4px;
  border-radius: 2px;
}

.thread-item:first-child {
  margin-top: 5px;
}

.thread-item .thread-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.thread-item .thread-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.2px;
}

.thread-item .thread-time {
  font-size: 11px;
  color: v-bind(primaryColor);
  margin-bottom: 0;
  letter-spacing: 0.1px;
}

.thread-item .thread-message {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.thread-item:hover {
  background: v-bind(primaryHover);
}

.thread-item.active {
  background: rgba(40, 90, 130, 0.25);
  border-left-color: v-bind(activeColor);
  box-shadow: inset 3px 0 5px -2px v-bind(activeColor);
}

.thread-item.active .thread-title {
  color: rgba(255, 255, 255, 1);
  font-weight: 500;
}

.thread-item.active .thread-time {
  color: v-bind(activeColor);
}

.thread-actions {
  display: flex;
  gap: 4px;
  align-items: center;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.25s ease;
  flex-shrink: 0;
  right: 0;
}

.thread-item:hover .thread-actions {
  opacity: 1;
  pointer-events: auto;
}

.thread-action-btn {
  color: rgba(255, 255, 255, 0.4);
  min-height: 24px;
  padding: 0;
  transition: all 0.2s ease;
}

.thread-action-btn:hover {
  background: v-bind('`rgba(${primaryColor.split("(")[1].split(")")[0]}, 0.15)`') !important;
  color: rgba(255, 255, 255, 0.9);
}

.thread-action-btn i {
  font-size: 14px;
}

.thread-items-wrapper::-webkit-scrollbar {
  width: 3px;
}

.thread-items-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.thread-items-wrapper::-webkit-scrollbar-thumb {
  background: v-bind(primaryHover);
  border-radius: 1.5px;
}

.thread-items-wrapper::-webkit-scrollbar-thumb:hover {
  background: v-bind(primaryColor);
}

@media (max-width: 768px) {
  .thread-items-wrapper {
    flex: 1;
    overflow-y: auto;
    height: calc(100vh - 60px);
  }
}
</style> 