<template>
  <div class="default-group-manager-container">

    <!-- 提示信息 -->
    <el-alert 
      title="提示信息"
      type="info"
      description="当用户首次在本系统创建人物时，系统会自动将此处配置的所有访问组授予该新建的人物。"
      show-icon
      :closable="false"
      style="margin-bottom: 20px;" 
    />

    <!-- 操作区域 -->
    <div class="action-bar">
      <el-button type="success" @click="openAddDialog" :icon="PlusIcon">添加默认访问组</el-button>
      <el-button 
        type="danger" 
        @click="removeSelectedDefaultGroups"
        :icon="DeleteIcon"
        :disabled="selectedRows.length === 0"
        style="margin-left: 10px;"
      >
        批量移除
      </el-button>
    </div>

    <!-- 默认分组列表 -->
    <div class="default-group-table">
      <el-table
        :data="list"
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
        ref="tableRef"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column 
          prop="code" 
          label="组标识" 
          min-width="150" 
          show-overflow-tooltip
        />
        <el-table-column 
          prop="name" 
          label="组名称" 
          min-width="150" 
          show-overflow-tooltip
        />
         <el-table-column 
          prop="memberCount" 
          label="成员数" 
          min-width="90" 
        />
         <el-table-column 
          prop="permissionCount" 
          label="权限数" 
          min-width="90" 
        />
        <el-table-column label="系统内置" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isSystem ? 'warning' : 'info'">
              {{ scope.row.isSystem ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          prop="createTime"
          label="添加时间" 
          min-width="180"
        />
        <el-table-column label="操作" fixed="right" min-width="100">
          <template #default="scope">
            <el-button 
              link
              type="danger" 
              size="small" 
              @click="removeDefaultGroup(scope.row)"
              :icon="DeleteIcon"
            >
              移除
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>

    <!-- 添加默认分组模态框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加默认访问组"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form v-if="dialogVisible">
        <el-form-item label="选择分组">
          <el-select
            v-model="selectedGroupIds"
            multiple
            filterable
            placeholder="请选择要添加为默认的分组"
            style="width: 100%"
            :loading="groupLoading"
          >
            <el-option
              v-for="group in allGroups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="addDefaultGroups"
            :loading="submitLoading"
            :disabled="!selectedGroupIds || selectedGroupIds.length === 0"
           >
            确定添加
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, markRaw } from "vue";
import type { GetPlayerDefaultGroupListVo } from "@/commons/api/AdminPlayerDefaultGroupApi";
import type { GetGroupDefinitionsVo } from "@/commons/api/AdminGroupApi";
import PlayerDefaultGroupApi from "@/commons/api/AdminPlayerDefaultGroupApi";
import GroupApi from "@/commons/api/AdminGroupApi";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete, Plus } from '@element-plus/icons-vue';
import type PageQuery from "@/entity/PageQuery";
import type { ElTable } from 'element-plus';

// --- 图标 --- 
const DeleteIcon = markRaw(Delete);
const PlusIcon = markRaw(Plus);

// --- 状态 --- 
const query = reactive<PageQuery>({
  page: 1,
  pageSize: 10
});
const list = ref<GetPlayerDefaultGroupListVo[]>([]);
const total = ref(0);
const loading = ref(false);

const dialogVisible = ref(false);
const groupLoading = ref(false); // 加载分组列表的loading
const submitLoading = ref(false);
const allGroups = ref<GetGroupDefinitionsVo[]>([]); // 所有可选的分组
const selectedGroupIds = ref<string[]>([]); // 选中的分组ID

const tableRef = ref<InstanceType<typeof ElTable>>();
const selectedRows = ref<GetPlayerDefaultGroupListVo[]>([]);

// --- API 调用 --- 
const loadDefaultGroupList = async () => {
  loading.value = true;
  try {
    const res = await PlayerDefaultGroupApi.getPlayerDefaultGroupList(query);
    list.value = res.rows;
    total.value = res.count;
  } catch (e) {
    console.error("加载默认分组列表失败", e);
    ElMessage.error('加载默认分组列表失败');
  } finally {
    loading.value = false;
  }
};

