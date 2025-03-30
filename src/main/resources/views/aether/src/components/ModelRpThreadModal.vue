<template>
  <div class="thread-modal-overlay" @click.self="closeModal">
    <div class="thread-modal-container">
      <div class="thread-modal-header">
        <h3 class="thread-modal-title">{{ selectedRoleName }} 的会话列表</h3>
        <button class="thread-modal-close" @click="closeModal">×</button>
      </div>
      <div class="thread-modal-body">
        <div v-if="loading" class="loading-state">
          <i class="bi bi-arrow-repeat spinning"></i>
          加载中...
        </div>
        <div v-else-if="threads.length === 0" class="empty-state">
          <i class="bi bi-chat-square-text"></i>
          <div class="empty-title">还没有会话记录</div>
          <div class="empty-desc">选择角色后发送消息自动创建会话</div>
        </div>
        <div v-else class="thread-list">
          <div v-for="thread in threads" 
               :key="thread.id"
               @click="handleThreadSelect(thread.id)"
               class="thread-item"
               :class="{ 'active': currentThreadId === thread.id }">
            <div class="thread-content">
              <div class="thread-title">{{ thread.title || '未命名会话' }}</div>
              <div class="thread-time">{{ formatTime(thread.updateTime) }}</div>
              <div class="thread-message" v-if="thread.lastMessage">{{ thread.lastMessage }}</div>
            </div>
            <div class="thread-actions">
              <LaserButton 
                class="thread-action-btn"
                width="28px"
                height="28px"
                background-color="transparent"
                border-color="transparent"
                glowIntensity="0"
                @click.stop="handleEditThread(thread)"
                title="编辑标题">
                <i class="bi bi-pencil"></i>
              </LaserButton>
              <LaserButton 
                class="thread-action-btn"
                width="28px"
                height="28px"
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import LaserButton from './LaserButton.vue'
import { useThemeStore } from '../stores/theme'

// 获取主题颜色
const themeStore = useThemeStore()
const primaryColor = computed(() => themeStore.primaryColor)
const activeColor = computed(() => themeStore.activeColor)
const primaryHover = computed(() => themeStore.primaryHover)

// Props
const props = defineProps<{
  selectedRoleId: string
  selectedRoleName: string
}>()

// Emits
const emit = defineEmits<{
  (e: 'threadChecked', roleId: string, threadId: string): void
  (e: 'close'): void
}>()

// State
const threads = ref<any[]>([])
const loading = ref(false)
const currentThreadId = ref('')

// Methods
const loadThreadList = async () => {
  if (!props.selectedRoleId) return
  
  loading.value = true
  try {
    const response = await axios.post('/model/rp/getModelRoleThreadList', {
      modelRoleId: props.selectedRoleId
    })
    
    if (response.data.code === 0) {
      threads.value = response.data.data || []
      
      // 获取当前选中的会话ID
      const defaultThread = threads.value.find(t => t.checked === 1)
      if (defaultThread) {
        currentThreadId.value = defaultThread.id
      }
    } else {
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
    
    return `${year}年${month}月${day}日 ${hours}:${minutes}`
  } catch (e) {
    console.error('时间格式化错误:', e)
    return ''
  }
}

const handleThreadSelect = (threadId: string) => {
  emit('threadChecked', props.selectedRoleId, threadId)
}

const handleEditThread = async (thread: any) => {
  const newTitle = prompt('请输入新的会话标题', thread.title || '未命名会话')
  if (newTitle === null) return // 用户取消
  
  if (newTitle.trim() === '') {
    alert('会话标题不能为空')
    return
  }
  
  try {
    const response = await axios.post('/model/rp/updateRpThread', {
      threadId: thread.id,
      title: newTitle.trim()
    })
    
    if (response.data.code === 0) {
      thread.title = newTitle.trim()
      console.log('会话标题更新成功')
    } else {
      console.error('更新会话标题失败:', response.data.message)
    }
  } catch (error) {
    console.error('更新会话标题失败:', error)
  }
}

const handleDeleteThread = async (threadId: string) => {
  if (!confirm('确定要删除这个会话吗？此操作不可恢复。')) return
  
  try {
    const response = await axios.post('/model/rp/removeThread', {
      threadId: threadId
    })
    
    if (response.data.code === 0) {
      threads.value = threads.value.filter(t => t.id !== threadId)
      console.log('会话删除成功')
    } else {
      console.error('删除会话失败:', response.data.message)
    }
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

const closeModal = () => {
  emit('close')
}

// 监听selectedRoleId变化，加载对应的会话列表
watch(() => props.selectedRoleId, (newValue) => {
  if (newValue) {
    loadThreadList()
  }
})

// 组件挂载后加载会话列表
onMounted(() => {
  if (props.selectedRoleId) {
    loadThreadList()
  }
})
</script>

<style scoped>
.thread-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.thread-modal-container {
  width: 90%;
  max-width: 800px;
  max-height: 80vh;
  background: rgba(30, 30, 30, 0.95);
  border-radius: 0;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalIn 0.3s ease;
}

@keyframes modalIn {
  from { 
    transform: scale(0.95); 
    opacity: 0;
  }
  to { 
    transform: scale(1); 
    opacity: 1;
  }
}

.thread-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(40, 40, 40, 0.9);
}

.thread-modal-title {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: normal;
}

.thread-modal-close {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 22px;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.thread-modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.thread-modal-body {
  flex: 1;
  padding: 0;
  overflow-y: auto;
  min-height: 200px;
  max-height: calc(80vh - 60px);
}

/* 加载状态 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: rgba(255, 255, 255, 0.6);
  gap: 10px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.empty-state i {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: 20px;
}

.empty-title {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

/* 会话列表 */
.thread-list {
  padding: 10px;
}

.thread-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  border-radius: 0;
  margin-bottom: 6px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.05);
  transition: all 0.2s ease;
  position: relative;
}

.thread-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.thread-item.active {
  background: v-bind('`rgba(${primaryColor.split("(")[1].split(")")[0]}, 0.2)`');
  border-left: 3px solid v-bind(activeColor);
}

.thread-content {
  flex: 1;
  min-width: 0;
}

.thread-title {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 5px;
  font-weight: 500;
}

.thread-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 5px;
}

.thread-message {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 500px;
}

.thread-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.thread-item:hover .thread-actions {
  opacity: 1;
}

.thread-action-btn {
  color: rgba(255, 255, 255, 0.6);
  transition: color 0.2s ease;
}

.thread-action-btn:hover {
  color: rgba(255, 255, 255, 0.9);
}

/* 自定义滚动条 */
.thread-modal-body::-webkit-scrollbar {
  width: 4px;
}

.thread-modal-body::-webkit-scrollbar-track {
  background: transparent;
}

.thread-modal-body::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.thread-modal-body::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style> 