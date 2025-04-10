<template>
  <div class="customize-wallpaper">
    <GlowTab
      :items="tabItems"
      v-model:activeTab="currentTab"
      @tab-change="handleTabChange"
    >
      <div class="tab-content">
        <!-- 当前壁纸面板 -->
        <div v-show="currentTab === 'current'" class="tab-panel">
          <div class="current-wallpaper-container">
            <div class="current-wallpaper-preview" :style="currentWallpaperStyle">
              <div v-if="isCurrentWallpaperLoading" class="wallpaper-loading">
                <div class="dot-loading">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </div>
              </div>
              <div class="wallpaper-actions" v-if="!isCurrentWallpaperLoading">
                <button class="action-btn download-btn" @click="downloadWallpaper" title="下载当前背景">
                  <i class="bi bi-download"></i>
                </button>
                <button class="action-btn reset-btn" @click="resetWallpaper" title="恢复默认背景">
                  <i class="bi bi-arrow-counterclockwise"></i>
                </button>
              </div>
            </div>
            <div class="upload-container">
              <div class="upload-button" @click="triggerFileUpload">
                <i class="bi bi-upload"></i>
                <span>上传新背景</span>
              </div>
              <input 
                type="file" 
                ref="fileInput" 
                class="file-input" 
                accept="image/jpeg, image/png, image/gif" 
                @change="handleFileChange" 
              />
              <p class="upload-tip">支持JPG、PNG格式，建议分辨率不低于1920×1080</p>
            </div>
            
            <!-- 添加缓存提示 -->
            <div class="cache-notice">
              <i class="bi bi-info-circle"></i>
              <div class="notice-content">
                <p class="notice-title">背景更新可能会有数分钟的缓存延迟</p>
                <p class="notice-solution">
                  您可以：
                  <span class="solution-item">• 按<kbd>Ctrl</kbd>+<kbd>F5</kbd>强制刷新页面</span>
                  <span class="solution-item">• 清除浏览器缓存后刷新页面</span>
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 预设壁纸面板 -->
        <div v-show="currentTab === 'preset'" class="tab-panel">
          <div v-if="isLoadingWallpapers" class="loading-wrapper">
            <i class="bi bi-arrow-repeat spinning"></i>
            <span>加载背景中...</span>
          </div>
          <div v-else-if="defaultWallpapers.length === 0" class="empty-wallpaper">
            <i class="bi bi-image"></i>
            <span>暂无可用背景</span>
          </div>
          <div v-else class="wallpaper-grid">
            <div 
              v-for="(wallpaper, index) in defaultWallpapers" 
              :key="index"
              class="wallpaper-item"
              :class="{ active: currentWallpaper === wallpaper.path }"
              @click="selectWallpaper(wallpaper.path)"
            >
              <div class="wallpaper-preview" :style="{ backgroundImage: `url(${wallpaper.path})` }">
                <div class="wallpaper-overlay">
                  <i class="bi bi-check-circle-fill" v-if="currentWallpaper === wallpaper.path"></i>
                </div>
              </div>
              <div class="wallpaper-name">{{ wallpaper.name }}</div>
            </div>
          </div>
        </div>
      </div>
    </GlowTab>

    <!-- 确认对话框组件 -->
    <GlowConfirm ref="confirmRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import { GLOW_THEME_INJECTION_KEY, defaultTheme } from '../../components/glow-ui/GlowTheme.ts'
import type { GlowThemeColors } from '../../components/glow-ui/GlowTheme.ts'
import GlowConfirm from '../../components/glow-ui/GlowConfirm.vue'
import GlowTab from '../../components/glow-ui/GlowTab.vue'
import axios from 'axios'

// 注入主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// Tab相关状态
const currentTab = ref('current')
const tabItems = [
  { title: '当前壁纸', action: 'current' },
  { title: '预设壁纸', action: 'preset' }
]

const handleTabChange = (action: string) => {
  currentTab.value = action
}

// 确认对话框引用
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null)

// 背景相关状态
const isLoadingWallpapers = ref(false);
const defaultWallpapers = ref<Array<{path: string, name: string}>>([]);
const currentWallpaper = ref('');
const fileInput = ref<HTMLInputElement | null>(null);
const isUploading = ref(false);
const uploadError = ref('');
const isCurrentWallpaperLoading = ref(true);

// 当前壁纸的样式,包含加载状态
const currentWallpaperStyle = computed(() => {
  if (!isCurrentWallpaperLoading.value && currentWallpaper.value) {
    return { backgroundImage: `url(${currentWallpaper.value})` };
  } 
  return {};
});

