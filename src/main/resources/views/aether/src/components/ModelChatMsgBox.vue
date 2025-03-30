<!-- 聊天消息组件 -->
<template>
  <div class="chat-messages-wrapper">
    <!-- 无会话时的空状态提示 -->
    <div v-if="!currentThreadId" class="empty-state">
      <i class="bi bi-chat-square-text"></i>
      <div class="title">请选择一个会话开始对话</div>
      <div class="subtitle">从左侧列表选择一个会话，开始精彩的对话之旅</div>
    </div>
    
    <!-- 有会话但无消息时的空状态提示 -->
    <div v-else-if="messages.length === 0" class="empty-state">
      <i class="bi bi-chat-dots"></i>
      <div class="title">暂无消息</div>
      <div class="subtitle">在下方输入框输入内容开始对话</div>
    </div>
    
    <!-- 消息列表 -->
    <div v-else class="chat-messages messages-container-fade-in" ref="messagesContainer" :key="`${currentThreadId}`">
      <div v-for="(message, index) in messages" 
           :key="index"
           :class="['message', 'message-hover-effect', message.role === 'user' ? 'user' : 'assistant', { 'editing': message.isEditing }]">
        <div class="message-header">
          <div class="avatar" :class="{ 'no-image': !message.avatarPath }">
            <img v-if="message.avatarPath" :src="message.avatarPath" :alt="message.name">
            <i v-else class="bi bi-person"></i>
          </div>
        </div>
        <div class="message-content">
          <div class="name">
            {{ message.name }}
            <span class="time">{{ formatTime(message.createTime) }}</span>
          </div>
          <div v-if="!message.isEditing">
            <div class="text" v-if="message.role === 'user'">{{ message.content }}</div>
            <div class="text" v-else v-html="renderMarkdown(message.content)"></div>
          </div>
          <div v-else>
            <textarea class="editable-content" 
                      v-model="message.editContent" 
                      ref="editTextarea"
                      @input="(event) => adjustEditTextareaHeight(event.target as HTMLTextAreaElement, message)"></textarea>
          </div>
        </div>
        <div class="message-actions">
          <template v-if="!message.isEditing">
            <button v-if="message.role === 'assistant' && index === messages.length - 1" 
                    class="message-regenerate-btn" 
                    @click="handleRegenerate(message)"
                    title="重新生成">
              <i class="bi bi-arrow-repeat"></i>
            </button>
            <button class="message-edit-btn" 
                    @click="handleEdit(message)"
                    title="编辑消息">
              <i class="bi bi-pencil"></i>
            </button>
            <button class="message-delete-btn" 
                    @click="handleDelete(message)"
                    title="删除消息">
              <i class="bi bi-trash"></i>
            </button>
          </template>
          <template v-else>
            <button class="message-confirm-btn" 
                    @click="handleConfirmEdit(message)"
                    :disabled="isEditing"
                    title="确认编辑">
              <i class="bi bi-check-lg"></i>
            </button>
            <button class="message-cancel-btn" 
                    @click="handleCancelEdit(message)"
                    :disabled="isEditing"
                    title="取消编辑">
              <i class="bi bi-x-lg"></i>
            </button>
          </template>
        </div>
      </div>
    </div>
    
    <!-- 确认删除对话框 -->
    <ConfirmModal ref="confirmModal" />
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { marked } from 'marked'
import { useThemeStore } from '../stores/theme'
import ConfirmModal from './ConfirmModal.vue'

// 初始化主题
const themeStore = useThemeStore()

// 定义消息类型接口
interface ChatMessage {
  id?: string;
  role: 'user' | 'assistant';
  content: string;
  name: string;
  avatarPath?: string;
  createTime: string;
  isEditing?: boolean;
  editContent?: string;
  isTyping?: boolean;
  hasReceivedData?: boolean;
}

// Props
const props = defineProps<{
  messages: ChatMessage[]
  currentThreadId: string | null
  isEditing: boolean
}>()

// Emits
const emit = defineEmits<{
  (e: 'messageEdit', historyId: string, content: string): void
  (e: 'messageRemove', historyId: string): void
  (e: 'regenerate', message: ChatMessage): void
  (e: 'scrollToBottom'): void
}>()

