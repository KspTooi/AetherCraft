<template>
  <div class="chat-container">
    <div class="chat-layout">
      <!-- 移动端菜单按钮 -->
      <LaserButton 
        class="mobile-menu-btn" 
        @click="toggleThreadList"
        :corners="['top-left']"
        corner-size="15px">
        角色列表
      </LaserButton>
      
      <!-- 遮罩层 -->
      <div class="thread-list-mask" :class="{ show: isMobileMenuOpen }" @click="toggleThreadList"></div>
      
      <!-- 角色列表组件 -->
      <div class="thread-list" :class="{ show: isMobileMenuOpen }">
        <ModelRpThreadList
            ref="threadListRef"
            :currentRoleId="currentRoleId"
            :currentThreadId="currentThreadId"
            :isMobileMenuOpen="isMobileMenuOpen"
            @roleChecked="handleRoleChecked"
            @roleEdit="handleRoleEdit"
            @roleConfig="handleRoleConfig"
            @threadManage="showThreadManagement" />
      </div>

      <!-- 主聊天区域 -->
      <div class="chat-main">
        <div class="model-select">
          <ModelSeriesSelector v-model="selectedModel" />
        </div>
        
        <!-- 聊天消息区域 -->
        <div class="chat-messages-wrapper">
          <ModelRpMsgBox
            ref="messagesRef"
            :messages="messages"
            :currentThreadId="currentThreadId"
            :currentRoleId="currentRoleId"
            :isEditing="isEditing"
            @messageEdit="handleMessageEdit"
            @messageRemove="handleMessageRemove"
            @regenerate="handleRegenerate"
            @scrollToBottom="scrollToBottom" />
        </div>
        
        <!-- 聊天输入区域 -->
        <ModelRpInput 
          :isLoading="isLoading"
          :isDisabled="!currentThreadId || !currentRoleId"
          @send="sendMessage"
          @stop="terminateAIResponse" />
      </div>
    </div>

    <!-- 角色会话管理模态框 -->
    <ModelRpThreadModal
      v-if="showThreadModal"
      :selectedRoleId="selectedModalRoleId"
      :selectedRoleName="selectedModalRoleName"
      @close="closeThreadModal"
      @threadChecked="handleThreadChecked" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import ModelSeriesSelector from "@/components/ModelSeriesSelector.vue"
import ModelRpThreadList from "@/components/ModelRpThreadList.vue"
import ModelRpMsgBox from "@/components/ModelRpMsgBox.vue"
import ModelRpInput from "@/components/ModelRpInput.vue"
import ModelRpThreadModal from "@/components/ModelRpThreadModal.vue"
import LaserButton from "@/components/LaserButton.vue"
import axios from 'axios'
import { marked } from 'marked'
import { useThemeStore } from '../stores/theme'

// 获取主题颜色
const themeStore = useThemeStore()
const mainBlur = computed(() => themeStore.mainBlur)
const sideBlur = computed(() => themeStore.sideBlur)
const primaryColor = computed(() => themeStore.primaryColor)
const activeColor = computed(() => themeStore.activeColor)
const primaryHover = computed(() => themeStore.primaryHover)
const primaryButton = computed(() => themeStore.primaryButton)
const primaryButtonBorder = computed(() => themeStore.primaryButtonBorder)

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
const currentRoleId = ref<string | null>(null)
const currentThreadId = ref<string | null>(null)
const isMobileMenuOpen = ref(false)
const isEditing = ref(false)
const threadListRef = ref<InstanceType<typeof ModelRpThreadList> | null>(null)
const messagesRef = ref<InstanceType<typeof ModelRpMsgBox> | null>(null)
const showThreadModal = ref(false)
const selectedModalRoleId = ref('')
const selectedModalRoleName = ref('')
const currentUserRole = ref<any>(null)

// 声明 showToast 函数类型
declare function showToast(type: string, message: string): void;

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

// 重置聊天状态
const resetChatState = () => {
  currentThreadId.value = null
  messages.value = []
  isLoading.value = false
}

