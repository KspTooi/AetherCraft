<template>
  <div class="chat-container">
    <div class="chat-layout">
      <!-- 移动端菜单按钮 -->
      <button class="mobile-menu-btn" @click="toggleThreadList">
        <i class="bi bi-chat-left-text"></i>
      </button>
      
      <!-- 遮罩层 -->
      <div class="thread-list-mask" :class="{ show: isMobileMenuOpen }" @click="toggleThreadList"></div>
      
      <!-- 会话列表组件 -->
      <ModelChatThreadList 
        ref="threadListRef" 
        :currentThreadId="currentThreadId" 
        :isMobileMenuOpen="isMobileMenuOpen"
        @threadChecked="handleThreadChecked"
        @threadEdit="handleThreadEdit"
        @threadRemove="handleThreadRemove"
        @createNewThread="createNewThread" />

      <!-- 主聊天区域 -->
      <div class="chat-main">
        <div class="model-select">
          <ModelSeriesSelector v-model="selectedModel" />
        </div>
        
        <!-- 聊天消息区域 -->
        <div class="chat-messages-wrapper">
          <!-- 空状态提示 -->
          <div v-if="!currentThreadId" class="empty-state">
            <i class="bi bi-chat-square-text"></i>
            <div class="title">请选择一个会话开始对话</div>
            <div class="subtitle">从左侧列表选择一个会话，开始精彩的对话之旅</div>
          </div>
          
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
                            ref="editTextarea"></textarea>
                </div>
              </div>
              <div class="message-actions">
                <template v-if="!message.isEditing">
                  <button v-if="message.role === 'assistant' && index === messages.length - 1" 
                          class="message-regenerate-btn" 
                          @click="regenerateAIResponse(message)"
                          title="重新生成">
                    <i class="bi bi-arrow-repeat"></i>
                  </button>
                  <button class="message-edit-btn" 
                          @click="editMessage(message)"
                          title="编辑消息">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="message-delete-btn" 
                          @click="deleteMessage(message.id)"
                          title="删除消息">
                    <i class="bi bi-trash"></i>
                  </button>
                </template>
                <template v-else>
                  <button class="message-confirm-btn" 
                          @click="confirmEdit(message)"
                          :disabled="isEditing"
                          title="确认编辑">
                    <i class="bi bi-check-lg"></i>
                  </button>
                  <button class="message-cancel-btn" 
                          @click="cancelEdit(message)"
                          :disabled="isEditing"
                          title="取消编辑">
                    <i class="bi bi-x-lg"></i>
                  </button>
                </template>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 聊天输入区域 -->
        <div class="chat-input">
          <div class="chat-input-wrapper">
            <textarea 
              v-model="messageInput"
              ref="messageTextarea"
              placeholder="怎麽不问问神奇的 Gemini 呢?"
              @keydown="handleKeyPress"
              @input="adjustTextareaHeight"></textarea>
          </div>
          <button v-if="!isLoading" class="send-btn" 
                  @click="sendMessage"
                  :disabled="!messageInput.trim()">
            发送
          </button>
          <button v-else class="stop-btn" 
                  @click="terminateAIResponse">
            停止生成
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import GlassBox from "@/components/GlassBox.vue"
import ModelSeriesSelector from "@/components/ModelSeriesSelector.vue"
import ModelChatThreadList from "@/components/ModelChatThreadList.vue"
import axios from 'axios'
import { marked } from 'marked'

// 定义消息类型接口
interface ChatMessage {
  id?: string; // historyId，用户发送的临时消息可能没有
  role: 'user' | 'assistant';
  content: string;
  name: string;
  avatarPath?: string;
  createTime: string;
  isEditing?: boolean;
  editContent?: string;
  isTyping?: boolean; // 用于AI助手输入状态
  hasReceivedData?: boolean; // 用于AI助手输入状态
}

