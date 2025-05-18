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
        <span class="time">{{ props.message.createTime }}</span>
        <span v-if="props.message.id === '-1'" class="typing-indicator">
          <span>正在输入</span>
          <span class="dot">.</span>
          <span class="dot">.</span>
          <span class="dot">.</span>
        </span>
      </div>
      <!-- Original Text / Markdown View -->
      <div v-if="!isEditing" class="text" :class="{ 'user': props.message.role === 'user' }">
        <div v-if="props.message.role === 'user'">{{ props.message.content }}</div>
        <div v-else v-html="renderMarkdown(props.message.content)"></div>
      </div>
      <!-- Editing View (using contenteditable div) -->
      <div v-else 
           ref="editableContentRef" 
           class="text editable-content" 
           :contenteditable="true" 
           @blur="handleBlur" 
           @keydown.enter.exact.prevent="handleConfirmEdit" 
           @keydown.esc.exact.prevent="handleCancelEdit"
           v-text="props.message.content" 
      ></div>
    </div>
    <!-- Only show actions for non-temporary messages -->
    <div v-if="props.message.id !== '-1'" class="message-actions">
      <!-- View Mode Actions -->
      <template v-if="!isEditing">
        <!-- Regenerate Button -->
        <button v-if="props.allowRegenerate" 
                class="message-regenerate-btn" 
                @click="handleRegenerate"
                :disabled="props.disabled" 
                title="重新生成">
          <i class="bi bi-arrow-repeat"></i>
        </button>
        <button class="message-edit-btn" 
                @click="handleEdit()"
                :disabled="props.disabled || props.message.id === '-1'" 
                title="编辑消息">
          <i class="bi bi-pencil"></i>
        </button>
        <button class="message-delete-btn" 
                @click="handleDelete(props.message.id)"
                :disabled="props.disabled || props.message.id === '-1'" 
                title="删除消息">
          <i class="bi bi-trash"></i>
        </button>
      </template>
      <!-- Edit Mode Actions -->
      <template v-else>
         <button class="message-confirm-btn" 
                @click="handleConfirmEdit"
                title="确认编辑 (Enter)">
          <i class="bi bi-check-lg"></i>
        </button>
        <button class="message-cancel-btn" 
                @click="handleCancelEdit"
                title="取消编辑 (Esc)">
          <i class="bi bi-x-lg"></i>
        </button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, nextTick } from 'vue'
import { marked } from 'marked'
import { GLOW_THEME_INJECTION_KEY, type GlowThemeColors } from '../glow-ui/GlowTheme'

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY) || {} as GlowThemeColors

// --- State for Editing ---
const isEditing = ref(false)
const editableContentRef = ref<HTMLDivElement | null>(null)

// 定义组件props
const props = defineProps<{
  message:{
    id: string, //消息记录ID(-1为临时消息)
    name: string, //发送者名称
    avatarPath: string //头像路径
    role: string //消息类型：0-用户消息，1-AI消息
    content: string //消息内容
    createTime: string //消息时间（后端已格式化的时间字符串）
  }
  disabled: boolean //如果为true 则无法点击 编辑、删除按钮
  allowRegenerate?: boolean
}>()

const emit = defineEmits<{
  (e: 'select-message', msgId: string): void;
  (e: 'update-message', params: {
    msgId: string,  //消息ID
    message: string //更新后的消息
  }): void;
  (e: 'delete-message', msgId: string): void;
  (e: 'regenerate', msgId: string): void;
}>()

// 渲染Markdown
const renderMarkdown = (content: string) => {
  if (!content) return ''
  return marked.parse(content) as string
}

// 处理消息编辑 - Start Editing
const handleEdit = () => {
  if (props.disabled) return; // Prevent editing if disabled
  isEditing.value = true;
  nextTick(() => {
    // Focus the contenteditable div
    const el = editableContentRef.value;
    if (el) {
      el.focus();
      // Optional: Move cursor to the end
      const range = document.createRange();
      const sel = window.getSelection();
      range.selectNodeContents(el);
      range.collapse(false);
      sel?.removeAllRanges();
      sel?.addRange(range);
    }
  });
};

