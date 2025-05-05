<template>
  <GlowDiv class="player-info-container" border="none">
    <div v-if="loading" class="loading-indicator">加载中...</div>
    <div v-else-if="playerDetails" class="form-panel">

      <div class="form-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-container">
            <img v-if="editableDetails.avatarUrl" :src="editableDetails.avatarUrl.startsWith('/') ? editableDetails.avatarUrl : '/res/' + editableDetails.avatarUrl" class="avatar-preview" alt="人物头像" />
            <div v-else class="avatar-placeholder">
              <i class="bi bi-person"></i>
            </div>
            <div class="avatar-upload">
              <div class="player-name-display" v-if="playerDetails">{{ playerDetails.name }}</div>
              <div class="avatar-buttons-row">
                <GlowButton
                  class="upload-btn"
                  @click="triggerFileUpload"
                  :disabled="saving || uploading"
                >
                  {{ uploading ? '上传中...' : '更换头像' }}
                </GlowButton>
                <GlowButton
                  @click="saveChanges"
                  :disabled="saving || uploading || !isFormValid"
                  class="save-button-avatar"
                  title="保存全部更改"
                  :corners="['bottom-right']"
                >
                  {{ saving ? '保存中...' : '保存全部更改' }}
                </GlowButton>
              </div>
              <input
                type="file"
                ref="fileInput"
                style="display: none"
                accept="image/jpeg,image/png"
                @change="handleFileUpload"
              />
              <p class="upload-tip">支持JPG、PNG格式，大小不超过2MB</p>
            </div>
          </div>
        </div>

        <!-- 只读信息 & 可编辑信息 (不再分组) -->
        <div class="form-row">
          <GlowInput
             :model-value="playerDetails.balance + ' CU'"
             title="钱包余额"
             disabled
          />
          <GlowInput
             :model-value="formatStatus(playerDetails.status)"
             title="状态"
             disabled
          />
          <GlowInput
              :model-value="formatDate(playerDetails.createTime)"
              title="诞生日期"
              disabled
          />
        </div>

        <div class="form-row">
          <GlowInput
            v-model="editableDetails.language"
            title="语言"
            :maxLength="20"
            showLength
            placeholder="如：中文, English, zh-CN, en-US"
            :disabled="saving"
          />
          <GlowInput
            v-model="editableDetails.era"
            title="年代"
            :maxLength="50"
            showLength
            placeholder="如：古代, 现代, 未来, 赛博朋克, 80S"
            :disabled="saving"
          />
          <GlowSelector
            v-model="editableDetails.contentFilterLevel"
            title="内容过滤等级"
            :options="filterLevelOptions"
            value-key="value"
            label-key="label"
            :disabled="saving"
          />
        </div>

        <div class="form-row">
          <GlowInputArea
            v-model="editableDetails.publicInfo"
            title="公开信息 (可选)"
            :maxLength="200"
            showLength
            :auto-resize="true"
            placeholder="其他人可见的简短介绍"
            :disabled="saving"
          />
        </div>

        <div class="form-row">
          <GlowInputArea
            v-model="editableDetails.description"
            title="人物描述 (可选, 内容将加密存储)"
            :maxLength="500"
            showLength
            :auto-resize="true"
            placeholder="更详细的人物背景、性格等设定"
            :disabled="saving"
          />
        </div>

      </div>
    </div>
    <div v-else class="error-indicator">
        未能加载玩家信息，请稍后再试。
    </div>
    <!-- 添加提示组件 -->
    <GlowAlter ref="alterRef" />
    <!-- 添加确认对话框组件 -->
    <GlowConfirm ref="confirmRef" />
  </GlowDiv>
</template>

<script setup lang="ts">
import { reactive, ref, computed, inject, onMounted } from "vue";
import { useRouter } from 'vue-router';
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import GlowInput from "@/components/glow-ui/GlowInput.vue";
import GlowInputArea from "@/components/glow-ui/GlowInputArea.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowSelector from "@/components/glow-ui/GlowSelector.vue";
import GlowAlter from "@/components/glow-ui/GlowAlter.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import type { GetAttachPlayerDetailsVo, EditAttachPlayerDetailsDto } from "@/commons/api/PlayerApi.ts";
import PlayerApi from "@/commons/api/PlayerApi.ts";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme';
import Http from "@/commons/Http";
import dayjs from 'dayjs'; // For date formatting

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme);
const router = useRouter();
const alterRef = ref<InstanceType<typeof GlowAlter> | null>(null);
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

