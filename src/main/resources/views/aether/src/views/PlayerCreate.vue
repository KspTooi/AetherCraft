<template>
  <GlowDiv class="player-create-container" border="none">
    <div class="form-panel">
      <h2 class="form-title">新人物信息采集清单</h2>
      <div class="form-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <p class="section-title">人物头像</p>
          <div class="avatar-container">
            <img v-if="details.avatarUrl" :src="details.avatarUrl" class="avatar-preview" alt="人物头像" />
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
                :disabled="uploading || loading"
                :corners="['bottom-right']"
              >
                {{ uploading ? '上传中...' : '上传头像' }}
              </GlowButton>
              <p class="upload-tip">支持JPG、PNG格式，大小不超过2MB</p>
            </div>
          </div>
        </div>
        
        <div class="form-row">
          <!-- Wrap input and error message together -->
          <div class="input-with-error">
            <GlowInput
              v-model="details.name"
              title="人物名称(不可更改)"
              :maxLength="24"
              showLength
              notBlank
              placeholder="字母、数字、中文，不能以数字开头"
              error-tip="名称格式不正确或已被占用"
              @typeDone="checkNameAvailability"
              :isInvalid="!isNameAvailable"
            />
            <div v-if="!isNameAvailable && details.name" class="glow-input-error-message">
              该名称已被占用或格式不正确
            </div>
          </div>
        </div>

        <div class="form-row">
          <GlowSelector
            v-model="details.gender"
            title="性别"
            :options="genderOptions"
            value-key="value"
            label-key="label"
            notBlank
          />
          <GlowInput
            v-if="isCustomGender"
            v-model="details.genderData"
            title="自定义性别"
            :maxLength="20"
            showLength
            notBlank
            placeholder="请输入自定义性别"
          />
        </div>
        
        <div class="form-row">
          <GlowInput
            v-model="details.language"
            title="语言"
            :maxLength="20"
            showLength
            notBlank
            placeholder="如：中文, English, zh-CN, en-US"
          />
          <GlowInput
            v-model="details.era"
            title="年代"
            :maxLength="50"
            showLength
            placeholder="如：古代, 现代, 未来, 赛博朋克, 80S"
          />
          <GlowSelector
            v-model="details.contentFilterLevel"
            title="内容过滤等级"
            :options="filterLevelOptions"
            value-key="value"
            label-key="label"
          />
        </div>
        
        <div class="form-row">
          <GlowInputArea
            v-model="details.publicInfo"
            title="公开信息 (可选)"
            :maxLength="200"
            showLength
            :auto-resize="true"
            placeholder="其他人可见的简短介绍"
          />
        </div>
        
        <div class="form-row">
          <GlowInputArea
            v-model="details.description"
            title="人物描述 (可选, 内容将加密存储)"
            :maxLength="500"
            showLength
            :auto-resize="true"
            placeholder="更详细的人物背景、性格等设定"
          />
        </div>
        
        <div class="action-buttons">
          <GlowButton
            v-if="showBackButton"
            @click="goBack"
            title="返回人物选择"
            :corners="['bottom-left']"
          >
            返回人物选择
          </GlowButton>
          <GlowButton
            @click="createPlayer"
            :disabled="loading || uploading || !isNameAvailable || !isFormValid"
            class="create-button"
            title="创建人物"
            :corners="['bottom-right']"
          >
            下一步
          </GlowButton>
        </div>
      </div>
    </div>
    <!-- 添加提示组件 -->
    <GlowAlter ref="alterRef" />
    <!-- 添加确认对话框组件 -->
    <GlowConfirm ref="confirmRef" />
  </GlowDiv>
</template>

<script setup lang="ts">
import { reactive, ref, computed, inject, watch } from "vue";
import { useRouter, useRoute } from 'vue-router';
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import GlowInput from "@/components/glow-ui/GlowInput.vue";
import GlowInputArea from "@/components/glow-ui/GlowInputArea.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowSelector from "@/components/glow-ui/GlowSelector.vue";
import GlowAlter from "@/components/glow-ui/GlowAlter.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import type { CreatePlayerDto } from "@/commons/api/PlayerApi.ts";
import PlayerApi from "@/commons/api/PlayerApi.ts"; // Import default export
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme';
import Http from "@/commons/Http";

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme);
const router = useRouter();
const route = useRoute();
const alterRef = ref<InstanceType<typeof GlowAlter> | null>(null);
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const uploading = ref(false);
const loading = ref(false);
const isNameAvailable = ref(true); // Track name availability

