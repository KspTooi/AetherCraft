<template>
  <div class="thread-modal-overlay" @click.self="closeModal">
    <div class="thread-modal-container">
      <div class="modal-glow-border"></div>
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
              <div class="thread-title">
                {{ thread.title || '未命名会话' }}
                <span v-if="thread.active === 1" class="active-badge">当前</span>
              </div>
              <div class="thread-time">{{ formatTime(thread.updateTime) }}</div>
              <div class="thread-message" v-if="thread.lastMessage">{{ thread.lastMessage }}</div>
              <div class="thread-message" v-else>暂无对话内容</div>
            </div>
            <div class="thread-actions">
              <LaserButton 
                v-if="thread.active !== 1"
                class="thread-action-btn activate-btn"
                width="30px"
                height="30px"
                background-color="transparent"
                border-color="transparent"
                glowIntensity="0"
                @click.stop="handleActivateThread(thread.id)"
                title="激活会话">
                <i class="bi bi-check-circle"></i>
              </LaserButton>
              <LaserButton 
                class="thread-action-btn delete-btn"
                width="30px"
                height="30px"
                background-color="transparent"
                border-color="transparent"
                glowIntensity="0"
                @click.stop="handleDeleteThread(thread.id)"
                :disabled="threads.length <= 1"
                :class="{ 'disabled': threads.length <= 1 }"
                title="删除会话">
                <i class="bi bi-trash"></i>
              </LaserButton>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 确认模态框 -->
  <ConfirmModal ref="confirmModalRef" />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import LaserButton from './LaserButton.vue'
import ConfirmModal from './ConfirmModal.vue'
import { useThemeStore } from '../stores/theme'

// 获取主题颜色
const themeStore = useThemeStore()
const primaryColor = computed(() => themeStore.primaryColor)
const activeColor = computed(() => themeStore.activeColor)
const primaryHover = computed(() => themeStore.primaryHover)
const brandColor = computed(() => themeStore.brandColor)
const primaryButton = computed(() => themeStore.primaryButton)
const primaryButtonBorder = computed(() => themeStore.primaryButtonBorder)
const mainBlur = computed(() => themeStore.mainBlur)
const sideBlur = computed(() => themeStore.sideBlur)
const textareaColor = computed(() => themeStore.textareaColor)
const modalColor = computed(() => themeStore.modalColor)
const modalBlur = computed(() => themeStore.modalBlur)
const modalActive = computed(() => themeStore.modalActive)

// Props
const props = defineProps<{
  selectedRoleId: string
  selectedRoleName: string
}>()

// Emits
const emit = defineEmits<{
  (e: 'threadChecked', roleId: string, threadId: string, shouldCloseModal: boolean): void
  (e: 'close'): void
}>()

