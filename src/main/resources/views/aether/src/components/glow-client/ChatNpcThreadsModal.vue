<template>
  <Teleport to="body">
    <div v-if="visible" class="thread-modal-overlay" @click.self="closeModal">
      <div class="thread-modal-container">
        <!-- 顶部发光线条 -->
        <div class="modal-glow-border"></div>
        
        <!-- 模态框头部 -->
        <div class="thread-modal-header">
          <h3 class="thread-modal-title">{{ selectedRoleName }} 的会话列表</h3>
          
          <!-- 搜索框 -->
          <div class="thread-search-container">
            <div class="search-input-wrapper">
              <i class="bi bi-search search-icon"></i>
              <input 
                v-model="searchKeyword"
                @input="handleSearch"
                class="search-input"
                type="text"
                placeholder="搜索标题..."
              />
              <button 
                v-if="searchKeyword"
                @click="clearSearch"
                class="clear-search-btn"
                title="清除搜索">
                <i class="bi bi-x"></i>
              </button>
            </div>
          </div>
          
          <button class="thread-modal-close" @click="closeModal">×</button>
        </div>

        <div class="thread-modal-body">
          <!-- 加载状态 - 顶部发光装饰条 -->
          <Transition name="glow-fade">
            <div v-if="internalLoading" class="loading-glow-border"></div>
          </Transition>
          
          <div v-if="loading && !internalLoading" class="loading-state">
            <i class="bi bi-arrow-repeat spinning"></i>
            加载中...
          </div>
          <div v-else-if="threads.length === 0" class="empty-state">
            <i class="bi bi-chat-square-text"></i>
            <div class="empty-title">还没有会话记录</div>
            <div class="empty-desc">选择角色后发送消息自动创建会话</div>
          </div>
          <div v-else class="thread-list">
            <div v-for="thread in threads" 
                 :key="thread.id"
                 @click="handleThreadSelect(thread.id)"
                 class="thread-item"
                 :class="{ 'active': currentThreadId === thread.id }">
              <div class="thread-content">
                <div class="thread-title">
                  {{ thread.title || '未命名会话' }}
                  <span v-if="thread.active === 1" class="active-badge">当前</span>
                </div>
                <div class="thread-time">{{ formatTime(thread.createTime) }}</div>
                <div class="thread-message" v-if="thread.lastMessage">{{ thread.lastMessage }}</div>
                <div class="thread-message" v-else>暂无对话内容</div>
              </div>
              <div class="thread-actions">
                <button 
                  class="thread-action-btn edit-btn"
                  @click.stop="handleEditThreadTitle(thread.id, thread.title)"
                  title="编辑标题">
                  <i class="bi bi-pencil"></i>
                </button>
                <button 
                  v-if="thread.active !== 1"
                  class="thread-action-btn activate-btn"
                  @click.stop="handleActivateThread(thread.id)"
                  title="激活会话">
                  <i class="bi bi-check-circle"></i>
                </button>
                <button 
                  class="thread-action-btn delete-btn"
                  @click.stop="handleDeleteThread(thread.id)"
                  :disabled="threads.length <= 1"
                  :class="{ 'disabled': threads.length <= 1 }"
                  title="删除会话">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
  
  <!-- 确认模态框 -->
  <GlowConfirm ref="confirmModalRef" />
  
  <!-- 输入模态框 -->
  <GlowConfirmInput ref="inputModalRef" />
</template>

<script setup lang="ts">
import { ref, inject, watch } from 'vue'
import GlowConfirm from '../glow-ui/GlowConfirm.vue'
import GlowConfirmInput from '../glow-ui/GlowConfirmInput.vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from '../glow-ui/GlowTheme'
import type { GlowThemeColors } from '../glow-ui/GlowTheme'
import ThreadApi, { type GetThreadListDto, type GetThreadListVo, type EditThreadTitleDto } from '@/commons/api/ThreadApi'
import type CommonIdDto from '@/entity/dto/CommonIdDto'

