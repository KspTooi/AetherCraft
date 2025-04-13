<template>
  <div class="role-manager-layout">
    <ModelRoleManagerList 
      ref="roleListRef"
      class="role-sidebar"
      :data="roleList"
      :selected="selectedRoleId"
      :loading="loading"
      @select-role="onSelectRole"
      @copy-role="onCopyRole"
    />

    <GlowDiv class="role-content" border="none">
      <!-- 右侧内容 -->
      <div class="role-detail-container">
        <div v-if="!selectedRoleId" class="empty-detail">
          <i class="bi bi-person-bounding-box"></i>
          <p>请从左侧选择一个角色进行设计</p>
        </div>
        <div v-else class="role-detail">
          <div class="role-detail-header">
            <h2 class="role-detail-title">角色详情</h2>
            <div class="role-actions">
              <GlowButton @click="saveRoleChanges" :disabled="loading">保存</GlowButton>
            </div>
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
            
            <!-- 头像区域可以后续添加 -->
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
    </GlowDiv>
    
    <!-- 添加提示组件 -->
    <GlowAlter ref="alterRef" />
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

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme);

// 角色列表引用
const roleListRef = ref<any>(null);
const alterRef = ref<any>(null);

// 角色列表数据
const roleList = ref<GetModelRoleListVo[]>([]);
const loading = ref(true);
const selectedRoleId = ref<string>("");
const fileInput = ref<HTMLInputElement | null>(null);
const uploading = ref(false);

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
      content: '无法加载角色列表，请稍后重试',
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
      content: '无法加载角色详情，请稍后重试',
      closeText: '确定'
    });
  }
}

const removeModelRole = async (roleId: string) => {
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
        content: '删除角色失败，请稍后重试',
        closeText: '确定'
      });
    }
    
    loading.value = false;
  } catch (error) {
    loading.value = false;
    
    // 使用GlowAlter显示错误提示
    alterRef.value?.showConfirm({
      title: '操作失败',
      content: '删除角色出错，请稍后重试',
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
        content: '保存角色信息失败，请稍后重试',
        closeText: '确定'
      });
    }
    
    loading.value = false;
  } catch (error) {
    loading.value = false;
    
    // 使用GlowAlter显示错误提示
    alterRef.value?.showConfirm({
      title: '保存失败',
      content: '保存角色信息出错，请稍后重试',
      closeText: '确定'
    });
  }
}

// 选择角色
const onSelectRole = async (roleId: string) => {
  selectedRoleId.value = roleId;
  
  // 加载选中角色的详情
  await loadModelRoleDetails();
};

// 复制角色 - 空实现
const onCopyRole = (roleId: string) => {
  // 这里添加复制角色的实现逻辑
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
      throw new Error('上传失败');
    }
    
    const data = await response.json();
    
    // 更新头像路径 (假设响应格式为 { data: 文件路径 })
    if (data && data.data) {
      currentRoleDetails.avatarPath = '/res/' + data.data;
    }
    
    uploading.value = false;
    
    // 清空文件输入框，允许重复上传相同文件
    target.value = '';
  } catch (error) {
    uploading.value = false;
    
    alterRef.value?.showConfirm({
      title: '上传失败',
      content: '头像上传失败，请稍后重试',
      closeText: '确定'
    });
    
    // 清空文件输入框
    target.value = '';
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

.role-detail-container {
  flex: 1;
  padding: 10px;
  overflow: auto;
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

.role-detail {
  padding: 10px;
}

.role-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
  padding-bottom: 10px;
}

.role-detail-title {
  font-size: 18px;
  margin: 0;
  color: v-bind('theme.boxTextColor');
}

.role-actions {
  display: flex;
  gap: 10px;
}

.role-detail-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
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
</style>