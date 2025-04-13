<template>
  <div class="role-manager-layout">
    <ModelRoleManagerList 
      ref="roleListRef"
      class="role-sidebar"
      :data="roleList"
      :selected="selectedRoleId"
      :loading="loading"
      @select-role="onSelectRole"
      @create-role="onCreateRole"
    />

    <GlowDiv class="role-content" border="none">
        <div v-if="!selectedRoleId" class="empty-detail">
          <i class="bi bi-person-bounding-box"></i>
          <p>请从左侧选择一个角色进行设计</p>
        </div>
      
      <GlowTab
        v-else
        :items="roleTabItems"
        v-model:activeTab="currentTab"
      >
        <!-- 角色基本信息 -->
        <div v-if="currentTab === 'base-info'" class="role-tab-panel">
          <div class="role-panel">
            <!-- 操作按钮区域（顶部） -->
            <div class="action-buttons">
              <GlowButton 
                @click="removeModelRole(selectedRoleId)"
                :disabled="loading"
                class="action-button danger-button"
                title="移除角色"
              >
                移除
              </GlowButton>
              <GlowButton 
                @click="copyCurrentRole"
                :disabled="loading"
                class="action-button"
                title="复制角色"
              >
                复制
              </GlowButton>
              <GlowButton 
                @click="saveRoleChanges" 
                :disabled="loading"
                class="action-button save-button"
                title="保存角色"
              >
                保存
              </GlowButton>
            </div>
            
            <div class="role-detail-form">
              <div class="form-row">
                <GlowInput
                  v-model="currentRoleDetails.name"
                  title="角色名称"
                  :maxLength="50"
                  showLength
                  notBlank
                />
              </div>
              
              <div class="form-row">
                <div class="input-group">
                  <GlowInput
                    v-model="currentRoleDetails.tags"
                    title="角色标签"
                    :maxLength="50"
                    showLength
                    placeholder="使用逗号分隔多个标签"
                  />
                </div>
                <div class="input-group status-group">
                  <div class="status-wrapper">
                    <GlowCheckBox 
                      v-model="roleEnabled"
                      tip="被禁用的角色在聊天界面将不可见">
                      启用角色
                    </GlowCheckBox>
                  </div>
                </div>
              </div>
              
              <div class="form-row">
                <GlowInputArea
                  v-model="currentRoleDetails.description"
                  title="角色描述"
                  :maxLength="50000"
                  showLength
                  :rows="3"
                />
              </div>
              
              <div class="form-row">
                <GlowInputArea
                  v-model="currentRoleDetails.roleSummary"
                  title="角色设定摘要"
                  :maxLength="50000"
                  showLength
                  :rows="4"
                />
              </div>
              
              <div class="form-row">
                <GlowInputArea
                  v-model="currentRoleDetails.scenario"
                  title="情景"
                  :maxLength="50000"
                  showLength
                  :rows="4"
                />
              </div>
              
              <div class="form-row">
                <GlowInputArea
                  v-model="currentRoleDetails.firstMessage"
                  title="首次对话内容"
                  :maxLength="50000"
                  showLength
                  :rows="4"
                />
              </div>
              
              <!-- 头像区域 -->
              <div class="avatar-section">
                <p class="section-title">角色头像</p>
                <div class="avatar-container">
                  <img v-if="currentRoleDetails.avatarPath" :src="currentRoleDetails.avatarPath" class="avatar-preview" alt="角色头像" />
                  <div v-else class="avatar-placeholder">
                    <i class="bi bi-person"></i>
                  </div>
                  <div class="avatar-upload">
                    <input 
                      type="file" 
                      ref="fileInput" 
                      style="display: none" 
                      accept="image/jpeg,image/png" 
                      @change="handleFileUpload"
                    />
                    <GlowButton 
                      class="upload-btn" 
                      @click="triggerFileUpload"
                      :disabled="uploading"
                    >
                      {{ uploading ? '上传中...' : '上传头像' }}
                    </GlowButton>
                    <p class="upload-tip">支持JPG、PNG格式图片</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 对话示例 -->
        <div v-if="currentTab === 'chat-examples'" class="role-tab-panel">
          <div class="role-panel">
            <div class="chat-examples-placeholder">
              <i class="bi bi-chat-dots"></i>
              <p>对话示例功能正在开发中...请转到旧版管理台</p>
              <GlowButton
                @click="goToOldPanel"
                class="redirect-btn"
                title="前往旧版管理台"
              >
                前往旧版管理台
              </GlowButton>
            </div>
          </div>
        </div>
      </GlowTab>
    </GlowDiv>
    
    <!-- 添加提示组件 -->
    <GlowAlter ref="alterRef" />
    
    <!-- 添加确认对话框组件 -->
    <GlowConfirm ref="confirmRef" />
    
    <!-- 添加确认输入框组件 -->
    <GlowConfirmInput ref="confirmInputRef" :close-on-click-overlay="false" />
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, reactive, watch} from 'vue';
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme';
import { inject } from 'vue';
import http from '@/commons/Http';
import type GetModelRoleListVo from '@/entity/GetModelRoleListVo';
import type PageableView from '@/entity/PageableView';
import ModelRoleManagerList from '@/components/glow-client/ModelRoleManagerList.vue';
import type GetModelRoleDetailsVo from "@/entity/model-role/vo/GetModelRoleDetailsVo";
import type SaveModelRoleDto from "@/entity/model-role/dto/SaveModelRoleDto";
import GlowAlter from "@/components/glow-ui/GlowAlter.vue";
import GlowInput from "@/components/glow-ui/GlowInput.vue";
import GlowInputArea from "@/components/glow-ui/GlowInputArea.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowCheckBox from "@/components/glow-ui/GlowCheckBox.vue";
import GlowTab from "@/components/glow-ui/GlowTab.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import GlowConfirmInput from "@/components/glow-ui/GlowConfirmInput.vue";
import { usePreferencesStore } from '@/stores/preferences';

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme);