// 处理确认编辑
const handleConfirmEdit = () => {
  const currentContent = editableContentRef.value?.textContent?.trim() || '';
  if (!currentContent) {
    console.warn('Edit content cannot be empty');
    return;
  }
  if (currentContent === props.message.content) {
    isEditing.value = false;
    return;
  }
  emit('update-message', {
    msgId: props.message.id,
    message: currentContent // Use textContent from the div
  });
  isEditing.value = false; 
};

// 处理取消编辑
const handleCancelEdit = () => {
  isEditing.value = false;
  // The div will automatically revert to showing the original content via v-text when re-rendered
};

// 处理失焦 (Optional: Decide behavior - cancel or confirm?)
// For now, let's just cancel editing on blur unless handled by Enter/Esc
const handleBlur = (event: FocusEvent) => {
  // Check if the blur was caused by clicking the confirm/cancel buttons
  // relatedTarget is the element receiving focus
  const relatedTarget = event.relatedTarget as HTMLElement | null;
  if (
    relatedTarget && 
    (relatedTarget.classList.contains('message-confirm-btn') || 
     relatedTarget.classList.contains('message-cancel-btn'))
  ) {
     // Don't cancel if focus moved to confirm/cancel buttons
    return; 
  }
  
  // If still in editing mode after a short delay (allowing button clicks)
  // We cancel editing. Alternatively, you could confirm here.
  setTimeout(() => { 
      if (isEditing.value) { 
          handleCancelEdit();
      }
  }, 100); // Small delay
};

// 处理消息删除
const handleDelete = (messageId: string) => {
  emit('delete-message', messageId)
}

// 处理重新生成 (New function)
const handleRegenerate = () => {
  emit('regenerate', props.message.id);
};

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

/* Typing indicator styles */
.typing-indicator {
  margin-left: 8px;
  display: inline-flex;
  align-items: baseline;
}

.typing-indicator span {
  opacity: 0.7;
}

.typing-indicator .dot {
  display: inline-block;
  animation: blink 1.4s infinite both;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  0% {
    opacity: 0.2;
  }
  20% {
    opacity: 1;
  }
  100% {
    opacity: 0.2;
  }
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

/* Editing Styles for contenteditable div */
.editable-content {
  width: 100%;
  min-height: 3em; /* Use em for relative height, adjust as needed */
  padding: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 0; /* Set border-radius to 0 for sharp corners */
  color: #fff;
  font-size: 14px; /* Ensure consistent font size */
  line-height: 1.5; /* Ensure consistent line height */
  font-family: inherit; /* Ensure consistent font family */
  overflow-y: auto; /* Allow scrolling if content overflows */
  white-space: pre-wrap; /* Preserve whitespace and wrap lines */
  word-break: break-word; /* Ensure long words break */
  box-sizing: border-box;
  cursor: text; /* Indicate text is editable */
}

.editable-content:focus {
  outline: none;
  border-color: rgba(79, 172, 254, 0.5);
  background: rgba(255, 255, 255, 0.15);
}

/* Edit/Confirm/Cancel Button Styles (reuse existing if possible or add specific) */
.message .message-confirm-btn,
.message .message-cancel-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  padding: 4px;
  font-size: 16px;
  line-height: 1;
  transition: all 0.2s;
}

.message .message-confirm-btn:hover,
.message .message-cancel-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.message .message-confirm-btn {
  color: rgba(75, 210, 143, 0.7); /* Greenish */
}

.message .message-confirm-btn:hover {
  color: rgba(75, 210, 143, 1);
}

.message .message-cancel-btn {
  color: rgba(255, 107, 107, 0.7); /* Reddish */
}

.message .message-cancel-btn:hover {
  color: rgba(255, 107, 107, 1);
}

/* Ensure actions are always visible in edit mode */
.message.editing .message-actions { 
  visibility: visible; 
  opacity: 1; 
}

/* Regenerate Button Style */
.message .message-regenerate-btn {
  background: transparent;
  border: none;
  color: rgba(79, 172, 254, 0.7); /* Bluish color */
  cursor: pointer;
  padding: 4px;
  font-size: 16px;
  line-height: 1;
  transition: all 0.2s;
}

.message .message-regenerate-btn:hover {
  background: rgba(79, 172, 254, 0.1);
  color: rgba(79, 172, 254, 1);
}

.message-regenerate-btn:disabled {
   opacity: 0.3 !important;
   cursor: not-allowed !important;
}
</style>