// State
const threads = ref<any[]>([])
const loading = ref(false)
const currentThreadId = ref('')
const confirmModalRef = ref<InstanceType<typeof ConfirmModal> | null>(null)

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
      
      // 获取当前活动的会话ID
      const activeThread = threads.value.find(t => t.active === 1)
      if (activeThread) {
        currentThreadId.value = activeThread.id
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
  emit('threadChecked', props.selectedRoleId, threadId, true)
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

const handleActivateThread = async (threadId: string) => {
  try {
    // 查找对应的线程对象，以获取正确的模型代码
    const thread = threads.value.find(t => t.id === threadId)
    if (!thread) {
      console.error('找不到相应的会话')
      return
    }
    
    console.log(`激活会话: roleId=${props.selectedRoleId}, threadId=${threadId}, modelCode=${thread.modelCode}`)
    
    // 使用线程对象中的模型代码
    const requestData = {
      modelRoleId: Number(props.selectedRoleId),
      threadId: Number(threadId),
      modelCode: thread.modelCode || 'gemini-pro' // 优先使用线程中的模型代码，如果没有则使用默认值
    }
    
    console.log('发送请求:', requestData)
    const response = await axios.post('/model/rp/recoverRpChat', requestData)
    
    if (response.data.code === 0) {
      console.log('激活会话成功, 响应:', response.data)
      
      // 更新当前激活会话
      threads.value.forEach(t => {
        t.active = t.id === threadId ? 1 : 0
      })
      currentThreadId.value = threadId
      
      // 触发threadChecked事件以加载消息，但指定不关闭模态框
      emit('threadChecked', props.selectedRoleId, threadId, false)
    } else {
      console.error('激活会话失败:', response.data.message)
      alert('激活会话失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('激活会话失败:', error)
    alert('激活会话失败: ' + (error instanceof Error ? error.message : '未知错误'))
  }
}

const handleDeleteThread = async (threadId: string) => {
  if (!confirmModalRef.value) return
  
  // 检查是否为最后一个会话
  if (threads.value.length <= 1) {
    return // 直接返回，不执行删除操作
  }
  
  const threadTitle = threads.value.find(t => t.id === threadId)?.title || '未命名会话'
  
  const confirmed = await confirmModalRef.value.showConfirm({
    title: '删除会话',
    content: `您确定要删除 "${threadTitle}" 会话吗？此操作不可恢复。`,
    confirmText: '删除',
    cancelText: '取消'
  })
  
  if (!confirmed) return
  
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
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2147483646;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.thread-modal-container {
  width: 90%;
  max-width: 800px;
  max-height: 80vh;
  background: v-bind(modalColor);
  backdrop-filter: blur(v-bind(modalBlur));
  -webkit-backdrop-filter: blur(v-bind(modalBlur));
  border-radius: 0px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalIn 0.25s ease;
  position: relative;
}

@keyframes modalIn {
  from { 
    transform: translateY(-10px); 
    opacity: 0;
  }
  to { 
    transform: translateY(0); 
    opacity: 1;
  }
}

.modal-glow-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: v-bind(modalActive);
  box-shadow: 0 0 10px 1px v-bind(modalActive);
  z-index: 1;
}

.thread-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: v-bind('`rgba(${modalColor.split("(")[1].split(")")[0].split(",").map((n, i) => i < 3 ? Math.max(0, parseInt(n) - 5) : n).join(",")}, 1)`');
  position: relative;
  margin-bottom: 8px;
}

.thread-modal-title {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 500;
  letter-spacing: 0.5px;
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
  border-radius: 0;
  transition: all 0.2s;
}

.thread-modal-close:hover {
  background: v-bind('`rgba(${modalActive.split("(")[1].split(")")[0]}, 0.2)`');
  color: white;
}

.thread-modal-body {
  flex: 1;
  padding: 0;
  overflow-y: auto;
  min-height: 200px;
  max-height: calc(80vh - 56px);
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
  padding: 30px 20px;
  text-align: center;
}

.empty-state i {
  font-size: 42px;
  color: v-bind('`rgba(${modalActive.split("(")[1].split(")")[0]}, 0.3)`');
  margin-bottom: 15px;
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
  padding: 4px 8px 8px;
}

.thread-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 15px;
  border-radius: 0px;
  margin-bottom: 4px;
  margin-left: 2px;
  margin-right: 2px;
  cursor: pointer;
  background: v-bind('`rgba(${modalColor.split("(")[1].split(")")[0]}, 0.5)`');
  transition: all 0.3s ease, border-left-color 0.25s ease, box-shadow 0.25s ease;
  position: relative;
  border-left: 3px solid transparent;
  overflow: hidden;
}

.thread-item:hover {
  background: v-bind('`rgba(${modalColor.split("(")[1].split(")")[0]}, 0.7)`');
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.thread-item.active {
  background: rgba(40, 90, 130, 0.25);
  border-left: 3px solid v-bind(modalActive);
  box-shadow: inset 3px 0 5px -2px v-bind(modalActive);
}

.thread-item.active::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    v-bind('`rgba(${modalActive.split("(")[1].split(")")[0]}, 0.05)`') 0%, 
    transparent 30%);
  pointer-events: none;
}

.thread-item.active .thread-title {
  color: rgba(255, 255, 255, 1);
  font-weight: 500;
}

.thread-item.active .thread-time {
  color: v-bind(modalActive);
}

.thread-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.thread-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 4px;
  font-weight: 500;
  display: flex;
  align-items: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.2px;
}

.active-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  background-color: v-bind(modalActive);
  color: rgba(255, 255, 255, 0.95);
  padding: 1px 6px;
  border-radius: 0px;
  margin-left: 8px;
  font-weight: normal;
  box-shadow: 0 0 8px v-bind('`rgba(${modalActive.split("(")[1].split(")")[0]}, 0.5)`');
  text-shadow: 0 0 5px rgba(0, 0, 0, 0.3);
  height: 18px;
  letter-spacing: 0.5px;
}

.thread-time {
  font-size: 11px;
  color: v-bind('`rgba(${modalActive.split("(")[1].split(")")[0]}, 0.7)`');
  margin-bottom: 4px;
}

.thread-message {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 500px;
}

.thread-actions {
  display: flex;
  gap: 4px;
  opacity: 1;
  transition: opacity 0.2s ease;
}

.thread-item:hover .thread-actions {
  opacity: 1;
}

.thread-action-btn {
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.2s ease;
  border-radius: 0 !important;
  padding: 6px 0;
  font-size: 13px;
  height: 28px;
}

.thread-action-btn:hover {
  color: rgba(255, 255, 255, 0.9);
  background: v-bind('`rgba(${modalActive.split("(")[1].split(")")[0]}, 0.2)`') !important;
}

.thread-item.active .thread-action-btn {
  color: rgba(255, 255, 255, 0.75);
}

.activate-btn:hover {
  color: v-bind(modalActive);
}

.delete-btn:hover {
  color: rgba(255, 100, 100, 0.9);
}

.thread-action-btn.disabled {
  opacity: 0.35;
  cursor: not-allowed;
  pointer-events: none;
}

.thread-action-btn.disabled:hover {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.6);
}

/* 自定义滚动条 */
.thread-modal-body::-webkit-scrollbar {
  width: 4px;
}

.thread-modal-body::-webkit-scrollbar-track {
  background: transparent;
}

.thread-modal-body::-webkit-scrollbar-thumb {
  background: v-bind('`rgba(${modalActive.split("(")[1].split(")")[0]}, 0.3)`');
  border-radius: 2px;
}

.thread-modal-body::-webkit-scrollbar-thumb:hover {
  background: v-bind(modalActive);
}
</style> 