// 获取偏好设置存储
const preferencesStore = usePreferencesStore();

// 角色列表引用
const roleListRef = ref<any>(null);
const alterRef = ref<any>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const uploading = ref(false);
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null);
const confirmInputRef = ref<InstanceType<typeof GlowConfirmInput> | null>(null);

// 角色列表数据
const roleList = ref<GetModelRoleListVo[]>([]);
const loading = ref(true);
const selectedRoleId = ref<string>("");

// 定义标签项
const roleTabItems = [
  { title: '角色基本', action: 'base-info' },
  { title: '对话示例', action: 'chat-examples' },
];

// 当前激活的标签
const currentTab = ref('base-info');

// 监听当前Tab变化并保存到偏好设置
watch(currentTab, (newValue) => {
  if (newValue) {
    preferencesStore.saveModelRoleEditPathTab(newValue);
  }
});

// 先初始化currentRoleDetails
const currentRoleDetails = reactive<GetModelRoleDetailsVo>({
  id:"",
  name:"",
  status:1,
  sortOrder:0,
  avatarPath:"",
  description:"",
  roleSummary:"",
  scenario:"",
  firstMessage:"",
  tags:""
});

// 然后初始化roleEnabled并设置监听
const roleEnabled = ref(currentRoleDetails.status === 1);

// 监听角色启用状态变化
watch(roleEnabled, (newValue) => {
  currentRoleDetails.status = newValue ? 1 : 0;
});

// 监听角色详情中的status变化
watch(() => currentRoleDetails.status, (newValue) => {
  roleEnabled.value = newValue === 1;
});

// 生命周期钩子
onMounted(async () => {
  await loadRoleList();
  
  // 从偏好设置中加载上次选择的角色ID和Tab
  const savedRoleId = preferencesStore.getModelRoleEditCurrentId;
  const savedTab = preferencesStore.getModelRoleEditPathTab;
  
  if (savedTab) {
    currentTab.value = savedTab;
  }
  
  if (savedRoleId) {
    // 检查保存的角色ID是否在列表中存在
    const roleExists = roleList.value.some(role => role.id === savedRoleId);
    if (roleExists) {
      selectedRoleId.value = savedRoleId;
      await loadModelRoleDetails();
    }
  }
});

// 加载角色列表
const loadRoleList = async () => {
  try {
    loading.value = true;
    const data = await http.postEntity<PageableView<GetModelRoleListVo>>('/model/role/getModelRoleList', {
      page: 1,
      pageSize: 20,
      keyword: ''
    });
    roleList.value = data.rows || [];
    loading.value = false;
  } catch (error) {
    roleList.value = [];
    loading.value = false;
    
    // 使用GlowAlter显示错误提示
    alterRef.value?.showConfirm({
      title: '加载失败',
      content: error instanceof Error ? error.message : '无法加载角色列表，请稍后重试',
      closeText: '确定'
    });
  }
};