// Computed property to control back button visibility
const showBackButton = computed(() => route.query.init !== 'true');

const details = reactive<CreatePlayerDto>({
  avatarUrl: "",
  contentFilterLevel: 1, // Default to 普通
  description: "",
  era: "",
  language: "中文", // Default language
  name: "",
  publicInfo: "",
  gender: 0, // Default to 男
  genderData: ""
});

const filterLevelOptions = [
  { label: "宽松", value: 0 },
  { label: "普通", value: 1 },
  { label: "严格", value: 2 },
];

const genderOptions = [
  { label: "男", value: 0 },
  { label: "女", value: 1 },
  { label: "不愿透露", value: 2 },
  { label: "自定义(男性)", value: 4 },
  { label: "自定义(女性)", value: 5 },
  { label: "自定义(其他)", value: 6 },
];

const isCustomGender = computed(() => {
  return [4, 5, 6].includes(details.gender);
});

// Watch for changes in gender and clear genderData if not custom
watch(() => details.gender, (newGender) => {
  if (![4, 5, 6].includes(newGender)) {
    details.genderData = "";
  }
});


const checkNameAvailability = async (name: string) => {

  if(!await PlayerApi.checkPlayerName({ name })){    
    isNameAvailable.value = false;
    return
  }
  isNameAvailable.value = true;
};

// --- Form Validation ---
const isFormValid = computed(() => {
  // Check if custom gender is selected and genderData is empty
  if (isCustomGender.value && details.genderData?.trim() === "") {
    return false;
  }
  return (
    isNameAvailable.value &&
    details.language.trim() !== ""
  );
});


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
    
    // Use Http utility for upload, assuming it handles FormData Content-Type automatically
    const response = await Http.postEntity<string>('/player/uploadAvatar', formData);

    if (response) {
       // Prepend /res/ like in ModelRoleManager
      details.avatarUrl = '/res/' + response;
    } else {
      throw new Error('上传失败：服务器未返回有效路径');
    }
  } catch (error) {
    alterRef.value?.showConfirm({
      title: '上传失败',
      content: error instanceof Error ? error.message : '头像上传失败，请稍后重试',
      closeText: '确定'
    });
  } finally {
    uploading.value = false;
    // Clear file input
    if (target) target.value = '';
  }
};


// --- Create Player Logic ---
const createPlayer = async () => {
  if (!isFormValid.value) {
    alterRef.value?.showConfirm({ title: '提示', content: '请检查表单信息是否完整且正确。', closeText: '确定' });
    return;
  }

  loading.value = true;
  try {
    // Remove /res/ prefix before sending to backend if it exists
    const payload = { ...details };
    if (payload.avatarUrl && payload.avatarUrl.startsWith('/res/')) {
        payload.avatarUrl = payload.avatarUrl.substring(5); // Remove "/res/"
    }

    const playerId = await PlayerApi.createPlayer(payload);
    
    // Show confirmation and redirect after it's closed
    await alterRef.value?.showConfirm({
        title: '创建成功',
        content: `人物 "${details.name}" 已成功创建！您的新冒险即将开始！`,
        closeText: '太棒了！'
    });
    // Redirect after the alter is closed to the lobby
    router.push('/playLobby');

  } catch (error) {
    alterRef.value?.showConfirm({
      title: '创建失败',
      content: error instanceof Error ? error.message : '创建人物时发生错误，请稍后重试。',
      closeText: '确定'
    });
  } finally {
    loading.value = false;
  }
};

const goBack = async () => {
    if (!confirmRef.value) return;

    const confirmed = await confirmRef.value.showConfirm({
        title: '丢弃更改',
        content: '您当前页面所做的更改尚未保存，确定要离开吗？所有未保存的数据将会丢失。',
        confirmText: '确认离开',
        cancelText: '继续编辑'
    });

    if (confirmed) {
        router.back();
    }
};

</script>

