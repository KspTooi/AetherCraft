<template>
  <div style="margin: 2px">
    <GlowTab
        :items="themeTabItem"
        @tab-change="onThemeTabChange"
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
          
        </div>
      </div>
    </GlowTab>
    
    <!-- 确认对话框组件 -->
    <GlowConfirm ref="confirmRef" />
  </div>
</template>

<script setup lang="ts">
import {inject, ref, onMounted} from "vue";
import {defaultTheme, GLOW_THEME_INJECTION_KEY, type GlowThemeColors} from "@/components/glow-ui/GlowTheme.ts";
import GlowTab from "@/components/glow-ui/GlowTab.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import type {GetUserThemeListVo} from "@/entity/costomize/GetUserThemeListVo.ts";
import type PageableView from "@/entity/PageableView.ts";
import axios from "axios";
import type Result from "@/entity/Result.ts";

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
  { title: '主题设计器', action: 'theme-designer' }
]

// 当前激活的标签
const themeCurrentTab = ref('my-themes')

// 处理标签切换
const onThemeTabChange = (action: string) => {
  themeCurrentTab.value = action
}

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

// 处理编辑主题
const onEditTheme = (theme: GetUserThemeListVo) => {
  // TODO: 实现编辑主题逻辑
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
}

.theme-panel::-webkit-scrollbar {
  width: 4px;
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

@media (min-height: 768px) {
  .theme-list {
    grid-template-rows: repeat(auto-fill, 180px);
  }
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
  height: 180px;
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
  gap: 4px;
  margin-top: 12px;
  width: 100%;
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
</style>