const loadModelRoleDetails = async () => {
  if (!selectedRoleId.value) {
    return;
  }
  
  try {
    const response = await http.postEntity<GetModelRoleDetailsVo>('/model/role/getModelRoleDetails', {
      id: selectedRoleId.value
    });
    
    Object.assign(currentRoleDetails, response);
  } catch (error) {
    alterRef.value?.showConfirm({
      title: '加载失败',
      content: error instanceof Error ? error.message : '无法加载角色详情，请稍后重试',
      closeText: '确定'
    });
  }
}

const removeModelRole = async (roleId: string) => {
  if (!confirmRef.value) return;
  
  // 显示确认对话框
  const confirmed = await confirmRef.value.showConfirm({
    title: '移除角色',
    content: '警告：这是一个不可逆的操作！移除角色将同时删除：\n\n' +
             '• 与该角色的所有聊天记录\n' +
             '• 角色的所有个人设定数据\n' +
             '• 所有对话示例\n\n' +
             '删除后，您将无法恢复这些数据。确定要继续吗？',
    confirmText: '确认移除',
    cancelText: '取消'
  });
  
  // 用户取消操作
  if (!confirmed) return;
  
  try {
    loading.value = true;
    const response = await http.postEntity<string>('/model/role/removeModelRole', {
      id: roleId
    });
    
    if (response === 'success') {
      // 如果删除的是当前选中的角色，清空选择
      if (roleId === selectedRoleId.value) {
        selectedRoleId.value = '';
      }
      
      // 刷新角色列表
      await loadRoleList();
    } else {
      // 使用GlowAlter显示错误提示
      alterRef.value?.showConfirm({
        title: '操作失败',
        content: '删除角色失败：' + (response || '未知错误'),
        closeText: '确定'
      });
    }
    
    loading.value = false;
  } catch (error) {
    loading.value = false;
    
    // 使用GlowAlter显示错误提示
    alterRef.value?.showConfirm({
      title: '操作失败',
      content: error instanceof Error ? error.message : '删除角色出错，请稍后重试',
      closeText: '确定'
    });
  }
}

const saveModelRole = async (roleData: SaveModelRoleDto) => {
  try {
    loading.value = true;
    const response = await http.postEntity<string>('/model/role/saveModelRole', roleData);
    
    // 返回的是新角色的ID
    if (response) {
      // 刷新角色列表
      await loadRoleList();
      
      // 如果是新创建的角色，选中它
      if (!roleData.id) {
        selectedRoleId.value = response;
        // 加载新创建角色的详情
        await loadModelRoleDetails();
      }
    } else {
      // 使用GlowAlter显示错误提示
      alterRef.value?.showConfirm({
        title: '保存失败',
        content: '保存角色信息失败：未获取有效响应',
        closeText: '确定'
      });
    }
    
    loading.value = false;
  } catch (error) {
    loading.value = false;
    
    // 使用GlowAlter显示错误提示
    alterRef.value?.showConfirm({
      title: '保存失败',
      content: error instanceof Error ? error.message : '保存角色信息出错，请稍后重试',
      closeText: '确定'
    });
  }
}

// 选择角色
const onSelectRole = async (roleId: string) => {
  selectedRoleId.value = roleId;
  
  // 保存选中的角色ID到偏好设置
  preferencesStore.saveModelRoleEditCurrentId(roleId);
  
  // 加载选中角色的详情
  await loadModelRoleDetails();
};

// 复制角色 - 空实现
const onCopyRole = async (roleId: string) => {
  try {
    loading.value = true;
    await http.postEntity<string>('/model/role/copyModelRole', {
      id: roleId
    });
    
    // 刷新角色列表
    await loadRoleList();
    loading.value = false;
    
    // 显示成功提示
    alterRef.value?.showConfirm({
      title: '复制成功',
      content: '角色已成功复制。\n\n请注意：\n• 仅复制角色基本设定和对话示例\n• 不会复制用户与角色的聊天历史记录\n• 您可以开始与新角色进行全新的对话',
      closeText: '确定'
    });
  } catch (error) {
    loading.value = false;
    
    // 显示错误提示
    alterRef.value?.showConfirm({
      title: '复制失败',
      content: error instanceof Error ? error.message : '复制角色失败，请稍后重试',
      closeText: '确定'
    });
  }
};

