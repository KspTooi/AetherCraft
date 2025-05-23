<template>
  <div class="chat-container">
    <div class="chat-layout">
      <!-- 移动端菜单按钮 -->
      <LaserButton 
        class="mobile-menu-btn" 
        @click="toggleThreadList"
        :corners="['top-left']"
        corner-size="15px">
        聊天列表
      </LaserButton>
      
      <!-- 遮罩层 -->
      <div class="thread-list-mask" :class="{ show: isMobileMenuOpen }" @click="toggleThreadList"></div>
      
      <!-- 会话列表组件 -->
      <div class="thread-list" :class="{ show: isMobileMenuOpen }">
        <ModelChatThreadList
            ref="threadListRef"
            :currentThreadId="currentThreadId"
            :isMobileMenuOpen="isMobileMenuOpen"
            @threadChecked="handleThreadChecked"
            @threadEdit="handleThreadEdit"
            @threadRemove="handleThreadRemove"
            @createNewThread="createNewThread" />
      </div>


      <!-- 主聊天区域 -->
      <div class="chat-main">
        <div class="model-select">
          <ModelSeriesSelector v-model="selectedModel" />
        </div>
        
        <!-- 聊天消息区域 -->
        <div class="chat-messages-wrapper">
          <ModelChatMsgBox
            ref="messagesRef"
            :messages="messages"
            :currentThreadId="currentThreadId"
            :isEditing="isEditing"
            @messageEdit="handleMessageEdit"
            @messageRemove="handleMessageRemove"
            @regenerate="handleRegenerate"
            @scrollToBottom="scrollToBottom" />
        </div>
        
        <!-- 聊天输入区域 -->
        <ModelChatInput 
          :isLoading="isLoading"
          :isDisabled="!currentThreadId"
          @send="sendMessage"
          @stop="terminateAIResponse" />
      </div>
    </div>
  </div>
  <ConfirmModal ref="confirmModal" />
  <InputModal ref="inputModal" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed, inject } from 'vue'
import GlassBox from "@/components/GlassBox.vue"
import ModelSeriesSelector from "@/components/ModelSeriesSelector.vue"
import ModelChatThreadList from "@/components/ModelChatThreadList.vue"
import ModelChatMsgBox from "@/components/ModelChatMsgBox.vue"
import ModelChatInput from "@/components/ModelChatInput.vue"
import axios from 'axios'
import { marked } from 'marked'
import { useThemeStore } from '../stores/theme'
import LaserButton from "@/components/LaserButton.vue"
import ConfirmModal from "@/components/ConfirmModal.vue"
import InputModal from "@/components/InputModal.vue"

// 获取主题颜色
const themeStore = useThemeStore()
const mainBlur = computed(() => themeStore.mainBlur)
const sideBlur = computed(() => themeStore.sideBlur)
const primaryColor = computed(() => themeStore.primaryColor)
const activeColor = computed(() => themeStore.activeColor)
const primaryHover = computed(() => themeStore.primaryHover)
const primaryButton = computed(() => themeStore.primaryButton)
const primaryButtonBorder = computed(() => themeStore.primaryButtonBorder)

// 获取提前解除加载状态的方法
const finishLoading = inject<() => void>('finishLoading')

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

// 状态定义
const messages = ref<ChatMessage[]>([])
const selectedModel = ref('')
const isLoading = ref(false)
const currentThreadId = ref<string | null>(null)
const isMobileMenuOpen = ref(false)
const isEditing = ref(false)
const refreshThreadListTimer = ref<number | null>(null)
const threadListRef = ref<InstanceType<typeof ModelChatThreadList> | null>(null)
const messagesRef = ref<InstanceType<typeof ModelChatMsgBox> | null>(null)
const confirmModal = ref<InstanceType<typeof ConfirmModal> | null>(null)
const inputModal = ref<InstanceType<typeof InputModal> | null>(null)

