<template>
  <div>
    <div class="section-header">
      <h2>主题颜色</h2>
      <p class="section-description">调整应用的主题和界面颜色</p>
    </div>

    <!-- 基础颜色设置 -->
    <div class="settings-panel">
      <h3 class="panel-title">基础颜色</h3>
      <div class="color-grid">
        <div class="color-item">
          <div class="color-label">品牌颜色</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.brandColor }"></div>
            <input type="text" v-model="themeStore.brandColor" @change="handleColorChange('brandColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">主题颜色</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.primaryColor }"></div>
            <input type="text" v-model="themeStore.primaryColor" @change="handleColorChange('primaryColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">激活颜色</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.activeColor }"></div>
            <input type="text" v-model="themeStore.activeColor" @change="handleColorChange('activeColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">悬浮颜色</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.primaryHover }"></div>
            <input type="text" v-model="themeStore.primaryHover" @change="handleColorChange('primaryHover', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
      </div>
    </div>

    <!-- 按钮颜色设置 -->
    <div class="settings-panel">
      <h3 class="panel-title">按钮样式</h3>
      <div class="color-grid">
        <div class="color-item">
          <div class="color-label">按钮背景</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.primaryButton }"></div>
            <input type="text" v-model="themeStore.primaryButton" @change="handleColorChange('primaryButton', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">按钮边框</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.primaryButtonBorder }"></div>
            <input type="text" v-model="themeStore.primaryButtonBorder" @change="handleColorChange('primaryButtonBorder', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
      </div>
    </div>

    <!-- 输入框颜色设置 -->
    <div class="settings-panel">
      <h3 class="panel-title">输入框样式</h3>
      <div class="color-grid">
        <div class="color-item">
          <div class="color-label">输入框背景</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.textareaColor }"></div>
            <input type="text" v-model="themeStore.textareaColor" @change="handleColorChange('textareaColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">输入框激活</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.textareaActive }"></div>
            <input type="text" v-model="themeStore.textareaActive" @change="handleColorChange('textareaActive', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">输入框边框</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.textareaBorder }"></div>
            <input type="text" v-model="themeStore.textareaBorder" @change="handleColorChange('textareaBorder', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
      </div>
    </div>

    <!-- 模态框颜色设置 -->
    <div class="settings-panel">
      <h3 class="panel-title">模态框样式</h3>
      <div class="color-grid">
        <div class="color-item">
          <div class="color-label">模态框背景</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.modalColor }"></div>
            <input type="text" v-model="themeStore.modalColor" @change="handleColorChange('modalColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">模态框激活</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.modalActive }"></div>
            <input type="text" v-model="themeStore.modalActive" @change="handleColorChange('modalActive', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
      </div>
    </div>

    <!-- 菜单颜色设置 -->
    <div class="settings-panel">
      <h3 class="panel-title">菜单样式</h3>
      <div class="color-grid">
        <div class="color-item">
          <div class="color-label">菜单背景</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.menuColor }"></div>
            <input type="text" v-model="themeStore.menuColor" @change="handleColorChange('menuColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">菜单激活</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.menuActiveColor }"></div>
            <input type="text" v-model="themeStore.menuActiveColor" @change="handleColorChange('menuActiveColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
      </div>
    </div>

    <!-- 选择器颜色设置 -->
    <div class="settings-panel">
      <h3 class="panel-title">选择器样式</h3>
      <div class="color-grid">
        <div class="color-item">
          <div class="color-label">选择器背景</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.selectorColor }"></div>
            <input type="text" v-model="themeStore.selectorColor" @change="handleColorChange('selectorColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">选择器激活</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.selectorActiveColor }"></div>
            <input type="text" v-model="themeStore.selectorActiveColor" @change="handleColorChange('selectorActiveColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">选择器边框</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.selectorBorderColor }"></div>
            <input type="text" v-model="themeStore.selectorBorderColor" @change="handleColorChange('selectorBorderColor', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
      </div>
    </div>

    <!-- 消息气泡颜色设置 -->
    <div class="settings-panel">
      <h3 class="panel-title">消息气泡样式</h3>
      <div class="color-grid">
        <div class="color-item">
          <div class="color-label">用户消息悬浮</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.messageHoverUser }"></div>
            <input type="text" v-model="themeStore.messageHoverUser" @change="handleColorChange('messageHoverUser', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
        <div class="color-item">
          <div class="color-label">模型消息悬浮</div>
          <div class="color-picker">
            <div class="color-preview" :style="{ background: themeStore.messageHoverModel }"></div>
            <input type="text" v-model="themeStore.messageHoverModel" @change="handleColorChange('messageHoverModel', ($event.target as HTMLInputElement).value)" />
          </div>
        </div>
      </div>
    </div>

    <!-- 模糊效果设置 -->
    <div class="settings-panel">
      <h3 class="panel-title">模糊效果</h3>
      <div class="blur-grid">
        <div class="blur-item">
          <div class="blur-label">导航栏模糊</div>
          <div class="blur-slider">
            <input 
              type="range" 
              min="0" 
              max="20" 
              step="1"
              :value="parseInt(themeStore.navBlur)"
              @input="handleBlurChange('navBlur', ($event.target as HTMLInputElement).value + 'px')"
            />
            <span class="blur-value">{{ themeStore.navBlur }}</span>
          </div>
        </div>
        <div class="blur-item">
          <div class="blur-label">主要内容模糊</div>
          <div class="blur-slider">
            <input 
              type="range" 
              min="0" 
              max="20" 
              step="1"
              :value="parseInt(themeStore.mainBlur)"
              @input="handleBlurChange('mainBlur', ($event.target as HTMLInputElement).value + 'px')"
            />
            <span class="blur-value">{{ themeStore.mainBlur }}</span>
          </div>
        </div>
        <div class="blur-item">
          <div class="blur-label">侧边栏模糊</div>
          <div class="blur-slider">
            <input 
              type="range" 
              min="0" 
              max="20" 
              step="1"
              :value="parseInt(themeStore.sideBlur)"
              @input="handleBlurChange('sideBlur', ($event.target as HTMLInputElement).value + 'px')"
            />
            <span class="blur-value">{{ themeStore.sideBlur }}</span>
          </div>
        </div>
        <div class="blur-item">
          <div class="blur-label">模态框模糊</div>
          <div class="blur-slider">
            <input 
              type="range" 
              min="0" 
              max="20" 
              step="1"
              :value="parseInt(themeStore.modalBlur)"
              @input="handleBlurChange('modalBlur', ($event.target as HTMLInputElement).value + 'px')"
            />
            <span class="blur-value">{{ themeStore.modalBlur }}</span>
          </div>
        </div>
        <div class="blur-item">
          <div class="blur-label">菜单模糊</div>
          <div class="blur-slider">
            <input 
              type="range" 
              min="0" 
              max="20" 
              step="1"
              :value="parseInt(themeStore.menuBlur)"
              @input="handleBlurChange('menuBlur', ($event.target as HTMLInputElement).value + 'px')"
            />
            <span class="blur-value">{{ themeStore.menuBlur }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useThemeStore } from '../stores/theme'