// Refs
const messagesContainer = ref<HTMLDivElement | null>(null)
const editTextarea = ref<HTMLTextAreaElement[] | null>(null)
const confirmModal = ref<any>(null)

// Methods
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
    const seconds = date.getSeconds().toString().padStart(2, '0')
    
    return `${year}年${month}月${day}日 ${hours}:${minutes}:${seconds}`
  } catch (e) {
    console.error('时间格式化错误:', e)
    return ''
  }
}

const renderMarkdown = (content: string) => {
  if (!content) return ''
  return marked.parse(content) as string
}

const handleEdit = (message: ChatMessage) => {
  message.isEditing = true
  message.editContent = message.content
  nextTick(() => {
    if (editTextarea.value && editTextarea.value.length > 0) {
      const textarea = editTextarea.value[0]
      textarea.focus()
      adjustEditTextareaHeight(textarea, message)
      
      const lastMessage = props.messages[props.messages.length - 1]
      if (lastMessage && lastMessage.id === message.id) {
        emit('scrollToBottom')
      }
    }
  })
}

const handleConfirmEdit = (message: ChatMessage) => {
  if (!message.editContent || message.editContent.trim() === '') {
    alert('消息内容不能为空')
    return
  }
  
  if (message.content === message.editContent) {
    message.isEditing = false
    return
  }

  if (message.id) {
    emit('messageEdit', message.id, message.editContent)
  }
}

const handleCancelEdit = (message: ChatMessage) => {
  message.editContent = message.content
  message.isEditing = false
}

const handleDelete = async (message: ChatMessage) => {
  if (!message.id) {
    return
  }
  
  const confirmed = await confirmModal.value.showConfirm({
    title: '删除消息',
    content: '确定要删除这条消息吗？此操作不可恢复。',
    confirmText: '删除',
    cancelText: '取消'
  })
  
  if (confirmed) {
    emit('messageRemove', message.id)
  }
}

const handleRegenerate = (message: ChatMessage) => {
  emit('regenerate', message)
}

const adjustEditTextareaHeight = (textarea: HTMLTextAreaElement | null, message: ChatMessage) => {
  if (!textarea) return
  
  // 创建一个临时元素来计算原始内容的高度
  const tempDiv = document.createElement('div')
  tempDiv.className = 'text'
  tempDiv.style.position = 'absolute'
  tempDiv.style.visibility = 'hidden'
  tempDiv.style.width = textarea.offsetWidth + 'px'
  tempDiv.style.fontSize = '14px'
  tempDiv.style.lineHeight = '1.5'
  tempDiv.style.padding = '8px'
  
  // 根据消息类型设置内容
  if (message.role === 'user') {
    tempDiv.textContent = message.content
  } else {
    tempDiv.innerHTML = renderMarkdown(message.content)
  }
  
  document.body.appendChild(tempDiv)
  
  // 获取内容高度并设置编辑框高度
  const contentHeight = tempDiv.offsetHeight
  const minHeight = 60 // 最小高度
  const maxHeight = 300 // 最大高度
  
  // 设置高度，但限制最大高度
  textarea.style.height = Math.min(Math.max(contentHeight, minHeight), maxHeight) + 'px'
  
  // 移除临时元素
  document.body.removeChild(tempDiv)
}

// 暴露方法给父组件
defineExpose({
  scrollToBottom: () => {
    nextTick(() => {
      const container = messagesContainer.value
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    })
  }
})
</script>

<style scoped>
/* 消息容器包装器 */
.chat-messages-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  position: relative;
  overflow-x: hidden;
  width: 100%;
  max-width: 100%;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 16px 12px 25px; /* 减少左侧内边距 */
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-height: 0;
  width: 100%; /* 使用完整宽度 */
  max-width: 100%;
  box-sizing: border-box;
}

/* 自定义滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  width: 100%;
  max-width: 400px;
  padding: 0 20px;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
  opacity: 0.8;
}

.empty-state .title {
  font-size: 18px;
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.empty-state .subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.5;
}

.message {
  width: 100%; /* 使用完整宽度 */
  padding: 6px 16px 6px 2px; /* 减少上下内边距，缩小左侧边距 */
  line-height: 1.5;
  font-size: 14px;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  position: relative; /* 确保伪元素定位正确 */
  box-sizing: border-box;
  display: flex;
  gap: 8px; /* 减少头像与内容间的间距 */
  transition: background-color 0.5s ease;
  border-radius: 6px;
  background-color: transparent;
  margin: 0; /* 移除外边距 */
}