// 复制当前角色
const copyCurrentRole = () => {
  if (selectedRoleId.value) {
    onCopyRole(selectedRoleId.value);
  }
};

// 创建新角色
const onCreateRole = async () => {
  if (!confirmInputRef.value) return;
  
  // 弹出输入框让用户输入角色名称
  const result = await confirmInputRef.value.showInput({
    title: '创建新角色',
    defaultValue: '新角色',
    placeholder: '请输入角色名称',
    message: '创建后可以继续编辑角色的其他信息',
    confirmText: '创建',
    cancelText: '取消'
  });
  
  // 用户取消创建
  if (!result.confirmed) return;
  
  // 用户确认创建且输入了角色名称
  if (result.value.trim()) {
    // 清空当前选中的角色ID
    selectedRoleId.value = "";
    
    // 初始化一个新的角色详情对象
    Object.assign(currentRoleDetails, {
      id: "",
      name: result.value.trim(), // 使用用户输入的角色名称
      status: 1,
      sortOrder: 0,
      avatarPath: "",
      description: "",
      roleSummary: "",
      scenario: "",
      firstMessage: "",
      tags: ""
    });
    
    // 等待下一个事件循环，确保表单已重置
    setTimeout(() => {
      // 自动保存新角色
      saveRoleChanges();
    }, 0);
  } else {
    // 用户输入为空，显示提示
    alterRef.value?.showConfirm({
      title: '创建失败',
      content: '角色名称不能为空',
      closeText: '确定'
    });
  }
};

// 保存角色更改
const saveRoleChanges = async () => {
  // 验证必填字段
  if (!currentRoleDetails.name.trim()) {
    alterRef.value?.showConfirm({
      title: '验证失败',
      content: '角色名称不能为空',
      closeText: '确定'
    });
    return;
  }
  
  // 处理头像路径，移除所有"/res/"前缀
  let avatarPath = currentRoleDetails.avatarPath;
  if (avatarPath) {
    // 使用正则表达式移除所有/res/前缀
    while (avatarPath.includes('/res/')) {
      avatarPath = avatarPath.replace('/res/', '');
    }
  }
  
  const roleData: SaveModelRoleDto = {
    id: currentRoleDetails.id,
    name: currentRoleDetails.name,
    description: currentRoleDetails.description,
    roleSummary: currentRoleDetails.roleSummary,
    scenario: currentRoleDetails.scenario,
    firstMessage: currentRoleDetails.firstMessage,
    tags: currentRoleDetails.tags,
    status: currentRoleDetails.status,
    sortOrder: currentRoleDetails.sortOrder,
    avatarPath: avatarPath
  };
  
  await saveModelRole(roleData);
};

// 触发文件上传对话框
const triggerFileUpload = () => {
  fileInput.value?.click();
};

// 处理文件上传
const handleFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const files = target.files;
  
  if (!files || files.length === 0) {
    return;
  }
  
  const file = files[0];
  
  // 检查文件类型
  if (file.type !== 'image/jpeg' && file.type !== 'image/png') {
    alterRef.value?.showConfirm({
      title: '文件类型错误',
      content: '请上传JPG或PNG格式的图片',
      closeText: '确定'
    });
    return;
  }
  
  // 检查文件大小（限制为2MB）
  if (file.size > 2 * 1024 * 1024) {
    alterRef.value?.showConfirm({
      title: '文件过大',
      content: '图片大小不能超过2MB',
      closeText: '确定'
    });
    return;
  }
  
  try {
    uploading.value = true;
    
    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', file);
    
    // 使用原生fetch API调用上传接口
    const response = await fetch('/model/role/uploadAvatar', {
      method: 'POST',
      body: formData,
    });
    
    if (!response.ok) {
      throw new Error('上传失败：' + response.statusText);
    }
    
    const data = await response.json();
    
    // 更新头像路径 (假设响应格式为 { data: 文件路径 })
    if (data && data.data) {
      currentRoleDetails.avatarPath = '/res/' + data.data;
    } else {
      throw new Error('上传失败：服务器返回数据格式不正确');
    }
    
    uploading.value = false;
    
    // 清空文件输入框，允许重复上传相同文件
    target.value = '';
  } catch (error) {
    uploading.value = false;
    
    alterRef.value?.showConfirm({
      title: '上传失败',
      content: error instanceof Error ? error.message : '头像上传失败，请稍后重试',
      closeText: '确定'
    });
    
    // 清空文件输入框
    target.value = '';
  }
};

