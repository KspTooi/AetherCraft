<template>
  <div class="model-variant-param-manager-container">
    <!-- 查询表单 -->
    <div class="query-form">
      <el-form :model="query" inline>
        <el-form-item label="模型变体">
          <el-select 
            v-model="query.modelVariantId" 
            placeholder="请选择模型变体" 
            clearable 
            style="width: 200px"
            @change="loadParamList"
          >
            <el-option 
              v-for="variant in modelVariants" 
              :key="variant.id" 
              :label="`${variant.code} (${variant.name})`"
              :value="variant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input 
            v-model="query.keyword" 
            placeholder="参数键/描述" 
            clearable 
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadParamList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="add-button-container">
        <el-button 
          type="success" 
          @click="openInsertModal"
          :disabled="!query.modelVariantId"
        >
          新增参数
        </el-button>
      </div>
    </div>

    <!-- 参数列表 -->
    <div class="param-table">
      <!-- 未选择模型变体时的提示 -->
      <div v-if="!query.modelVariantId" class="empty-state">
        <el-empty description="请先选择模型变体查看参数配置" />
      </div>
      
      <!-- 参数表格 -->
      <el-table
        v-else
        :data="list"
        stripe
        border
        v-loading="loading"
      >
        <el-table-column 
          prop="paramKey" 
          label="参数键" 
          min-width="120"
          show-overflow-tooltip
          resizable
        />
        <el-table-column 
          prop="globalVal" 
          label="全局默认值" 
          min-width="120" 
          show-overflow-tooltip
          resizable
        >
          <template #default="scope">
            <span v-if="scope.row.globalVal !== null" class="global-value">
              {{ scope.row.globalVal }}
            </span>
            <span v-else class="no-value">未设置</span>
          </template>
        </el-table-column>
        <el-table-column 
          prop="userVal" 
          label="我的值" 
          min-width="120"
          show-overflow-tooltip
          resizable
        >
          <template #default="scope">
            <span v-if="scope.row.userVal !== null" class="user-value">
              {{ scope.row.userVal }}
            </span>
            <span v-else class="no-value">使用全局值</span>
          </template>
        </el-table-column>
        <el-table-column 
          prop="type" 
          label="参数类型" 
          width="100"
          align="center"
          resizable
        >
          <template #default="scope">
            <el-tag 
              :type="getTypeTagType(scope.row.type)" 
              size="small"
            >
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          prop="description" 
          label="描述" 
          min-width="150"
          show-overflow-tooltip
          resizable
        >
          <template #default="scope">
            {{ scope.row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column 
          prop="createTime" 
          label="创建时间" 
          width="160"
          show-overflow-tooltip
          resizable
        >
          <template #default="scope">
            {{ scope.row.createTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="200" resizable>
          <template #default="scope">
            <el-button 
              link
              type="primary" 
              size="small" 
              @click="openUpdateGlobalModal(scope.row)"
              :icon="EditIcon"
            >
              编辑全局值
            </el-button>
            <el-button 
              link
              type="warning" 
              size="small" 
              @click="openUpdateUserModal(scope.row)"
              :icon="UserIcon"
            >
              设置我的值
            </el-button>
            <el-dropdown 
              v-if="scope.row.globalVal !== null || scope.row.userVal !== null"
              @command="(command: string) => handleDeleteCommand(command, scope.row)"
            >
              <el-button 
                link
                type="danger" 
                size="small" 
                :icon="DeleteIcon"
              >
                删除<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    v-if="scope.row.globalVal !== null" 
                    command="global"
                  >
                    删除全局参数
                  </el-dropdown-item>
                  <el-dropdown-item 
                    v-if="scope.row.userVal !== null" 
                    command="user"
                  >
                    删除我的参数
                  </el-dropdown-item>
                  <el-dropdown-item 
                    v-if="scope.row.globalVal !== null && scope.row.userVal !== null" 
                    command="both"
                    divided
                  >
                    删除全部参数
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div v-if="query.modelVariantId" class="pagination-container">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadParamList"
        @current-change="loadParamList"
      />
    </div>

    <!-- 新增/编辑参数对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="模型变体" prop="modelVariantId">
          <el-select 
            v-model="form.modelVariantId" 
            placeholder="请选择模型变体" 
            style="width: 100%"
            :disabled="editMode"
          >
            <el-option 
              v-for="variant in modelVariants" 
              :key="variant.id" 
              :label="`${variant.code} (${variant.name})`"
              :value="variant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="参数键" prop="paramKey">
          <el-input 
            v-model="form.paramKey" 
            placeholder="请输入参数键"
            :disabled="editMode"
          />
        </el-form-item>
        <el-form-item label="参数值" prop="paramVal">
          <el-input 
            v-model="form.paramVal" 
            placeholder="请输入参数值"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="参数类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择参数类型" style="width: 100%">
            <el-option label="字符串" :value="0" />
            <el-option label="整数" :value="1" />
            <el-option label="布尔值" :value="2" />
            <el-option label="浮点数" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="参数范围" prop="global">
          <el-radio-group v-model="form.global" :disabled="editMode">
            <el-radio :value="1">全局默认参数</el-radio>
            <el-radio :value="0">我的个人参数</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="form.description" 
            placeholder="请输入参数描述"
            type="textarea"
            :rows="2"
          />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number 
            v-model="form.seq" 
            :min="0" 
            :max="9999"
            placeholder="留空自动设置"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
            {{ editMode ? '更新' : '保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit as EditIcon, Delete as DeleteIcon, User as UserIcon, ArrowDown } from '@element-plus/icons-vue'
import AdminModelVariantParamApi from '@/commons/api/AdminModelVariantParamApi'
import AdminModelVariantApi from '@/commons/api/AdminModelVariantApi'
import type { GetModelVariantParamListDto, GetModelVariantParamListVo, SaveModelVariantParamDto, GetModelVariantParamDetailsDto, RemoveModelVariantParamDto } from '@/commons/api/AdminModelVariantParamApi'
import type { GetAdminModelSeriesListVo } from '@/commons/api/AdminModelVariantApi'

// 数据定义
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const editMode = ref(false)
const list = ref<GetModelVariantParamListVo[]>([])
const total = ref(0)
const modelVariants = ref<GetAdminModelSeriesListVo[]>([])

// 查询条件
const query = reactive<GetModelVariantParamListDto>({
  page: 1,
  pageSize: 20,
  modelVariantId: '',
  keyword: null
})

// 表单数据 - 移除id字段，使用三要素定位
const form = reactive<SaveModelVariantParamDto>({
  modelVariantId: '',
  paramKey: '',
  paramVal: '',
  type: 0,
  description: '',
  global: 1, // 默认为全局参数 1:是
  seq: undefined
})

// 表单引用
const formRef = ref()

// 表单验证规则
const formRules = {
  modelVariantId: [
    { required: true, message: '请选择模型变体', trigger: 'change' }
  ],
  paramKey: [
    { required: true, message: '请输入参数键', trigger: 'blur' },
    { min: 1, max: 128, message: '参数键长度在1-128个字符', trigger: 'blur' }
  ],
  paramVal: [
    { required: true, message: '请输入参数值', trigger: 'blur' },
    { max: 1000, message: '参数值长度不能超过1000个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择参数类型', trigger: 'change' }
  ],
  global: [
    { required: true, message: '请选择参数范围', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  if (editMode.value) {
    return form.global === 1 ? '编辑全局默认参数' : '编辑我的个人参数'
  }
  return '新增参数'
})

// 获取参数类型名称
const getTypeName = (type: number): string => {
  const typeNames = ['字符串', '整数', '布尔值', '浮点数']
  return typeNames[type] || '未知'
}

// 获取参数类型标签样式
const getTypeTagType = (type: number): string => {
  const tagTypes = ['primary', 'success', 'warning', 'danger']
  return tagTypes[type] || 'info'
}

// 加载模型变体列表
const loadModelVariants = async () => {
  try {
    const result = await AdminModelVariantApi.getModelVariantList({
      page: 1,
      pageSize: 1000,
      enabled: 1
    })
    modelVariants.value = result.rows
  } catch (error: any) {
    console.error('加载模型变体列表失败:', error)
    ElMessage.error(error.message || '加载模型变体列表失败')
  }
}

// 加载参数列表
const loadParamList = async () => {
  // 如果没有选择模型变体，则不加载参数列表
  if (!query.modelVariantId) {
    list.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const result = await AdminModelVariantParamApi.getModelVariantParamList(query)
    list.value = result.rows
    total.value = result.count
  } catch (error: any) {
    console.error('加载参数列表失败:', error)
    ElMessage.error(error.message || '加载参数列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  query.page = 1
  query.pageSize = 20
  // 保留模型变体选择，只重置关键字
  query.keyword = null
  // 重置后重新加载参数列表
  loadParamList()
}

// 打开新增对话框
const openInsertModal = () => {
  editMode.value = false
  form.modelVariantId = query.modelVariantId || ''
  form.global = 1 // 默认为全局参数
  dialogVisible.value = true
}

// 打开编辑全局参数对话框
const openUpdateGlobalModal = async (row: GetModelVariantParamListVo) => {
  if (row.globalVal === null) {
    ElMessage.warning('该参数没有全局默认值，请先新增')
    return
  }
  
  try {
    editMode.value = true
    const details = await AdminModelVariantParamApi.getModelVariantParamDetails({ 
      modelVariantId: query.modelVariantId,
      paramKey: row.paramKey,
      global: 1 // 查询全局参数
    })
    // 使用三要素定位，不再使用id
    Object.assign(form, {
      modelVariantId: details.modelVariantId,
      paramKey: details.paramKey,
      paramVal: details.paramVal,
      type: details.type,
      description: details.description,
      global: details.global,
      seq: details.seq
    })
    dialogVisible.value = true
  } catch (error: any) {
    console.error('加载参数详情失败:', error)
    ElMessage.error(error.message || '加载参数详情失败')
  }
}

// 打开编辑个人参数对话框
const openUpdateUserModal = (row: GetModelVariantParamListVo) => {
  editMode.value = true
  // 使用三要素定位，不再使用id
  Object.assign(form, {
    modelVariantId: query.modelVariantId, // 使用当前选择的模型变体ID
    paramKey: row.paramKey,
    paramVal: row.userVal || row.globalVal || '',
    type: row.type,
    description: row.description,
    global: 0, // 个人参数
    seq: row.seq
  })
  dialogVisible.value = true
}

// 处理删除命令
const handleDeleteCommand = (command: string, row: GetModelVariantParamListVo) => {
  let message = ''
  if (command === 'global') {
    message = `确定要删除全局参数 "${row.paramKey}" 吗？`
  } else if (command === 'user') {
    message = `确定要删除我的参数 "${row.paramKey}" 吗？`
  } else if (command === 'both') {
    message = `确定要删除参数 "${row.paramKey}" 的全部配置吗？`
  }
  
  ElMessageBox.confirm(message, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    removeParam(row, command)
  })
}

// 删除参数
const removeParam = async (row: GetModelVariantParamListVo, type: string = 'both') => {
  try {
    if (type === 'global' || type === 'both') {
      if (row.globalVal !== null) {
        await AdminModelVariantParamApi.removeModelVariantParam({ 
          modelVariantId: query.modelVariantId,
          paramKey: row.paramKey,
          global: 1 // 删除全局参数
        })
      }
    }
    
    if (type === 'user' || type === 'both') {
      if (row.userVal !== null) {
        await AdminModelVariantParamApi.removeModelVariantParam({ 
          modelVariantId: query.modelVariantId,
          paramKey: row.paramKey,
          global: 0 // 删除用户参数
        })
      }
    }
    
    ElMessage.success('删除成功')
    loadParamList()
  } catch (error: any) {
    console.error('删除失败:', error)
    ElMessage.error(error.message || '删除失败')
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  // 移除id字段
  Object.assign(form, {
    modelVariantId: '',
    paramKey: '',
    paramVal: '',
    type: 0,
    description: '',
    global: 1, // 默认为全局参数
    seq: undefined
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    await AdminModelVariantParamApi.saveModelVariantParam(form)
    ElMessage.success(editMode.value ? '更新成功' : '保存成功')
    dialogVisible.value = false
    loadParamList()
  } catch (error: any) {
    if (error === false) return // 表单验证失败
    
    // 对于提交表单的错误，使用更具体的默认消息
    const defaultMsg = editMode.value ? '更新失败' : '保存失败'
    console.error(defaultMsg + ':', error)
    ElMessage.error(error.message || defaultMsg)
  } finally {
    submitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadModelVariants()
  // 不在初始化时加载参数列表，等用户选择模型变体后再加载
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

.add-button-container {
  display: flex;
  gap: 10px;
}

.param-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
  background: #fff;
  border-radius: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.global-value {
  color: #409eff;
  font-weight: 500;
}

.user-value {
  color: #67c23a;
  font-weight: 500;
}

.no-value {
  color: #909399;
  font-style: italic;
}

.dialog-footer {
  text-align: right;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .query-form {
    flex-direction: column;
  }
  
  .add-button-container {
    margin-top: 15px;
    width: 100%;
  }
}
</style> 