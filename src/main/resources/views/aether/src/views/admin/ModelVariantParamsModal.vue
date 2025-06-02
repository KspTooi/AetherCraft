<template>
  <el-dialog
    :model-value="visible"
    :title="`管理参数 - ${modelVariantName}`"
    width="90%"
    :close-on-click-modal="false"
    @update:model-value="handleVisibleChange"
    @close="resetForm"
  >
    <div class="model-variant-params-modal-container">
      <!-- 查询表单 -->
      <div class="query-form">
        <el-form :model="query" inline>
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
          >
            新增参数
          </el-button>
        </div>
      </div>

      <!-- 参数列表 -->
      <div class="param-table">
        <el-table
          :data="list"
          stripe
          border
          v-loading="loading"
          max-height="400"
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
            label="缺省值" 
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
            label="用户值" 
            min-width="120"
            show-overflow-tooltip
            resizable
          >
            <template #default="scope">
              <span v-if="scope.row.userVal !== null" class="user-value">
                {{ scope.row.userVal }}
              </span>
              <span v-else class="no-value">使用缺省值</span>
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
          <el-table-column label="操作" fixed="right" min-width="200" resizable align="center">
            <template #default="scope">
              <div style="display: flex; align-items: center; justify-content: center; gap: 8px;">
                <el-button 
                  link
                  type="primary" 
                  size="small" 
                  @click="openUpdateGlobalModal(scope.row)"
                  :icon="EditIcon"
                >
                  编辑缺省值
                </el-button>
                <el-button 
                  link
                  type="warning" 
                  size="small" 
                  @click="openUpdateUserModal(scope.row)"
                  :icon="UserIcon"
                >
                  设置用户值
                </el-button>
                <el-dropdown 
                  v-if="scope.row.globalVal !== null || scope.row.userVal !== null"
                  @command="(command: string) => handleDeleteCommand(command, scope.row)"
                  style="display: flex; align-items: center;"
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
                        删除缺省参数
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="scope.row.userVal !== null" 
                        command="user"
                      >
                        删除用户参数
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
              </div>
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
          @size-change="loadParamList"
          @current-change="loadParamList"
        />
      </div>

      <!-- 新增/编辑参数对话框 -->
      <el-dialog
        v-model="paramDialogVisible"
        :title="paramDialogTitle"
        width="600px"
        @close="resetParamForm"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="formRules"
          label-width="100px"
        >
          <el-form-item label="模型变体" prop="modelVariantId">
            <el-input 
              :value="modelVariantName" 
              disabled
              style="width: 100%"
            />
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
              <el-radio :value="1">缺省参数</el-radio>
              <el-radio :value="0">用户参数</el-radio>
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
            <el-button @click="paramDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitForm" :loading="submitLoading">
              {{ editMode ? '更新' : '保存' }}
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit as EditIcon, Delete as DeleteIcon, User as UserIcon, ArrowDown } from '@element-plus/icons-vue'
import AdminModelVariantParamApi from '@/commons/api/AdminModelVariantParamApi'
import type { GetModelVariantParamListDto, GetModelVariantParamListVo, SaveModelVariantParamDto, GetModelVariantParamDetailsDto, RemoveModelVariantParamDto } from '@/commons/api/AdminModelVariantParamApi'

// Props定义
interface Props {
  visible: boolean
  modelVariantId: string
  modelVariantName?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelVariantName: '未知模型'
})

// Emits定义
const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

// 数据定义
const loading = ref(false)
const submitLoading = ref(false)
const paramDialogVisible = ref(false)
const editMode = ref(false)
const list = ref<GetModelVariantParamListVo[]>([])
const total = ref(0)

// 查询条件
const query = reactive<GetModelVariantParamListDto>({
  page: 1,
  pageSize: 20,
  modelVariantId: props.modelVariantId,
  keyword: null
})

// 表单数据
const form = reactive<SaveModelVariantParamDto>({
  modelVariantId: props.modelVariantId,
  paramKey: '',
  paramVal: '',
  type: 0,
  description: '',
  global: 1,
  seq: undefined
})

