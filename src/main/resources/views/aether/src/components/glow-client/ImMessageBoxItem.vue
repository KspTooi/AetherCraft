<template>
  <div :class="['message', 'message-hover-effect', props.message.role === 'user' ? 'user' : 'assistant']">
    <div class="message-header">
      <div class="avatar" :class="{ 'no-image': !props.message.avatarPath }">
        <img v-if="props.message.avatarPath" :src="props.message.avatarPath" :alt="props.message.name">
        <i v-else class="bi bi-person"></i>
      </div>
    </div>
    <div class="message-content">
      <div class="name">
        {{ props.message.name }}
        <span class="time">{{ formatTime(props.message.createTime) }}</span>
      </div>
      <div class="text" :class="{ 'user': props.message.role === 'user' }">
        <div v-if="props.message.role === 'user'">{{ props.message.content }}</div>
        <div v-else v-html="renderMarkdown(props.message.content)"></div>
      </div>
    </div>
    <div class="message-actions">
      <button class="message-edit-btn" 
              @click="handleEdit(props.message.id)"
              :disabled="props.disabled"
              title="编辑消息">
        <i class="bi bi-pencil"></i>
      </button>
      <button class="message-delete-btn" 
              @click="handleDelete(props.message.id)"
              :disabled="props.disabled"
              title="删除消息">
        <i class="bi bi-trash"></i>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { inject } from 'vue'
import { marked } from 'marked'
import { GLOW_THEME_INJECTION_KEY, type GlowThemeColors } from '../glow-ui/GlowTheme'

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY) || {} as GlowThemeColors

// 定义组件props
const props = defineProps<{
  message:{
    id: string, //消息记录ID(-1为临时消息)
    name: string, //发送者名称
    avatarPath: string //头像路径
    role: string //消息类型：0-用户消息，1-AI消息
    content: string //消息内容
    createTime: string //消息时间
  }
  disabled: boolean //如果为true 则无法点击 编辑、删除按钮
}>()

const emit = defineEmits<{
  (e: 'select-message', msgId: string): void;
  (e: 'update-message', params: {
    msgId: string,  //消息ID
    message: string //更新后的消息
  }): void;
  (e: 'delete-message', msgId: string): void;
}>()

// 格式化时间
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
    return ''
  }
}

// 渲染Markdown
const renderMarkdown = (content: string) => {
  if (!content) return ''
  return marked.parse(content) as string
}

// 处理消息编辑
const handleEdit = (messageId: string) => {
  emit('select-message', messageId)
}

// 处理消息删除
const handleDelete = (messageId: string) => {
  emit('delete-message', messageId)
}

// 暴露方法给父组件
defineExpose({
  
})
</script>

<style scoped>
.message {
  width: 100%;
  padding: 6px 16px 6px 2px;
  line-height: 1.5;
  font-size: 14px;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  position: relative;
  box-sizing: border-box;
  display: flex;
  gap: 8px;
  transition: background-color 0.3s ease;
  background-color: transparent;
  margin: 0;
  padding-right: 60px; /* 为操作按钮预留空间 */
}

.message .message-header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.message .message-header .avatar {
  width: 28px;
  height: 28px;
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
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 2px;
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
  word-break: break-word;
}

/* Markdown 内容样式 */
.message .message-content .text :deep(p) {
  margin-top: 0;
  margin-bottom: 10px;
}

.message .message-content .text :deep(ul), 
.message .message-content .text :deep(ol) {
  margin-top: 0;
  margin-bottom: 10px;
  padding-left: 20px;
}

.message .message-content .text :deep(pre) {
  background: rgba(0, 0, 0, 0.2);
  padding: 10px;
  overflow-x: auto;
  margin: 10px 0;
}

.message .message-content .text :deep(code) {
  font-family: 'Courier New', Courier, monospace;
  padding: 2px 4px;
  background: rgba(0, 0, 0, 0.2);
}

.message .message-content .text :deep(pre code) {
  padding: 0;
  background: transparent;
}

.message .message-content .text :deep(a) {
  color: #4dabf7;
  text-decoration: none;
}

.message .message-content .text :deep(a:hover) {
  text-decoration: underline;
}

.message .message-content .text :deep(blockquote) {
  border-left: 3px solid rgba(255, 255, 255, 0.2);
  margin: 10px 0;
  padding-left: 10px;
  color: rgba(255, 255, 255, 0.7);
}

.message .message-actions {
  position: absolute;
  right: 8px;
  top: 8px;
  visibility: hidden; /* 改用visibility而不是display */
  opacity: 0;
  z-index: 10;
  display: flex; /* 保持display:flex但默认不可见 */
  gap: 4px;
  transition: visibility 0.2s, opacity 0.2s; /* 添加过渡效果 */
}

.message:hover .message-actions {
  visibility: visible;
  opacity: 1;
}

.message .message-delete-btn,
.message .message-edit-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  padding: 4px;
  font-size: 16px;
  line-height: 1;
  transition: all 0.2s;
}

.message .message-delete-btn:hover,
.message .message-edit-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.message-edit-btn:disabled,
.message-delete-btn:disabled {
  opacity: 0.3 !important;
  cursor: not-allowed !important;
}

/* 鼠标悬浮效果 - 使用backdrop-filter实现毛玻璃效果 */
.message.message-hover-effect {
  position: relative;
  z-index: 1;
}

.message.message-hover-effect::before {
  content: '';
  position: absolute;
  top: 0;
  left: -10px;
  right: 0;
  bottom: 0;
  z-index: -1;
  background-color: v-bind('theme.boxAccentColorHover');
  opacity: 0;
  transition: opacity 0.3s ease, backdrop-filter 0.3s ease;
}

.message.message-hover-effect:hover {
  backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
}

.message.message-hover-effect:hover::before {
  opacity: 1;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .message {
    font-size: 13px;
    padding: 6px 12px 6px 2px;
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
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