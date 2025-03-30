<template>
  <div class="thread-list-container">
    <LaserButton
      class="manage-thread-btn"
      :corners="['bottom-left', 'bottom-right']"
      corner-size="15px"
      :background-color="primaryButton"
      :border-color="primaryButtonBorder"
      @click="handleRoleManage">
      管理角色
    </LaserButton>
    <div class="thread-items-wrapper">
      <div v-if="loading" class="loading-indicator">
        <i class="bi bi-arrow-repeat spinning"></i> 加载中...
      </div>
      <div v-else-if="roles.length === 0" class="empty-thread-tip">
        <i class="bi bi-person-plus"></i>
        <div class="tip-text">您还没有创建角色</div>
        <LaserButton
          class="create-thread-btn"
          :corners="['bottom-left', 'bottom-right']"
          corner-size="15px"
          :background-color="primaryButton"
          :border-color="primaryButtonBorder"
          :glow-color="primaryHover"
          @click="handleRoleManage">
          创建角色
        </LaserButton>
      </div>
      <div v-else class="thread-items">
        <div v-for="role in roles" 
             :key="role.id"
             @click="handleRoleClick(role.id)"
             :class="['thread-item', { active: String(role.id) === String(currentRoleId) }]"
             :title="role.name">
          <div class="role-avatar" :class="{ 'no-image': !role.avatarPath }">
            <img v-if="role.avatarPath" :src="role.avatarPath" :alt="role.name">
            <i v-else class="bi bi-person"></i>
          </div>
          <div class="thread-content">
            <div class="thread-title">{{ role.name }}</div>
            <div class="thread-time" v-if="role.createTime">{{ formatTime(role.createTime) }}</div>
          </div>
          <div class="menu-wrapper">
            <button class="menu-btn" @click.stop="toggleRoleMenu(role.id, role.name, $event)">
              <i class="bi bi-three-dots-vertical"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 三点菜单下拉框 -->
    <div class="dropdown-overlay" v-if="openMenuId !== null" @click="closeMenu"></div>
    <div class="dropdown-menu" v-if="openMenuId !== null" :style="menuPosition">
      <div class="menu-item" @click="handleStartNewSession(openMenuId)">
        <i class="bi bi-plus-circle"></i> 开始新会话
      </div>
      <div class="menu-item" @click="handleThreadManage(openMenuId, openMenuName)">
        <i class="bi bi-folder2-open"></i> 管理全部会话
      </div>
      <div class="menu-item" @click="handleEditRole(openMenuId)">
        <i class="bi bi-pencil-square"></i> 编辑角色
      </div>
      <div class="menu-item" @click="handleConfigRole(openMenuId)">
        <i class="bi bi-gear"></i> 设置模型角色
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import LaserButton from './LaserButton.vue'
import { useThemeStore } from '../stores/theme'

// 获取主题颜色
const themeStore = useThemeStore()
const primaryColor = computed(() => themeStore.primaryColor)
const activeColor = computed(() => themeStore.activeColor)
const primaryHover = computed(() => themeStore.primaryHover)
const primaryButton = computed(() => themeStore.primaryButton)
const primaryButtonBorder = computed(() => themeStore.primaryButtonBorder)
const sideBlur = computed(() => themeStore.sideBlur)

// Props
const props = defineProps<{
  currentRoleId: string | null
  currentThreadId: string | null
  isMobileMenuOpen: boolean
}>()

// Emits
const emit = defineEmits<{
  (e: 'roleChecked', roleId: string): void
  (e: 'roleEdit', roleId: string): void
  (e: 'roleConfig', roleId: string): void
  (e: 'threadManage', roleId: string, roleName: string): void
  (e: 'startNewSession', roleId: string): void
}>()

// State
const roles = ref<any[]>([])
const loading = ref(false)
const openMenuId = ref<string | null>(null)
const openMenuName = ref<string>('')
const menuPosition = ref({
  top: '0px',
  left: '0px'
})

