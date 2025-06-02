<template>
  <div class="model-variant-param-manager-container">
    <!-- 查询表单 -->
    <div class="query-form">
      <el-form :model="query" inline>
        <el-form-item label="关键字">
          <el-input 
            v-model="query.keyword" 
            placeholder="模型代码/名称/系列" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadModelList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 模型变体列表 -->
    <div class="model-variant-table">
      <el-table
        :data="modelList"
        stripe
        border
        v-loading="loading"
      >
        <el-table-column 
          prop="code" 
          label="模型代码" 
          min-width="120"
          show-overflow-tooltip
          resizable
        />
        <el-table-column 
          prop="name" 
          label="模型名称" 
          min-width="120"
          show-overflow-tooltip
          resizable
        />
        <el-table-column 
          prop="series" 
          label="模型系列" 
          min-width="120"
          show-overflow-tooltip
          resizable
        />
        <el-table-column 
          prop="paramCount" 
          label="参数数量" 
          width="100"
          align="center"
          resizable
        >
          <template #default="scope">
            <el-tag 
              :type="scope.row.paramCount > 0 ? 'success' : 'info'" 
              size="small"
            >
              {{ scope.row.paramCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          prop="enabled" 
          label="启用状态" 
          width="100"
          align="center"
          resizable
        >
          <template #default="scope">
            <el-tag 
              :type="scope.row.enabled === 1 ? 'success' : 'info'" 
              size="small"
            >
              {{ scope.row.enabled === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          prop="createTime" 
          label="创建时间" 
          width="160"
          show-overflow-tooltip
          resizable
        />
        <el-table-column label="操作" fixed="right" width="120" resizable>
          <template #default="scope">
            <el-button 
              link
              type="primary" 
              size="small" 
              @click="openManageParamsModal(scope.row)"
              :icon="Setting"
            >
              管理参数
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadModelList"
        @current-change="loadModelList"
      />
    </div>

    <!-- 参数管理模态框 -->
    <ModelVariantParamsModal
      v-if="paramsModalVisible" 
      :visible="paramsModalVisible"
      :model-variant-id="currentManagedModelVariantId"
      :model-variant-name="currentManagedModelVariantName"
      @update:visible="paramsModalVisible = $event"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'
import AdminModelVariantApi from '@/commons/api/AdminModelVariantApi'
import ModelVariantParamsModal from './ModelVariantParamsModal.vue' // 引入模态框组件
import type { GetAdminModelVariantListDto, GetAdminModelVariantListVo } from '@/commons/api/AdminModelVariantApi'

// 数据定义
const loading = ref(false)
const modelList = ref<GetAdminModelVariantListVo[]>([]) // 用于存储模型变体列表
const total = ref(0)

// 模态框相关状态
const paramsModalVisible = ref(false)
const currentManagedModelVariantId = ref<string>('')
const currentManagedModelVariantName = ref<string>('')

// 查询条件
const query = reactive<GetAdminModelVariantListDto>({
  page: 1,
  pageSize: 10,
  keyword: null,
  enabled: 1 // 默认只查询启用的模型变体
})

// 加载模型变体列表
const loadModelList = async () => {
  loading.value = true
  try {
    const result = await AdminModelVariantApi.getModelVariantList(query)
    modelList.value = result.rows
    total.value = result.count
  } catch (error: any) {
    console.error('加载模型变体列表失败:', error)
    ElMessage.error(error.message || '加载模型变体列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  query.page = 1
  query.pageSize = 10
  query.keyword = null
  query.enabled = 1 // 默认重置为只查询启用
  loadModelList()
}

// 打开管理参数模态框
const openManageParamsModal = (row: GetAdminModelVariantListVo) => {
  currentManagedModelVariantId.value = row.id
  currentManagedModelVariantName.value = row.name || row.code
  paramsModalVisible.value = true
}

// 初始化
onMounted(() => {
  loadModelList()
})
</script>

<style scoped>
.model-variant-param-manager-container {
  padding: 20px;
}

.query-form {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.model-variant-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 移除旧的样式规则，因为它们不再适用 */
/* .add-button-container, .param-table, .empty-state, .global-value, .user-value, .no-value, .dialog-footer */
/* 与弹窗相关的样式也从这里移除，因为它们现在属于ModelVariantParamsModal.vue */

/* 响应式布局 */
@media (max-width: 768px) {
  .query-form {
    flex-direction: column;
  }
}
</style> 