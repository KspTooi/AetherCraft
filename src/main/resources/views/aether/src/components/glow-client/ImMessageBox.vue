<template>
  <GlowDiv class="im-message-box" border="none">
    <!-- 空状态提示 -->
    <div v-if="messages.length === 0" class="empty-state">
      <i class="bi bi-chat-dots"></i>
      <div class="title">暂无消息</div>
      <div class="subtitle">开始对话创建第一条消息</div>
    </div>
    
    <!-- 消息列表 -->
    <div v-else class="messages-container" ref="messagesContainer">
      <ImMessageBoxItem
        v-for="msg in messages"
        :key="msg.id"
        :message="msg"
        :disabled="hasTempMessage"
        @select-message="handleSelectMessage"
        @delete-message="handleDeleteMessage"
      />
    </div>
  </GlowDiv>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, nextTick } from 'vue'
import axios from 'axios'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue"
import GlowButton from "@/components/glow-ui/GlowButton.vue"
import ImMessageBoxItem from "@/components/glow-client/ImMessageBoxItem.vue"
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'

interface Message {
  id: string, //消息记录ID(-1为临时消息)
  name: string, //发送者名称
  avatarPath: string //头像路径
  role: string //消息类型：0-用户消息，1-AI消息
  content: string //消息内容
  createTime: string //消息时间
}

// 获取 glow 主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 消息列表引用
const messagesContainer = ref<HTMLDivElement | null>(null)

// 消息列表数据
const messages = ref<Message[]>([])

// 定义组件props
const props = defineProps<{
  messages?: Message[]
}>()

// 监听外部消息列表变化
if (props.messages) {
  messages.value = props.messages
}

//当前选择的消息ID
const selectMessageId = ref<string>()

//当前是否有临时消息
const hasTempMessage = ref<boolean>(false)

// 事件定义
const emit = defineEmits<{
  (e: 'select-message', msgId: string): void;
  (e: 'update-message', params:{
      msgId: string,  //消息ID
      message: string //更新后的消息
  }): void;
  (e: 'delete-message', msgId: string): void;
}>()

// 处理消息选择
const handleSelectMessage = (msgId: string) => {
  selectMessageId.value = msgId
  emit('select-message', msgId)
}

// 处理消息删除
const handleDeleteMessage = (msgId: string) => {
  emit('delete-message', msgId)
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    const container = messagesContainer.value
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

//创建一个临时消息 用于AI流式回复(如果当前已有临时消息则不创建) 临时消息的ID固定为-1
const createTempMessage = (msg: Message) => {
  if (hasTempMessage.value) return
  
  // 确保临时消息的ID为-1
  const tempMessage = {
    ...msg,
    id: '-1'
  }
  
  // 添加临时消息到消息列表
  messages.value.push(tempMessage)
  hasTempMessage.value = true
  
  // 滚动到底部
  scrollToBottom()
}

//向临时消息中追加更多内容
const appendTempMessage = (content: string) => {
  if (!hasTempMessage.value) return
  
  // 找到临时消息
  const tempMessage = messages.value.find(msg => msg.id === '-1')
  if (tempMessage) {
    tempMessage.content += content
    
    // 滚动到底部
    scrollToBottom()
  }
}

//移除当前的临时消息(如果当前没有则什么都不做)
const deleteTempMessage = () => {
  if (!hasTempMessage.value) return
  
  // 移除临时消息
  messages.value = messages.value.filter(msg => msg.id !== '-1')
  hasTempMessage.value = false
}

// 初始化
onMounted(() => {
  scrollToBottom()
})

// 暴露方法给父组件
defineExpose({
  selectMessageId,
  createTempMessage,
  appendTempMessage,
  deleteTempMessage,
  scrollToBottom
})
</script>

<style scoped>
.im-message-box {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  border-radius: 8px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 自定义滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
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

/* 移动端适配 */
@media (max-width: 768px) {
  .messages-container {
    padding: 10px 12px;
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  .messages-container {
    padding: 8px 10px;
  }
  
  .empty-state i {
    font-size: 36px;
  }
  
  .empty-state .title {
    font-size: 16px;
  }
  
  .empty-state .subtitle {
    font-size: 12px;
  }
}
</style>