// 状态定义
const messages = ref<ChatMessage[]>([])
const selectedModel = ref('')
const messageInput = ref('')
const isLoading = ref(false)
const currentThreadId = ref<string | null>(null)
const isMobileMenuOpen = ref(false)
const isEditing = ref(false)
const refreshThreadListTimer = ref<number | null>(null) // 明确类型
const messagesContainer = ref<HTMLDivElement | null>(null) // 明确类型
const messageTextarea = ref<HTMLTextAreaElement | null>(null) // 明确类型
const editTextarea = ref<HTMLTextAreaElement[] | null>(null) // 注意这是数组
const threadListRef = ref<InstanceType<typeof ModelChatThreadList> | null>(null)

// 工具函数
const throttle = (fn: Function, delay: number) => {
  let timer: any = null
  return function(...args: any[]) {
    if(timer) {
      return
    }
    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}

// 加载会话列表
const loadThreadList = async () => {
  try {
    const response = await axios.post('/model/chat/getThreadList')
    
    if (response.data.code === 0) {
      // threads.value = response.data.data || []
      
      if (!currentThreadId.value && response.data.data.length === 0) {
        resetChatState()
      }
    } else {
      showToast("danger", response.data.message || "加载会话列表失败")
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    showToast("danger", "加载会话列表失败，请检查网络连接")
  }
}

// 使用节流包装loadThreadList
const throttledLoadThreadList = throttle(loadThreadList, 3000)

// 重置聊天状态
const resetChatState = () => {
  currentThreadId.value = null
  messages.value = []
  messageInput.value = ''
  isLoading.value = false
}

// 方法定义
const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// 声明 showToast 函数类型 (根据实际情况调整)
declare function showToast(type: string, message: string): void;

const adjustTextareaHeight = () => {
  const textarea = messageTextarea.value // 类型已明确
  if (!textarea) return
  
  textarea.style.height = '40px'
  const scrollHeight = textarea.scrollHeight

  if (scrollHeight <= 40 || !messageInput.value.trim()) {
    textarea.style.height = '40px'
    textarea.style.overflowY = 'hidden'
    return
  }

  if (scrollHeight > 120) {
    textarea.style.height = '120px'
    textarea.style.overflowY = 'auto'
  } else {
    textarea.style.height = scrollHeight + 'px'
    textarea.style.overflowY = 'hidden'
  }
}

const sendMessage = async () => {
  if (!messageInput.value.trim() || isLoading.value) return

  const userMessage = messageInput.value.trim()
  messageInput.value = ''
  await nextTick()
  adjustTextareaHeight()

  // 添加用户消息到列表
  const tempUserMessage: ChatMessage = {
    role: 'user',
    content: userMessage,
    name: '用户',
    createTime: new Date().toISOString()
  }
  
  messages.value.push(tempUserMessage)

  isLoading.value = true
  
  try {
    const response = await axios.post('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      model: selectedModel.value,
      message: userMessage,
      queryKind: 0
    })
    
    if (response.data.code === 0) {
      const segment = response.data.data
      if (segment && segment.historyId) {
        tempUserMessage.id = segment.historyId
      }
      if (segment && segment.name) {
        tempUserMessage.name = segment.name
      }
      if (segment && segment.avatarPath) {
        tempUserMessage.avatarPath = segment.avatarPath
      }
      
      messages.value = [...messages.value]
      
      const assistantMessage: ChatMessage = {
        role: 'assistant',
        content: '正在输入...',
        name: 'AI助手',
        createTime: new Date().toISOString(),
        isTyping: true,
        hasReceivedData: false
      }
      
      messages.value.push(assistantMessage)
      
      scrollToBottom()
      
      await pollAIResponse(assistantMessage)
    } else {
      showToast("danger", response.data.message || "发送消息失败")
      isLoading.value = false
    }
  } catch (error) {
    console.error('发送消息错误:', error)
    showToast("danger", "发送消息失败，请检查网络连接")
    isLoading.value = false
  }
}

const pollAIResponse = async (assistantMessage: any) => {
  while (isLoading.value) {
    try {
      const response = await axios.post('/model/chat/completeBatch', {
        threadId: currentThreadId.value,
        queryKind: 1
      })
      
      if (response.data.code === 0) {
        const segment = response.data.data
        
        if (!segment) {
          await new Promise(resolve => setTimeout(resolve, 500))
          continue
        }
        
        if (segment.type === 1) {
          // 数据片段
          // 如果是第一个数据片段，清除"正在输入..."文本
          if (!assistantMessage.hasReceivedData) {
            assistantMessage.content = ''
            assistantMessage.hasReceivedData = true
            assistantMessage.isTyping = false
          }
          assistantMessage.content += segment.content
          
          // 更新名称和头像（如果后端返回了这些信息）
          if (segment.name) {
            assistantMessage.name = segment.name
          }
          if (segment.avatarPath) {
            assistantMessage.avatarPath = segment.avatarPath
          }
          
          // 触发视图更新
          messages.value = [...messages.value]
          scrollToBottom()
        } else if (segment.type === 2) {
          // 结束片段
          // 如果是第一个数据片段，清除"正在输入..."文本
          if (!assistantMessage.hasReceivedData) {
            assistantMessage.content = ''
            assistantMessage.hasReceivedData = true
            assistantMessage.isTyping = false
          }
          
          if (segment.content) {
            assistantMessage.content += segment.content
          }
          
          // 更新historyId
          if (segment.historyId) {
            assistantMessage.id = segment.historyId
          }
          
          // 更新名称和头像（如果后端返回了这些信息）
          if (segment.name) {
            assistantMessage.name = segment.name
          }
          if (segment.avatarPath) {
            assistantMessage.avatarPath = segment.avatarPath
          }
          
          // 触发视图更新
          messages.value = [...messages.value]
          scrollToBottom()
          isLoading.value = false

          // 清除之前的定时器
          if(refreshThreadListTimer.value !== null) {
            clearTimeout(refreshThreadListTimer.value)
          }
          
          // 3秒后刷新会话列表
          refreshThreadListTimer.value = setTimeout(() => {
            throttledLoadThreadList()
          }, 3000)
          
          break
        } else if (segment.type === 10) {
          // 错误片段
          assistantMessage.content = segment.content || "AI响应出错"
          assistantMessage.hasReceivedData = true
          assistantMessage.isTyping = false
          
          // 更新名称和头像（如果后端返回了这些信息）
          if (segment.name) {
            assistantMessage.name = segment.name
          }
          if (segment.avatarPath) {
            assistantMessage.avatarPath = segment.avatarPath
          }
          
          // 触发视图更新
          messages.value = [...messages.value]
          scrollToBottom()
          isLoading.value = false
          
          // 显示错误提示
          showToast("danger", "生成失败: " + assistantMessage.content)
          break
        }
      }
    } catch (error) {
      console.error('获取AI响应错误:', error)
      assistantMessage.content = "获取AI响应出错，请重试"
      assistantMessage.hasReceivedData = true
      assistantMessage.isTyping = false
      
      // 触发视图更新
      messages.value = [...messages.value]
      scrollToBottom()
      isLoading.value = false
      
      // 显示错误提示
      showToast("danger", "网络错误，请检查连接")
      break
    }
    
    await new Promise(resolve => setTimeout(resolve, 500))
  }
}

const terminateAIResponse = async () => {
  if (!isLoading.value) return
  
  try {
    const response = await axios.post('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      queryKind: 2
    })
    
    if (response.data.code === 0) {
      showToast("success", "已停止生成")
      isLoading.value = false
    }
  } catch (error) {
    console.error('停止生成错误:', error)
    showToast("danger", "停止生成失败，请检查网络连接")
  }
}

