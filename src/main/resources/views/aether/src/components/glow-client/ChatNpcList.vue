<template>
  <div class="model-chat-list-wrapper">
    <!-- 移动端悬浮按钮 -->
    <GlowButton
      v-if="isMobile"
      @click="toggleMobileMenu"
      class="mobile-menu-btn"
      :corners="[`top-left`]"
    >
      NPC列表
    </GlowButton>

    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && mobileMenuOpen" 
      class="mobile-overlay"
      @click="closeMobileMenu"
    ></div>

    <GlowDiv 
      border="right" 
      class="chat-list-container"
      :class="{ 'mobile-open': isMobile && mobileMenuOpen, 'mobile': isMobile }"
    >

      <!-- 管理NPC按钮 -->
      <div class="manage-role-btn-wrapper"> 
        <GlowButton
          @click="handleRoleManage" 
          class="manage-role-btn"
          :corners="['bottom-right']"
        >
          NPC设计器
        </GlowButton>
      </div>
      
      <!-- NPC列表内容 -->
      <div class="npcs-wrapper">
        <!-- 加载中状态 -->
        <div v-if="loading" class="loading-indicator">
          <i class="bi bi-arrow-repeat spinning"></i> 
          <span>加载中...</span>
        </div>
        
        <!-- 空列表状态 -->
        <div v-else-if="threads.length === 0" class="empty-list">
          <i class="bi bi-person-plus"></i>
          <div class="empty-text">您可以通过NPC设计器创建您的第一个NPC</div>
          <GlowButton
            @click="handleRoleManage" 
            class="empty-create-btn"
          >
            进入NPC设计器
          </GlowButton>
        </div>
        
        <!-- NPC列表 -->
        <div v-else class="npc-list">
          <div 
            v-for="npc in threads" 
            :key="npc.id"
            @click="handleRoleClick(npc)"
            :class="['npc-item', { active: npc.id == activeThreadId }]"
          >
            <div class="role-avatar" :class="{ 'no-image': !npc.avatarUrl }">
              <img v-if="npc.avatarUrl" :src="npc.avatarUrl" :alt="npc.name">
              <i v-else class="bi bi-person"></i>
            </div>
            <div class="npc-content">
              <div class="npc-title">{{ npc.name }}</div>
              <div class="npc-thread-count">{{ npc.threadCount }} 个对话</div>
            </div>
            <!-- 操作按钮组 -->
            <div class="action-buttons">
              <!-- 开始新会话按钮 -->
              <button class="new-thread-btn" @click.stop="handleNewThread(npc)" title="开始新会话">
                <i class="bi bi-chat"></i>
              </button>
              <!-- 三点菜单按钮 -->
              <button class="menu-btn" @click.stop="toggleContextMenu(npc, $event)" title="更多选项">
                <i class="bi bi-three-dots-vertical"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </GlowDiv>

    <!-- 上下文菜单 -->
    <GlowContextMenu
      ref="contextMenu"
      :header="true" 
      :actions="menuActions"
      @click="onContextMenuClick"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, watch, onBeforeUnmount, computed, reactive } from 'vue'
import GlowDiv from "@/components/glow-ui/GlowDiv.vue"
import GlowButton from "@/components/glow-ui/GlowButton.vue"
import GlowContextMenu from '../glow-ui/GlowContextMenu.vue' // 导入上下文菜单组件
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '../glow-ui/GlowTheme'
import { useRouter } from 'vue-router' // 导入 router
import type { GetNpcListVo, GetNpcListDto } from '@/commons/api/NpcApi.ts';
import NpcApi from '@/commons/api/NpcApi.ts'; // 导入NpcApi

// 获取 glow 主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)
// 获取路由器
const router = useRouter()

const listData = ref<GetNpcListVo[]>([])
const listTotal = ref(0)
const listQuery = reactive<GetNpcListDto>({
  keyword: null,
  status: 1,
  page: 1,
  pageSize: 1000
})
const selectedNpc = ref<GetNpcListVo | null>(null)


// 事件定义
const emit = defineEmits<{
  (e: 'select-npc', npc: GetNpcListVo): void;
  (e: 'create-thread', role: GetNpcListVo): void;
  (e: 'edit-role', role: GetNpcListVo): void;
  (e: 'manage-threads', role: GetNpcListVo): void;
}>()

const props = defineProps<{
  loading?: boolean
}>()

// 状态
const loading = computed(() => props.loading ?? false)
const threads = computed(() => listData.value)

// 计算属性：活动会话ID
const activeThreadId = computed(() => selectedNpc.value?.id || "")