// 加载角色的会话
const loadRoleThread = async (roleId: string, newThread: boolean = false, threadId: string | null = null) => {
  isLoading.value = true
  resetChatState()
  
  try {
    const requestData: any = {
      roleId: roleId
    }
    
    if (threadId) {
      // 加载指定会话
      requestData.threadId = threadId
    } else if (newThread) {
      // 创建新会话
      requestData.newThread = 0
    } else {
      // 加载最近的会话
      requestData.newThread = 1
    }
    
    const response = await axios.post('/model/rp/recoverRpChat', requestData)
    
    if (response.data.code === 0) {
      const data = response.data.data
      
      // 更新当前角色ID
      currentRoleId.value = roleId
      
      // 保存会话ID
      currentThreadId.value = data.threadId
      
      // 更新选中的模型
      if (data.modelCode) {
        selectedModel.value = data.modelCode
      }
      
      // 设置用户角色信息
      if (data.userRole) {
        currentUserRole.value = {
          id: data.userRole.id,
          name: data.userRole.name || '用户',
          avatar: data.userRole.avatarPath
        }
      }
      
      // 更新消息列表
      messages.value = (data.histories || []).map((item: any) => ({
        id: item.id,
        role: item.isUser ? 'user' : 'assistant',
        content: item.content,
        name: item.name || (item.isUser ? '用户' : '助手'),
        avatarPath: item.avatarPath,
        createTime: item.createTime
      }))
      
      // 滚动到底部
      nextTick(() => {
        messagesRef.value?.scrollToBottom()
      })
    } else {
      showToast("danger", response.data.message || "加载会话失败")
      resetChatState()
    }
  } catch (error) {
    console.error('加载会话失败:', error)
    showToast("danger", "加载会话失败，请检查网络连接")
    resetChatState()
  } finally {
    isLoading.value = false
  }
}

const toggleThreadList = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const showThreadManagement = (roleId: string, roleName: string) => {
  selectedModalRoleId.value = roleId
  selectedModalRoleName.value = roleName
  showThreadModal.value = true
}

const closeThreadModal = () => {
  showThreadModal.value = false
}

const scrollToBottom = () => {
  messagesRef.value?.scrollToBottom()
}

const handleRoleChecked = async (roleId: string) => {
  if (roleId === currentRoleId.value) return
  await loadRoleThread(roleId)
}

const handleRoleEdit = (roleId: string) => {
  window.location.href = `/panel/model/role/list?id=${roleId}`
}

const handleRoleConfig = async (roleId: string) => {
  showToast("info", "模型角色设置功能即将上线")
  await loadRoleThread(roleId, true, null)
}

const handleThreadChecked = async (roleId: string, threadId: string) => {
  await loadRoleThread(roleId, false, threadId)
  closeThreadModal()
}

const handleMessageEdit = async (historyId: string, content: string) => {
  if (!currentThreadId.value) return
  
  try {
    isEditing.value = true
    const response = await axios.post('/model/rp/updateRpHistory', {
      historyId: historyId,
      content: content
    })
    
    if (response.data.code === 0) {
      // 更新消息内容
      const message = messages.value.find(m => m.id === historyId)
      if (message) {
        message.content = content
        message.isEditing = false
      }
      showToast("success", "消息更新成功")
    } else {
      showToast("danger", response.data.message || "更新消息失败")
    }
  } catch (error) {
    console.error('更新消息失败:', error)
    showToast("danger", "更新消息失败，请检查网络连接")
  } finally {
    isEditing.value = false
  }
}

const handleMessageRemove = async (historyId: string) => {
  if (!currentThreadId.value) return
  
  try {
    const response = await axios.post('/model/rp/removeRpHistory', {
      historyId: historyId
    })
    
    if (response.data.code === 0) {
      // 删除消息
      messages.value = messages.value.filter(m => m.id !== historyId)
      showToast("success", "消息删除成功")
    } else {
      showToast("danger", response.data.message || "删除消息失败")
    }
  } catch (error) {
    console.error('删除消息失败:', error)
    showToast("danger", "删除消息失败，请检查网络连接")
  }
}

