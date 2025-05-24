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
        v-for="(msg, index) in messages"
        :key="msg.id"
        :message="msg"
        :disabled="props.isGenerating"
        :allow-regenerate="index === messages.length - 1"
        @select-message="handleSelectMessage"
        @delete-message="handleDeleteMessage"
        @update-message="handleUpdateMessageForward"
        @regenerate="handleRegenerateForward"
      />
    </div>
  </GlowDiv>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, nextTick, watch } from 'vue'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue"
import ImMessageBoxItem from "@/components/glow-client/ImMessageBoxItem.vue"
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'

interface Message {
  id: string, //消息记录ID(-1为临时消息)
  name: string, //发送者名称
  avatarPath: string //头像路径
  role: string //消息类型：0-用户消息，1-AI消息
  content: string //消息内容
  createTime: string | null//消息时间
}

// 获取 glow 主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 消息列表引用
const messagesContainer = ref<HTMLDivElement | null>(null)

// 消息列表数据 (内部状态)
const messages = ref<Message[]>([])

// 定义组件props
const props = defineProps<{
  data?: Message[];
  isGenerating?: boolean;
}>()

// 滚动到底部 (定义移到 watch 之前)
const scrollToBottom = () => {
  nextTick(() => {
    const container = messagesContainer.value
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

// --- 监听外部 props.data 的变化 --- 
watch(() => props.data, (newData) => {
  messages.value = newData || []; // 使用新的数据更新内部 messages
  scrollToBottom(); // 数据更新后滚动到底部
}, { 
  immediate: true, // 立即执行一次，处理初始数据
  deep: true // 深度监听，如果 Message 对象内部可能变化 (通常不需要)
});

//当前选择的消息ID
const selectMessageId = ref<string>()

// 事件定义
const emit = defineEmits<{
  (e: 'select-message', msgId: string): void;
  (e: 'update-message', params:{
      msgId: string,  //消息ID
      message: string //更新后的消息
  }): void;
  (e: 'delete-message', msgId: string): void;
  (e: 'regenerate', msgId: string): void;
}>()

// 处理消息选择
const handleSelectMessage = (msgId: string) => {
  selectMessageId.value = msgId
  emit('select-message', msgId)
}

// 处理消息删除
const handleDeleteMessage = (msgId: string) => {
  emit('delete-message', msgId); // Re-emit the event upwards
}

// 处理消息更新转发
const handleUpdateMessageForward = (params: { msgId: string; message: string }) => {
  emit('update-message', params); // Re-emit the event upwards
};

// 处理重新生成转发
const handleRegenerateForward = (msgId: string) => {
  emit('regenerate', msgId); // Re-emit the event upwards
};

// 初始化
onMounted(() => {
  scrollToBottom()
})

// 暴露方法给父组件
defineExpose({
  selectMessageId,
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