const loading = ref(true);
const saving = ref(false);
const uploading = ref(false);
const playerDetails = ref<GetAttachPlayerDetailsVo | null>(null);
// Separate state for editable fields to easily construct the DTO
const editableDetails = reactive<Partial<EditAttachPlayerDetailsDto>>({
    id: undefined, // Will be populated after fetch
    avatarUrl: "",
    contentFilterLevel: 1,
    description: "",
    era: "",
    language: "",
    publicInfo: ""
});

const filterLevelOptions = [
  { label: "宽松", value: 0 },
  { label: "普通", value: 1 },
  { label: "严格", value: 2 },
];

// --- Data Fetching ---
onMounted(async () => {
  loading.value = true;
  try {
    const details = await PlayerApi.getAttachPlayerDetails();
    playerDetails.value = details;
    // Populate editable details
    editableDetails.id = details.id;
    editableDetails.avatarUrl = details.avatarUrl ?? ""; // Handle null avatarUrl
    editableDetails.contentFilterLevel = details.contentFilterLevel;
    editableDetails.description = details.description ?? "";
    editableDetails.era = details.era ?? "";
    editableDetails.language = details.language;
    editableDetails.publicInfo = details.publicInfo ?? "";
  } catch (error) {
    console.error("Failed to fetch player details:", error);
    playerDetails.value = null; // Indicate error
    alterRef.value?.showConfirm({
        title: '加载失败',
        content: error instanceof Error ? error.message : '无法加载玩家信息，请稍后重试。',
        closeText: '确定'
    });
  } finally {
    loading.value = false;
  }
});

// --- Form Validation ---
const isFormValid = computed(() => {
  // Basic validation: language is required
  return !!editableDetails.language?.trim();
});

// --- Formatters ---
const formatStatus = (status: number | undefined): string => {
    if (status === undefined) return '未知';
    switch (status) {
        case 0: return '正在使用';
        case 1: return '不活跃';
        case 2: return '等待删除';
        case 3: return '已删除';
        default: return `未知 (${status})`;
    }
};

const formatDate = (dateString: string | undefined): string => {
    if (!dateString) return '未知';
    // Assuming backend returns ISO 8601 or similar parsable string
    return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss');
};

// --- Avatar Upload ---
const triggerFileUpload = () => {
  fileInput.value?.click();
};

const handleFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const files = target.files;

  if (!files || files.length === 0) return;
  const file = files[0];

  if (file.type !== 'image/jpeg' && file.type !== 'image/png') {
    alterRef.value?.showConfirm({ title: '文件类型错误', content: '请上传JPG或PNG格式的图片', closeText: '确定' });
    return;
  }
  if (file.size > 2 * 1024 * 1024) {
    alterRef.value?.showConfirm({ title: '文件过大', content: '图片大小不能超过2MB', closeText: '确定' });
    return;
  }

  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);

    const response = await Http.postEntity<string>('/player/uploadAvatar', formData);

    if (response) {
        // Store raw path from backend, add /res/ prefix when displaying if needed
        editableDetails.avatarUrl = response;
    } else {
        throw new Error('上传失败：服务器未返回有效路径');
    }
    alterRef.value?.showConfirm({ title: '成功', content: '头像上传成功！请记得保存更改。', closeText: '好的' });
  } catch (error) {
    alterRef.value?.showConfirm({
      title: '上传失败',
      content: error instanceof Error ? error.message : '头像上传失败，请稍后重试',
      closeText: '确定'
    });
  } finally {
    uploading.value = false;
    if (target) target.value = '';
  }
};


