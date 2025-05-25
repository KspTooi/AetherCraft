<template>
  <GlowDiv class="im-message-box" border="none">
    <!-- 加载状态 - 顶部发光装饰条 -->
    <Transition name="glow-fade">
      <div v-if="internalLoading" class="loading-glow-border"></div>
    </Transition>
    
    <!-- 空状态提示 -->
    <div v-if="messages.length === 0" class="empty-state">
      <i class="bi bi-chat-dots"></i>
      <div class="title">暂无消息</div>
      <div class="subtitle">开始对话创建第一条消息</div>
    </div>
    
    <!-- 消息列表 -->
    <div v-else class="messages-container" ref="messagesContainer">
      <ChatMessageItem
        v-for="(msg, index) in messages"
        :key="msg.id"
        :message="msg"
        :disabled="props.isGenerating"
        :allow-regenerate="shouldAllowRegenerate(msg, index)"
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
import ChatMessageItem from "@/components/glow-client/ChatMessageItem.vue"
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

// 内部loading状态，用于控制发光条的延迟消失
const internalLoading = ref<boolean>(false)

// 定义组件props
const props = defineProps<{
  data?: Message[];
  isGenerating?: boolean;
  loading?: boolean;
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

// --- 监听loading状态变化，实现延迟消失效果 ---
watch(() => props.loading, (newLoading) => {
  if (newLoading) {
    // 开始加载时立即显示
    internalLoading.value = true;
  } else {
    // 加载完成时延迟0.5秒后消失，确保有足够时间显示完成状态
    setTimeout(() => {
      internalLoading.value = false;
    }, 500);
  }
}, { immediate: true });

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

// 添加一个新的函数来判断是否应该显示重新生成按钮
const shouldAllowRegenerate = (msg: Message, index: number): boolean => {
  // 只有最后一条消息才可能显示重新生成按钮
  if (index !== messages.value.length - 1) {
    return false;
  }
  
  // 如果只有一条消息且这条消息是NPC消息，则不显示重新生成按钮
  if (messages.value.length === 1 && (msg.role === 'model' || msg.role === '1')) {
    return false;
  }
  
  // 其他情况下，最后一条消息可以显示重新生成按钮
  return true;
}
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

/* 加载状态样式 */
.loading-glow-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 15px 2px v-bind('theme.boxGlowColor');
  z-index: 10;
  animation: glowPulse 2s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { 
    box-shadow: 0 0 15px 2px v-bind('theme.boxGlowColor');
  }
  50% { 
    box-shadow: 0 0 25px 4px v-bind('theme.boxGlowColor');
  }
}

/* Transition动画样式 - 确保渐隐效果明显 */
.glow-fade-enter-active {
  transition: all 0.3s ease-out;
}

.glow-fade-leave-active {
  transition: all 0.8s ease-out;
}

.glow-fade-enter-from,
.glow-fade-leave-to {
  opacity: 0;
  transform: scaleY(0);
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