<template>
  <div class="chat-messages-wrapper">
    <!-- 无角色时的空状态提示 -->
    <div v-if="!currentRoleId" class="empty-state">
      <i class="bi bi-people"></i>
      <div class="title">请选择一个角色开始对话</div>
      <div class="subtitle">从左侧列表选择一个角色，开始精彩的角色扮演对话</div>
    </div>
    
    <!-- 有角色但无会话时的空状态提示 -->
    <div v-else-if="!currentThreadId" class="empty-state">
      <i class="bi bi-chat-square-text"></i>
      <div class="title">正在初始化会话</div>
      <div class="subtitle">请稍等片刻，或刷新页面重试</div>
    </div>
    
    <!-- 有会话但无消息时的空状态提示 -->
    <div v-else-if="messages.length === 0" class="empty-state">
      <i class="bi bi-chat-dots"></i>
      <div class="title">暂无消息</div>
      <div class="subtitle">在下方输入框输入内容开始角色扮演对话</div>
    </div>
    
    <!-- 消息列表 -->
    <div v-else class="chat-messages messages-container-fade-in" ref="messagesContainer" :key="`${currentThreadId}`">
      <div v-for="(message, index) in messages" 
           :key="index"
           :class="['message', 'message-hover-effect', message.role === 'user' ? 'user' : 'assistant', { 'editing': message.isEditing }]">
        <div class="message-header">
          <div class="avatar" :class="{ 'no-image': !message.avatarPath, 'avatar-loading': message.isTyping }">
            <img v-if="message.avatarPath" :src="message.avatarPath" :alt="message.name">
            <i v-else class="bi bi-person"></i>
          </div>
        </div>
        <div class="message-content">
          <div class="name">
            {{ message.name }}
            <span class="time">{{ formatTime(message.createTime) }}</span>
            <span v-if="message.isTyping" class="typing">正在输入<span class="typing-dots"><span>.</span><span>.</span><span>.</span></span></span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { marked } from 'marked'
import { useThemeStore } from '../stores/theme'

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
  currentRoleId: string | null
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

const handleDelete = (message: ChatMessage) => {
  if (!message.id) return
  if (!confirm('确定要删除这条消息吗？此操作不可恢复。')) return
  emit('messageRemove', message.id)
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

// 自动滚动到底部
onMounted(() => {
  nextTick(() => {
    const container = messagesContainer.value
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
})
</script>

<style scoped>
/* 消息容器包装器 */
.chat-messages-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 空状态提示 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  height: 100%;
  padding: 20px;
  background: rgba(0, 0, 0, 0.2);
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 15px;
  color: rgba(255, 255, 255, 0.5);
}

.empty-state .title {
  font-size: 18px;
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.8);
}

.empty-state .subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  max-width: 300px;
  line-height: 1.4;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
}

/* 消息项 */
.message {
  display: flex;
  position: relative;
  padding: 15px;
  border-radius: 0;
  transition: background-color 0.3s ease;
}

.message.user {
  background-color: rgba(0, 0, 0, 0.1);
}

.message.assistant {
  background-color: rgba(50, 50, 60, 0.1);
}

.message-header {
  margin-right: 12px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar.no-image i {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
}

.avatar-loading {
  filter: grayscale(100%);
}

.message-content {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 5px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.time {
  margin-left: 8px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: normal;
}

.typing {
  margin-left: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: normal;
  display: flex;
  align-items: center;
}

.typing-dots span {
  animation: typingDot 1.4s infinite;
  animation-fill-mode: both;
  margin-left: 2px;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingDot {
  0% { opacity: 0.2; }
  20% { opacity: 1; }
  100% { opacity: 0.2; }
}

.text {
  font-size: 14px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.9);
  white-space: pre-wrap;
  word-break: break-word;
}

/* Markdown 内容样式 */
.text :deep(p) {
  margin-top: 0;
  margin-bottom: 10px;
}

.text :deep(ul), .text :deep(ol) {
  margin-top: 0;
  margin-bottom: 10px;
  padding-left: 20px;
}

.text :deep(pre) {
  background: rgba(0, 0, 0, 0.2);
  padding: 10px;
  border-radius: 0;
  overflow-x: auto;
  margin: 10px 0;
}

.text :deep(code) {
  font-family: 'Courier New', Courier, monospace;
  padding: 2px 4px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.text :deep(pre code) {
  padding: 0;
  background: transparent;
  border-radius: 0;
}

.text :deep(a) {
  color: #4dabf7;
  text-decoration: none;
}

.text :deep(a:hover) {
  text-decoration: underline;
}

.text :deep(blockquote) {
  border-left: 3px solid rgba(255, 255, 255, 0.2);
  margin: 10px 0;
  padding-left: 10px;
  color: rgba(255, 255, 255, 0.7);
}

.text :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
}

.text :deep(th), .text :deep(td) {
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 8px;
  text-align: left;
}

.text :deep(th) {
  background: rgba(0, 0, 0, 0.3);
}

/* 可编辑消息 */
.editable-content {
  width: 100%;
  min-height: 60px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 0;
  color: white;
  resize: vertical;
  font-size: 14px;
  line-height: 1.5;
  font-family: inherit;
}

.editable-content:focus {
  outline: none;
  border-color: rgba(255, 255, 255, 0.4);
}

/* 消息操作按钮 */
.message-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 5px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.message-hover-effect:hover .message-actions {
  opacity: 1;
}

.message.editing .message-actions {
  opacity: 1;
}

.message-actions button {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.message-actions button:hover {
  background: rgba(0, 0, 0, 0.5);
  color: white;
}

.message-actions button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.message-edit-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
}

.message-delete-btn:hover {
  background: rgba(255, 0, 0, 0.2) !important;
  border-color: rgba(255, 0, 0, 0.4);
}

.message-confirm-btn:hover {
  background: rgba(0, 255, 0, 0.1) !important;
  border-color: rgba(0, 255, 0, 0.3);
}

.message-cancel-btn:hover {
  background: rgba(255, 0, 0, 0.1) !important;
  border-color: rgba(255, 0, 0, 0.3);
}

.message-regenerate-btn:hover {
  background: rgba(0, 100, 255, 0.2) !important;
  border-color: rgba(0, 100, 255, 0.4);
}

/* 自定义滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 5px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2.5px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style> 