// 预加载壁纸图像
const preloadWallpaper = (url: string) => {
  isCurrentWallpaperLoading.value = true;
  
  const img = new Image();
  img.onload = () => {
    isCurrentWallpaperLoading.value = false;
  };
  
  img.onerror = () => {
    console.error('壁纸图像加载失败:', url);
    isCurrentWallpaperLoading.value = false;
  };
  
  img.src = url;
};

// 获取默认背景列表
const fetchDefaultWallpapers = async () => {
  isLoadingWallpapers.value = true;
  try {
    const response = await axios.get('/customize/getDefaultWallpaper');
    if (response.data.code === 0) {
      defaultWallpapers.value = response.data.data || [];

      // 如果有默认背景并且当前未选择背景，设置第一个为当前背景
      if (defaultWallpapers.value.length > 0 && !currentWallpaper.value) {
        checkCurrentWallpaper();
      }
    }
  } catch (error) {
    console.error('获取默认背景失败:', error);
  } finally {
    isLoadingWallpapers.value = false;
  }
};

// 检查当前设置的壁纸
const checkCurrentWallpaper = async () => {
  isCurrentWallpaperLoading.value = true;
  try {
    // 直接获取壁纸，不进行额外检查
    const wallpaperUrl = '/customize/getWallpaper?t=' + new Date().getTime();
    currentWallpaper.value = wallpaperUrl;
    preloadWallpaper(wallpaperUrl);
  } catch (error) {
    console.error('获取壁纸失败:', error);
    // 发生错误，使用默认壁纸（如果有）
    if (defaultWallpapers.value.length > 0) {
      currentWallpaper.value = defaultWallpapers.value[0].path;
      preloadWallpaper(defaultWallpapers.value[0].path);
    } else {
      isCurrentWallpaperLoading.value = false;
    }
  }
};

// 选择背景
const selectWallpaper = async (path: string) => {
  try {
    // 使用确认模态框询问用户
    if (!confirmRef.value) return;
    
    const confirmed = await confirmRef.value.showConfirm({
      title: '设置背景',
      content: '确定要将该图片设置为当前背景吗？',
      confirmText: '确定',
      cancelText: '取消'
    });
    
    if (!confirmed) return;
    
    // 对于预设背景，需要先将图片转换为base64格式
    let imageData = path;
    
    // 如果是预设背景（路径以/img/开头）
    if (path.startsWith('/img/')) {
      // 获取图片的base64数据
      try {
        const response = await fetch(path);
        const blob = await response.blob();
        const reader = new FileReader();
        
        // 使用Promise包装FileReader
        imageData = await new Promise((resolve) => {
          reader.onloadend = () => resolve(reader.result as string);
          reader.readAsDataURL(blob);
        });
      } catch (error) {
        console.error('图片转base64失败:', error);
        return;
      }
    }
    
    // 调用后端API保存壁纸设置
    const response = await axios.post('/customize/setWallpaper', { 
      imageData: imageData
    });
    
    if (response.data.code === 0) {
      // 成功后重新获取当前壁纸
      const wallpaperUrl = '/customize/getWallpaper?t=' + new Date().getTime();
      currentWallpaper.value = wallpaperUrl;
      preloadWallpaper(wallpaperUrl);
    } else {
      console.error('设置壁纸失败:', response.data.message);
    }
  } catch (error) {
    console.error('设置壁纸出错:', error);
  }
};

// 下载当前壁纸
const downloadWallpaper = async () => {
  try {
    // 创建下载链接并触发点击
    const link = document.createElement('a');
    link.href = '/customize/getWallpaper?t=' + new Date().getTime();
    link.download = 'wallpaper.jpg';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error('下载壁纸失败:', error);
  }
};

// 触发文件上传
const triggerFileUpload = () => {
  if (fileInput.value) {
    fileInput.value.click();
  }
};

// 处理文件变更事件
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target?.files?.[0];
  
  if (!file) return;
  
  // 验证文件类型
  if (!file.type.match('image/jpeg') && !file.type.match('image/png')) {
    uploadError.value = '只支持JPG和PNG格式的图片';
    return;
  }
  
  // 验证文件大小（限制为5MB）
  if (file.size > 5 * 1024 * 1024) {
    uploadError.value = '图片大小不能超过5MB';
    return;
  }
  
  isUploading.value = true;
  uploadError.value = '';
  
  try {
    // 读取文件为base64
    const reader = new FileReader();
    reader.readAsDataURL(file);
    
    reader.onload = async (e) => {
      const imageData = e.target?.result as string;
      
      // 调用壁纸上传接口
      const response = await axios.post('/customize/setWallpaper', {
        imageData
      });
      
      if (response.data.code === 0) {
        // 上传成功，更新当前壁纸
        const wallpaperUrl = '/customize/getWallpaper?t=' + new Date().getTime();
        currentWallpaper.value = wallpaperUrl;
        preloadWallpaper(wallpaperUrl);
      } else {
        uploadError.value = response.data.message || '上传失败';
      }
      
      isUploading.value = false;
    };
    
    reader.onerror = () => {
      uploadError.value = '读取文件失败';
      isUploading.value = false;
    };
  } catch (error) {
    console.error('上传壁纸出错:', error);
    uploadError.value = '上传过程中发生错误';
    isUploading.value = false;
  } finally {
    // 重置文件输入，以便可以重复上传相同文件
    if (fileInput.value) {
      fileInput.value.value = '';
    }
  }
};