const renderMarkdown = (content: string) => {
  if (!content) return ''
  return marked.parse(content)
}

const scrollToBottom = () => {
  nextTick(() => {
    const container = messagesContainer.value // 类型已明确
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

const toggleThreadList = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const createNewThread = async () => {
  try {
    const response = await axios.post('/model/chat/createEmptyThread', {
      model: selectedModel.value
    })
    if (response.data.code === 0) {
      const newThreadId = response.data.data.threadId
      await loadThread(newThreadId)
      await threadListRef.value?.loadThreadList()
      isMobileMenuOpen.value = false
      showToast("success", "新会话创建成功")
    } else {
      showToast("danger", response.data.message || "创建会话失败")
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    showToast("danger", "创建会话失败，请检查网络连接")
  }
}

const loadThread = async (threadId: string) => {
  try {
    const response = await axios.post('/model/chat/recoverChat', {
      threadId: threadId
    })
    if (response.data.code === 0) {
      const data = response.data.data
      currentThreadId.value = threadId
      localStorage.setItem('lastThreadId', threadId)
      if (data.modelCode) {
        nextTick(() => {
          selectedModel.value = data.modelCode
        })
      }
      messages.value = data.messages.map((msg: any): ChatMessage => ({
        id: msg.id,
        role: msg.role === 0 ? 'user' : 'assistant',
        content: msg.content,
        name: msg.name || (msg.role === 0 ? '用户' : 'AI助手'),
        avatarPath: msg.avatarPath,
        createTime: msg.createTime,
        isEditing: false,
        editContent: msg.content
      }))
      nextTick(() => {
        scrollToBottom()
      })
      isMobileMenuOpen.value = false
    } else {
      showToast("danger", response.data.message || "加载会话失败")
    }
  } catch (error) {
    console.error('加载会话失败:', error)
    showToast("danger", "加载会话失败，请检查网络连接")
  }
}

const editThreadTitle = async (thread: any) => {
  const newTitle = prompt('请输入新的会话标题:', thread.title)
  if (!newTitle || newTitle.trim() === thread.title) return
  try {
    const response = await axios.post('/model/chat/editThread', {
      threadId: thread.id,
      title: newTitle.trim()
    })
    if (response.data.code === 0) {
      await threadListRef.value?.loadThreadList()
      showToast("success", "会话标题已更新")
    } else {
      showToast("danger", response.data.message || "更新会话标题失败")
    }
  } catch (error) {
    console.error('更新会话标题失败:', error)
    showToast("danger", "更新会话标题失败，请检查网络连接")
  }
}

const deleteThread = async (threadId: string) => {
  if (!confirm('确定要删除这个会话吗？此操作不可恢复。')) {
    return
  }
  try {
    const response = await axios.post('/model/chat/removeThread', {
      threadId: threadId
    })
    if (response.data.code === 0) {
      if (String(threadId) === String(currentThreadId.value)) {
        resetChatState()
      }
      await threadListRef.value?.loadThreadList()
      showToast("success", "会话已删除")
    } else {
      showToast("danger", response.data.message || "删除会话失败")
    }
  } catch (error: any) {
    console.error('删除会话失败:', error)
    showToast("danger", "删除会话失败，请检查网络连接")
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
    const seconds = date.getSeconds().toString().padStart(2, '0')
    
    return `${year}年${month}月${day}日 ${hours}:${minutes}:${seconds}`
  } catch (e) {
    console.error('时间格式化错误:', e)
    return ''
  }
}

const editMessage = (message: ChatMessage) => {
  message.isEditing = true
  message.editContent = message.content
  nextTick(() => {
    if (editTextarea.value && editTextarea.value.length > 0) {
      // 假设我们总是操作第一个编辑框（或根据逻辑选择）
      const textarea = editTextarea.value[0]
      textarea.focus()
      adjustEditTextareaHeight(textarea, message) // 传递 textarea
      
      const lastMessage = messages.value[messages.value.length - 1]
      if (lastMessage && lastMessage.id === message.id) {
        scrollToBottom()
      }
    }
  })
}

const confirmEdit = (message: ChatMessage) => {
  if (!message.editContent || message.editContent.trim() === '') {
    alert('消息内容不能为空')
    return
  }
  
  if (message.content === message.editContent) {
    message.isEditing = false
    return
  }
  
  // 调用后端API保存编辑后的消息
  axios.post('/model/chat/editHistory', {
    historyId: message.id,
    content: message.editContent
  }).then(response => {
    if (response.data.code === 0) {
      // 更新本地消息内容
      message.content = message.editContent
      message.isEditing = false
      showToast("success", "消息编辑成功")
    } else {
      showToast("danger", response.data.message || '更新失败')
      console.error('更新消息失败:', response.data)
    }
  }).catch(error => {
    console.error('更新消息失败:', error)
    showToast("danger", '更新失败，请稍后重试')
  })
}

const cancelEdit = (message: ChatMessage) => {
  // 取消编辑，恢复原始内容
  message.editContent = message.content
  message.isEditing = false
}

const deleteMessage = async (messageId: string) => {
  if (!confirm('确定要删除这条消息吗？此操作不可恢复。')) {
    return
  }
  
  try {
    const response = await axios.post('/model/chat/removeHistory', {
      threadId: currentThreadId.value,
      historyId: messageId
    })
    
    if (response.data.code === 0) {
      // 从本地消息列表中移除该消息
      const index = messages.value.findIndex(msg => msg.id === messageId)
      if (index !== -1) {
        messages.value.splice(index, 1)
      }
      showToast("success", "消息已删除")
    } else {
      showToast("danger", response.data.message || "删除消息失败")
    }
  } catch (error: any) {
    console.error('删除消息失败:', error)
    showToast("danger", "删除消息失败，请检查网络连接")
  }
}

const adjustEditTextareaHeight = (textarea: HTMLTextAreaElement, message: ChatMessage) => {
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
    tempDiv.innerHTML = renderMarkdown(message.content ?? '')
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

const regenerateAIResponse = async (message: ChatMessage) => {
  if (isLoading.value) return
  
  // 验证threadId是否存在
  if (!currentThreadId.value) {
    showToast("danger", "会话ID不存在，无法重新生成")
    return
  }
  
  // 设置加载状态
  isLoading.value = true
  
  try {
    // 发送重新生成请求
    const response = await axios.post('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      model: selectedModel.value,
      queryKind: 3 // 3:重新生成最后一条AI消息
    })
    
    if (response.data.code === 0) {
      // 找到当前AI消息的索引
      const currentMsgIndex = messages.value.findIndex(m => m.id === message.id)
      
      // 创建新的助手消息对象
      const assistantMessage: ChatMessage = {
        id: message.id,
        role: 'assistant',
        content: '正在重新生成...',
        name: message.name || 'AI助手',
        avatarPath: message.avatarPath,
        createTime: new Date().toISOString(),
        isTyping: true,
        hasReceivedData: false
      }
      
      // 仅替换当前AI消息，保留其他所有消息
      if (currentMsgIndex !== -1) {
        messages.value.splice(currentMsgIndex, 1, assistantMessage)
        // 确保Vue能够检测到变化
        messages.value = [...messages.value]
      } else {
        // 如果找不到原消息，则添加到消息列表末尾
        messages.value.push(assistantMessage)
      }
      
      // 滚动到底部
      scrollToBottom()
      
      // 开始轮询获取AI响应
      await pollAIResponse(assistantMessage)
    } else {
      // 重新生成失败，显示错误信息
      showToast("danger", response.data.message || '重新生成失败')
      isLoading.value = false
    }
  } catch (error) {
    console.error('重新生成错误:', error)
    showToast("danger", '重新生成失败: ' + (error.response?.data?.message || error.message))
    isLoading.value = false
  }
}

// 事件处理
const handleThreadChecked = (threadId: string) => {
  loadThread(threadId)
}

const handleThreadEdit = (thread: any) => {
  editThreadTitle(thread)
}

const handleThreadRemove = (threadId: string) => {
  deleteThread(threadId)
}

// 生命周期钩子
onMounted(async () => {
  adjustTextareaHeight()
  
  // 调用子组件的加载方法
  await threadListRef.value?.loadThreadList()
})

// 组件销毁前清理定时器
onUnmounted(() => {
  if(refreshThreadListTimer.value !== null) { // 明确检查 null
    clearTimeout(refreshThreadListTimer.value)
  }
})
</script>

<style scoped>
/* 移除主要容器的圆角 */
.chat-container {
  color: #fff;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: 0 !important;
  position: relative;
}

.chat-layout {
  display: flex;
  height: 100%;
  min-height: 0;
  border-radius: 0 !important;
  position: relative;
  overflow: hidden;
}

/* 主聊天区域 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: transparent;
  overflow: hidden;
  border-radius: 0 !important;
}

/* 模型选择区域 */
.model-select {
  padding: 8px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 0 !important;
}

.model-select label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  white-space: nowrap;
}

.model-select select {
  flex: 1;
  padding: 6px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.model-select select:hover {
  background: rgba(255, 255, 255, 0.15);
}

.model-select select:focus {
  outline: none;
  border-color: rgba(255, 255, 255, 0.3);
}

.model-select select option {
  background: #2c2c2c;
  color: #fff;
}

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
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 0;
  width: 100%;
  max-width: 100%;
}

.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
}

.empty-state .title {
  font-size: 18px;
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.8);
}

.empty-state .subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

.message {
  width: 100%;
  padding: 8px 16px;
  line-height: 1.5;
  font-size: 14px;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  position: relative;
  box-sizing: border-box;
  display: flex;
  gap: 12px;
  transition: background-color 0.5s ease;
  border-radius: 6px;
  background-color: transparent;
}

.message .message-header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.message .message-header .avatar {
  width: 32px;
  height: 32px;
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
}

.message .message-content .name {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 4px;
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
  right: 12px;
  top: 8px;
  display: none;
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

/* 输入区域样式 */
.chat-input {
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  gap: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-height: 48px;
  max-height: 150px;
  padding: 4px;
}

.chat-input textarea {
  flex: 1;
  width: 100%;
  min-height: 40px;
  padding: 8px 12px;
  background: transparent;
  border: none;
  color: #fff;
  resize: none;
  font-size: 15px;
  line-height: 1.5;
  transition: all 0.3s;
  overflow-y: hidden;
  max-height: 120px;
  display: block;
  margin: 0;
  box-sizing: border-box;
}

.chat-input textarea::placeholder {
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.5;
  padding: 0;
  margin: 0;
}

.chat-input textarea:focus {
  outline: none;
}

.chat-input-wrapper:focus-within {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.15);
}

.send-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 24px;
  background: linear-gradient(135deg, #4facfe 0%, #00ffb8c4 100%);
  color: white;
  cursor: pointer;
  transition: all 0.3s;
  align-self: center;
  font-size: 15px;
  height: 48px;
}

.send-btn:hover {
  background: rgba(79, 172, 254, 0.8);
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.stop-btn {
  min-width: 100px;
  height: 40px;
  border: none;
  border-radius: 8px;
  background: rgba(254, 79, 79, 0.6);
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 10px;
}

.stop-btn:hover {
  background: rgba(254, 79, 79, 0.8);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .mobile-menu-btn {
    display: flex !important;
    position: absolute;
    left: 12px;
    top: 12px;
    z-index: 101;
    background: rgba(79, 172, 254, 0.3);
    border: none;
    color: white;
    padding: 8px 12px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 20px;
    line-height: 1;
    align-items: center;
    justify-content: center;
    transition: opacity 0.3s ease, transform 0.3s ease;
  }
  
  .mobile-menu-btn.hide {
    opacity: 0;
    pointer-events: none;
    transform: translateX(-20px);
  }
  
  .thread-list-mask {
    display: none;
    position: absolute;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 99;
  }
  
  .thread-list-mask.show {
    display: block;
  }
}

.mobile-menu-btn {
  display: none;
}
</style> 