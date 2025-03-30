<template>
  <div>
    <div class="section-header">
      <h2>背景设定</h2>
      <p class="section-description">自定义应用的背景图像和效果</p>
    </div>
    
    <!-- 当前背景面板 -->
    <div class="settings-panel">
      <h3 class="panel-title">当前背景</h3>
      <div class="current-wallpaper-container">
        <div class="current-wallpaper-preview" :style="{ backgroundImage: currentWallpaperPreview }">
          <div class="wallpaper-actions">
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
      </div>
    </div>
    
    <!-- 默认背景面板 -->
    <div class="settings-panel">
      <h3 class="panel-title">默认背景</h3>
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
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '../stores/theme'
import axios from 'axios'

// 获取主题颜色
const themeStore = useThemeStore()
const primaryColor = computed(() => themeStore.primaryColor)
const primaryHover = computed(() => themeStore.primaryHover)
const activeColor = computed(() => themeStore.activeColor)
const primaryButton = computed(() => themeStore.primaryButton)
const primaryButtonBorder = computed(() => themeStore.primaryButtonBorder)

// 声明props以接收confirmModal
const props = defineProps({
  confirmModal: {
    type: Object,
    required: true
  }
});

// 背景相关状态
const isLoadingWallpapers = ref(false);
const defaultWallpapers = ref<Array<{path: string, name: string}>>([]);
const currentWallpaper = ref('');
const fileInput = ref<HTMLInputElement | null>(null);
const isUploading = ref(false);
const uploadError = ref('');

// 当前壁纸的预览，带有fallback
const currentWallpaperPreview = computed(() => {
  if (currentWallpaper.value) {
    return `url(${currentWallpaper.value})`;
  }
  return 'url(/img/bg1.jpg)';
});

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
  try {
    // 检查当前用户是否有设置过壁纸
    const checkResponse = await axios.get('/customize/getWallpaper', {
      params: { check: true }
    });
    
    if (checkResponse.status === 200) {
      // 用户有自定义壁纸，把当前壁纸设置为对应的API地址
      currentWallpaper.value = '/customize/getWallpaper?t=' + new Date().getTime();
    } else if (defaultWallpapers.value.length > 0) {
      // 用户没有自定义壁纸，使用第一个默认壁纸
      currentWallpaper.value = defaultWallpapers.value[0].path;
    }
  } catch (error) {
    // 发生错误，可能是没有设置壁纸，使用默认值
    if (defaultWallpapers.value.length > 0) {
      currentWallpaper.value = defaultWallpapers.value[0].path;
    }
  }
};

// 选择背景
const selectWallpaper = async (path: string) => {
  try {
    // 使用确认模态框询问用户
    const confirmed = await props.confirmModal.showConfirm({
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
      currentWallpaper.value = '/customize/getWallpaper?t=' + new Date().getTime();
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
    // 检查当前是否是自定义壁纸
    const response = await axios.get('/customize/getWallpaper', {
      params: { check: true },
      validateStatus: () => true  // 允许所有状态码
    });
    
    if (response.status !== 200) {
      console.warn('当前使用的是默认壁纸，无需下载');
      return;
    }
    
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
        currentWallpaper.value = '/customize/getWallpaper?t=' + new Date().getTime();
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
    const confirmed = await props.confirmModal.showConfirm({
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
.section-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
}

.section-header::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 80px;
  height: 2px;
  background-color: v-bind(activeColor);
}

.section-header h2 {
  font-size: 20px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-description {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin: 0;
}

.settings-panel {
  background-color: rgba(30, 40, 60, 0.3);
  border-radius: 0;
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 20px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.panel-title {
  font-size: 16px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
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
  background-color: v-bind(activeColor);
}

/* 当前壁纸容器 */
.current-wallpaper-container {
  display: flex;
  gap: 20px;
  align-items: stretch;
  flex-wrap: wrap;
}

.current-wallpaper-preview {
  width: 280px;
  height: 180px;
  border-radius: 0;
  background-size: cover;
  background-position: center;
  position: relative;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  flex-shrink: 0;
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
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.8);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.05);
}

.reset-btn:hover {
  color: rgba(255, 200, 100, 1);
}

.download-btn:hover {
  color: rgba(100, 255, 200, 1);
}

.upload-container {
  width: 280px;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(40, 50, 70, 0.3);
  border-radius: 0;
  border: 1px dashed rgba(255, 255, 255, 0.2);
  padding: 0;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.upload-container:hover {
  background: rgba(40, 50, 70, 0.4);
  border-color: rgba(255, 255, 255, 0.4);
}

.upload-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 0;
  background: v-bind(primaryButton);
  border: 1px solid v-bind(primaryButtonBorder);
  color: white;
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
  border-bottom: 2px solid v-bind(activeColor);
  border-right: 2px solid v-bind(activeColor);
}

.upload-button:hover {
  background: rgba(100, 140, 180, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.upload-button i {
  font-size: 22px;
  margin-bottom: 3px;
}

.upload-tip {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  text-align: center;
  margin: 0;
  padding: 0 10px;
}

.file-input {
  display: none;
}

/* 背景列表样式 */
.wallpaper-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
  width: 100%;
}

.wallpaper-item {
  border-radius: 0;
  overflow: hidden;
  background: rgba(20, 30, 40, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
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
  border-color: v-bind(activeColor);
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
  color: white;
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
  color: rgba(255, 255, 255, 0.9);
}

/* 加载和空状态 */
.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 0;
  color: rgba(255, 255, 255, 0.6);
  gap: 10px;
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
  color: rgba(255, 255, 255, 0.5);
  gap: 10px;
}

.empty-wallpaper i {
  font-size: 36px;
  opacity: 0.7;
}

.wallpaper-item:hover {
  background: rgba(40, 50, 70, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.2);
}

.wallpaper-item.active {
  background: rgba(40, 60, 80, 0.6);
  border-color: v-bind(activeColor);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .wallpaper-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 12px;
  }
  
  .current-wallpaper-container {
    flex-direction: column;
    gap: 15px;
  }
  
  .current-wallpaper-preview,
  .upload-container {
    width: 100%;
    height: 180px;
    max-width: none;
    box-sizing: border-box;
  }
  
  .settings-panel {
    padding: 15px;
  }
}
</style> 