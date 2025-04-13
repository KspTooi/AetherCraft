<template>
  <Teleport to="body">
    <div v-if="visible" class="glow-input-overlay" @click="handleOverlayClick">
      <div class="glow-input-modal-container" :style="modalStyle" @click.stop>
        <!-- 顶部发光线条 -->
        <div class="modal-glow-border"></div>
        
        <!-- 输入框内容区域 -->
        <div class="glow-input-body">
          <!-- 自定义头部 -->
          <div class="glow-input-header">
            <div class="input-icon">
              <i class="bi bi-pencil-square"></i>
            </div>
            <h3 class="input-title">{{ title }}</h3>
          </div>

          <!-- 输入内容 -->
          <div class="glow-input-content">
            <div class="glow-input-field-container">
              <input 
                ref="inputRef"
                v-model="inputValue" 
                class="glow-input-field" 
                :type="type"
                :placeholder="placeholder"
                @keyup.enter="handleConfirm"
              />
            </div>
            <div v-if="message" class="glow-input-message">{{ message }}</div>
          </div>

          <!-- 按钮区域 -->
          <div class="glow-input-buttons">
            <GlowButton
                :corners="['bottom-left']"
                class="confirm-btn"
                :background-color="theme.boxAccentColorHover"
                :border-color="theme.boxBorderColorHover"
                @click="handleConfirm">
              {{ confirmText }}
            </GlowButton>
            <GlowButton
                :corners="['bottom-right']"
                class="cancel-btn"
                background-color="rgba(60, 60, 60, 0.5)"
                border-color="rgba(120, 120, 120, 0.5)"
                @click="handleCancel">
              {{ cancelText }}
            </GlowButton>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, nextTick, inject, computed } from 'vue'
import GlowButton from './GlowButton.vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from './GlowTheme.ts'
import type { GlowThemeColors } from './GlowTheme.ts'

// --- Props (保留可能需要的，或转换为内部状态) ---
const props = defineProps({
  // 模态框宽度 (可以保留为 prop 或设为固定值)
  width: {
    type: String,
    default: '320px' 
  },
  // 点击遮罩层是否关闭 (可以保留为 prop 或设为固定值)
  closeOnClickOverlay: {
    type: Boolean,
    default: true 
  },
  // 可以添加其他从GlowModal继承的props...
})

// --- 主题注入 ---
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// --- 状态 ---
const visible = ref(false) // 直接控制自身可见性
const title = ref('请输入')
const inputValue = ref('')
const placeholder = ref('请在此输入...')
const message = ref('')
const type = ref('text')
const confirmText = ref('确定')
const cancelText = ref('取消')
const resolvePromise = ref<((value: {confirmed: boolean, value: string}) => void) | null>(null)
const inputRef = ref<HTMLInputElement | null>(null)

// --- 计算属性 (从 GlowModal 合并) ---
const modalStyle = computed(() => ({
  width: props.width,
  // 可以添加 maxHeight 等其他样式
}));

// --- 方法 (合并和调整) ---

// 关闭模态框 (现在是关闭自身)
const closeModal = () => {
  visible.value = false;
  // 如果有取消回调，需要在这里处理
  if (resolvePromise.value) {
    resolvePromise.value({ confirmed: false, value: inputValue.value }); // 取消时也传递当前值
    resolvePromise.value = null;
  }
};

// 点击遮罩层 (从 GlowModal 合并)
const handleOverlayClick = () => {
  if (props.closeOnClickOverlay) {
    closeModal();
  }
};

// 处理确认
const handleConfirm = () => {
  visible.value = false
  if (resolvePromise.value) {
    resolvePromise.value({
      confirmed: true,
      value: inputValue.value
    })
    resolvePromise.value = null
  }
}

// 处理取消 (现在调用 closeModal)
const handleCancel = () => {
  closeModal();
}

// 显示输入对话框 (核心方法)
const showInput = async (options?: {
  title?: string
  defaultValue?: string
  placeholder?: string
  message?: string
  type?: string
  confirmText?: string
  cancelText?: string
  width?: string // 可以覆盖默认宽度
  closeOnClickOverlay?: boolean // 可以覆盖默认行为
}): Promise<{confirmed: boolean, value: string}> => {
  
  // 更新选项
  title.value = options?.title ?? '请输入';
  inputValue.value = options?.defaultValue ?? '';
  placeholder.value = options?.placeholder ?? '请在此输入...';
  message.value = options?.message ?? '';
  type.value = options?.type ?? 'text';
  confirmText.value = options?.confirmText ?? '确定';
  cancelText.value = options?.cancelText ?? '取消';
  
  // 可以选择性地更新 props 或内部状态
  // 例如，如果允许通过 options 覆盖宽度:
  // if (options?.width) modalStyle.value.width = options.width; // 需要调整 modalStyle 为 ref 或直接修改 props

  // 显示模态框
  visible.value = true
  
  // 等待DOM更新后聚焦输入框
  await nextTick()
  if (inputRef.value) {
    inputRef.value.focus()
  }

  // 返回Promise
  return new Promise((resolve) => {
    resolvePromise.value = resolve
  })
}

