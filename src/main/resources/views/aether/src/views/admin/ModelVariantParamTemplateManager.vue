<template>
  <div class="template-manager-container">
    <!-- 查询表单 -->
    <div class="query-form">
      <el-form :model="query" inline>
        <el-form-item label="关键字">
          <el-input 
            v-model="query.keyword" 
            placeholder="模板名称" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTemplateList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="add-button-container">
        <el-button 
          type="success" 
          @click="openInsertModal"
          :icon="PlusIcon"
        >
          新增模板
        </el-button>
      </div>
    </div>

    <!-- 模板列表 -->
    <div class="template-table">
      <el-table
        :data="list"
        stripe
        border
        v-loading="loading"
      >
        <el-table-column 
          prop="name" 
          label="模板名称" 
          min-width="150"
          show-overflow-tooltip
          resizable
        />
        <el-table-column 
          prop="valueCount" 
          label="参数数量" 
          width="100"
          align="center"
          resizable
        >
          <template #default="scope">
            <el-tag type="info" size="small">
              {{ scope.row.valueCount }} 个
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
        <el-table-column 
          prop="updateTime" 
          label="更新时间" 
          width="160"
          show-overflow-tooltip
          resizable
        />
        <el-table-column label="操作" fixed="right" min-width="240" resizable>
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
              type="info" 
              size="small" 
              @click="viewTemplateDetails(scope.row)"
              :icon="ViewIcon"
            >
              查看
            </el-button>
            <el-button
              link
              type="success"
              size="small"
              @click="openTemplateValueManager(scope.row)"
              :icon="ManageIcon"
            >
              管理
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
        @size-change="loadTemplateList"
        @current-change="loadTemplateList"
      />
    </div>

    <!-- 新增/编辑模板对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入模板名称"
            maxlength="128"
            show-word-limit
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

    <!-- 模板详情对话框 -->
    <el-dialog
      v-model="detailsVisible"
      title="模板详情"
      width="600px"
    >
      <div v-if="templateDetails" class="template-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="模板名称">
            {{ templateDetails.name }}
          </el-descriptions-item>
          <el-descriptions-item label="模板ID">
            {{ templateDetails.id }}
          </el-descriptions-item>
          <el-descriptions-item label="所属用户ID">
            {{ templateDetails.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="所属玩家ID">
            {{ templateDetails.playerId }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ templateDetails.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ templateDetails.updateTime }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="template-note">
          <el-alert
            title="说明"
            description="模板值管理功能由专门的模板值管理界面提供，此处只显示模板基本信息。"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailsVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 模板值管理模态框 -->
    <ModelVariantParamTemplateValueManager
      v-model:visible="templateValueManagerVisible"
      :templateId="currentTemplateId"
      :templateName="currentTemplateName"
      @template-value-changed="loadTemplateList" 
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit as EditIcon, Delete as DeleteIcon, View as ViewIcon, Plus as PlusIcon, Management as ManageIcon } from '@element-plus/icons-vue'
import AdminModelVariantParamTemplateApi from '@/commons/api/AdminModelVariantParamTemplateApi'
import ModelVariantParamTemplateValueManager from '@/views/admin/ModelVariantParamTemplateValueManager.vue'
import type { 
  GetModelVariantParamTemplateListDto, 
  GetModelVariantParamTemplateListVo, 
  SaveModelVariantParamTemplateDto,
  GetModelVariantParamTemplateDetailsVo
} from '@/commons/api/AdminModelVariantParamTemplateApi'

// 数据定义
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailsVisible = ref(false)
const editMode = ref(false)
const list = ref<GetModelVariantParamTemplateListVo[]>([])
const total = ref(0)
const templateDetails = ref<GetModelVariantParamTemplateDetailsVo | null>(null)

// 模板值管理相关数据
const templateValueManagerVisible = ref(false)
const currentTemplateId = ref('')
const currentTemplateName = ref('')

// 查询条件
const query = reactive<GetModelVariantParamTemplateListDto>({
  page: 1,
  pageSize: 20,
  keyword: null
})

// 表单数据
const form = reactive<SaveModelVariantParamTemplateDto>({
  templateId: undefined,
  name: ''
})

// 表单引用
const formRef = ref()

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 1, max: 128, message: '模板名称长度在1-128个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return editMode.value ? '编辑模板' : '新增模板'
})

// 加载模板列表
const loadTemplateList = async () => {
  loading.value = true
  try {
    const result = await AdminModelVariantParamTemplateApi.getModelVariantParamTemplateList(query)
    list.value = result.rows
    total.value = result.count
  } catch (error: any) {
    console.error('加载模板列表失败:', error)
    ElMessage.error(error.message || '加载模板列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  query.page = 1
  query.pageSize = 20
  query.keyword = null
  loadTemplateList()
}

// 打开新增对话框
const openInsertModal = () => {
  editMode.value = false
  dialogVisible.value = true
}

// 打开编辑对话框
const openUpdateModal = async (row: GetModelVariantParamTemplateListVo) => {
  try {
    editMode.value = true
    const details = await AdminModelVariantParamTemplateApi.getModelVariantParamTemplateDetails({ 
      templateId: row.id
    })
    Object.assign(form, {
      templateId: details.id,
      name: details.name
    })
    dialogVisible.value = true
  } catch (error: any) {
    console.error('加载模板详情失败:', error)
    ElMessage.error(error.message || '加载模板详情失败')
  }
}

// 查看模板详情
const viewTemplateDetails = async (row: GetModelVariantParamTemplateListVo) => {
  try {
    templateDetails.value = await AdminModelVariantParamTemplateApi.getModelVariantParamTemplateDetails({ 
      templateId: row.id
    })
    detailsVisible.value = true
  } catch (error: any) {
    console.error('加载模板详情失败:', error)
    ElMessage.error(error.message || '加载模板详情失败')
  }
}

// 打开模板值管理模态框
const openTemplateValueManager = (row: GetModelVariantParamTemplateListVo) => {
  currentTemplateId.value = row.id;
  currentTemplateName.value = row.name;
  templateValueManagerVisible.value = true;
};

// 确认删除
const confirmRemove = (row: GetModelVariantParamTemplateListVo) => {
  ElMessageBox.confirm(
    `确定要删除模板 "${row.name}" 吗？删除模板会同时删除其包含的所有参数值。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    removeTemplate(row.id)
  })
}

// 删除模板
const removeTemplate = async (templateId: string) => {
  try {
    await AdminModelVariantParamTemplateApi.removeModelVariantParamTemplate({ id: templateId })
    ElMessage.success('删除成功')
    loadTemplateList()
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
    templateId: undefined,
    name: ''
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    await AdminModelVariantParamTemplateApi.saveModelVariantParamTemplate(form)
    ElMessage.success(editMode.value ? '更新成功' : '保存成功')
    dialogVisible.value = false
    loadTemplateList()
  } catch (error: any) {
    if (error === false) return // 表单验证失败
    
    const defaultMsg = editMode.value ? '更新失败' : '保存失败'
    console.error(defaultMsg + ':', error)
    ElMessage.error(error.message || defaultMsg)
  } finally {
    submitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadTemplateList()
})
</script>

<style scoped>
.template-manager-container {
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

.template-table {
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

.template-details {
  margin-bottom: 20px;
}

.template-note {
  margin-top: 20px;
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