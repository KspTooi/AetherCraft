<template>
  <div class="npc-manager-layout">
    <ChatNpcListCreator
      ref="npcListRef"
      class="npc-sidebar"
      @select-npc="onSelectNpc"
      @create-npc="onCreateNpc"
    />

    <GlowDiv class="npc-content" border="none">
        <div v-if="!selectedNpcId" class="empty-detail">
          <i class="bi bi-person-bounding-box"></i>
          <p>请从左侧选择一个NPC进行设计</p>
        </div>
      
      <GlowTab
        v-else
        :items="npcTabItems"
        v-model:activeTab="currentTab"
      >
        <!-- NPC基本信息 -->
        <div v-if="currentTab === 'base-info'" class="npc-tab-panel">
          <div class="npc-panel">
            <!-- 操作按钮区域（顶部） -->
            <div class="action-buttons">
              <GlowButton 
                @click="removeNpc(selectedNpcId)"
                :disabled="loading"
                class="action-button danger-button"
                title="移除NPC"
              >
                移除
              </GlowButton>
              <GlowButton 
                @click="copyCurrentNpc"
                :disabled="loading"
                class="action-button"
                title="复制NPC"
              >
                复制
              </GlowButton>
              <GlowButton 
                @click="saveNpcChanges" 
                :disabled="loading"
                class="action-button save-button"
                title="保存NPC"
              >
                保存
              </GlowButton>
            </div>
            
            <div class="npc-detail-form">
              <div class="form-row">
                <GlowInput
                  v-model="currentNpcDetails.name"
                  title="NPC名称"
                  :maxLength="50"
                  showLength
                  notBlank
                />
              </div>
              
              <div class="form-row">
                <div class="input-group">
                  <GlowInput
                    v-model="currentNpcDetails.tags"
                    title="NPC标签"
                    :maxLength="50"
                    showLength
                    placeholder="使用逗号分隔多个标签"
                  />
                </div>
                <div class="input-group status-group">
                  <div class="status-wrapper">
                    <GlowCheckBox 
                      v-model="npcEnabled"
                      tip="被禁用的NPC在聊天界面将不可见">
                      启用NPC
                    </GlowCheckBox>
                  </div>
                </div>
              </div>
              
              <div class="form-row">
                <GlowInputArea
                  v-model="currentNpcDetails.description"
                  title="NPC描述"
                  :maxLength="50000"
                  showLength
                  :auto-resize="true"
                />
              </div>
              
              <div class="form-row">
                <GlowInputArea
                  v-model="currentNpcDetails.roleSummary"
                  title="NPC设定摘要"
                  :maxLength="50000"
                  showLength
                  :auto-resize="true"
                />
              </div>
              
              <div class="form-row">
                <GlowInputArea
                  v-model="currentNpcDetails.scenario"
                  title="情景"
                  :maxLength="50000"
                  showLength
                  :auto-resize="true"
                />
              </div>
              
              <div class="form-row">
                <GlowInputArea
                  v-model="currentNpcDetails.firstMessage"
                  title="首次对话内容"
                  :maxLength="50000"
                  showLength
                  :auto-resize="true"
                />
              </div>
              
              <!-- 头像区域 -->
              <div class="avatar-section">
                <p class="section-title">NPC头像</p>
                <div class="avatar-container">
                  <img v-if="currentNpcDetails.avatarUrl" :src="currentNpcDetails.avatarUrl" class="avatar-preview" alt="NPC头像" />
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
        <div v-if="currentTab === 'chat-examples'" class="npc-tab-panel">
          <div class="npc-panel">
            <NpcChatExampleTab v-if="selectedNpcId" :npcId="selectedNpcId" />
            <div v-else class="chat-examples-placeholder">
              <i class="bi bi-chat-dots"></i>
              <p>请先选择一个NPC以查看和编辑对话示例</p>
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
import ChatNpcListCreator from '@/components/glow-client/ChatNpcListCreator.vue';
import GlowAlter from "@/components/glow-ui/GlowAlter.vue";
import GlowInput from "@/components/glow-ui/GlowInput.vue";
import GlowInputArea from "@/components/glow-ui/GlowInputArea.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowCheckBox from "@/components/glow-ui/GlowCheckBox.vue";
import GlowTab from "@/components/glow-ui/GlowTab.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import GlowConfirmInput from "@/components/glow-ui/GlowConfirmInput.vue";
import { usePreferencesStore } from '@/stores/preferences';
import NpcChatExampleTab from '@/components/glow-client/NpcChatExampleTab.vue';
import { useRoute } from 'vue-router';
import NpcApi, { type GetNpcDetailsVo, type SaveNpcDto, type GetNpcListVo } from '@/commons/api/NpcApi';
import type CommonIdDto from '@/entity/dto/CommonIdDto';

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme);

// 获取路由和路由参数
const route = useRoute();

// 获取偏好设置存储
const preferencesStore = usePreferencesStore();

// NPC列表引用
const npcListRef = ref<any>(null);
const alterRef = ref<any>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const uploading = ref(false);
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null);
const confirmInputRef = ref<InstanceType<typeof GlowConfirmInput> | null>(null);