.message .message-header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.message .message-header .avatar {
  width: 28px; /* 减小头像尺寸 */
  height: 28px; /* 减小头像尺寸 */
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message .message-header .avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message .message-header .avatar.no-image {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.message .message-content {
  flex: 1;
  min-width: 0;
  /*padding-right: 60px;*/ /* 减少右侧内边距，为按钮留出空间 */
}

.message .message-content .name {
  font-size: 13px; /* 减小名称字体大小 */
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 2px; /* 减少名称与内容的间距 */
  display: flex;
  align-items: center;
  gap: 8px;
}

.message .message-content .name .time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.message .message-content .text {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
}

.message .message-actions {
  position: absolute;
  right: 8px; /* 减少右侧距离，防止按钮超出容器 */
  top: 8px;
  display: none;
  z-index: 10; /* 确保按钮位于最上层 */
}

/* 编辑模式下总是显示操作按钮 */
.message.editing .message-actions {
  display: flex;
  gap: 4px;
}

.message:hover .message-actions {
  display: flex;
  gap: 4px;
}

.message .message-delete-btn,
.message .message-edit-btn,
.message .message-confirm-btn,
.message .message-cancel-btn,
.message .message-regenerate-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  padding: 4px;
  font-size: 16px;
  line-height: 1;
  border-radius: 4px;
  transition: all 0.2s;
}

.message .message-delete-btn:hover,
.message .message-edit-btn:hover,
.message .message-confirm-btn:hover,
.message .message-cancel-btn:hover,
.message .message-regenerate-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.message .message-confirm-btn:disabled,
.message .message-cancel-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.message .message-confirm-btn {
  color: rgba(75, 210, 143, 0.7);
}

.message .message-confirm-btn:hover {
  color: rgba(75, 210, 143, 1);
}

.message .message-cancel-btn {
  color: rgba(255, 107, 107, 0.7);
}

.message .message-cancel-btn:hover {
  color: rgba(255, 107, 107, 1);
}

.message .message-regenerate-btn {
  color: rgba(79, 172, 254, 0.7);
}

.message .message-regenerate-btn:hover {
  color: rgba(79, 172, 254, 1);
}

.message .editable-content {
  width: 100%;
  min-height: 60px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: #fff;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  font-family: inherit;
  overflow-y: auto;
}

.message .editable-content:focus {
  outline: none;
  border-color: rgba(79, 172, 254, 0.5);
  background: rgba(255, 255, 255, 0.15);
}

/* 鼠标悬浮效果 - 使用伪元素扩展背景 */
.message.message-hover-effect.user:hover::before,
.message.message-hover-effect.assistant:hover::before {
  content: '';
  position: absolute;
  top: 0;
  left: -10px; /* 减小向左延伸的宽度，避免触发水平滚动 */
  right: 0;
  bottom: 0;
  border-radius: 6px;
  z-index: -1;
}

.message.message-hover-effect.user:hover::before {
  background-color: v-bind('themeStore.getMessageHoverUser()');
}

.message.message-hover-effect.assistant:hover::before {
  background-color: v-bind('themeStore.getMessageHoverModel()');
}

/* 移除原有的背景色设置 */
.message.message-hover-effect.user:hover {
  background-color: transparent;
}

.message.message-hover-effect.assistant:hover {
  background-color: transparent;
}

/* 添加角色类型特定样式 */
.message.user {
  background: transparent;
}

.message.assistant {
  background: transparent;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .chat-messages {
    /*padding: 12px 12px 12px 6px;*/
  }
  
  .message {
    font-size: 13px;
    padding: 6px 12px 6px 2px;
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  .chat-messages {
    padding: 8px 8px 8px 4px;
    gap: 1px;
  }
  
  .message {
    font-size: 12px;
    padding: 4px 10px 4px 1px;
  }
  
  .message .message-header .avatar {
    width: 24px;
    height: 24px;
  }
}
</style> 