// 重置壁纸为系统默认
const resetWallpaper = async () => {
  try {
    // 使用确认模态框询问用户
    if (!confirmRef.value) return;
    
    const confirmed = await confirmRef.value.showConfirm({
      title: '重置背景',
      content: '确定要恢复系统默认背景吗？',
      confirmText: '确定',
      cancelText: '取消'
    });
    
    if (!confirmed) return;
    
    const response = await axios.post('/customize/resetWallpaper');
    
    if (response.data.code === 0) {
      // 重置成功后，重新检查当前壁纸状态
      checkCurrentWallpaper();
    } else {
      console.error('重置壁纸失败:', response.data.message);
    }
  } catch (error) {
    console.error('重置壁纸出错:', error);
  }
};

// 组件挂载时加载数据
onMounted(() => {
  fetchDefaultWallpapers();
  checkCurrentWallpaper();
});
</script>

<style scoped>
.customize-wallpaper {
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.tab-content {
  height: calc(100vh - 95px);
  overflow: hidden;
}

/* Tab面板样式 */
.tab-panel {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  max-height: calc(100vh - 135px);
}

.tab-panel::-webkit-scrollbar {
  width: 8px;
}

.tab-panel::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
}

.tab-panel::-webkit-scrollbar-track {
  background: v-bind('theme.boxSecondColor');
}

/* 当前壁纸容器 */
.current-wallpaper-container {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 24px;
  align-items: flex-start;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.current-wallpaper-preview {
  width: 280px;
  height: 180px;
  border-radius: 0;
  background-size: cover;
  background-position: center;
  position: relative;
  border: 1px solid v-bind('theme.boxBorderColor');
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  flex-shrink: 0;
}

.upload-container {
  width: 280px;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: v-bind('theme.boxSecondColor');
  border-radius: 0;
  border: 1px dashed v-bind('theme.boxBorderColor');
  padding: 20px;
  transition: all 0.3s ease;
  box-sizing: border-box;
  flex-shrink: 0;
}

.upload-container:hover {
  background: v-bind('theme.boxSecondColorHover');
  border-color: v-bind('theme.boxBorderColorHover');
}

/* 缓存提示样式 */
.cache-notice {
  flex: 1 1 100%;
  min-width: 0;
  max-width: 100%;
  box-sizing: border-box;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  margin-top: 8px;
  background: v-bind('theme.boxSecondColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0;
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 13px;
  line-height: 1.5;
}

.cache-notice i {
  color: v-bind('theme.boxGlowColor');
  font-size: 16px;
  margin-top: 2px;
  flex-shrink: 0;
}

.notice-content {
  flex: 1;
  min-width: 0;
  overflow-wrap: break-word;
  word-wrap: break-word;
  hyphens: auto;
}

.notice-title {
  color: v-bind('theme.boxTextColor');
  margin: 0 0 4px 0;
  font-weight: 500;
}

.notice-solution {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.solution-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.cache-notice kbd {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 1px 5px;
  height: 18px;
  font-size: 12px;
  font-family: var(--font-mono, monospace);
  color: v-bind('theme.boxTextColor');
  background-color: v-bind('theme.boxColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0;
  margin: 0 2px;
}

/* 壁纸加载样式 */
.wallpaper-loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
}

.dot-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  background-color: v-bind('theme.boxGlowColor');
  border-radius: 50%;
  display: inline-block;
  opacity: 0.6;
}

.dot:nth-child(1) {
  animation: dot-flashing 1s infinite alternate;
  animation-delay: 0s;
}

.dot:nth-child(2) {
  animation: dot-flashing 1s infinite alternate;
  animation-delay: 0.3s;
}

.dot:nth-child(3) {
  animation: dot-flashing 1s infinite alternate;
  animation-delay: 0.6s;
}

@keyframes dot-flashing {
  0% {
    opacity: 0.2;
    transform: scale(1);
  }
  100% {
    opacity: 1;
    transform: scale(1.2);
  }
}

/* 预设壁纸网格 */
.wallpaper-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  width: 100%;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .tab-panel {
    padding: 16px;
  }
  
  .current-wallpaper-container {
    flex-direction: column;
    gap: 16px;
  }
  
  .current-wallpaper-preview,
  .upload-container {
    width: 100%;
    height: auto;
    min-height: 180px;
  }
  
  .wallpaper-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 16px;
  }
  
  .cache-notice {
    padding: 12px;
    font-size: 12px;
    margin-top: 0;
    width: 100%;
    min-width: 0;
  }
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid v-bind('theme.boxBorderColor');
  position: relative;
}

