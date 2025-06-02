<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="80%"
    @close="handleClose"
    :close-on-click-modal="false"
  >
    <div class="template-value-manager-container">
      <!-- 查询表单 -->
      <div class="query-form">
        <el-form :model="query" inline>
          <el-form-item label="关键字">
            <el-input 
              v-model="query.keyword" 
              placeholder="参数键或描述" 
              clearable 
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadTemplateValueList">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="add-button-container">
          <el-button 
            type="success" 
            @click="openInsertModal"
            :icon="PlusIcon"
          >
            新增参数值
          </el-button>
        </div>
      </div>

      <!-- 参数值列表 -->
      <div class="template-value-table">
        <el-table
          :data="list"
          stripe
          border
          v-loading="loading"
        >
          <el-table-column 
            prop="paramKey" 
            label="参数键" 
            min-width="150"
            show-overflow-tooltip
            resizable
          />
          <el-table-column 
            prop="paramVal" 
            label="参数值" 
            min-width="200"
            show-overflow-tooltip
            resizable
          />
          <el-table-column 
            label="类型" 
            width="100"
            align="center"
            resizable
          >
            <template #default="scope">
              <el-tooltip 
                :content="getTypeDescription(scope.row.type)" 
                placement="top"
              >
                <el-tag
                  :type="getTypeTagType(scope.row.type)"
                  size="small"
                  effect="light"
                  class="type-tag"
                >
                  {{ getTypeNameText(scope.row.type) }}
                </el-tag>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column 
            prop="description" 
            label="描述" 
            min-width="180"
            show-overflow-tooltip
            resizable
          />
          <el-table-column 
            prop="seq" 
            label="排序号" 
            width="80"
            align="center"
            resizable
          />
          <el-table-column 
            prop="createTime" 
            label="创建时间" 
            width="160"
            show-overflow-tooltip
            resizable
          />
          <el-table-column 
            prop="updateTime" 
            label="更新时间" 
            width="160"
            show-overflow-tooltip
            resizable
          />
          <el-table-column label="操作" fixed="right" min-width="180" resizable>
            <template #default="scope">
              <el-button 
                link
                type="primary" 
                size="small" 
                @click="openUpdateModal(scope.row)"
                :icon="EditIcon"
              >
                编辑
              </el-button>
              <el-button 
                link
                type="danger" 
                size="small" 
                @click="confirmRemove(scope.row)"
                :icon="DeleteIcon"
              >
                删除
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
          @size-change="loadTemplateValueList"
          @current-change="loadTemplateValueList"
        />
      </div>

      <!-- 新增/编辑参数值对话框 -->
      <el-dialog
        v-model="valueDialogVisible"
        :title="dialogTitle"
        width="500px"
        @close="resetValueForm"
        append-to-body
      >
        <el-form
          ref="valueFormRef"
          :model="valueForm"
          :rules="valueFormRules"
          label-width="100px"
        >
          <el-form-item label="所属模板">
            <el-input :value="templateName" disabled />
          </el-form-item>
          <el-form-item label="参数键" prop="paramKey">
            <el-input 
              v-model="valueForm.paramKey" 
              placeholder="请输入参数键"
              maxlength="128"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="参数类型" prop="type">
            <el-select 
              v-model="valueForm.type" 
              placeholder="请选择参数类型"
              style="width: 100%"
            >
              <el-option 
                v-for="(typeName, typeValue) in typeOptions" 
                :key="Number(typeValue)" 
                :label="typeName" 
                :value="Number(typeValue)"
              >
                <el-tag 
                  :type="getTypeTagType(Number(typeValue))" 
                  size="small" 
                  effect="light"
                  class="type-tag"
                >
                  {{ typeName }}
                </el-tag>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="参数值" prop="paramVal">
            <el-input 
              v-model="valueForm.paramVal" 
              placeholder="请输入参数值"
              type="textarea"
              :rows="3"
            />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input 
              v-model="valueForm.description" 
              placeholder="请输入参数描述"
              type="textarea"
              :rows="2"
              maxlength="255"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="排序号" prop="seq">
            <el-input-number 
              v-model="valueForm.seq" 
              :min="0"
              :max="99999"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="valueDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitValueForm" :loading="valueSubmitLoading">
              {{ valueEditMode ? '更新' : '保存' }}
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit as EditIcon, Delete as DeleteIcon, Plus as PlusIcon } from '@element-plus/icons-vue'
import AdminModelVariantParamTemplateValueApi from '@/commons/api/AdminModelVariantParamTemplateValueApi'
import type { 
  GetModelVariantParamTemplateValueListDto, 
  GetModelVariantParamTemplateValueVo, 
  SaveModelVariantParamTemplateValueDto
} from '@/commons/api/AdminModelVariantParamTemplateValueApi'
import type CommonIdDto from '@/entity/dto/CommonIdDto';