// 移动端相关状态
const isMobile = ref(window.innerWidth <= 768)
const mobileMenuOpen = ref(false)

// 上下文菜单引用
const contextMenu = ref<InstanceType<typeof GlowContextMenu> | null>(null)

// 上下文菜单操作项
const menuActions = ref([
  { name: '开始新会话', action: 'new-thread' },
  { name: '管理全部会话', action: 'manage-threads' },
  { name: '编辑NPC', action: 'edit-role' },
])

// 处理点击NPC
const handleRoleClick = (npc: GetNpcListVo) => {
  if (npc.id === activeThreadId.value) {
    closeMobileMenu(); // 点击当前选中项时也关闭移动菜单
    return; 
  }
  selectedNpc.value = npc; // 更新内部选中状态
  emit('select-npc', npc)
  closeMobileMenu()
}

// 处理管理NPC按钮点击
const handleRoleManage = () => {
  router.push('/model-role-manager')
  closeMobileMenu()
}

// 处理新会话按钮点击
const handleNewThread = (npc: GetNpcListVo) => {
  emit('create-thread', npc)
  closeMobileMenu()
}

// 监听窗口大小变化
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    mobileMenuOpen.value = false
  }
}

// 移动端菜单控制
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  if (isMobile.value) {
    mobileMenuOpen.value = false
  }
}

// 打开/关闭上下文菜单
const toggleContextMenu = (npc: GetNpcListVo, event: MouseEvent) => {
  if (contextMenu.value) {
    contextMenu.value.show(npc, event, npc.name) // 直接传递NPC对象
  }
}

// 处理上下文菜单点击事件
const onContextMenuClick = (context: any, action: string) => {
  const npc = context as GetNpcListVo;
  
  if (action === 'new-thread') {
    emit('create-thread', npc)
  }
  if (action === 'manage-threads') {
    emit('manage-threads', npc)
  }
  if (action === 'edit-role') {
    emit('edit-role', npc)
  }

  closeMobileMenu() // 操作菜单后也关闭移动菜单
}

//加载NPC列表
const loadNpcList = async () => {
  try {
    const response = await NpcApi.getNpcListVo(listQuery);
    listData.value = response.rows || [];
    listTotal.value = response.count || 0;
  } catch (error) {
    console.error('获取NPC列表失败:', error);
    listData.value = [];
    listTotal.value = 0;
  }
}

//选中默认的NPC
const selectDefaultNpc = () => {
  const activeNpc = listData.value.find(npc => npc.active === 1);
  if (activeNpc) {
    selectedNpc.value = activeNpc; // 更新内部选中状态
    emit('select-npc', activeNpc);
  }
}

//设置选中的NPC
const setSelectedNpc = (npcId: string) => {
  const npc = listData.value.find(item => item.id === npcId);
  if (npc) {
    selectedNpc.value = npc;
  }
}