// 表单引用
const formRef = ref()

// 表单验证规则
const formRules = {
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
const paramDialogTitle = computed(() => {
  if (editMode.value) {
    return form.global === 1 ? '编辑缺省参数' : '编辑用户参数'
  }
  return '新增参数'
})

// 监听modelVariantId变化
watch(() => props.modelVariantId, (newId) => {
  query.modelVariantId = newId
  form.modelVariantId = newId
  if (props.visible && newId) {
    loadParamList()
  }
})

// 监听visible变化
watch(() => props.visible, (newVisible) => {
  if (newVisible && props.modelVariantId) {
    loadParamList()
  }
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

// 加载参数列表
const loadParamList = async () => {
  if (!props.modelVariantId) {
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
  query.keyword = null
  loadParamList()
}

// 打开新增对话框
const openInsertModal = () => {
  editMode.value = false
  form.modelVariantId = props.modelVariantId
  form.global = 1
  paramDialogVisible.value = true
}

// 打开编辑全局参数对话框
const openUpdateGlobalModal = async (row: GetModelVariantParamListVo) => {
  if (row.globalVal === null) {
    ElMessage.warning('该参数没有缺省值，请先新增')
    return
  }
  
  try {
    editMode.value = true
    const details = await AdminModelVariantParamApi.getModelVariantParamDetails({ 
      modelVariantId: props.modelVariantId,
      paramKey: row.paramKey,
      global: 1
    })
    Object.assign(form, {
      modelVariantId: details.modelVariantId,
      paramKey: details.paramKey,
      paramVal: details.paramVal,
      type: details.type,
      description: details.description,
      global: details.global,
      seq: details.seq
    })
    paramDialogVisible.value = true
  } catch (error: any) {
    console.error('加载参数详情失败:', error)
    ElMessage.error(error.message || '加载参数详情失败')
  }
}

// 打开编辑个人参数对话框
const openUpdateUserModal = (row: GetModelVariantParamListVo) => {
  editMode.value = true
  Object.assign(form, {
    modelVariantId: props.modelVariantId,
    paramKey: row.paramKey,
    paramVal: row.userVal || row.globalVal || '',
    type: row.type,
    description: row.description,
    global: 0,
    seq: row.seq
  })
  paramDialogVisible.value = true
}

// 处理删除命令
const handleDeleteCommand = (command: string, row: GetModelVariantParamListVo) => {
  let message = ''
  if (command === 'global') {
    message = `确定要删除缺省参数 "${row.paramKey}" 吗？`
  } else if (command === 'user') {
    message = `确定要删除用户参数 "${row.paramKey}" 吗？`
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
          modelVariantId: props.modelVariantId,
          paramKey: row.paramKey,
          global: 1
        })
      }
    }
    
    if (type === 'user' || type === 'both') {
      if (row.userVal !== null) {
        await AdminModelVariantParamApi.removeModelVariantParam({ 
          modelVariantId: props.modelVariantId,
          paramKey: row.paramKey,
          global: 0
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
  Object.assign(form, {
    modelVariantId: props.modelVariantId,
    paramKey: '',
    paramVal: '',
    type: 0,
    description: '',
    global: 1,
    seq: undefined
  })
}

// 重置参数表单
const resetParamForm = () => {
  resetForm()
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    await AdminModelVariantParamApi.saveModelVariantParam(form)
    ElMessage.success(editMode.value ? '更新成功' : '保存成功')
    paramDialogVisible.value = false
    loadParamList()
  } catch (error: any) {
    if (error === false) return
    
    const defaultMsg = editMode.value ? '更新失败' : '保存失败'
    console.error(defaultMsg + ':', error)
    ElMessage.error(error.message || defaultMsg)
  } finally {
    submitLoading.value = false
  }
}

// 处理模态框显示变化
const handleVisibleChange = (value: boolean) => {
  emit('update:visible', value)
}

// 处理关闭
const handleClose = () => {
  emit('update:visible', false)
}
</script>

<style scoped>
.model-variant-params-modal-container {
  padding: 0;
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