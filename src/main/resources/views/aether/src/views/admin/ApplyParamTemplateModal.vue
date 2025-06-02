<template>
  <el-dialog
    v-model="visible"
    title="应用参数模板"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="apply-template-content">
      <div class="selected-models-info">
        <el-alert
          title=""
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            将参数模板应用于 <strong>{{ selectedModelVariants.length }}</strong> 个选定的模型变体：
            <div class="model-list">
              <el-tag
                v-for="model in selectedModelVariants"
                :key="model.id"
                size="small"
                style="margin: 2px;"
              >
                {{ model.code }} ({{ model.name }})
              </el-tag>
            </div>
          </template>
        </el-alert>
      </div>

      <el-form 
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        style="margin-top: 20px;"
      >
        <el-form-item label="选择模板" prop="templateId">
          <el-select
            v-model="formData.templateId"
            placeholder="请选择参数模板"
            style="width: 100%"
            filterable
            :loading="templateLoading"
            @focus="loadTemplateList"
          >
            <el-option
              v-for="template in templateList"
              :key="template.id"
              :label="`${template.name} (${template.valueCount}个参数)`"
              :value="template.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ template.name }}</span>
                <span style="color: #8492a6; font-size: 12px;">
                  {{ template.valueCount }}个参数 | {{ formatDateTime(template.createTime) }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="应用范围" prop="global">
          <el-radio-group v-model="formData.global">
            <el-radio :value="1">
              <span style="font-weight: 500;">全局参数</span>
              <div style="color: #666; font-size: 12px; margin-top: 2px;">
                作为系统默认参数，影响所有用户
              </div>
            </el-radio>
            <el-radio :value="0">
              <span style="font-weight: 500;">当前用户参数</span>
              <div style="color: #666; font-size: 12px; margin-top: 2px;">
                仅作为当前管理员的个人参数配置
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleApply" 
          :loading="applyLoading"
        >
          确定应用
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import dayjs from 'dayjs'
import AdminModelVariantParamTemplateApi from '@/commons/api/AdminModelVariantParamTemplateApi'
import AdminModelVariantApi from '@/commons/api/AdminModelVariantApi'
import type { 
  GetModelVariantParamTemplateListVo
} from '@/commons/api/AdminModelVariantParamTemplateApi'
import type { 
  GetAdminModelSeriesListVo,
  ApplyModelVariantParamTemplateDto
} from '@/commons/api/AdminModelVariantApi'

// Props
interface Props {
  visible: boolean
  selectedModelVariants: GetAdminModelSeriesListVo[]
}

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'applied': []
}>()

const props = defineProps<Props>()

// 表单数据
const formData = reactive({
  templateId: '',
  global: 1 // 默认选择全局参数
})

// 表单引用
const formRef = ref<FormInstance>()

// 模板列表相关
const templateList = ref<GetModelVariantParamTemplateListVo[]>([])
const templateLoading = ref(false)

// 应用加载状态
const applyLoading = ref(false)

// 计算属性：控制模态框显示
const visible = computed({
  get: () => props.visible,
  set: (value: boolean) => {
    emit('update:visible', value)
  }
})

// 表单校验规则
const rules = {
  templateId: [
    { required: true, message: '请选择参数模板', trigger: 'change' }
  ],
  global: [
    { required: true, message: '请选择应用范围', trigger: 'change' }
  ]
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('MM-DD HH:mm')
}

// 加载模板列表
const loadTemplateList = async () => {
  if (templateList.value.length > 0) return // 避免重复加载
  
  templateLoading.value = true
  try {
    const res = await AdminModelVariantParamTemplateApi.getModelVariantParamTemplateList({
      page: 1,
      pageSize: 100 // 加载前100个模板
    })
    templateList.value = res.rows
  } catch (error) {
    ElMessage.error('加载参数模板列表失败')
    console.error('加载参数模板列表失败', error)
  } finally {
    templateLoading.value = false
  }
}

// 处理应用操作
const handleApply = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await ElMessageBox.confirm(
        `确定要将选择的参数模板应用到 ${props.selectedModelVariants.length} 个模型变体吗？`,
        '确认应用',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      applyLoading.value = true
      
      const dto: ApplyModelVariantParamTemplateDto = {
        templateId: formData.templateId,
        modelVariantIds: props.selectedModelVariants.map(model => model.id),
        global: formData.global
      }
      
      await AdminModelVariantApi.applyModelVariantParamTemplate(dto)
      
      ElMessage.success('参数模板应用成功')
      emit('applied')
      handleClose()
      
    } catch (error) {
      if (error !== 'cancel') {
        const errorMsg = error instanceof Error ? error.message : '应用参数模板失败'
        ElMessage.error(errorMsg)
      }
    } finally {
      applyLoading.value = false
    }
  })
}

// 处理关闭
const handleClose = () => {
  visible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  formData.templateId = ''
  formData.global = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 监听模态框显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    resetForm()
    loadTemplateList()
  }
})
</script>

<style scoped>
.apply-template-content {
  padding: 0;
}

.selected-models-info {
  margin-bottom: 20px;
}

.model-list {
  margin-top: 8px;
  max-height: 120px;
  overflow-y: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-radio) {
  display: block;
  margin-bottom: 12px;
  height: auto;
  line-height: 1.5;
}

:deep(.el-radio__label) {
  white-space: normal;
  line-height: 1.5;
}
</style> 