const loadAllGroups = async () => {
  if (allGroups.value.length > 0) return; // 避免重复加载
  groupLoading.value = true;
  try {
    allGroups.value = await GroupApi.getGroupDefinitions();
  } catch (e) {
     console.error("加载分组定义失败", e);
     ElMessage.error('加载可用分组列表失败，请稍后重试');
  } finally {
    groupLoading.value = false;
  }
}

// --- 事件处理 --- 
const handleSelectionChange = (val: GetPlayerDefaultGroupListVo[]) => {
  selectedRows.value = val;
};

const handleSizeChange = (val: number) => {
  query.pageSize = val;
  loadDefaultGroupList();
};

const handleCurrentChange = (val: number) => {
  query.page = val;
  loadDefaultGroupList();
};

// 打开添加模态框
const openAddDialog = () => {
  selectedGroupIds.value = []; // 清空上次选择
  dialogVisible.value = true;
  loadAllGroups(); // 加载分组选项
};

// 添加默认分组
const addDefaultGroups = async () => {
  if (!selectedGroupIds.value || selectedGroupIds.value.length === 0) {
    ElMessage.warning('请至少选择一个分组');
    return;
  }
  
  submitLoading.value = true;
  try {
    await PlayerDefaultGroupApi.addPlayerDefaultGroup({ ids: selectedGroupIds.value });
    ElMessage.success('添加默认分组成功');
    dialogVisible.value = false;
    // 添加成功后，跳转到第一页重新加载，因为新数据在最前面
    query.page = 1;
    loadDefaultGroupList(); 
  } catch (error) {
    const errorMsg = error instanceof Error ? error.message : '添加失败';
    ElMessage.error(errorMsg);
    console.error("添加默认分组失败", error);
  } finally {
    submitLoading.value = false;
  }
};

// 移除默认分组
const removeDefaultGroup = async (row: GetPlayerDefaultGroupListVo) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除默认分组 ${row.name} 吗？`, 
      '警告',
      {
        confirmButtonText: '确定移除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await PlayerDefaultGroupApi.removePlayerDefaultGroup({ ids: [row.id] });
    ElMessage.success('移除默认分组成功');
    // 如果删除的是当前页最后一条数据，可能需要跳转到前一页
    if (list.value.length === 1 && query.page > 1) {
      query.page--;
    }
    loadDefaultGroupList();
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error instanceof Error ? error.message : '移除失败';
      ElMessage.error(errorMsg);
      console.error("移除默认分组失败", error);
    }
  }
};

// 批量移除选中的默认分组
const removeSelectedDefaultGroups = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一项进行移除');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要移除选中的 ${selectedRows.value.length} 个默认分组吗？`, 
      '警告',
      {
        confirmButtonText: '确定移除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    const idsToRemove = selectedRows.value.map(row => row.id);
    await PlayerDefaultGroupApi.removePlayerDefaultGroup({ ids: idsToRemove });
    ElMessage.success('批量移除默认分组成功');
    
    // 清除表格选择状态
    if (tableRef.value) {
       tableRef.value.clearSelection();
    }
    selectedRows.value = []; // 清空选中的行
    
    // 重新加载数据，可能需要调整页码
    // 简单的处理方式是回到第一页，或者根据删除数量和当前页数据量判断是否需要减页
    query.page = 1;
    loadDefaultGroupList();
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error instanceof Error ? error.message : '批量移除失败';
      ElMessage.error(errorMsg);
      console.error("批量移除默认分组失败", error);
    }
  }
};

// --- 生命周期 --- 
onMounted(() => {
  loadDefaultGroupList();
});

</script>

<style scoped>
.default-group-manager-container {
  padding: 20px;
  max-width: 100%;
  overflow-x: auto; 
}

.action-bar {
  margin-bottom: 20px;
}

.default-group-table {
  margin-bottom: 20px;
  width: 100%;
  overflow-x: auto; 
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  width: 100%;
}
</style>