// 当组件单独运行时，如果没有注入主题，则使用默认主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// Emits
const emit = defineEmits<{
  (e: 'activate-thread', roleId: string, threadId: string, modelCode: string): void
  (e: 'reload-thread', threadId: string): void
}>()

// State
const visible = ref(false)
const selectedRoleId = ref('')
const selectedRoleName = ref('')
const threads = ref<GetThreadListVo[]>([])
const loading = ref(false)
const currentThreadId = ref('')
const confirmModalRef = ref<InstanceType<typeof GlowConfirm> | null>(null)
const inputModalRef = ref<InstanceType<typeof GlowConfirmInput> | null>(null)
const searchKeyword = ref('')
const searchTimer = ref<number | null>(null)
const internalLoading = ref(false)

// --- 监听loading状态变化，实现延迟消失效果 ---
watch(() => loading.value, (newLoading) => {
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

// Methods for component
const loadThreadList = async (title?: string) => {
  if (!selectedRoleId.value) return;
  
  loading.value = true
  try {
    const dto: GetThreadListDto = {
      type: 1, // RP会话
      npcId: selectedRoleId.value,
      title: title || undefined, // 添加标题搜索参数
      page: 1,
      pageSize: 1000
    }
    
    const response = await ThreadApi.getThreadList(dto)
    threads.value = response.rows || []
    
    // 获取当前活动的会话ID
    const activeThread = threads.value.find(t => t.active === 1)
    if (activeThread) {
      currentThreadId.value = activeThread.id
    } else if (threads.value.length > 0) {
      currentThreadId.value = threads.value[0].id
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    threads.value = [] // 失败时清空
  } finally {
    loading.value = false
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
    
    return `${year}年${month}月${day}日 ${hours}:${minutes}`
  } catch (e) {
    console.error('时间格式化错误:', e)
    return ''
  }
}

const handleThreadSelect = (threadId: string) => {
  // 点击整个列表项时，调用激活并关闭模态框
  const thread = threads.value.find(t => t.id === threadId);
  if (!thread) {
    console.error('找不到相应的会话');
    return;
  }
  
  // 触发激活事件由父组件处理
  emit('activate-thread', selectedRoleId.value, threadId, thread.modelCode || 'gemini-pro');
  
  // 本地更新UI状态
  threads.value.forEach(t => {
    t.active = t.id === threadId ? 1 : 0;
  });
  currentThreadId.value = threadId;
  
  // 关闭模态框
  closeModal();
}

const handleActivateThread = (threadId: string) => {
  // 只点击激活按钮时，只激活但不关闭模态框
  const thread = threads.value.find(t => t.id === threadId);
  if (!thread) {
    console.error('找不到相应的会话');
    return;
  }
  
  // 触发激活事件由父组件处理
  emit('activate-thread', selectedRoleId.value, threadId, thread.modelCode || 'gemini-pro');
  
  // 本地更新UI状态
  threads.value.forEach(t => {
    t.active = t.id === threadId ? 1 : 0;
  });
  currentThreadId.value = threadId;
  
  // 不关闭模态框
}

const handleDeleteThread = async (threadId: string) => {
  if (!confirmModalRef.value) return
  
  // 检查是否为最后一个会话
  if (threads.value.length <= 1) {
    return // 直接返回，不执行删除操作
  }
  
  const threadTitle = threads.value.find(t => t.id === threadId)?.title || '未命名会话'
  
  const confirmed = await confirmModalRef.value.showConfirm({
    title: '删除会话',
    content: `您确定要删除 "${threadTitle}" 会话吗？此操作不可恢复。`,
    confirmText: '删除',
    cancelText: '取消'
  })
  
  if (!confirmed) return
  
  try {
    const dto: CommonIdDto = { id: threadId }
    await ThreadApi.removeThread(dto)
    
    threads.value = threads.value.filter(t => t.id !== threadId)
    console.log('会话删除成功')

    // 如果当前激活的会话被删除，更新当前会话ID
    if (currentThreadId.value === threadId && threads.value.length > 0) {
      const firstThread = threads.value[0]
      currentThreadId.value = firstThread.id
      
      // 如果没有激活的会话，自动激活第一个会话
      if (!threads.value.find(t => t.active === 1)) {
        firstThread.active = 1
        emit('activate-thread', selectedRoleId.value, firstThread.id, firstThread.modelCode || 'gemini-pro')
      }
    }
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

const closeModal = () => {
  visible.value = false
}

/**
 * 显示会话列表模态框
 * @param roleId 角色ID
 * @param roleName 角色名称
 */
const show = (roleId: string, roleName: string) => {
  selectedRoleId.value = roleId
  selectedRoleName.value = roleName
  visible.value = true
  // loading.value = true // loading 状态由 loadThreadList 内部管理
  
  // 直接在内部加载列表
  loadThreadList()
}

const handleEditThreadTitle = async (threadId: string, currentTitle: string) => {
  if (!inputModalRef.value) return
  
  const result = await inputModalRef.value.showInput({
    title: '编辑会话标题',
    defaultValue: currentTitle,
    placeholder: '请输入新的会话标题'
  })
  
  if (result.confirmed && result.value.trim()) {
    try {
      const dto: EditThreadTitleDto = {
        threadId: threadId,
        title: result.value.trim()
      }
      await ThreadApi.editThreadTitle(dto)
      
      // 更新本地状态
      threads.value.forEach(t => {
        if (t.id === threadId) {
          t.title = result.value.trim()
        }
      })
      console.log('会话标题更新成功')
    } catch (error) {
      console.error('编辑会话标题失败:', error)
    }
  }
}

// 搜索处理（防抖）
const handleSearch = () => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  
  searchTimer.value = setTimeout(() => {
    loadThreadList(searchKeyword.value.trim())
  }, 300) // 300ms 防抖
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  loadThreadList() // 重新加载所有数据
}

// 暴露方法
defineExpose({
  show
})
</script>

<style scoped>
.thread-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2147483646;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.thread-modal-container {
  width: 800px;
  max-width: 90%;
  height: 600px;
  max-height: 80vh;
  background: v-bind('theme.boxAccentColor');
  backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlur + "px"'));
  border-radius: 0;
  border: 1px solid v-bind('theme.boxBorderColor');
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalIn 0.25s ease;
  position: relative;
  transition: background-color 0.3s ease, backdrop-filter 0.3s ease;
}

.thread-modal-container:hover {
  background-color: v-bind('theme.boxAccentColorHover');
  backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlurHover + "px"'));
}

@keyframes modalIn {
  from { 
    transform: translateY(-10px); 
    opacity: 0;
  }
  to { 
    transform: translateY(0); 
    opacity: 1;
  }
}

.modal-glow-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 10px 1px v-bind('theme.boxGlowColor');
  z-index: 1;
}

.thread-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 16px;
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
  position: relative;
  gap: 16px;
  flex-shrink: 0;
}

.thread-modal-title {
  font-size: 16px;
  color: v-bind('theme.boxTextColor');
  margin: 0;
  font-weight: 500;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

.thread-modal-close {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 22px;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0;
  transition: all 0.2s;
}

.thread-modal-close:hover {
  background: v-bind('theme.boxColorHover');
  color: v-bind('theme.boxTextColor');
}

.thread-modal-body {
  flex: 1;
  padding: 0;
  overflow-y: auto;
  min-height: 0;
  position: relative;
}

/* 自定义滚动条样式 */
.thread-modal-body::-webkit-scrollbar {
  width: 4px;
}

.thread-modal-body::-webkit-scrollbar-track {
  background: transparent;
}

.thread-modal-body::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 0;
}

.thread-modal-body::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.boxBorderColor');
}

/* 加载状态 - 顶部发光装饰条 */
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

/* Transition动画样式 */
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

/* 加载状态 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: rgba(255, 255, 255, 0.6);
  gap: 10px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px;
  text-align: center;
  height: 100%;
}

.empty-state i {
  font-size: 42px;
  color: v-bind('theme.boxGlowColor');
  margin-bottom: 15px;
  opacity: 0.3;
}

.empty-title {
  font-size: 16px;
  color: v-bind('theme.boxTextColor');
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: v-bind('theme.boxTextColorNoActive');
}

/* 会话列表 */
.thread-list {
  padding: 4px 8px 8px;
}

.thread-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 15px;
  border-radius: 0;
  margin-bottom: 4px;
  margin-left: 2px;
  margin-right: 2px;
  cursor: pointer;
  background: v-bind('theme.boxColor');
  transition: all 0.3s ease;
  position: relative;
  border-left: 3px solid transparent;
  overflow: hidden;
}