const themeStore = useThemeStore()

// 处理颜色变更
const handleColorChange = (key: string, value: string) => {
  const setter = `set${key.charAt(0).toUpperCase() + key.slice(1)}` as keyof typeof themeStore
  if (typeof themeStore[setter] === 'function') {
    (themeStore[setter] as Function)(value)
  }
}

// 处理模糊度变更
const handleBlurChange = (key: string, value: string) => {
  const setter = `set${key.charAt(0).toUpperCase() + key.slice(1)}` as keyof typeof themeStore
  if (typeof themeStore[setter] === 'function') {
    (themeStore[setter] as Function)(value)
  }
}
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
  background-color: v-bind('themeStore.activeColor');
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
  background-color: v-bind('themeStore.activeColor');
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.color-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.color-picker {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(0, 0, 0, 0.2);
  padding: 8px;
  border-radius: 0;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.color-preview {
  width: 32px;
  height: 32px;
  border-radius: 0;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.color-picker input {
  flex: 1;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
  padding: 6px 10px;
  font-size: 14px;
  border-radius: 0;
}

.color-picker input:focus {
  outline: none;
  border-color: v-bind('themeStore.activeColor');
}

.blur-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.blur-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.blur-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.blur-slider {
  display: flex;
  align-items: center;
  gap: 10px;
}

.blur-slider input[type="range"] {
  flex: 1;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  outline: none;
  -webkit-appearance: none;
}

.blur-slider input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  background: v-bind('themeStore.activeColor');
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
}

.blur-slider input[type="range"]::-webkit-slider-thumb:hover {
  transform: scale(1.1);
}

.blur-value {
  min-width: 45px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

@media (max-width: 768px) {
  .color-grid,
  .blur-grid {
    grid-template-columns: 1fr;
  }
  
  .settings-panel {
    padding: 15px;
  }
}
</style> 