.section-header::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 80px;
  height: 2px;
  background-color: v-bind('theme.boxGlowColor');
}

.section-header h2 {
  font-size: 20px;
  font-weight: 500;
  color: v-bind('theme.boxTextColor');
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-description {
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 14px;
  margin: 0;
}

.settings-panel {
  background-color: v-bind('theme.boxColor');
  border-radius: 0;
  border: 1px solid v-bind('theme.boxBorderColor');
  padding: 20px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.panel-title {
  font-size: 16px;
  font-weight: 500;
  color: v-bind('theme.boxTextColor');
  margin-top: 0;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
}

.panel-title::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 60px;
  height: 1px;
  background-color: v-bind('theme.boxGlowColor');
}

.wallpaper-actions {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.current-wallpaper-preview:hover .wallpaper-actions {
  opacity: 1;
}

.action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid v-bind('theme.boxBorderColor');
  color: v-bind('theme.boxTextColor');
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.8);
  border-color: v-bind('theme.boxBorderColorHover');
  transform: scale(1.05);
}

.reset-btn:hover {
  color: v-bind('theme.dangerColorActive');
}

.download-btn:hover {
  color: v-bind('theme.boxGlowColor');
}

.upload-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 0;
  background: v-bind('theme.mainColor');
  border: 1px solid v-bind('theme.mainBorderColor');
  color: v-bind('theme.mainTextColor');
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 10px;
  position: relative;
  overflow: hidden;
}

.upload-button::before {
  content: '';
  position: absolute;
  width: 12px;
  height: 12px;
  bottom: 0;
  right: 0;
  border-bottom: 2px solid v-bind('theme.boxGlowColor');
  border-right: 2px solid v-bind('theme.boxGlowColor');
}

.upload-button:hover {
  background: v-bind('theme.mainColorHover');
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.upload-button i {
  font-size: 22px;
  margin-bottom: 3px;
}

.upload-tip {
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 12px;
  text-align: center;
  margin: 0;
  padding: 0 10px;
}

.file-input {
  display: none;
}

.wallpaper-item {
  border-radius: 0;
  overflow: hidden;
  background: v-bind('theme.boxSecondColor');
  border: 1px solid v-bind('theme.boxBorderColor');
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.wallpaper-item::before,
.wallpaper-item::after {
  content: '';
  position: absolute;
  width: 10px;
  height: 10px;
}

.wallpaper-item::before {
  top: 0;
  left: 0;
  border-top: 2px solid transparent;
  border-left: 2px solid transparent;
  transition: all 0.3s ease;
}

.wallpaper-item::after {
  bottom: 0;
  right: 0;
  border-bottom: 2px solid transparent;
  border-right: 2px solid transparent;
  transition: all 0.3s ease;
}

.wallpaper-item:hover::before,
.wallpaper-item:hover::after,
.wallpaper-item.active::before,
.wallpaper-item.active::after {
  border-color: v-bind('theme.boxGlowColor');
}

.wallpaper-preview {
  height: 100px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
}

.wallpaper-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

.wallpaper-item:hover .wallpaper-overlay {
  background: rgba(0, 0, 0, 0.3);
}

.wallpaper-item.active .wallpaper-overlay {
  background: rgba(0, 0, 0, 0.4);
}

.wallpaper-overlay i {
  font-size: 24px;
  color: v-bind('theme.boxTextColor');
  text-shadow: 0 0 8px rgba(0, 0, 0, 0.5);
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s ease;
}

.wallpaper-item.active .wallpaper-overlay i {
  opacity: 1;
  transform: scale(1);
}

.wallpaper-name {
  padding: 8px 10px;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
  color: v-bind('theme.boxTextColor');
}

.loading-wrapper i {
  font-size: 24px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-wallpaper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 0;
  color: v-bind('theme.boxTextColorNoActive');
  gap: 10px;
}

.empty-wallpaper i {
  font-size: 36px;
  opacity: 0.7;
}

.wallpaper-item:hover {
  background: v-bind('theme.boxSecondColorHover');
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  border-color: v-bind('theme.boxBorderColorHover');
}

.wallpaper-item.active {
  background: v-bind('theme.boxColorActive');
  border-color: v-bind('theme.boxGlowColor');
}
</style> 