.thread-item:hover {
  background: v-bind('theme.boxAccentColorHover');
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
}

.thread-item.active {
  background: v-bind('theme.boxColorActive');
  border-left: 3px solid v-bind('theme.boxGlowColor');
}

.thread-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.thread-title {
  font-size: 14px;
  color: v-bind('theme.boxTextColor');
  margin-bottom: 4px;
  font-weight: 500;
  display: flex;
  align-items: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.2px;
}

.active-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  background-color: v-bind('theme.boxGlowColor');
  color: rgba(255, 255, 255, 0.95);
  padding: 1px 6px;
  border-radius: 0;
  margin-left: 8px;
  font-weight: normal;
  box-shadow: 0 0 8px v-bind('theme.boxGlowColor');
  height: 18px;
  letter-spacing: 0.5px;
}

.thread-time {
  font-size: 11px;
  color: v-bind('theme.boxGlowColor');
  margin-bottom: 4px;
  opacity: 0.7;
}

.thread-message {
  font-size: 12px;
  color: v-bind('theme.boxTextColorNoActive');
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 500px;
}

.thread-actions {
  display: flex;
  gap: 4px;
  opacity: 1;
  transition: opacity 0.2s ease;
}

.thread-item:hover .thread-actions {
  opacity: 1;
}

