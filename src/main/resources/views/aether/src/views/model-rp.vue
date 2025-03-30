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
            :currentRoleId="currentRoleId !== null ? String(currentRoleId) : null"
            :currentThreadId="currentThreadId !== null ? String(currentThreadId) : null"
            :isMobileMenuOpen="isMobileMenuOpen"
            @roleChecked="handleRoleChecked"
            @roleEdit="handleRoleEdit"
            @roleConfig="handleRoleConfig"
            @threadManage="showThreadManagement"
            @startNewSession="handleStartNewSession" />
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
            :currentThreadId="currentThreadId ? String(currentThreadId) : null"
            :currentRoleId="currentRoleId ? String(currentRoleId) : null"
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
import { ref, onMounted, onUnmounted, nextTick, computed, inject } from 'vue'
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

// 获取提前解除加载状态的方法
const finishLoading = inject<() => void>('finishLoading')

// 定义消息类型接口
interface ChatMessage {
  id?: string;
  role: 'user' | 'assistant';
  content: string;
  name: string;
  avatarPath?: string | null;
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
// 缓存当前 AI 角色信息
const currentAIRole = ref<{id: string, name: string, avatarPath: string | null}>({
  id: '',
  name: '助手',
  avatarPath: null
})

// 挂载后初始化
onMounted(async () => {
  console.log('组件挂载，开始加载角色列表...')
  // 获取可用模型列表
  try {
    const response = await axios.post('/model/series/getModelSeries')
    if (response.data.code === 0 && response.data.data.length > 0) {
      // 设置默认模型
      selectedModel.value = response.data.data[0].modelCode
      console.log('设置默认模型:', selectedModel.value)
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
  }
  
  // 加载角色列表
  await threadListRef.value?.loadRoleList()
  
  // 获取用户上次使用的会话和角色
  try {
    console.log('获取用户上次使用的会话和角色...')
    const lastStatusResponse = await axios.post('/model/rp/getRpLastStatus')
    
    if (lastStatusResponse.data.code === 0) {
      const lastStatus = lastStatusResponse.data.data
      console.log('获取到上次状态:', lastStatus)
      
      // 如果有上次使用的角色和会话，恢复它们
      if (lastStatus.lastRole && lastStatus.lastThread) {
        console.log(`恢复上次会话: 角色ID=${lastStatus.lastRole}, 会话ID=${lastStatus.lastThread}`)
        await loadRoleThread(lastStatus.lastRole, false, lastStatus.lastThread)
      }
    }
  } catch (error) {
    console.error('获取上次会话状态失败:', error)
  }
  
  // 角色列表加载完成后，提前结束加载动画
  if (finishLoading) {
    console.log('调用finishLoading解除加载状态')
    finishLoading() 
  }
  
  // 获取URL中的角色ID参数
  const urlParams = new URLSearchParams(window.location.search)
  const roleIdParam = urlParams.get('roleId')
  
  // 如果URL中有角色ID参数，自动加载该角色的会话（优先级高于上次会话状态）
  if (roleIdParam) {
    console.log(`从URL参数加载角色: ${roleIdParam}`)
    await loadRoleThread(roleIdParam, false, null)
  }
})

// 重置聊天状态
const resetChatState = () => {
  currentThreadId.value = null
  messages.value = []
  isLoading.value = false
}

// 加载角色的会话
const loadRoleThread = async (roleId: string, newThread: boolean = false, threadId: string | null = null) => {
  if (!roleId) {
    console.error("角色ID不能为空")
    return
  }
  
  isLoading.value = true
  resetChatState()
  
  try {
    const requestData: any = {
      modelRoleId: roleId,
      modelCode: selectedModel.value || 'gemini-pro'
    }
    
    if (threadId) {
      // 加载指定会话
      console.log(`加载指定会话, threadId: ${threadId}`)
      requestData.threadId = threadId
    } else if (newThread === true) {
      // 创建新会话
      console.log('创建新会话, newThread=0')
      requestData.newThread = 0
    } else {
      // 加载最近的会话
      console.log('加载最近会话, newThread=1')
      requestData.newThread = 1
    }
    
    console.log('加载角色会话, 请求参数:', JSON.stringify(requestData))
    const response = await axios.post('/model/rp/recoverRpChat', requestData)
    
    if (response.data.code === 0) {
      const data = response.data.data
      console.log('API返回数据:', data)
      
      // 更新当前角色ID
      currentRoleId.value = roleId
      
      // 保存会话ID
      currentThreadId.value = data.threadId ? String(data.threadId) : null
      
      console.log('会话加载成功, threadId:', data.threadId)
      
      // 更新选中的模型
      if (data.modelCode) {
        console.log('从会话获取模型代码:', data.modelCode)
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

      // 从角色列表中获取当前 AI 角色的详细信息并缓存
      if (threadListRef.value && threadListRef.value.roles && threadListRef.value.roles.length > 0) {
        const aiRole = threadListRef.value.roles.find((r: any) => String(r.id) === String(roleId))
        if (aiRole) {
          console.log('缓存 AI 角色信息:', aiRole)
          currentAIRole.value = {
            id: aiRole.id,
            name: aiRole.name || '助手',
            avatarPath: aiRole.avatarPath || null
          }
        }
      }
      
      // 更新消息列表
      // 检查API返回的消息字段 - 优先检查messages，然后才是histories
      let messageList: any[] = []

      if (data.messages && Array.isArray(data.messages)) {
        console.log(`API返回${data.messages.length}条messages:`, data.messages)
        messageList = data.messages
      } else if (data.histories && Array.isArray(data.histories)) {
        console.log(`API返回${data.histories.length}条histories:`, data.histories)
        messageList = data.histories
      } else {
        console.log('API未返回消息列表，尝试检查非数组字段')
        // 如果返回的不是数组，看看是否单个消息对象放在了别的字段里
        if (typeof data.message === 'object' && data.message !== null) {
          console.log('找到单个消息对象:', data.message)
          messageList = [data.message]
        }
      }

      if (messageList.length > 0) {
        console.log(`找到${messageList.length}条消息`)
        
        // 查看第一条消息的结构
        if (messageList[0]) {
          console.log('第一条消息字段:')
          Object.keys(messageList[0]).forEach(key => {
            console.log(`- ${key}: ${JSON.stringify(messageList[0][key])}`)
          })
        }
        
        // 将API返回的消息映射到统一格式
        messages.value = messageList.map((item: any) => {
          const isAssistant = item.type === 1 || item.isUser === false || false
          
          // 获取消息内容 - 可能在rawContent或content字段中
          const content = item.rawContent || item.content || ''
          
          return {
            id: item.id,
            role: isAssistant ? 'assistant' : 'user',
            content: content,
            name: item.name || (isAssistant ? '助手' : '用户'),
            avatarPath: item.avatarPath,
            createTime: item.createTime
          }
        })
        
        console.log(`处理后消息列表(${messages.value.length}条):`, messages.value)
      } else {
        console.log('API未返回任何可用消息')
        messages.value = []
      }
      
      // 滚动到底部
      nextTick(() => {
        messagesRef.value?.scrollToBottom()
      })
    } else {
      console.error('加载会话失败, 错误码:', response.data.code, '错误信息:', response.data.message)
      resetChatState()
    }
  } catch (error) {
    console.error('加载会话失败:', error)
    resetChatState()
  } finally {
    isLoading.value = false
  }
}

const toggleThreadList = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const showThreadManagement = (roleId: string, roleName: string) => {
  // 设置当前选中的角色ID和名称
  selectedModalRoleId.value = roleId
  selectedModalRoleName.value = roleName
  
  // 显示会话管理模态框
  showThreadModal.value = true
  
  console.log('打开会话管理模态框, 角色:', roleName, '(ID:', roleId, ')')
}

const closeThreadModal = () => {
  showThreadModal.value = false
}

const scrollToBottom = () => {
  messagesRef.value?.scrollToBottom()
}

const handleRoleChecked = async (roleId: string) => {
  try {
    console.log(`处理角色选择事件: roleId=${roleId}, 当前roleId=${currentRoleId.value}, 当前threadId=${currentThreadId.value}`)
    
    // 隐藏移动端菜单（如果打开）
    if (isMobileMenuOpen.value) {
      toggleThreadList()
    }
    
    // 如果点击的是当前角色，根据情况处理
    if (roleId === String(currentRoleId.value) && currentThreadId.value) {
      console.log("点击了当前已选中的角色，无需重新加载会话")
      return
    }
    
    // 重置当前会话ID，避免使用上一个角色的会话ID
    currentThreadId.value = null
    
    // 加载角色会话
    console.log(`加载角色 ${roleId} 的会话...`)
    await loadRoleThread(roleId)
  } catch (error) {
    console.error('加载角色会话失败:', error)
  }
}

const handleRoleEdit = (roleId: string) => {
  // 跳转到角色编辑页面
  window.location.href = `/dashboard?redirect=/panel/model/role/list?id=${roleId}`
}

const handleRoleConfig = async (roleId: string) => {
  // 目前模型角色设置功能未上线，创建新会话
  console.log("模型角色设置功能即将上线")
  
  try {
    // 创建新会话
    await loadRoleThread(roleId, true, null)
  } catch (error) {
    console.error('创建新会话失败:', error)
  }
}

const handleStartNewSession = async (roleId: string) => {
  try {
    console.log(`开始新会话: roleId=${roleId}`)
    // 创建新会话
    await loadRoleThread(roleId, true, null)
  } catch (error) {
    console.error('创建新会话失败:', error)
  }
}

const handleThreadChecked = async (roleId: string, threadId: string) => {
  console.log(`选择会话: roleId=${roleId}, threadId=${threadId}`)
  // 传递threadId参数加载指定会话
  await loadRoleThread(roleId, false, threadId)
  closeThreadModal()
}

const handleMessageEdit = async (historyId: string, content: string) => {
  if (!currentThreadId.value) return
  
  try {
    isEditing.value = true
    const response = await axios.post('/model/rp/editHistory', {
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
      console.log("消息更新成功")
    } else {
      console.error("更新消息失败:", response.data.message)
    }
  } catch (error) {
    console.error('更新消息失败:', error)
  } finally {
    isEditing.value = false
  }
}

const handleMessageRemove = async (historyId: string) => {
  if (!currentThreadId.value) return
  
  try {
    const response = await axios.post('/model/rp/removeHistory', {
      historyId: historyId
    })
    
    if (response.data.code === 0) {
      // 删除消息
      messages.value = messages.value.filter(m => m.id !== historyId)
      console.log("消息删除成功")
    } else {
      console.error("删除消息失败:", response.data.message)
    }
  } catch (error) {
    console.error('删除消息失败:', error)
  }
}

const handleRegenerate = async (message: ChatMessage) => {
  if (isLoading.value || !currentThreadId.value) return
  
  try {
    isLoading.value = true
    
    // 检查最后一条消息的类型
    const lastMessage = messages.value[messages.value.length - 1];
    
    // 如果最后一条是AI消息，则删除它
    if (lastMessage.role === 'assistant') {
      console.log('最后一条是AI消息，删除它并创建新的临时消息');
      
      // 如果有ID，先从服务器删除
      if (lastMessage.id) {
        try {
          await axios.post('/model/rp/removeHistory', {
            historyId: lastMessage.id
          });
        } catch(e) {
          console.error('删除最后一条AI消息失败，但继续执行重新生成:', e);
        }
      }
      
      // 从本地消息列表中删除
      messages.value.pop();
    }
    
    // 使用缓存的AI角色信息创建新的临时消息
    const assistantMessage: ChatMessage = {
      role: 'assistant',
      content: '',
      name: currentAIRole.value.name,
      avatarPath: currentAIRole.value.avatarPath,
      createTime: new Date().toISOString(),
      isTyping: true,
      hasReceivedData: false
    };
    
    // 添加临时消息到列表
    console.log("添加新的AI临时消息:", assistantMessage);
    messages.value.push(assistantMessage);
    messagesRef.value?.scrollToBottom();
    
    // 发送重新生成请求
    const response = await axios.post('/model/rp/rpCompleteBatch', {
      threadId: currentThreadId.value,
      model: selectedModel.value || 'gemini-pro',
      queryKind: 3 // 3:重新生成最后一条AI消息
    });
    
    if (response.data.code === 0) {
      console.log('重新生成请求成功');
      
      // 开始轮询获取新的响应
      await pollAIResponse(assistantMessage);
    } else {
      console.error("重新生成失败:", response.data.message);
      // 移除临时消息
      messages.value.pop();
      isLoading.value = false;
    }
  } catch (error) {
    console.error('重新生成失败:', error);
    // 确保在错误情况下也移除临时消息
    if (messages.value.length > 0 && messages.value[messages.value.length - 1].isTyping) {
      messages.value.pop();
    }
    isLoading.value = false;
  }
}

const sendMessage = async (message: string) => {
  console.log('开始发送消息')
  if (isLoading.value || !currentThreadId.value) {
    console.log('无法发送消息：' + (isLoading.value ? '正在加载中' : '当前没有活动会话'))
    return
  }
  
  isLoading.value = true
  
  try {
    // 确保有有效的模型代码
    const modelCode = selectedModel.value || 'gemini-pro'
    
    const requestParams = {
      threadId: currentThreadId.value,
      model: modelCode,
      message: message,
      queryKind: 0
    }
    console.log('发送消息请求参数:', requestParams)
    
    const response = await axios.post('/model/rp/rpCompleteBatch', requestParams)
    console.log('接收到发送消息响应:', response.data)
    
    if (response.data.code === 0) {
      const userData = response.data.data
      console.log('后端返回的用户消息数据:', userData)
      
      // 在收到后端响应后，根据返回数据添加用户消息到列表
      const userMessage: ChatMessage = {
        id: userData.historyId,
        role: 'user',
        content: userData.content || message,
        name: userData.roleName || currentUserRole.value?.name || '用户',
        avatarPath: userData.roleAvatarPath || currentUserRole.value?.avatar,
        createTime: new Date().toISOString()
      }
      
      console.log('添加用户消息到消息列表:', userMessage)
      messages.value.push(userMessage)
      messagesRef.value?.scrollToBottom()
      
      // 使用缓存的 AI 角色信息创建临时消息
      const assistantMessage: ChatMessage = {
        role: 'assistant',
        content: '', // 不再显示"正在输入..."文本
        name: currentAIRole.value.name,
        avatarPath: currentAIRole.value.avatarPath,
        createTime: new Date().toISOString(),
        isTyping: true,
        hasReceivedData: false
      }
      
      console.log("添加AI临时消息:", assistantMessage)
      messages.value.push(assistantMessage)
      messagesRef.value?.scrollToBottom()
      
      console.log("开始轮询AI响应...")
      await pollAIResponse(assistantMessage)
    } else {
      console.error("发送消息失败:", response.data)
      isLoading.value = false
    }
  } catch (error) {
    console.error('发送消息错误:', error)
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
        
        console.log('收到流式响应片段:', segment)
        
        if (segment.type === 1) {
          // 数据片段
          // 如果是第一个数据片段，设置接收状态标记
          if (!assistantMessage.hasReceivedData) {
            assistantMessage.hasReceivedData = true
            assistantMessage.content = '' // 确保内容从空开始
            
            // 更新ID和其他信息
            if (segment.historyId) {
              assistantMessage.id = segment.historyId
            }
          }
          
          // 检查segment中的内容字段
          const contentToAdd = segment.content || segment.rawContent || ''
          if (contentToAdd) {
            console.log(`添加内容片段: "${contentToAdd.substring(0, 30)}..."`)
            assistantMessage.content += contentToAdd
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
          
          // 滚动到底部
          messagesRef.value?.scrollToBottom()
        } else if (segment.type === 2) {
          // 数据流结束
          assistantMessage.isTyping = false
          isLoading.value = false
          
          // 将结束片段中的historyId和角色信息附加到消息
          if (segment.historyId && !assistantMessage.id) {
            console.log(`为消息附加历史ID: ${segment.historyId}`)
            assistantMessage.id = segment.historyId
          }
          
          // 更新角色信息（如果有）
          if (segment.roleName) {
            assistantMessage.name = segment.roleName
          }
          
          if (segment.roleAvatarPath) {
            assistantMessage.avatarPath = segment.roleAvatarPath && segment.roleAvatarPath.startsWith('/res/') 
              ? segment.roleAvatarPath 
              : segment.roleAvatarPath ? `/res/${segment.roleAvatarPath}` : null
          }
          
          // 强制更新视图
          messages.value = [...messages.value]
          
          console.log('流式响应完成，最终消息:', assistantMessage)
          
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
    // 调用终止响应接口
    await axios.post('/model/rp/rpCompleteBatch', {
      threadId: currentThreadId.value,
      queryKind: 2  // 2:终止AI响应
    })
    
    console.log('已终止AI响应')
    isLoading.value = false
  } catch (error) {
    console.error('终止响应失败:', error)
    // 即使出错也要将loading状态重置
    isLoading.value = false
  }
}

// 工具函数 - 保留throttle函数，可能在某处被使用
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
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  color: #fff;
  min-height: 0;
  position: relative;
  border-radius: 0 !important;
}

.chat-layout {
  display: flex;
  height: 100%;
  min-height: 0;
  border-radius: 0 !important;
  position: relative;
  overflow: hidden;
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
  border-radius: 0 !important;
  backdrop-filter: blur(v-bind(mainBlur));
  -webkit-backdrop-filter: blur(v-bind(mainBlur));
}

.model-select {
  padding: 8px 20px;
  display: flex;
  align-items: center;
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

/* 移动端适配 */
.mobile-menu-btn {
  display: none;
  position: fixed;
  left: 12px;
  top: 12px;
  z-index: 1001;
  padding: 6px 10px;
  font-size: 12px;
  width: auto;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
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
    top: 0;
    left: -240px;
    bottom: 0;
    z-index: 100;
    transform: none;
    height: 100vh;
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.3);
    background-color: rgba(20, 30, 40, 0.95);
  }
  
  .thread-list.show {
    transform: translateX(240px);
  }
  
  .mobile-menu-btn {
    display: flex !important;
  }
  
  .thread-list-mask {
    display: block;
  }
}

.chat-messages-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  position: relative;
  overflow: hidden;
  width: 100%;
  max-width: 100%;
}
</style> 