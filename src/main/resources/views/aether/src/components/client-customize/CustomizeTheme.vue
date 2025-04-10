<template>
  <div style="margin: 2px">
    <GlowTab
        :items="themeTabItem"
        v-model:activeTab="themeCurrentTab"
    >
      <div class="tab-content">
        <!-- 我的主题 -->
        <div v-if="themeCurrentTab === 'my-themes'" class="theme-panel">
          <div class="theme-list">
            <!-- 创建新主题卡片 -->
            <div class="theme-item create-theme-card" @click="onCreateTheme">
              <div class="create-theme-content">
                <div class="create-icon">+</div>
                <div class="create-text">创建新主题</div>
              </div>
            </div>
            <div v-for="item in themeList.rows" :key="item.id" class="theme-item" :class="{'theme-item-active': item.isActive === 1}">
              <div class="theme-item-header">
                <div class="theme-name">{{ item.themeName }}</div>
                <div class="theme-tags">
                  <span v-if="item.isSystem === 1" class="theme-tag system-tag">系统</span>
                  <span v-if="item.isActive === 1" class="theme-tag active-tag">使用中</span>
                </div>
              </div>
              <div class="theme-description">{{ item.description || '暂无描述' }}</div>
              <div class="theme-footer">
                <div class="theme-actions">
                  <GlowButton 
                    v-if="item.isActive !== 1" 
                    @click="onActiveTheme(item)"
                  >激活</GlowButton>
                  <GlowButton 
                    v-if="!item.isSystem"
                    @click="onEditTheme(item)"
                  >设计</GlowButton>
                  <GlowButton 
                    v-if="!item.isSystem && item.isActive !== 1" 
                    theme="danger"
                    :corners="['bottom-right']"
                    @click="onRemoveTheme(item)"
                  >移除</GlowButton>  
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 主题设计器 -->
        <div v-if="themeCurrentTab === 'theme-designer'" class="theme-panel">
          <!-- 没有选择主题时的提示 -->
          <div v-if="!curThemeId" class="empty-state">
            <div class="empty-icon">🎨</div>
            <div class="empty-title">请先选择一个主题进行设计</div>
            <div class="empty-desc">您可以从"我的主题"标签中选择一个主题进行编辑，或者创建一个新的主题</div>
            <GlowButton @click="themeCurrentTab = 'my-themes'">返回我的主题</GlowButton>
          </div>
          
          <!-- 已选择主题时显示设计器 -->
          <template v-else>
            <div class="designer-header">
              <div class="theme-title-container">
                <h3 class="designer-title" v-if="!isEditingTitle" @click="startEditTitle">
                  编辑主题: {{ currentThemeName }} <span class="edit-icon">✏️</span>
                </h3>
                <div v-else class="title-edit-container">
                  <input 
                    ref="titleInputRef"
                    v-model="editingThemeName" 
                    class="title-input" 
                    @blur="saveThemeTitle" 
                    @keyup.enter="saveThemeTitle"
                    @keyup.esc="cancelEditTitle"
                  />
                </div>
              </div>
              <div class="btn-group">
                <GlowButton @click="" class="theme-opt-btn">恢复默认</GlowButton>
                <GlowButton @click="onSaveTheme(false)" class="theme-opt-btn">应用</GlowButton>
                <GlowButton @click="onSaveTheme" class="theme-opt-btn" :corners="[`bottom-right`]">保存</GlowButton>
              </div>
            </div>
            
            <div class="designer-content">
              <div class="color-section">
                <h4>基础颜色</h4>
                <div class="color-row-grid">
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">盒子颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxColor = color"
                      />
                    </div>
                    <div class="color-row-item">
                      <div class="color-row-label">盒子悬停颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxColorHover || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxColorHover = color"
                      />
                    </div>
                  </div>
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">盒子激活颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxColorActive || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxColorActive = color"
                      />
                    </div>
                    <div class="color-row-item">
                      <div class="color-row-label">强调颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxAccentColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxAccentColor = color"
                      />
                    </div>
                  </div>
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">边框颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxBorderColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxBorderColor = color"
                      />
                    </div>
                    <div class="color-row-item">
                      <div class="color-row-label">边框悬停颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxBorderColorHover || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxBorderColorHover = color"
                      />
                    </div>
                  </div>
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">辉光颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxGlowColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxGlowColor = color"
                      />
                    </div>
                    <div class="color-row-item">
                      <div class="color-row-label">次级颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxSecondColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxSecondColor = color"
                      />
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="color-section">
                <h4>文字颜色</h4>
                <div class="color-row-grid">
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">主文字颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxTextColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxTextColor = color"
                      />
                    </div>
                    <div class="color-row-item">
                      <div class="color-row-label">次文字颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.boxTextColorNoActive || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.boxTextColorNoActive = color"
                      />
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="color-section">
                <h4>主元素颜色</h4>
                <div class="color-row-grid">
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">主元素颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.mainColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.mainColor = color"
                      />
                    </div>
                    <div class="color-row-item">
                      <div class="color-row-label">主元素文字颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.mainTextColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.mainTextColor = color"
                      />
                    </div>
                  </div>
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">主元素边框颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.mainBorderColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.mainBorderColor = color"
                      />
                    </div>
                    <div class="color-row-item">
                      <div class="color-row-label">主元素悬停颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.mainColorHover || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.mainColorHover = color"
                      />
                    </div>
                  </div>
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">主元素激活颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.mainColorActive || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.mainColorActive = color"
                      />
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="color-section">
                <h4>危险元素颜色</h4>
                <div class="color-row-grid">
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">危险颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.dangerColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.dangerColor = color"
                      />
                    </div>
                    <div class="color-row-item">
                      <div class="color-row-label">危险文字颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.dangerTextColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.dangerTextColor = color"
                      />
                    </div>
                  </div>
                  <div class="color-row-group">
                    <div class="color-row-item">
                      <div class="color-row-label">危险边框颜色</div>
                      <GlowColorPicker 
                        :color="curThemeValues.dangerBorderColor || '#ffffff'" 
                        @on-color-selected="color => curThemeValues.dangerBorderColor = color"
                      />
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="color-section">
                <h4>透明度设置</h4>
                <div class="slider-grid">
                  <div class="slider-item">
                    <div class="slider-label">普通模糊: {{ curThemeValues.boxBlur }}</div>
                    <input type="range" v-model="curThemeValues.boxBlur" min="0" max="20" step="1" />
                  </div>
                  <div class="slider-item">
                    <div class="slider-label">悬停模糊: {{ curThemeValues.boxBlurHover }}</div>
                    <input type="range" v-model="curThemeValues.boxBlurHover" min="0" max="20" step="1" />
                  </div>
                  <div class="slider-item">
                    <div class="slider-label">激活模糊: {{ curThemeValues.boxBlurActive }}</div>
                    <input type="range" v-model="curThemeValues.boxBlurActive" min="0" max="20" step="1" />
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </GlowTab>
    
    <!-- 确认对话框组件 -->
    <GlowConfirm ref="confirmRef" />
  </div>