.thread-action-btn {
  color: v-bind('theme.boxTextColorNoActive');
  transition: all 0.2s ease;
  border-radius: 0;
  padding: 0;
  font-size: 14px;
  width: 28px;
  height: 28px;
  min-width: 28px;
  min-height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  margin: 0 2px;
  background-color: transparent;
  cursor: pointer;
  outline: none;
}

.thread-action-btn:hover {
  color: v-bind('theme.boxTextColor');
  background-color: v-bind('theme.boxAccentColorHover');
}

.thread-item.active .thread-action-btn {
  color: v-bind('theme.boxTextColor');
}

.edit-btn:hover {
  color: v-bind('theme.boxGlowColor');
  background-color: v-bind('theme.boxAccentColorHover');
}

.activate-btn:hover {
  color: v-bind('theme.boxGlowColor');
  background-color: v-bind('theme.boxAccentColorHover');
}

.delete-btn:hover {
  color: rgba(255, 100, 100, 0.9);
  background-color: v-bind('theme.boxAccentColorHover');
}

.thread-action-btn.disabled {
  opacity: 0.35;
  cursor: not-allowed;
  pointer-events: none;
}

.thread-action-btn.disabled:hover {
  background: transparent;
  color: v-bind('theme.boxTextColorNoActive');
}

/* 搜索框 */
.thread-search-container {
  flex: 1;
  max-width: 300px;
  min-width: 200px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.search-icon {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 12px;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 6px 28px 6px 24px;
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0;
  background: v-bind('theme.boxColor');
  color: v-bind('theme.boxTextColor');
  font-size: 12px;
  outline: none;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
  height: 28px;
}

.search-input:focus {
  border-color: v-bind('theme.boxGlowColor');
}

.search-input:hover:not(:focus) {
  border-color: v-bind('theme.boxBorderColorHover');
}

.search-input::placeholder {
  color: v-bind('theme.boxTextColorNoActive');
  opacity: 0.7;
}

.clear-search-btn {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 12px;
  cursor: pointer;
  padding: 2px;
  border-radius: 0;
  transition: color 0.2s ease, background-color 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
}

.clear-search-btn:hover {
  color: v-bind('theme.boxTextColor');
  background-color: v-bind('theme.boxAccentColorHover');
}
</style>