// 定义组件props
const props = defineProps<{ 
  visible: boolean, // 控制对话框显示隐藏
  templateId: string, // 所属模板ID
  templateName: string // 所属模板名称
}>()

// 定义组件emits
const emit = defineEmits<{ 
  (e: 'update:visible', value: boolean): void,
  (e: 'template-value-changed'): void // 通知父组件模板值有变动，可能需要更新父组件的paramCount
}>()

// 数据定义
const dialogVisible = computed<boolean>({
  get() { return props.visible },
  set(value) { emit('update:visible', value) }
})
const dialogTitle = computed(() => valueEditMode.value ? '编辑模板参数值' : '新增模板参数值')

const loading = ref(false)
const valueSubmitLoading = ref(false)
const valueDialogVisible = ref(false)
const valueEditMode = ref(false)
const list = ref<GetModelVariantParamTemplateValueVo[]>([])
const total = ref(0)

// 查询条件
const query = reactive<GetModelVariantParamTemplateValueListDto>({
  page: 1,
  pageSize: 20,
  templateId: props.templateId, // 从props获取模板ID
  keyword: null
})

// 表单数据
const valueForm = reactive<SaveModelVariantParamTemplateValueDto>({
  id: undefined,
  templateId: props.templateId, // 从props获取模板ID
  paramKey: '',
  paramVal: '',
  type: 0, // 默认为String
  description: undefined,
  seq: 0
})

// 表单引用
const valueFormRef = ref()