// 工具函数
const throttle = (fn: Function, delay: number) => {
  let timer: any = null
  return function(this: any, ...args: any[]) {
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
      if (!currentThreadId.value && response.data.data.length === 0) {
        resetChatState()
      }
    } else if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '加载失败',
        content: response.data.message || '加载会话列表失败',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '加载失败',
        content: '加载会话列表失败，请检查网络连接',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  }
}

// 使用节流包装loadThreadList
const throttledLoadThreadList = throttle(loadThreadList, 3000)

// 重置聊天状态
const resetChatState = () => {
  currentThreadId.value = null
  messages.value = []
  isLoading.value = false
}

const sendMessage = async (message: string) => {
  if (isLoading.value) return

  // 添加用户消息到列表
  const tempUserMessage: ChatMessage = {
    role: 'user',
    content: message,
    name: '用户',
    createTime: new Date().toISOString()
  }
  
  messages.value.push(tempUserMessage)

  isLoading.value = true
  
  try {
    const response = await axios.post('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      model: selectedModel.value,
      message: message,
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
        content: '',
        name: '----',
        createTime: new Date().toISOString(),
        isTyping: true,
        hasReceivedData: false
      }
      
      messages.value.push(assistantMessage)
      
      messagesRef.value?.scrollToBottom()
      
      await pollAIResponse(assistantMessage)
    } else if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '发送失败',
        content: response.data.message || '发送消息失败',
        confirmText: '确定',
        cancelText: undefined
      })
      isLoading.value = false
    }
  } catch (error) {
    console.error('发送消息错误:', error)
    if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '发送失败',
        content: '发送消息失败，请检查网络连接',
        confirmText: '确定',
        cancelText: undefined
      })
    }
    isLoading.value = false
  }
}