</template>

<script setup lang="ts">
import {inject, ref, onMounted, reactive, computed, nextTick} from "vue";
import {defaultTheme, GLOW_THEME_INJECTION_KEY, type GlowThemeColors} from "@/components/glow-ui/GlowTheme.ts";
import GlowTab from "@/components/glow-ui/GlowTab.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import GlowColorPicker from "@/components/glow-ui/GlowColorPicker.vue";
import type {GetUserThemeListVo} from "@/entity/vo/GetUserThemeListVo.ts";
import type PageableView from "@/entity/PageableView.ts";
import axios from "axios";
import type Result from "@/entity/Result.ts";
import type ThemeValuesDto from "@/entity/dto/ThemeValuesDto.ts";
import type SaveThemeDto from "@/entity/dto/SaveThemeDto.ts";

//当前正在设计的主题
const curThemeValues = reactive<ThemeValuesDto>(defaultTheme);
const curThemeId = ref<string>();
const currentThemeName = ref<string>('');

// 标题编辑状态
const isEditingTitle = ref(false);
const editingThemeName = ref('');
const titleInputRef = ref<HTMLInputElement | null>(null);

// 开始编辑标题
const startEditTitle = () => {
  editingThemeName.value = currentThemeName.value;
  isEditingTitle.value = true;
  
  // 在下一个渲染周期后，聚焦到输入框
  nextTick(() => {
    titleInputRef.value?.focus();
  });
};

// 保存标题
const saveThemeTitle = () => {
  if (editingThemeName.value.trim()) {
    currentThemeName.value = editingThemeName.value.trim();
  }
  isEditingTitle.value = false;
};

// 取消编辑标题
const cancelEditTitle = () => {
  isEditingTitle.value = false;
};

const themeList = ref<PageableView<GetUserThemeListVo>>({
  rows: [],
  count: 0,
  currentPage: 1,
  pageSize: 10
});

const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 定义标签项
const themeTabItem = [
  { title: '我的主题', action: 'my-themes' },
  { title: '主题设计器', action: 'theme-designer'},
]

