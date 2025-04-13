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
      <!-- 右侧内容 - 预留空白 -->
      <div class="role-detail-container">
        <div v-if="!selectedRoleId" class="empty-detail">
          <i class="bi bi-person-bounding-box"></i>
          <p>请从左侧选择一个角色进行设计</p>
        </div>
        <div v-else class="role-detail">
          <!-- 角色详情内容将在这里实现 -->
          <h2>角色详情面板</h2>
          <p>角色ID: {{ selectedRoleId }}</p>
        </div>
      </div>
    </GlowDiv>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme';
import { inject } from 'vue';
import http from '@/commons/Http';
import type GetModelRoleListVo from '@/entity/GetModelRoleListVo';
import type PageableView from '@/entity/PageableView';
import ModelRoleManagerList from '@/components/glow-client/ModelRoleManagerList.vue';

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme);

// 角色列表引用
const roleListRef = ref<any>(null);

// 角色列表数据
const roleList = ref<GetModelRoleListVo[]>([]);
const loading = ref(true);
const selectedRoleId = ref<string>("");

// 生命周期钩子
onMounted(async () => {
  await loadRoleList();
});

// 加载角色列表
const loadRoleList = async () => {
  try {
    loading.value = true;
    const data = await http.postEntity<PageableView<GetModelRoleListVo>>('/model/rp/getRoleList', {});
    roleList.value = data.rows || [];
    loading.value = false;
  } catch (error) {
    console.error('加载角色列表失败:', error);
    roleList.value = [];
    loading.value = false;
  }
};

// 选择角色
const onSelectRole = (roleId: string) => {
  selectedRoleId.value = roleId;
  console.log(`选择了角色: ${roleId}`);
};

// 复制角色 - 空实现
const onCopyRole = (roleId: string) => {
  console.log(`复制角色: ${roleId}`);
  // 这里添加复制角色的实现逻辑
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
  padding: 20px;
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
  padding: 20px;
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
}
</style>