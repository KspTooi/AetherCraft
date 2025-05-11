<template>
  <div class="session-manager-container">
    <!-- 查询表单 -->
    <div class="query-form">
      <el-form :model="query" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="query.userName"
            placeholder="输入用户名查询"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="玩家名">
          <el-input
            v-model="query.playerName"
            placeholder="输入玩家名查询"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadSessionList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 会话列表 -->
    <div class="session-table">
      <el-table
        :data="list"
        stripe
        v-loading="loading"
      >
        <el-table-column
          prop="id"
          label="会话ID"
          min-width="100"
        />
        <el-table-column
          prop="username"
          label="用户名"
          min-width="150"
        />
        <el-table-column
          prop="playerName"
          label="当前登录角色"
          min-width="150"
        />
        <el-table-column
          prop="createTime"
          label="登入时间"
          min-width="180"
        />
        <el-table-column
          prop="expiresAt"
          label="过期时间"
          min-width="180"
        />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button
              link
              type="primary"
              size="small"
              @click="openViewModal(scope.row)"
              :icon="ViewIcon"
            >
              详情
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleCloseSession(scope.row)"
              :icon="CloseIcon"
            >
              关闭会话
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="loadSessionList"
          @current-change="loadSessionList"
          background
        />
      </div>
    </div>

    <!-- 会话详情模态框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="会话详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border v-if="currentSessionDetails">
        <el-descriptions-item label="会话ID">{{ currentSessionDetails.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentSessionDetails.username }}</el-descriptions-item>
        <el-descriptions-item label="当前登录角色">{{ currentSessionDetails.playerName }}</el-descriptions-item>
        <el-descriptions-item label="登入时间">{{ currentSessionDetails.createTime }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">{{ currentSessionDetails.expiresAt }}</el-descriptions-item>
        <el-descriptions-item label="权限节点">
          <div v-if="currentSessionDetails.permissions && currentSessionDetails.permissions.length > 0">
            <el-input 
              v-model="permissionSearchKeyword" 
              placeholder="搜索权限节点" 
              clearable 
              style="margin-bottom: 10px; width: 100%;"
            />
            <el-table 
              :data="filteredPermissions" 
              stripe 
              max-height="300px"
              style="width: 100%;"
            >
              <el-table-column label="权限代码" min-width="500">
                <template #default="scope">
                  {{ scope.row }}
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 10px; text-align: right;">
              总权限数：{{ currentSessionDetails.permissions.length }}
            </div>
          </div>
          <el-tag v-else type="info">无权限</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {reactive, ref, onMounted, markRaw, computed} from "vue";
import type {GetSessionListDto, GetSessionListVo, GetSessionDetailsVo} from "@/commons/api/AdminSessionApi.ts";
import AdminSessionApi from "@/commons/api/AdminSessionApi.ts";
import { ElMessage, ElMessageBox } from 'element-plus';
import { View, CloseBold } from '@element-plus/icons-vue';

const ViewIcon = markRaw(View);
const CloseIcon = markRaw(CloseBold);

const query = reactive<GetSessionListDto>({
  userName: null,
  playerName: null,
  page: 1,
  pageSize: 10
});

const list = ref<GetSessionListVo[]>([]);
const total = ref(0);
const loading = ref(false);

const detailsDialogVisible = ref(false);
const currentSessionDetails = ref<GetSessionDetailsVo | null>(null);
const permissionSearchKeyword = ref('');
const filteredPermissions = computed(() => {
  if (!currentSessionDetails.value?.permissions) return [];
  
  return currentSessionDetails.value.permissions.filter(permission => 
    permission.toLowerCase().includes(permissionSearchKeyword.value.toLowerCase())
  );
});

const loadSessionList = async () => {
  loading.value = true;
  try {
    const res = await AdminSessionApi.getSessionList(query);
    list.value = res.rows;
    total.value = res.count;
  } catch (e) {
    ElMessage.error('加载会话列表失败');
    console.error("加载会话列表失败", e);
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  query.userName = null;
  query.playerName = null;
  query.page = 1;
  loadSessionList();
};

const openViewModal = async (row: GetSessionListVo) => {
  loading.value = true;
  try {
    const res = await AdminSessionApi.getSessionDetails({ id: row.id });
    currentSessionDetails.value = res;
    permissionSearchKeyword.value = ''; // 重置搜索关键词
    detailsDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取会话详情失败');
    console.error('获取会话详情失败', error);
  } finally {
    loading.value = false;
  }
};

const handleCloseSession = async (row: GetSessionListVo) => {
  try {
    await ElMessageBox.confirm(
      `确定要关闭用户 ${row.username} (角色: ${row.playerName}) 的会话吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    await AdminSessionApi.closeSession({ id: row.id });
    ElMessage.success('会话关闭成功');
    loadSessionList(); // Refresh the list
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error instanceof Error ? error.message : '关闭会话失败';
      ElMessage.error(errorMsg);
    }
  }
};

onMounted(() => {
  loadSessionList();
});
</script>

<style scoped>
.session-manager-container {
  padding: 20px;
  max-width: 100%;
  overflow-x: auto;
}

.query-form {
  margin-bottom: 20px;
}

.session-table {
  margin-bottom: 20px;
  width: 100%;
  overflow-x: auto; /* Ensures table is scrollable on smaller screens */
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  width: 100%;
}

.el-descriptions {
  margin-top: 0; /* Reset margin if any default from el-dialog content */
}

.el-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

/* Ensure tooltip content is readable if it becomes too long */
:deep(.el-tooltip__popper) {
  max-width: 400px; /* Adjust as needed */
  word-break: break-all;
}
</style>