// 当前激活的标签
const themeCurrentTab = ref('my-themes')

// 加载主题列表
const reloadThemeList = async () => {
  try {
    const response = await axios.post<PageableView<GetUserThemeListVo>>('/customize/theme/getThemeList');
    themeList.value = response.data;
  } catch (error) {
    console.error('加载主题列表失败:', error);
  }
}

// 处理使用主题
const onActiveTheme = async (theme: GetUserThemeListVo) => {
  try {
    await axios.post<Result<string>>('/customize/theme/activeTheme', {
      themeId: theme.id
    });
    await reloadThemeList();
  } catch (error) {
    console.error('激活主题失败:', error);
  }
}

const onCreateTheme = async () => {
  try {
    const response = await axios.post<Result<string>>('/customize/theme/saveTheme', {});
    if (response.data.code === 0) {
      await reloadThemeList();
    }
  } catch (error) {
    console.error('创建新主题失败:', error);
  }
}

// 处理设计主题
const onEditTheme = async (theme: GetUserThemeListVo) => {
  try {
    const response = await axios.post<Result<any>>('/customize/theme/getThemeValues', {
      themeId: theme.id
    });
    
    if (response.data.code === 0) {
      // 如果有值就使用，否则使用默认值
      if (response.data.data && response.data.data.themeValues) {
        // 将返回的主题值赋给当前编辑的主题
        Object.assign(curThemeValues, response.data.data.themeValues);
      } else {
        // 无值使用默认
        Object.assign(curThemeValues, defaultTheme);
      }
      
      // 设置当前编辑的主题ID
      curThemeId.value = theme.id;
      currentThemeName.value = theme.themeName;
      
      // 切换到主题设计器标签
      themeCurrentTab.value = 'theme-designer';
    }
  } catch (error) {
    console.error('获取主题值失败:', error);
  }
}

// 保存主题
const onSaveTheme = async (leave:boolean = false) => {
  if (!curThemeId.value) {
    console.error('没有选中的主题');
    return;
  }
  
  try {

    const body:SaveThemeDto = {
      themeId: curThemeId.value,
      themeValues: curThemeValues,
      themeName: currentThemeName.value
    }

    const response = await axios.post<Result<string>>('/customize/theme/saveTheme', body);
    
    if (response.data.code === 0) {
      // 保存成功后返回主题列表
      if(leave){
        themeCurrentTab.value = 'my-themes';
        await reloadThemeList();
      }
    }
  } catch (error) {
    console.error('保存主题失败:', error);
  }
}

// GlowConfirm 引用
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null);

// 处理移除主题
const onRemoveTheme = async (theme: GetUserThemeListVo) => {
  if (!confirmRef.value) {
    return;
  }
  
  const confirmed = await confirmRef.value.showConfirm({
    title: '移除主题',
    content: `确定要移除主题"${theme.themeName}"吗？此操作不可恢复。`,
    confirmText: '移除',
    cancelText: '取消'
  });
  
  if (confirmed) {
    try {
      const response = await axios.post<Result<string>>('/customize/theme/removeTheme', {
        themeId: theme.id
      });
      
      if (response.data.code === 0) {
        await reloadThemeList();
      }
    } catch (error) {
      console.error('移除主题失败:', error);
    }
  }
}

// 组件挂载时加载数据
onMounted(() => {
  reloadThemeList();
})

</script>

<style scoped>
.tab-content {
  height: calc(100vh - 95px);
  overflow: hidden;
}

.theme-panel {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  max-height: calc(100vh - 135px);
}

.theme-panel::-webkit-scrollbar {
  width: 8px;
}

.theme-panel::-webkit-scrollbar-thumb {
  background: v-bind('theme.boxBorderColor');
}

.theme-panel::-webkit-scrollbar-track {
  background: v-bind('theme.boxSecondColor');
}

.theme-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 100%;
  padding-bottom: 50px;
  align-content: start;
}

.theme-item {
  background: linear-gradient(135deg,
    rgba(255, 182, 193, 0.25),
    rgba(135, 206, 235, 0.25),
    rgba(144, 238, 144, 0.25),
    rgba(255, 191, 135, 0.25)
  );
  background-size: 400% 400%;
  padding: 16px;
  transition: all 0.3s ease;
  border: 1px solid v-bind('theme.boxBorderColor');
  box-shadow: 0 0 0 0 v-bind('theme.boxGlowColor');
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.theme-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  transform: skewX(-15deg);
}

.theme-item-active {
  border: 1px solid v-bind('theme.mainBorderColor');
  background: v-bind('theme.boxColorActive');
}