// 加载状态
const loading = ref(false);
const selectedNpcId = ref<string>(""); //当前选择的NPC_ID

// 定义标签项
const npcTabItems = [
  { title: 'NPC基本', action: 'base-info' },
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

// 先初始化currentNpcDetails
const currentNpcDetails = reactive<GetNpcDetailsVo>({
  id:"",
  name:"",
  status:1,
  sortOrder:0,
  avatarUrl:"",
  description:"",
  roleSummary:"",
  scenario:"",
  firstMessage:"",
  tags:""
});

// 然后初始化npcEnabled并设置监听
const npcEnabled = ref(currentNpcDetails.status === 1);

// 监听NPC启用状态变化
watch(npcEnabled, (newValue) => {
  currentNpcDetails.status = newValue ? 1 : 0;
});

// 监听NPC详情中的status变化
watch(() => currentNpcDetails.status, (newValue) => {
  npcEnabled.value = newValue === 1;
});

// 生命周期钩子
onMounted(async () => {
  // 优先检查URL中的roleId参数
  const urlRoleId = route.query.roleId as string;
  
  // 从偏好设置中加载上次选择的NPCID和Tab
  const savedRoleId = preferencesStore.getModelRoleEditCurrentId;
  const savedTab = preferencesStore.getModelRoleEditPathTab;
  
  if (savedTab) {
    currentTab.value = savedTab;
  }
  
  // 等待NPC列表组件加载完成
  await new Promise(resolve => setTimeout(resolve, 100));
  
  // 如果URL中有roleId，优先使用它
  if (urlRoleId) {
    selectedNpcId.value = urlRoleId;
    // 保存到偏好设置中，以便下次访问时记住
    preferencesStore.saveModelRoleEditCurrentId(urlRoleId);
    // 同步子组件的选中状态
    npcListRef.value?.selectNpc(urlRoleId);
    await loadNpcDetails();
  } 
  // 否则，尝试使用保存的roleId
  else if (savedRoleId) {
    // 检查保存的NPCID是否在列表中存在
    const npcExists = npcListRef.value?.listData?.some((npc: any) => npc.id === savedRoleId);
    if (npcExists) {
      selectedNpcId.value = savedRoleId;
      // 同步子组件的选中状态
      npcListRef.value?.selectNpc(savedRoleId);
      await loadNpcDetails();
    }
  }
});

// 加载NPC详情
const loadNpcDetails = async () => {
  if (!selectedNpcId.value) {
    return;
  }
  
  try {
    const dto: CommonIdDto = { id: selectedNpcId.value };
    const response = await NpcApi.getNpcDetails(dto);
    
    Object.assign(currentNpcDetails, response);
  } catch (error) {
    alterRef.value?.showConfirm({
      title: '加载失败',
      content: error instanceof Error ? error.message : '无法加载NPC详情，请稍后重试',
      closeText: '确定'
    });
  }
}

// 移除NPC
const removeNpc = async (roleId: string) => {
  if (!confirmRef.value) return;
  
  // 显示确认对话框
  const confirmed = await confirmRef.value.showConfirm({
    title: '移除NPC',
    content: '警告：这是一个不可逆的操作！移除NPC将同时删除：\n\n' +
             '• 与该NPC的所有聊天记录\n' +
             '• NPC的所有个人设定数据\n' +
             '• 所有对话示例\n\n' +
             '删除后，您将无法恢复这些数据。确定要继续吗？',
    confirmText: '确认移除',
    cancelText: '取消'
  });
  
  // 用户取消操作
  if (!confirmed) return;
  
  try {
    loading.value = true;
    const dto: CommonIdDto = { id: roleId };
    await NpcApi.removeNpc(dto);
    
    // 如果删除的是当前选中的NPC，清空选择
    if (roleId === selectedNpcId.value) {
      selectedNpcId.value = '';
    }
    
    // 刷新NPC列表
    await npcListRef.value?.loadNpcList();
    
    loading.value = false;
  } catch (error) {
    loading.value = false;
    
    // 使用GlowAlter显示错误提示
    alterRef.value?.showConfirm({
      title: '操作失败',
      content: error instanceof Error ? error.message : '删除NPC出错，请稍后重试',
      closeText: '确定'
    });
  }
}

// 保存NPC
const saveNpc = async (npcData: SaveNpcDto) => {
  try {
    loading.value = true;
    const response = await NpcApi.saveNpc(npcData);
    
    // 返回的是新NPC的ID
    if (response) {
      // 刷新NPC列表
      await npcListRef.value?.loadNpcList();
      
      // 如果是新创建的NPC，选中它
      if (!npcData.id) {
        selectedNpcId.value = response;
        // 同步子组件的选中状态
        npcListRef.value?.selectNpc(response);
        // 加载新创建NPC的详情
        await loadNpcDetails();
      }
    } else {
      // 使用GlowAlter显示错误提示
      alterRef.value?.showConfirm({
        title: '保存失败',
        content: '保存NPC信息失败：未获取有效响应',
        closeText: '确定'
      });
    }
    
    loading.value = false;
  } catch (error) {
    loading.value = false;
    
    // 使用GlowAlter显示错误提示
    alterRef.value?.showConfirm({
      title: '保存失败',
      content: error instanceof Error ? error.message : '保存NPC信息出错，请稍后重试',
      closeText: '确定'
    });
  }
}

// 选择NPC
const onSelectNpc = async (npc: GetNpcListVo) => {
  selectedNpcId.value = npc.id;
  
  // 保存选中的NPCID到偏好设置
  preferencesStore.saveModelRoleEditCurrentId(npc.id);
  
  // 加载选中NPC的详情
  await loadNpcDetails();
};

// 复制NPC
const onCopyNpc = async (roleId: string) => {
  try {
    loading.value = true;
    const dto: CommonIdDto = { id: roleId };
    await NpcApi.copyNpc(dto);
    
    // 刷新NPC列表
    await npcListRef.value?.loadNpcList();
    loading.value = false;
    
    // 显示成功提示
    alterRef.value?.showConfirm({
      title: '复制成功',
      content: 'NPC已成功复制。\n\n请注意：\n• 仅复制NPC基本设定和对话示例\n• 不会复制用户与NPC的聊天历史记录\n• 您可以开始与新NPC进行全新的对话',
      closeText: '确定'
    });
  } catch (error) {
    loading.value = false;
    
    // 显示错误提示
    alterRef.value?.showConfirm({
      title: '复制失败',
      content: error instanceof Error ? error.message : '复制NPC失败，请稍后重试',
      closeText: '确定'
    });
  }
};

// 复制当前NPC
const copyCurrentNpc = () => {
  if (selectedNpcId.value) {
    onCopyNpc(selectedNpcId.value);
  }
};

// 创建新NPC
const onCreateNpc = async () => {
  if (!confirmInputRef.value) return;
  
  // 弹出输入框让用户输入NPC名称
  const result = await confirmInputRef.value.showInput({
    title: '创建新NPC',
    defaultValue: '新NPC',
    placeholder: '请输入NPC名称',
    message: '创建后可以继续编辑NPC的其他信息',
    confirmText: '创建',
    cancelText: '取消'
  });
  
  // 用户取消创建
  if (!result.confirmed) return;
  
  // 用户确认创建且输入了NPC名称
  if (result.value.trim()) {
    // 清空当前选中的NPCID
    selectedNpcId.value = "";
    
    // 初始化一个新的NPC详情对象
    Object.assign(currentNpcDetails, {
      id: "",
      name: result.value.trim(), // 使用用户输入的NPC名称
      status: 1,
      sortOrder: 0,
      avatarUrl: "",
      description: "",
      roleSummary: "",
      scenario: "",
      firstMessage: "",
      tags: ""
    });
    
    // 等待下一个事件循环，确保表单已重置
    setTimeout(() => {
      // 自动保存新NPC
      saveNpcChanges();
    }, 0);
  } else {
    // 用户输入为空，显示提示
    alterRef.value?.showConfirm({
      title: '创建失败',
      content: 'NPC名称不能为空',
      closeText: '确定'
    });
  }
};

// 保存NPC更改
const saveNpcChanges = async () => {
  // 验证必填字段
  if (!currentNpcDetails.name.trim()) {
    alterRef.value?.showConfirm({
      title: '验证失败',
      content: 'NPC名称不能为空',
      closeText: '确定'
    });
    return;
  }
  
  // 处理头像路径，移除所有"/res/"前缀
  let avatarUrl = currentNpcDetails.avatarUrl;
  if (avatarUrl) {
    // 使用正则表达式移除所有/res/前缀
    while (avatarUrl.includes('/res/')) {
      avatarUrl = avatarUrl.replace('/res/', '');
    }
  }
  
  const npcData: SaveNpcDto = {
    id: currentNpcDetails.id,
    name: currentNpcDetails.name,
    description: currentNpcDetails.description,
    roleSummary: currentNpcDetails.roleSummary,
    scenario: currentNpcDetails.scenario,
    firstMessage: currentNpcDetails.firstMessage,
    tags: currentNpcDetails.tags,
    status: currentNpcDetails.status,
    sortOrder: currentNpcDetails.sortOrder,
    avatarUrl: avatarUrl
  };
  
  await saveNpc(npcData);
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
    
    // 使用NpcApi上传头像
    const avatarUrl = await NpcApi.uploadAvatar(file);
    
    // 更新头像路径
    currentNpcDetails.avatarUrl = '/res/' + avatarUrl;
    
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
</script>

<style scoped>
.npc-manager-layout {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.npc-sidebar {
  flex-shrink: 0;
}

.npc-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.npc-tab-panel {
  height: 100%;
  overflow: hidden;
}

.npc-panel {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  max-height: calc(100vh - 135px);
}

.npc-panel::-webkit-scrollbar {
  width: 8px;
}

.npc-panel::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
}

.npc-panel::-webkit-scrollbar-track {
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

.npc-detail-form {
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
  .npc-manager-layout {
    flex-direction: column;
    height: 100%;
    position: relative;
  }

  .npc-content {
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