<style scoped>
.player-create-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start; /* Align form to the top */
  /* padding-top: 40px; Remove padding from the scrolling container */
  overflow-y: auto; 
  box-sizing: border-box; /* Ensure padding/border are included in height */
  /* Ensure global scrollbar is hidden. This might need to be set in App.vue or a global CSS file:
     html, body {
       overflow: hidden;
       height: 100%;
     }
  */
}

/* Apply scrollbar styles inspired by PlayerLobby.vue */
.player-create-container::-webkit-scrollbar {
  width: 8px;
}
.player-create-container::-webkit-scrollbar-thumb {
  background: rgba(200, 200, 200, 0.18); /* Match PlayerLobby */
  border-radius: 4px; /* Match PlayerLobby, but you wanted square corners? Let's keep it square */
  border-radius: 0;
}
.player-create-container::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.04); /* Match PlayerLobby */
}

.form-panel {
  width: 100%;
  max-width: 800px; /* Limit max width */
  margin-top: 20px;
  /* border: 2px #00bdaf dotted; Remove previous border */
  padding: 25px;
  margin-bottom: 25px;

  /* SVG Border - Long dashes */
/*  border: 2px solid transparent; !* Define the area and thickness *!
  border-image-source: url("data:image/svg+xml,%3Csvg width='40' height='2' viewBox='0 0 40 2' xmlns='http://www.w3.org/2000/svg'%3E%3Cline x1='0' y1='1' x2='20' y2='1' stroke='%236ba5c8' stroke-width='2'/%3E%3C/svg%3E");
  border-image-slice: 1; !* Slice matches stroke-width *!
  border-image-width: 1px; !* Optional, often matches border-width *!
  border-image-repeat: round; !* Repeat the pattern smoothly *!*/
}

/* Remove scrollbar styling from form-panel as the outer container should handle scrolling */
/*
.form-panel::-webkit-scrollbar {
  width: 6px;
}

.form-panel::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
  border-radius: 0; 
}

.form-panel::-webkit-scrollbar-track {
  background: transparent; 
}
*/

.form-title {
  margin: 0;
  padding: 0;
  font-family: 'Chakra Petch', sans-serif;
  text-align: center;
  color: v-bind('theme.boxGlowColor');
  font-size: 24px;
  font-weight: 600;
  overflow-y: auto; /* Allow scrolling if content overflows */
  /* Ensure global scrollbar is hidden. This might need to be set in App.vue or a global CSS file:
     html, body {
       overflow: hidden;
       height: 100%;
     }
  */
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 20px; /* Spacing between form rows/sections */
}

.form-row {
  display: flex;
  gap: 15px;
}

/* Make inputs in the same row take equal width */
.form-row > * {
  flex: 1;
}

/* Style for the wrapper */
.input-with-error {
    display: flex;
    flex-direction: column;
    flex: 1; /* Allow wrapper to take space in form-row */
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
  margin-bottom: 10px; /* Add some space below avatar section */
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 20px;
  background-color: rgba(255, 255, 255, 0.05); /* Slightly lighter background for avatar area */
  padding: 15px;
  border-radius: 0; /* Remove border-radius */
  border: 1px solid v-bind('theme.boxBorderColor');
}

.avatar-preview {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0; /* Remove border-radius */
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: v-bind('theme.boxSecondColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0; /* Remove border-radius */
}

.avatar-placeholder i {
  font-size: 40px;
  color: v-bind('theme.boxTextColorNoActive');
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.upload-btn {
  min-width: 100px;
}

.upload-tip {
  font-size: 12px;
  color: v-bind('theme.boxTextColorNoActive');
  margin: 0;
}

.action-buttons {
  display: flex;
  justify-content: center; /* Center the create button */
  margin-top: 30px;
  gap: 15px; /* Add gap between buttons */
}

.create-button {
  min-width: 150px;
  background-color: v-bind('theme.mainColor');
  border-color: v-bind('theme.mainBorderColor');
  color: v-bind('theme.mainTextColor');
  font-weight: bold;
}

.create-button:hover:not(:disabled) {
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
      padding: 0 20px 35px 20px;
  }
}

/* Add import for the error message style if not global */
.glow-input-error-message {
    font-family: 'Chakra Petch', sans-serif;
    font-size: 11px;
    color: v-bind('theme.dangerBorderColorActive'); /* Use theme color */
    padding-left: 2px;
    margin-top: 2px;
    min-height: 14px; /* Reserve space */
}

</style>