const pollAIResponse = async (assistantMessage: any) => {
  while (isLoading.value) {
    try {
      const response = await axios.post('/model/chat/completeBatch', {
        threadId: currentThreadId.value,
        queryKind: 1 // 1:查询响应流
      })
      
      if (response.data.code === 0) {
        const segment = response.data.data
        
        if (!segment) {
          await new Promise(resolve => setTimeout(resolve, 500))
          continue
        }
        
        console.log('收到流式响应片段:', segment)
        
        if (segment.type === 10) {
          // 错误类型，停止轮询并显示错误提示
          console.error('收到错误类型片段:', segment)
          isLoading.value = false
          
          // 移除临时消息
          if (messages.value.length > 0 && messages.value[messages.value.length - 1].isTyping) {
            messages.value.pop()
          }
          
          // 显示错误提示
          if (confirmModal.value) {
            await confirmModal.value.showConfirm({
              title: '生成失败',
              content: segment.content || '生成回复时发生错误，请稍后重试。',
              confirmText: '确定',
              cancelText: undefined
            })
          }
          break
        }
        
        if (segment.type === 1) {
          // 数据片段
          // 如果是第一个数据片段，清除"正在输入..."文本
          if (!assistantMessage.hasReceivedData) {
            assistantMessage.content = ''
            assistantMessage.hasReceivedData = true
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
          messagesRef.value?.scrollToBottom()
        } else if (segment.type === 2) {
          // 结束片段
          // 如果是第一个数据片段，清除"正在输入..."文本
          if (!assistantMessage.hasReceivedData) {
            assistantMessage.content = ''
            assistantMessage.hasReceivedData = true
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
          messagesRef.value?.scrollToBottom()
          
          // 消息完全接收完后，才取消输入状态
          assistantMessage.isTyping = false
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
        }
      } else {
        // 请求失败，移除临时消息并显示错误提示
        console.error('获取AI响应失败:', response.data.message)
        isLoading.value = false
        
        // 移除临时消息
        if (messages.value.length > 0 && messages.value[messages.value.length - 1].isTyping) {
          messages.value.pop()
        }
        
        if (confirmModal.value) {
          await confirmModal.value.showConfirm({
            title: '生成失败',
            content: response.data.message || '获取AI响应失败',
            confirmText: '确定',
            cancelText: undefined
          })
        }
        break
      }
    } catch (error) {
      console.error('获取AI响应错误:', error)
      isLoading.value = false
      
      // 移除临时消息
      if (messages.value.length > 0 && messages.value[messages.value.length - 1].isTyping) {
        messages.value.pop()
      }
      
      // 显示错误提示
      if (confirmModal.value) {
        await confirmModal.value.showConfirm({
          title: '网络错误',
          content: '网络错误，请检查连接',
          confirmText: '确定',
          cancelText: undefined
        })
      }
      break
    }
    
    await new Promise(resolve => setTimeout(resolve, 500))
  }
}

const terminateAIResponse = async () => {
  if (!isLoading.value) return
  
  try {
    // 使用确认模态框确认是否终止响应
    if (confirmModal.value) {
      const confirmed = await confirmModal.value.showConfirm({
        title: '终止生成',
        content: '您确定要终止AI正在生成的回复吗？',
        confirmText: '终止',
        cancelText: '继续生成'
      })
      
      if (!confirmed) return
    }
    
    // 调用终止响应接口
    const response = await axios.post('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      queryKind: 2  // 2:终止AI响应
    })
    
    if (response.data.code === 0) {
      console.log('已终止AI响应')
      isLoading.value = false
      
      // 移除临时消息
      if (messages.value.length > 0 && messages.value[messages.value.length - 1].isTyping) {
        messages.value.pop()
      }
    } else if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '终止失败',
        content: response.data.message || '终止生成失败',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  } catch (error) {
    console.error('终止响应失败:', error)
    if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '终止失败',
        content: '终止生成失败，请检查网络连接',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  } finally {
    // 即使出错也要将loading状态重置
    isLoading.value = false
  }
}

const renderMarkdown = (content: string) => {
  if (!content) return ''
  return marked.parse(content)
}

const scrollToBottom = () => {
  messagesRef.value?.scrollToBottom()
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
    } else if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '创建失败',
        content: response.data.message || '创建会话失败',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '创建失败',
        content: '创建会话失败，请检查网络连接',
        confirmText: '确定',
        cancelText: undefined
      })
    }
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
    } else if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '加载失败',
        content: response.data.message || '加载会话失败',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  } catch (error) {
    console.error('加载会话失败:', error)
    if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '加载失败',
        content: '加载会话失败，请检查网络连接',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  }
}

const editThreadTitle = async (thread: any) => {
  if (!inputModal.value) return
  
  const newTitle = await inputModal.value.showInput({
    title: '编辑会话标题',
    content: '请输入新的会话标题:',
    defaultValue: thread.title,
    placeholder: '输入新标题...',
    confirmText: '保存',
    cancelText: '取消'
  })
  
  if (!newTitle || newTitle.trim() === thread.title) return
  
  try {
    const response = await axios.post('/model/chat/editThread', {
      threadId: thread.id,
      title: newTitle.trim()
    })
    if (response.data.code === 0) {
      await threadListRef.value?.loadThreadList()
    } else if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '更新失败',
        content: response.data.message || '更新会话标题失败',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  } catch (error) {
    console.error('更新会话标题失败:', error)
    if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '更新失败',
        content: '更新会话标题失败，请检查网络连接',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  }
}