// --- Save Changes Logic ---
const saveChanges = async () => {
  if (!isFormValid.value || !editableDetails.id) {
    alterRef.value?.showConfirm({ title: '提示', content: '请确保语言字段不为空。', closeText: '确定' });
    return;
  }

  saving.value = true;
  try {
    // Construct the DTO for saving
    const payload: EditAttachPlayerDetailsDto = {
        id: editableDetails.id, // id is required
        language: editableDetails.language, // language is required
        // Include other fields if they have values or default if needed
        avatarUrl: editableDetails.avatarUrl || undefined,
        contentFilterLevel: editableDetails.contentFilterLevel,
        description: editableDetails.description || undefined,
        era: editableDetails.era || undefined,
        publicInfo: editableDetails.publicInfo || undefined,
    };
    
    // Remove /res/ prefix if it exists before sending
    if (payload.avatarUrl && payload.avatarUrl.startsWith('/res/')) {
        payload.avatarUrl = payload.avatarUrl.substring(5);
    }

    await PlayerApi.editAttachPlayerDetails(payload);

    alterRef.value?.showConfirm({
        title: '保存成功',
        content: '人物信息已更新！',
        closeText: '好的'
    });

    // Optionally refetch data to ensure consistency, or update playerDetails ref locally
    // For simplicity, we'll just show success message here. Refetch might be better.
    // const updatedDetails = await PlayerApi.getAttachPlayerDetails();
    // playerDetails.value = updatedDetails;


  } catch (error) {
    alterRef.value?.showConfirm({
      title: '保存失败',
      content: error instanceof Error ? error.message : '保存信息时发生错误，请稍后重试。',
      closeText: '确定'
    });
  } finally {
    saving.value = false;
  }
};

</script>

<style scoped>
/* Copy most styles from PlayerCreate.vue */
.player-info-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  overflow-y: auto;
  box-sizing: border-box;
}

.player-info-container::-webkit-scrollbar {
  width: 8px;
}
.player-info-container::-webkit-scrollbar-thumb {
  background: rgba(200, 200, 200, 0.18);
  border-radius: 0;
}
.player-info-container::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.04);
}

.loading-indicator, .error-indicator {
    margin-top: 50px;
    color: v-bind('theme.boxTextColor');
    font-size: 18px;
    text-align: center;
}

.form-panel {
  width: 100%;
  max-width: 800px;
  margin-top: 20px;
  padding: 25px;
  margin-bottom: 25px;
}

.form-title {
  margin: 0 0 20px 0; /* Added bottom margin */
  padding: 0;
  font-family: 'Chakra Petch', sans-serif;
  text-align: center;
  color: v-bind('theme.boxGlowColor');
  font-size: 24px;
  font-weight: 600;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row > * {
  flex: 1;
}

.section-title {
  font-family: 'Chakra Petch', sans-serif;
  font-size: 12px;
  color: v-bind('theme.boxTextColor');
  margin-bottom: 8px;
  opacity: 0.8;
  font-weight: 500;
  border-bottom: 1px solid v-bind('theme.boxBorderColor'); /* Add separator line */
  padding-bottom: 5px; /* Space below line */
}

.avatar-section {
  margin-bottom: 10px;
  background-color: rgba(255, 255, 255, 0.03); 
  padding: 15px;
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0;
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 20px;
  /* Removed background and border, now handled by avatar-section */
}

.avatar-preview {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: v-bind('theme.boxSecondColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0;
}

.avatar-placeholder i {
  font-size: 40px;
  color: v-bind('theme.boxTextColorNoActive');
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.player-name-display {
    font-family: 'Chakra Petch', sans-serif;
    font-size: 18px;
    font-weight: 600;
    color: v-bind('theme.boxTextColor');
    margin-bottom: 10px;
    text-align: left;
}

.avatar-buttons-row {
    display: flex;
    flex-direction: row;
    gap: 10px;
    align-items: center;
}

.upload-btn {
  min-width: 100px;
}

.upload-tip {
  font-size: 12px;
  color: v-bind('theme.boxTextColorNoActive');
  margin: 0;
}

/* Style for the save button within the avatar section */
.save-button-avatar {
    min-width: 100px;
    background-color: v-bind('theme.mainColor');
    border-color: v-bind('theme.mainBorderColor');
    color: v-bind('theme.mainTextColor');
    font-weight: bold;
}

.save-button-avatar:hover:not(:disabled) {
    background-color: v-bind('theme.mainColorHover');
    border-color: v-bind('theme.mainBorderColorHover');
}

/* Responsive adjustments */
@media (max-width: 600px) {
  .form-row {
    flex-direction: column;
  }
  .avatar-container {
      flex-direction: column;
      align-items: flex-start;
  }
  .form-panel {
      padding: 15px; /* Adjust padding */
  }
}

</style>