.theme-item-active::before {
  display: none;
}

@keyframes gradientAnimation {
  0% { background-position: 0% 50% }
  50% { background-position: 100% 50% }
  100% { background-position: 0% 50% }
}

.theme-item:hover {
  box-shadow: 0 0 0 1px v-bind('theme.boxGlowColor'),
              0 0 1px 0 v-bind('theme.boxGlowColor');
  border-color: v-bind('theme.boxBorderColorHover');
}

.theme-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.theme-name {
  font-size: 16px;
  font-weight: 600;
  color: v-bind('theme.boxTextColor');
}

.theme-tags {
  display: flex;
  gap: 8px;
}

.theme-tag {
  padding: 2px 8px;
  font-size: 12px;
}

.system-tag {
  background: v-bind('theme.dangerColor');
  color: v-bind('theme.dangerTextColor');
}

.active-tag {
  background: v-bind('theme.mainColor');
  color: v-bind('theme.mainTextColor');
}

.theme-description {
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 14px;
  margin-bottom: 16px;
  min-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.theme-footer {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  font-size: 12px;
  color: v-bind('theme.boxTextColorNoActive');
  margin-top: auto;
}

.theme-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.theme-actions :deep(.laser-button) {
  padding: 4px 8px;
  min-height: 24px;
  font-size: 12px;
}

.theme-actions :deep(.button-content) {
  gap: 4px;
}

.create-theme-card {
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  background: v-bind('theme.boxSecondColor');
  border: 1px dashed v-bind('theme.boxBorderColor');
  transition: all 0.3s ease;
}

.create-theme-card:hover {
  border: 1px dashed v-bind('theme.mainColor');
}

.create-theme-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.create-icon {
  font-size: 36px;
  color: v-bind('theme.boxTextColorNoActive');
  margin-bottom: 8px;
}

.create-text {
  color: v-bind('theme.boxTextColorNoActive');
  font-size: 14px;
}

.create-theme-card:hover .create-icon,
.create-theme-card:hover .create-text {
  color: v-bind('theme.mainColor');
}

/* 主题设计器样式 */
.designer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
}

.theme-title-container {
  display: flex;
  align-items: center;
}

.designer-title {
  color: v-bind('theme.boxTextColor');
  margin: 0;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
}

.designer-title:hover {
  color: v-bind('theme.mainColor');
}

.edit-icon {
  margin-left: 8px;
  font-size: 0.8em;
  opacity: 0.6;
}

.designer-title:hover .edit-icon {
  opacity: 1;
}

.title-edit-container {
  height: 32px;
}

.title-input {
  height: 100%;
  font-size: 1.17em;
  font-weight: 600;
  padding: 0 8px;
  border: 1px solid v-bind('theme.boxBorderColor');
  background: v-bind('theme.boxColor');
  color: v-bind('theme.boxTextColor');
  outline: none;
  min-width: 300px;
}

.title-input:focus {
  border-color: v-bind('theme.mainColor');
  box-shadow: 0 0 0 2px v-bind('theme.boxGlowColor');
}

.designer-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 50px;
}

.color-section {
  border: 1px solid v-bind('theme.boxBorderColor');
  padding: 16px;
}

.color-section h4 {
  margin-top: 0;
  margin-bottom: 12px;
  color: v-bind('theme.boxTextColor');
  font-size: 16px;
}

/* 垂直排列的颜色选择器样式 */
.color-row-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.color-row-group {
  display: flex;
  gap: 20px;
}

.color-row-item {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.color-row-label {
  width: 100px;
  font-size: 14px;
  color: v-bind('theme.boxTextColorNoActive');
  text-align: right;
}

.slider-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 20px;
}

.slider-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.slider-label {
  width: 120px;
  font-size: 14px;
  color: v-bind('theme.boxTextColorNoActive');
  text-align: right;
}

input[type="range"] {
  flex: 1;
  accent-color: v-bind('theme.mainColor');
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 20px;
  height: 100%;
  min-height: 400px;
}

.empty-icon {
  font-size: 64px;
  color: v-bind('theme.boxTextColorNoActive');
  margin-bottom: 24px;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: v-bind('theme.boxTextColor');
  margin-bottom: 12px;
}

.empty-desc {
  font-size: 16px;
  color: v-bind('theme.boxTextColorNoActive');
  margin-bottom: 32px;
  max-width: 600px;
  line-height: 1.5;
}
.theme-opt-btn{
  padding: 0 8px 0 8px;
  min-height: 32px;
  height: 32px;
  font-size: 12px;
}

.btn-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

</style>