const deleteThread = async (threadId: string) => {
  if (!confirmModal.value) return
  
  const confirmed = await confirmModal.value.showConfirm({
    title: '确认删除',
    content: '确定要删除这个会话吗？此操作不可恢复。',
    confirmText: '确认删除',
    cancelText: '取消'
  })
  
  if (!confirmed) return
  
  try {
    const response = await axios.post('/model/chat/removeThread', {
      threadId: threadId
    })
    if (response.data.code === 0) {
      if (String(threadId) === String(currentThreadId.value)) {
        resetChatState()
      }
      await threadListRef.value?.loadThreadList()
    } else {
      await confirmModal.value.showConfirm({
        title: '删除失败',
        content: response.data.message || '删除会话失败',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  } catch (error: any) {
    console.error('删除会话失败:', error)
    await confirmModal.value.showConfirm({
      title: '删除失败',
      content: '删除会话失败，请检查网络连接',
      confirmText: '确定',
      cancelText: undefined
    })
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

const handleMessageEdit = async (historyId: string, content: string) => {
  try {
    const response = await axios.post('/model/chat/editHistory', {
      historyId: historyId,
      content: content
    })
    
    if (response.data.code === 0) {
      // 更新本地消息内容
      const message = messages.value.find(msg => msg.id === historyId)
      if (message) {
        message.content = content
        message.isEditing = false
      }
    } else if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '更新失败',
        content: response.data.message || '更新失败',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  } catch (error) {
    console.error('更新消息失败:', error)
    if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '更新失败',
        content: '更新失败，请稍后重试',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  }
}

const handleMessageRemove = async (historyId: string) => {
  try {
    const response = await axios.post('/model/chat/removeHistory', {
      threadId: currentThreadId.value,
      historyId: historyId
    })
    
    if (response.data.code === 0) {
      // 从本地消息列表中移除该消息
      const index = messages.value.findIndex(msg => msg.id === historyId)
      if (index !== -1) {
        messages.value.splice(index, 1)
      }
    } else if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '删除失败',
        content: response.data.message || '删除消息失败',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  } catch (error) {
    console.error('删除消息失败:', error)
    if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '删除失败',
        content: '删除消息失败，请检查网络连接',
        confirmText: '确定',
        cancelText: undefined
      })
    }
  }
}

const handleRegenerate = async (message: ChatMessage) => {
  if (isLoading.value) return
  
  // 验证threadId是否存在
  if (!currentThreadId.value && confirmModal.value) {
    await confirmModal.value.showConfirm({
      title: '重新生成失败',
      content: '会话ID不存在，无法重新生成',
      confirmText: '确定',
      cancelText: undefined
    })
    return
  }
  
  // 设置加载状态
  isLoading.value = true
  
  try {
    // 获取消息列表中的最后一条消息
    const lastMessage = messages.value[messages.value.length - 1];
    
    // 如果最后一条是用户消息，直接根据它生成AI响应
    if (lastMessage.role === 'user') {
      console.log('最后一条是用户消息，直接根据它生成AI响应');
      
      // 创建新的AI消息
      const assistantMessage: ChatMessage = {
        role: 'assistant',
        content: '',
        name: '----',
        avatarPath: undefined,
        createTime: new Date().toISOString(),
        isTyping: true,
        hasReceivedData: false
      };
      
      // 添加AI消息到列表
      messages.value.push(assistantMessage);
      messagesRef.value?.scrollToBottom();
      
      // 发送标准请求
      const response = await axios.post('/model/chat/completeBatch', {
        threadId: currentThreadId.value,
        model: selectedModel.value,
        queryKind: 3 // 3:重新生成
      });
      
      if (response.data.code === 0) {
        // 开始轮询获取AI响应
        await pollAIResponse(assistantMessage);
      } else if (confirmModal.value) {
        await confirmModal.value.showConfirm({
          title: '生成失败',
          content: response.data.message || '生成失败',
          confirmText: '确定',
          cancelText: undefined
        })
        // 移除临时消息
        messages.value.pop();
        isLoading.value = false;
      }
      return;
    }
    
    // 如果最后一条是AI消息，删除它并重新生成
    if (lastMessage.role === 'assistant') {
      console.log('最后一条是AI消息，删除它并创建新的临时消息');
      
      // 如果有ID，先从服务器删除
      if (lastMessage.id) {
        try {
          // 先删除最后一条AI消息
          await axios.post('/model/chat/removeHistory', {
            threadId: currentThreadId.value,
            historyId: lastMessage.id
          });
        } catch(e) {
          console.error('删除最后一条AI消息失败，但继续执行重新生成:', e);
        }
      }
      
      // 从本地消息列表中删除
      messages.value.pop();
    }
    
    // 创建新的助手消息对象
    const assistantMessage: ChatMessage = {
      role: 'assistant',
      content: '',
      name: '----',
      avatarPath: undefined,
      createTime: new Date().toISOString(),
      isTyping: true,
      hasReceivedData: false
    };
    
    // 添加AI消息到列表
    messages.value.push(assistantMessage);
    messagesRef.value?.scrollToBottom();
    
    // 发送重新生成请求
    const response = await axios.post('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      model: selectedModel.value,
      queryKind: 3 // 3:重新生成最后一条AI消息
    });
    
    if (response.data.code === 0) {
      // 开始轮询获取AI响应
      await pollAIResponse(assistantMessage);
    } else if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '生成失败',
        content: response.data.message || '生成失败',
        confirmText: '确定',
        cancelText: undefined
      })
      // 移除临时消息
      messages.value.pop();
      isLoading.value = false;
    }
  } catch (error) {
    console.error('重新生成错误:', error);
    if (confirmModal.value) {
      await confirmModal.value.showConfirm({
        title: '重新生成失败',
        content: '重新生成失败，请稍后重试',
        confirmText: '确定',
        cancelText: undefined
      })
    }
    // 确保在错误情况下也移除临时消息
    if (messages.value.length > 0 && 
        messages.value[messages.value.length - 1].isTyping) {
      messages.value.pop();
    }
    isLoading.value = false;
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
  await threadListRef.value?.loadThreadList()
  
  // 数据加载完成后，提前解除加载状态
  if (finishLoading) finishLoading()
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

.thread-list{
  backdrop-filter: blur(v-bind(sideBlur));
  -webkit-backdrop-filter: blur(v-bind(sideBlur));
}

/* 主聊天区域 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: transparent;
  overflow: hidden;
  border-radius: 0 !important;
  backdrop-filter: blur(v-bind(mainBlur));
  -webkit-backdrop-filter: blur(v-bind(mainBlur));
}

/* 模型选择区域 */
.model-select {
  padding: 8px 20px;
  border-bottom: none;
  position: relative;
}

/* 添加渐变边框效果，替代之前的实线边框 */
.model-select::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: linear-gradient(to right, 
    transparent, 
    rgba(79, 172, 254, 0.15), 
    rgba(79, 172, 254, 0.3), 
    rgba(79, 172, 254, 0.15), 
    transparent
  );
  pointer-events: none;
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
  overflow: hidden; /* 修改为hidden避免双重滚动条 */
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
    position: fixed;
    left: 12px;
    top: 12px;
    z-index: 1001; /* 比TopNav的z-index(1000)高 */
    cursor: pointer;
    min-height: 32px;
    height: auto;
    width: auto;
    padding: 5px 8px !important; /* 覆盖LaserButton默认内边距 */
    font-size: 12px!important;
  }
  
  .mobile-menu-btn.hide {
    opacity: 0;
    pointer-events: none;
    transform: translateX(-20px);
  }
  
  .mobile-menu-btn:hover {
    transform: translateY(0) !important; /* 禁用LaserButton默认的悬停效果 */
  }
  
  .thread-list-mask {
    display: none;
    position: fixed;
    left: 0;
    top: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.5);
    z-index: 1000; /* 确保在导航栏上层，但在线程列表下层 */
  }
  
  .thread-list-mask.show {
    display: block;
  }
  
  .thread-list {
    position: fixed;
    left: -240px;
    top: 0;
    height: 100vh;
    width: 240px;
    z-index: 1001; /* 与按钮相同层级 */
    transition: transform 0.3s ease;
    background-color: rgba(20, 30, 40, 0.9); /* 确保背景不透明 */
  }
  
  .thread-list.show {
    transform: translateX(240px);
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.3);
  }
  
  .chat-main {
    width: 100%;
    margin-left: 0;
  }
}

.mobile-menu-btn {
  display: none; /* 默认隐藏 */
  position: fixed;
  left: 12px;
  top: 8px;
  z-index: 1001;
  width: auto;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
}
</style> 