// Methods
const loadRoleList = async () => {
  loading.value = true
  try {
    console.log('开始加载角色列表...')
    const response = await axios.post('/model/rp/getRoleList', {
      page: 1,
      size: 100  // 设置一个较大的值以获取所有角色
    })
    
    if (response.data.code === 0) {
      // 处理分页响应，从PageableView中获取rows作为角色列表
      const pageData = response.data.data
      roles.value = pageData.rows || []
      console.log(`角色列表加载成功，共 ${roles.value.length} 个角色`)
      
      // 如果列表为空，记录日志
      if (roles.value.length === 0) {
        console.log('角色列表为空，请先创建角色')
      }
    } else {
      console.error('加载角色列表失败:', response.data.message)
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
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

// 跳转到角色管理页面
const handleRoleManage = () => {
  window.location.href = "/dashboard?redirect=/panel/model/role/list"
}

// Event Handlers
const handleRoleClick = (roleId: string) => {
  console.log(`角色点击: roleId=${roleId}, 当前roleId=${props.currentRoleId}`)
  emit('roleChecked', roleId)
}

const handleEditRole = (roleId: string) => {
  emit('roleEdit', roleId)
}

const handleConfigRole = (roleId: string) => {
  emit('roleConfig', roleId)
}

const handleThreadManage = (roleId: string, roleName: string) => {
  emit('threadManage', roleId, roleName)
}

const handleStartNewSession = (roleId: string) => {
  console.log(`触发startNewSession事件: roleId=${roleId}`)
  emit('startNewSession', roleId)
}

const toggleRoleMenu = (roleId: string, roleName: string, event: Event) => {
  event.stopPropagation()
  
  // 如果已经打开同一个菜单，则关闭
  if (openMenuId.value === roleId) {
    openMenuId.value = null
    return
  }
  
  openMenuId.value = roleId
  openMenuName.value = roleName
  
  // 获取当前点击的元素位置
  const target = event.currentTarget as HTMLElement
  const rect = target.getBoundingClientRect()
  
  // 计算菜单位置
  menuPosition.value = {
    top: `${rect.bottom + 5}px`,
    left: `${rect.right - 180}px` // 菜单宽度180px，右对齐
  }
  
  // 添加点击事件监听器以关闭菜单
  document.addEventListener('click', closeMenuOnClickOutside)
}

const closeMenuOnClickOutside = (event: MouseEvent) => {
  closeMenu()
  document.removeEventListener('click', closeMenuOnClickOutside)
}

const closeMenu = () => {
  openMenuId.value = null
}

onMounted(() => {
  loadRoleList()
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenuOnClickOutside)
})

// Expose loadRoleList function to parent
defineExpose({
  loadRoleList
})
</script>

<style scoped>
.thread-list-container {
  width: 240px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid v-bind('`rgba(${primaryColor.split("(")[1].split(")")[0]}, 0.1)`');
  border-radius: 0 !important;
  height: 100%;
  overflow: hidden;
}

.manage-thread-btn {
  margin: 12px 12px 5px 12px;
  padding: 10px 15px;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.manage-thread-btn i {
  font-size: 16px;
}

.thread-items-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  border-top: none;
  min-height: 0;
  margin-top: 5px;
  height: calc(100% - 60px);
}

.loading-indicator {
  padding: 20px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 10px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-thread-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 20px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  flex: 1;
  margin-top: 10px;
}

.empty-thread-tip i {
  font-size: 36px;
  margin-bottom: 12px;
}

.empty-thread-tip .tip-text {
  margin-bottom: 16px;
  font-size: 14px;
}

.create-thread-btn {
  font-size: 14px;
  background-color: v-bind(primaryButton) !important;
  border-color: v-bind(primaryButtonBorder) !important;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.thread-items {
  display: flex;
  flex-direction: column;
  padding: 4px 0;
}

.thread-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.25s ease, border-left-color 0.25s ease, box-shadow 0.25s ease;
  border-left: 3px solid transparent;
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  overflow: hidden;
  margin: 2px 4px;
  border-radius: 2px;
}

.thread-item:first-child {
  margin-top: 5px;
}

.thread-item .thread-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
  justify-content: center;
}

.thread-item .thread-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.2px;
}

.thread-item .thread-time {
  font-size: 11px;
  color: v-bind(primaryColor);
  margin-bottom: 0;
  letter-spacing: 0.1px;
}

.thread-item:hover {
  background: v-bind(primaryHover);
}

.thread-item.active {
  background: rgba(40, 90, 130, 0.25);
  border-left-color: v-bind(activeColor);
  box-shadow: inset 3px 0 5px -2px v-bind(activeColor);
}

.thread-item.active .thread-title {
  color: rgba(255, 255, 255, 1);
  font-weight: 500;
}

.thread-item.active .thread-time {
  color: v-bind(activeColor);
}

.thread-actions {
  display: flex;
  gap: 4px;
  align-items: center;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.25s ease;
  flex-shrink: 0;
  right: 0;
}

.thread-item:hover .thread-actions {
  opacity: 1;
  pointer-events: auto;
}

.thread-action-btn {
  color: rgba(255, 255, 255, 0.4);
  min-height: 24px;
  padding: 0;
  transition: all 0.2s ease;
}

.thread-action-btn:hover {
  background: v-bind('`rgba(${primaryColor.split("(")[1].split(")")[0]}, 0.15)`') !important;
  color: rgba(255, 255, 255, 0.9);
}

.thread-action-btn i {
  font-size: 14px;
}

.role-avatar {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
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

.thread-items-wrapper::-webkit-scrollbar {
  width: 3px;
}

.thread-items-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.thread-items-wrapper::-webkit-scrollbar-thumb {
  background: v-bind(primaryHover);
  border-radius: 1.5px;
}

.thread-items-wrapper::-webkit-scrollbar-thumb:hover {
  background: v-bind(primaryColor);
}

@media (max-width: 768px) {
  .thread-items-wrapper {
    flex: 1;
    overflow-y: auto;
    height: calc(100vh - 60px);
  }
}

.menu-wrapper {
  position: relative;
  margin-left: auto;
  display: flex;
  align-items: center;
  height: 36px;
}

.menu-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 16px;
  transition: all 0.3s;
  opacity: 0;
  margin-right: -5px; /* 轻微缩小右侧间距 */
}

.thread-item:hover .menu-btn {
  opacity: 1;
}

.menu-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
}

.dropdown-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 998;
  background: transparent;
}

.dropdown-menu {
  position: absolute;
  background: rgba(40, 40, 40, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 6px;
  width: 180px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  z-index: 999;
  overflow: hidden;
  animation: menuFadeIn 0.15s ease-out;
  transform-origin: top right;
}

@keyframes menuFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.menu-item {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
}

.menu-item:hover {
  background: v-bind(primaryHover);
  color: white;
}

.menu-item i {
  font-size: 14px;
}

.thread-item.active .role-avatar {
  border-color: v-bind(activeColor);
  box-shadow: 0 0 8px v-bind('`rgba(${activeColor.split("(")[1].split(")")[0].split(",").join(", ")}, 0.5)`');
}
</style> 