// 暴露方法
defineExpose({
  showInput
})
</script>

<style scoped>
/* --- 从 GlowModal 合并的样式 --- */
.glow-input-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2147483646; /* 保持高层级 */
  animation: glow-input-fadeIn 0.2s ease;
}

@keyframes glow-input-fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.glow-input-modal-container {
  /* 基本样式 */
  border-radius: 0; /* 保持直角 */
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止内容溢出 */
  animation: glow-input-modalIn 0.25s ease;
  position: relative;
  padding: 0; /* 内部 padding 由 glow-input-body 控制 */
  
  /* 主题相关样式 */
  background-color: v-bind("theme.boxAccentColor");
  border: 1px solid v-bind("theme.boxBorderColor");
  transition: background-color 0.3s ease, backdrop-filter 0.3s ease;
  color: v-bind("theme.boxTextColor");
  backdrop-filter: blur(v-bind("theme.boxBlur + 'px'"));
  -webkit-backdrop-filter: blur(v-bind("theme.boxBlur + 'px'"));
}

.glow-input-modal-container:hover {
  background-color: v-bind("theme.boxAccentColorHover");
  backdrop-filter: blur(v-bind("theme.boxBlurHover + 'px'"));
  -webkit-backdrop-filter: blur(v-bind("theme.boxBlurHover + 'px'"));
}

@keyframes glow-input-modalIn {
  from { 
    opacity: 0;
    /* transform: scale(0.95); 可选的进入动画 */
  }
  to { 
    opacity: 1;
    /* transform: scale(1); */
  }
}

.modal-glow-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: v-bind("theme.boxGlowColor");
  box-shadow: 0 0 10px 1px v-bind("theme.boxGlowColor");
  z-index: 1; /* 在内容之上 */
}

/* --- GlowConfirmInput 自身样式 (需要调整以适应新结构) --- */

/* .glow-input-container 已经被 .glow-input-modal-container 替代 */
/* 内部内容容器 */
.glow-input-body {
  padding: 15px; /* 从 modal-body 继承 */
  /* 可能需要添加 overflow-y: auto; 和 max-height (如果内容可能超长) */
  /* max-height: calc(80vh - 40px); 示例，需要根据设计调整 */
  box-sizing: border-box;
  width: 100%; /* 确保撑满容器 */
}

.glow-input-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
  width: 100%;
  box-sizing: border-box;
  padding-top: 5px; /* 避免与辉光线重叠太近 */
}

.input-icon {
  font-size: 24px;
  color: v-bind("theme.boxGlowColor");
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.input-title {
  font-size: 16px;
  color: v-bind("theme.boxTextColor");
  margin: 0;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.glow-input-content {
  margin-bottom: 20px;
  /* padding-left 和 padding-right 由 glow-input-body 控制 */
  width: 100%;
  box-sizing: border-box;
}

.glow-input-field-container {
  position: relative;
  margin-bottom: 8px;
  width: 100%;
  box-sizing: border-box;
}

.glow-input-field {
  width: 100%;
  background-color: rgba(0, 0, 0, 0.2);
  border: 1px solid v-bind("theme.boxBorderColor");
  /* border-radius: 4px; 之前的修改移除了圆角 */
  color: v-bind("theme.boxTextColor");
  padding: 8px 12px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s, box-shadow 0.3s; /* 优化 transition */
  box-sizing: border-box;
}

.glow-input-field:focus {
  border-color: v-bind("theme.boxGlowColor");
}

.glow-input-field:hover:not(:focus) {
  border-color: v-bind("theme.boxBorderColorHover");
}

.glow-input-message {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
  word-break: break-word;
}

.glow-input-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 10px;
  width: 100%;
  box-sizing: border-box;
}

.confirm-btn, .cancel-btn {
  flex: 1;
  height: 32px;
  min-height: 32px;
  font-size: 14px;
  max-width: 120px;
}

/* 可以添加滚动条样式，如果 .glow-input-body 需要滚动 */
/*
.glow-input-body::-webkit-scrollbar {
  width: 4px;
}
.glow-input-body::-webkit-scrollbar-track {
  background: transparent;
}
.glow-input-body::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 0;
}
.glow-input-body::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.boxBorderColor');
}
*/
</style>