const handleRegenerate = async (message: ChatMessage) => {
  if (isLoading.value || !currentThreadId.value || !message.id) return
  
  try {
    isLoading.value = true
    
    // 删除当前消息
    messages.value = messages.value.filter(m => m.id !== message.id)
    
    // 获取上一条用户消息
    const lastUserMessage = [...messages.value].reverse().find(m => m.role === 'user')
    
    if (lastUserMessage && lastUserMessage.content) {
      // 创建临时助手消息
      const assistantMessage: ChatMessage = {
        role: 'assistant',
        content: '正在输入...',
        name: '助手',
        createTime: new Date().toISOString(),
        isTyping: true,
        hasReceivedData: false
      }
      
      messages.value.push(assistantMessage)
      messagesRef.value?.scrollToBottom()
      
      // 调用完成接口
      const response = await axios.post('/model/rp/rpCompleteBatch', {
        threadId: currentThreadId.value,
        model: selectedModel.value,
        queryKind: 0,
        regenerate: 1
      })
      
      if (response.data.code === 0) {
        // 开始轮询响应
        await pollAIResponse(assistantMessage)
      } else {
        showToast("danger", response.data.message || "重新生成失败")
        isLoading.value = false
        // 移除临时消息
        messages.value.pop()
      }
    } else {
      showToast("warning", "无法找到上一条用户消息")
      isLoading.value = false
    }
  } catch (error) {
    console.error('重新生成失败:', error)
    showToast("danger", "重新生成失败，请检查网络连接")
    isLoading.value = false
  }
}

const sendMessage = async (message: string) => {
  if (isLoading.value || !currentThreadId.value) return

  // 添加用户消息到列表
  const tempUserMessage: ChatMessage = {
    role: 'user',
    content: message,
    name: currentUserRole.value?.name || '用户',
    avatarPath: currentUserRole.value?.avatar,
    createTime: new Date().toISOString()
  }
  
  messages.value.push(tempUserMessage)
  messagesRef.value?.scrollToBottom()

  isLoading.value = true
  
  try {
    const response = await axios.post('/model/rp/rpCompleteBatch', {
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
      
      const assistantMessage: ChatMessage = {
        role: 'assistant',
        content: '正在输入...',
        name: '助手',
        createTime: new Date().toISOString(),
        isTyping: true,
        hasReceivedData: false
      }
      
      messages.value.push(assistantMessage)
      messagesRef.value?.scrollToBottom()
      
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
      const response = await axios.post('/model/rp/rpCompleteBatch', {
        threadId: currentThreadId.value,
        queryKind: 1 // 1:查询响应流
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
            
            // 更新ID和其他信息
            if (segment.historyId) {
              assistantMessage.id = segment.historyId
            }
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
          
          // 滚动到底部
          messagesRef.value?.scrollToBottom()
        } else if (segment.type === 2) {
          // 数据流结束
          assistantMessage.isTyping = false
          isLoading.value = false
          
          // 通知线程列表更新
          threadListRef.value?.loadRoleList()
          
          break
        }
      } else {
        console.error('获取AI响应失败:', response.data.message)
        isLoading.value = false
        break
      }
    } catch (error) {
      console.error('获取AI响应错误:', error)
      isLoading.value = false
      break
    }
  }
}

const terminateAIResponse = async () => {
  if (!isLoading.value || !currentThreadId.value) return
  
  try {
    await axios.post('/model/rp/terminateRpResponse', {
      threadId: currentThreadId.value
    })
    
    isLoading.value = false
  } catch (error) {
    console.error('终止响应失败:', error)
  }
}

// 加载初始角色列表
onMounted(async () => {
  // 加载角色列表
  if (threadListRef.value) {
    await threadListRef.value.loadRoleList()
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.chat-layout {
  display: flex;
  height: 100%;
}

.thread-list {
  width: 240px;
  height: 100%;
  overflow: hidden;
  backdrop-filter: blur(v-bind(sideBlur));
  -webkit-backdrop-filter: blur(v-bind(sideBlur));
  background: rgba(0, 0, 0, 0.2);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  transition: transform 0.3s ease;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: transparent;
  overflow: hidden;
}

.model-select {
  padding: 8px 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-messages-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
}

/* 移动端适配 */
.mobile-menu-btn {
  display: none;
  position: fixed;
  top: 60px;
  left: 10px;
  z-index: 100;
  padding: 6px 10px;
  font-size: 12px;
}

.thread-list-mask {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 90;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.thread-list-mask.show {
  opacity: 1;
  pointer-events: auto;
}

@media (max-width: 768px) {
  .thread-list {
    position: fixed;
    top: 45px;
    left: 0;
    bottom: 0;
    z-index: 100;
    transform: translateX(-100%);
  }
  
  .thread-list.show {
    transform: translateX(0);
  }
  
  .mobile-menu-btn {
    display: block;
  }
  
  .thread-list-mask {
    display: block;
  }
}
</style> 