// 组件挂载时设置窗口大小监听
onMounted(async () => {
  window.addEventListener('resize', handleResize)
  await loadNpcList();
  await selectDefaultNpc();
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

// 暴露方法给父组件
defineExpose({
  closeMobileMenu,
  setSelectedNpc,
  loadNpcList
})
</script>

<style scoped>
.model-chat-list-wrapper {
  position: relative;
  height: 100%;
  user-select: none; /* 禁止文本选择 */
  -webkit-user-select: none; /* 兼容性前缀 (Safari, Chrome) */
  -moz-user-select: none;    /* 兼容性前缀 (Firefox) */
  -ms-user-select: none;     /* 兼容性前缀 (IE/Edge) */
}

.mobile-menu-btn {
  position: fixed;
  top: 8px;
  left: 10px;
  z-index: 1000;
  height: 32px;
  min-height: 32px;
  font-size: 12px;
  padding: 8px;
}

.mobile-menu-btn i {
  font-size: 24px;
}

.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.chat-list-container {
  width: 240px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1000;
}

.manage-role-btn-wrapper { 
  padding: 12px;
  flex-shrink: 0;
}

.manage-role-btn { 
  width: 100%;
  padding: 10px 0;
  font-size: 14px;
}

.npcs-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* 加载中状态 */
.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: v-bind('theme.boxTextColorNoActive');
  gap: 8px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 空列表状态 */
.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px;
  flex: 1;
}

.empty-list i {
  font-size: 36px;
  margin-bottom: 16px;
  color: v-bind('theme.boxTextColorNoActive');
}

.empty-text {
  font-size: 14px;
  margin-bottom: 20px;
  color: v-bind('theme.boxTextColorNoActive');
}

.empty-create-btn {
  width: 80%;
  max-width: 160px;
}

/* NPC列表 */
.npc-list {
  padding: 1px 0;
}

.npc-item {
  display: flex;
  align-items: center; /* 垂直居中对齐 */
  padding: 10px 8px 10px 20px; /* 调整右边距给菜单按钮空间 */
  margin: 4px 0;
  cursor: pointer;
  transition: background-color 0.2s ease, border-left-color 0.2s ease;
  border-left: 3px solid transparent;
  position: relative; /* 为菜单按钮定位 */
}

.npc-item:hover {
  background-color: v-bind('theme.boxAccentColor');
}

.npc-item.active {
  background-color: v-bind('theme.boxColorActive');
  border-left-color: v-bind('theme.boxGlowColor');
  box-shadow: inset 5px 0 8px -2px v-bind('theme.boxGlowColor');
  backdrop-filter: blur(v-bind('theme.boxBlurActive') + 'px');
  -webkit-backdrop-filter: blur(v-bind('theme.boxBlurActive') + 'px');
}

.npc-content {
  flex: 1;
  min-width: 0;
  /* align-self: center; */ /* 移除，因为父级已居中 */
  margin-right: 24px; /* 给菜单按钮留出空间 */
}

.npc-title {
  font-size: 14px;
  color: v-bind('theme.boxTextColorNoActive');
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s ease;
}

.npc-item:hover .npc-title,
.npc-item.active .npc-title {
  color: v-bind('theme.boxTextColor');
}

.npc-item:hover .npc-thread-count,
.npc-item.active .npc-thread-count {
  color: v-bind('theme.boxTextColor');
  opacity: 0.8;
}

.npc-item.active .npc-title {
  font-weight: 500;
}

.npc-thread-count {
  font-size: 12px;
  color: v-bind('theme.boxTextColorNoActive');
  opacity: 0.7;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s ease, opacity 0.2s ease;
}

/* 新增 role-avatar 样式 */
.role-avatar {
  width: 36px;
  height: 36px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  margin-right: 10px; /* 添加右边距 */
  border-radius: 0; /* 直角 */
}

.role-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.role-avatar.no-image i {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
}

.npc-item.active .role-avatar {
  border-color: v-bind('theme.boxGlowColor');
  box-shadow: 0 0 8px v-bind('theme.boxGlowColor');
}

/* 操作按钮组样式 */
.action-buttons {
  position: absolute; /* 绝对定位到 npc-item 右侧 */
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 4px; /* 按钮之间的间距 */
  opacity: 0; /* 默认隐藏 */
  transition: opacity 0.2s ease;
}

.npc-item:hover .action-buttons {
  opacity: 1; /* 悬浮时显示 */
}

/* 新会话按钮样式 */
.new-thread-btn {
  background: transparent;
  border: none;
  color: v-bind('theme.boxGlowColor'); /* 使用主题发光色 */
  padding: 4px 6px;
  border-radius: 2px;
  cursor: pointer;
  transition: color 0.2s ease, background-color 0.2s ease, transform 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.new-thread-btn:hover {
  color: v-bind('theme.mainColorActive');
  background: v-bind('theme.boxAccentColorHover');
  transform: scale(1.1);
}

.new-thread-btn i {
  font-size: 14px;
}

/* 三点菜单按钮样式 */
.menu-btn {
  background: transparent;
  border: none;
  color: v-bind('theme.boxTextColorNoActive');
  padding: 4px 6px;
  border-radius: 2px;
  cursor: pointer;
  transition: color 0.2s ease, background-color 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-btn:hover {
  color: v-bind('theme.boxTextColor');
  background: v-bind('theme.boxAccentColorHover');
}

.menu-btn i {
  font-size: 16px;
}

/* 滚动条样式 */
.npcs-wrapper::-webkit-scrollbar {
  width: 3px;
}

.npcs-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.npcs-wrapper::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
}

.npcs-wrapper::-webkit-scrollbar-thumb:hover {
  background: v-bind('theme.boxBorderColorHover');
}

/* 响应式样式 */
@media (max-width: 768px) {
  .chat-list-container {
    position: fixed;
    top: 0;
    left: -240px;
    height: 100vh;
    transition: transform 0.3s ease;
    box-shadow: none;
  }
  
  .chat-list-container.mobile-open {
    transform: translateX(240px);
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
  }
  
  /* 移动端操作按钮总是显示 */
  .action-buttons {
    opacity: 1;
  }
  
  /* 调整移动端按钮大小 */
  .new-thread-btn,
  .menu-btn {
    padding: 6px 8px;
  }
  
  .new-thread-btn i {
    font-size: 16px;
  }
  
  .menu-btn i {
    font-size: 18px;
  }
}
</style>