// 表单验证规则
const valueFormRules = {
  paramKey: [
    { required: true, message: '请输入参数键', trigger: 'blur' },
    { min: 1, max: 128, message: '参数键长度在1-128个字符', trigger: 'blur' }
  ],
  paramVal: [
    { required: true, message: '请输入参数值', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择参数类型', trigger: 'change' }
  ]
}

// 类型选项
const typeOptions = {
  0: '字符串',
  1: '整数',
  2: '布尔值',
  3: '浮点'
}

// 监听templateId变化，重新加载列表
watch(() => props.templateId, (newId) => {
  if (newId) {
    query.templateId = newId;
    loadTemplateValueList();
  }
}, { immediate: true }); // immediate: true 立即执行一次

// 加载模板值列表
const loadTemplateValueList = async () => {
  if (!query.templateId) return;
  loading.value = true
  try {
    const result = await AdminModelVariantParamTemplateValueApi.getModelVariantParamTemplateValueList(query)
    list.value = result.rows
    total.value = result.count
  } catch (error: any) {
    console.error('加载模板参数值列表失败:', error)
    ElMessage.error(error.message || '加载模板参数值列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  query.page = 1
  query.pageSize = 20
  query.keyword = null
  loadTemplateValueList()
}

// 打开新增对话框
const openInsertModal = () => {
  resetValueForm() // 确保在打开新增对话框时重置表单
  valueEditMode.value = false
  valueDialogVisible.value = true
}

// 打开编辑对话框
const openUpdateModal = async (row: GetModelVariantParamTemplateValueVo) => {
  try {
    valueEditMode.value = true
    const details = await AdminModelVariantParamTemplateValueApi.getModelVariantParamTemplateValueDetails({ 
      id: row.id
    })
    Object.assign(valueForm, {
      id: details.id,
      templateId: details.templateId,
      paramKey: details.paramKey, // 允许编辑模式下修改参数键
      paramVal: details.paramVal,
      type: details.type,
      description: details.description,
      seq: details.seq
    })
    valueDialogVisible.value = true
  } catch (error: any) {
    console.error('加载模板参数值详情失败:', error)
    ElMessage.error(error.message || '加载模板参数值详情失败')
  }
}

// 确认删除
const confirmRemove = (row: GetModelVariantParamTemplateValueVo) => {
  ElMessageBox.confirm(
    `确定要删除参数键 "${row.paramKey}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    removeTemplateValue(row.id)
  })
}

// 删除模板参数值
const removeTemplateValue = async (id: string) => {
  try {
    await AdminModelVariantParamTemplateValueApi.removeModelVariantParamTemplateValue({ id })
    ElMessage.success('删除成功')
    emit('template-value-changed') // 通知父组件更新paramCount
    loadTemplateValueList()
  } catch (error: any) {
    console.error('删除失败:', error)
    ElMessage.error(error.message || '删除失败')
  }
}

// 重置表单
const resetValueForm = () => {
  if (valueFormRef.value) {
    valueFormRef.value.resetFields()
  }
  Object.assign(valueForm, {
    id: undefined,
    templateId: props.templateId, // 确保重置后templateId依然是当前模板的
    paramKey: '',
    paramVal: '',
    type: 0,
    description: undefined,
    seq: 0
  })
}

// 提交表单
const submitValueForm = async () => {
  if (!valueFormRef.value) return
  
  try {
    await valueFormRef.value.validate()
    valueSubmitLoading.value = true
    
    await AdminModelVariantParamTemplateValueApi.saveModelVariantParamTemplateValue(valueForm)
    ElMessage.success(valueEditMode.value ? '更新成功' : '保存成功')
    valueDialogVisible.value = false
    emit('template-value-changed') // 通知父组件更新paramCount
    loadTemplateValueList()
  } catch (error: any) {
    if (error === false) return // 表单验证失败
    
    const defaultMsg = valueEditMode.value ? '更新失败' : '保存失败'
    console.error(defaultMsg + ':', error)
    ElMessage.error(error.message || defaultMsg)
  } finally {
    valueSubmitLoading.value = false
  }
}

// 处理对话框关闭
const handleClose = () => {
  emit('update:visible', false);
}

// 类型映射函数
const getTypeNameText = (type: number): string => {
  const typeMap: Record<number, string> = {
    0: '字符串',
    1: '整数',
    2: '布尔值',
    3: '浮点'
  }
  return typeMap[type] || '未知类型'
}

// 获取类型描述
const getTypeDescription = (type: number): string => {
  const typeDescMap: Record<number, string> = {
    0: '字符串类型 (String)',
    1: '整数类型 (Integer)',
    2: '布尔类型 (Boolean)',
    3: '浮点类型 (Float)'
  }
  return typeDescMap[type] || '未知类型'
}

// 获取类型标签样式
const getTypeTagType = (type: number): string => {
  const typeTagMap: Record<number, string> = {
    0: 'info',     // 字符串 - 蓝色
    1: 'success',  // 整数 - 绿色
    2: 'warning',  // 布尔值 - 黄色
    3: 'danger'    // 浮点 - 红色
  }
  return typeTagMap[type] || ''
}

// 初始化
onMounted(() => {
  // 初始加载在watch中处理了，因为需要props.templateId
})
</script>

<style scoped>
.template-value-manager-container {
  padding: 0px; /* 对话框内部，无需外部padding */
}

.query-form {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border: 1px solid #e0e0e0;
}

.add-button-container {
  display: flex;
  gap: 10px;
}

.template-value-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e0e0e0;
}

.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  text-align: right;
}

.type-tag {
  padding: 2px 8px;
  border-radius: 4px;
  min-width: 60px;
  display: inline-block;
  text-align: center;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .query-form {
    flex-direction: column;
  }
  
  .add-button-container {
    margin-top: 10px;
    width: 100%;
  }
}
</style> 