<template>
  <div class="role-list-container">
    <div class="role-items-wrapper">
      <div v-if="loading" class="loading-indicator">
        <i class="bi bi-arrow-repeat spinning"></i> 加载中...
      </div>
      <div v-else-if="roles.length === 0" class="empty-role-tip">
        <i class="bi bi-people"></i>
        <div class="tip-text">还没有角色</div>
        <div class="tip-desc">请先在角色管理面板创建角色</div>
      </div>
      <div v-else class="role-items">
        <div v-for="role in roles" 
             :key="role.id"
             @click="handleRoleClick(role.id)"
             :class="['role-item', { active: String(role.id) === String(currentRoleId) }]"
             :title="role.name">
          <div class="role-avatar" :class="{ 'no-image': !role.avatarPath }">
            <img v-if="role.avatarPath" :src="role.avatarPath" :alt="role.name">
            <i v-else class="bi bi-person"></i>
          </div>
          <div class="role-content">
            <div class="role-name">{{ role.name }}</div>
            <div class="role-create-time" v-if="role.createTime">{{ formatTime(role.createTime) }}</div>
          </div>
          <div class="role-actions">
            <LaserButton 
              class="role-action-btn"
              width="24px"
              height="24px"
              background-color="transparent"
              border-color="transparent"
              glowIntensity="0"
              @click.stop="handleEditRole(role.id)"
              title="编辑角色">
              <i class="bi bi-pencil"></i>
            </LaserButton>
            <LaserButton 
              class="role-action-btn"
              width="24px"
              height="24px"
              background-color="transparent"
              border-color="transparent"
              glowIntensity="0"
              @click.stop="handleThreadManage(role.id, role.name)"
              title="会话管理">
              <i class="bi bi-chat-left-text"></i>
            </LaserButton>
            <LaserButton 
              class="role-action-btn"
              width="24px"
              height="24px"
              background-color="transparent"
              border-color="transparent"
              glowIntensity="0"
              @click.stop="handleConfigRole(role.id)"
              title="设置模型角色">
              <i class="bi bi-gear"></i>
            </LaserButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
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
}>()

// State
const roles = ref<any[]>([])
const loading = ref(false)

// Methods
const loadRoleList = async () => {
  loading.value = true
  try {
    const response = await axios.post('/model/role/list')
    if (response.data.code === 0) {
      roles.value = response.data.data || []
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
    return `${year}年${month}月${day}日`
  } catch (e) {
    console.error('时间格式化错误:', e)
    return ''
  }
}

// Event Handlers
const handleRoleClick = (roleId: string) => {
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

// Expose loadRoleList function to parent
defineExpose({
  loadRoleList
})
</script>

<style scoped>
.role-list-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.role-items-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  padding: 10px 0;
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

.empty-role-tip {
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

.empty-role-tip i {
  font-size: 36px;
  margin-bottom: 12px;
}

.empty-role-tip .tip-text {
  margin-bottom: 8px;
  font-size: 15px;
}

.empty-role-tip .tip-desc {
  font-size: 13px;
  opacity: 0.7;
}

.role-items {
  display: flex;
  flex-direction: column;
  padding: 0;
}

.role-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 0;
  position: relative;
  margin: 2px 8px;
}

.role-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  background: transparent;
  transition: background 0.2s ease;
}

.role-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.role-item.active {
  background: rgba(255, 255, 255, 0.07);
}

.role-item.active::before {
  background: v-bind(activeColor);
}

.role-avatar {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  overflow: hidden;
  margin-right: 10px;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.2);
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

.role-content {
  flex: 1;
  min-width: 0;
  padding-right: 10px;
}

.role-name {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-create-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.role-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.role-item:hover .role-actions {
  opacity: 1;
}

.role-action-btn {
  color: rgba(255, 255, 255, 0.6);
  transition: color 0.2s ease;
}

.role-action-btn:hover {
  color: rgba(255, 255, 255, 0.9);
}

/* 自定义滚动条样式 */
.role-items-wrapper::-webkit-scrollbar {
  width: 3px;
}

.role-items-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.role-items-wrapper::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1.5px;
}

.role-items-wrapper::-webkit-scrollbar-thumb:hover {
  background: v-bind(primaryColor);
}
</style> 