// 跳转到旧版控制台角色管理页面
const goToOldPanel = () => {
  if (selectedRoleId.value) {
    window.location.href = `/panel/model/role/list?id=${selectedRoleId.value}`;
  } else {
    window.location.href = '/panel/model/role/list';
  }
};
</script>

<style scoped>
.role-manager-layout {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.role-sidebar {
  flex-shrink: 0;
}

.role-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.role-tab-panel {
  height: 100%;
  overflow: hidden;
}

.role-panel {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  max-height: calc(100vh - 135px);
}

.role-panel::-webkit-scrollbar {
  width: 8px;
}

.role-panel::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
}

.role-panel::-webkit-scrollbar-track {
  background: v-bind('theme.boxSecondColor');
}

.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: rgba(255, 255, 255, 0.5);
}

.empty-detail i {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-detail p {
  font-size: 16px;
}

/* 对话示例占位符样式 */
.chat-examples-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: rgba(255, 255, 255, 0.5);
}

.chat-examples-placeholder i {
  font-size: 48px;
  margin-bottom: 16px;
}

.chat-examples-placeholder p {
  font-size: 16px;
}

.role-detail-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding-bottom: 50px; /* 确保底部有足够的空间 */
}

.form-row {
  display: flex;
  gap: 15px;
}

.input-group {
  flex: 1;
}

.status-group {
  flex: 0 0 150px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.status-wrapper {
  display: flex;
  align-items: center;
  height: 32px; /* 与输入框高度一致 */
  margin-top: 22px; /* 加上输入框标题的高度，使复选框与输入框垂直对齐 */
}

.status-label {
  font-family: 'Chakra Petch', sans-serif;
  font-size: 12px;
  color: v-bind('theme.boxTextColor');
  margin-bottom: 10px;
  opacity: 0.8;
  font-weight: 500;
}

.section-title {
  font-family: 'Chakra Petch', sans-serif;
  font-size: 12px;
  color: v-bind('theme.boxTextColor');
  margin-bottom: 8px;
  opacity: 0.8;
  font-weight: 500;
}

.avatar-section {
  margin-top: 15px;
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border: 1px solid v-bind('theme.boxBorderColor');
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: v-bind('theme.boxSecondColor');
  border: 1px solid v-bind('theme.boxBorderColor');
}

.avatar-placeholder i {
  font-size: 48px;
  color: v-bind('theme.boxTextColorNoActive');
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.upload-tip {
  font-size: 12px;
  color: v-bind('theme.boxTextColorNoActive');
  margin: 0;
}

/* 操作按钮容器 */
.action-buttons {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
  gap: 8px;
}

/* 通用图标按钮样式 */
.action-button {
  min-width: 32px; /* 调整为正方形按钮 */
  min-height: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-button i {
  font-size: 20px; /* 统一图标大小 */
}

/* 危险按钮样式 */
.danger-button {
  background-color: v-bind('theme.dangerColor');
  border-color: v-bind('theme.dangerBorderColor');
  color: v-bind('theme.dangerTextColor');
}

.danger-button:hover {
  background-color: v-bind('theme.dangerColorHover');
  border-color: v-bind('theme.dangerBorderColorHover');
}

/* 保存按钮样式 */
.save-button {
  background-color: v-bind('theme.mainColor');
  border-color: v-bind('theme.mainBorderColor');
  color: v-bind('theme.mainTextColor');
}

.save-button:hover {
  background-color: v-bind('theme.mainColorHover');
  border-color: v-bind('theme.mainBorderColorHover');
}

@media (max-width: 768px) {
  .role-manager-layout {
    flex-direction: column;
    height: 100%;
    position: relative;
  }

  .role-content {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
  }
  
  .form-row {
    flex-direction: column;
  }
  
  .status-group {
    flex: 1;
  }
}

/* 对话示例区域中的按钮样式 */
.redirect-btn {
  margin-top: 15px;
  min-width: 150px;
  background-color: v-bind('theme.boxSecondColor');
}
</style>