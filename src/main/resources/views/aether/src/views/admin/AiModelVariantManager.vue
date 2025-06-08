<template>
  <div class="model-series-manager-container">
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
        <el-form-item label="启用状态">
          <el-select 
            v-model="query.enabled" 
            placeholder="请选择状态" 
            clearable 
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadModelSeriesList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="add-button-container">
        <el-button type="success" @click="openInsertModal">新增模型变体</el-button>
        <el-button 
          type="primary" 
          @click="batchToggle(1)" 
          :disabled="selectedRows.length === 0"
          :loading="batchLoading"
        >
          批量启用 ({{ selectedRows.length }})
        </el-button>
        <el-button 
          type="warning" 
          @click="batchToggle(0)" 
          :disabled="selectedRows.length === 0"
          :loading="batchLoading"
        >
          批量禁用 ({{ selectedRows.length }})
        </el-button>
        <el-button 
          type="info" 
          @click="openApplyTemplateModal" 
          :disabled="selectedRows.length === 0"
        >
          应用参数模板 ({{ selectedRows.length }})
        </el-button>
      </div>
    </div>

    <!-- 模型变体列表 -->
    <div class="model-series-table">
      <el-table
        ref="tableRef"
        :data="list"
        stripe
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column 
          type="selection" 
          width="55"
          align="center"
        />
        <el-table-column 
          prop="code" 
          label="模型代码" 
          min-width="150"
          show-overflow-tooltip
          resizable
        />
        <el-table-column 
          prop="name" 
          label="模型名称" 
          min-width="150" 
          show-overflow-tooltip
          resizable
        />
        <el-table-column 
          prop="type" 
          label="模型类型" 
          width="100"
          align="center"
          resizable
        >
          <template #default="scope">
            <el-tag 
              :type="scope.row.type === 2 ? 'danger' : scope.row.type === 1 ? 'warning' : 'primary'" 
              size="small"
            >
              {{ ['文本', '图形', '多模态'][scope.row.type] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          prop="series" 
          label="模型系列" 
          min-width="100"
          show-overflow-tooltip
          resizable
        />
        <el-table-column
            prop="enabled"
            label="状态"
            width="80"
            align="center"
            resizable
        >
          <template #default="scope">
            <el-tag :type="scope.row.enabled === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          prop="thinking" 
          label="思考能力" 
          width="100"
          align="center"
          resizable
        >
          <template #default="scope">
            <el-tag :type="scope.row.thinking === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.thinking === 1 ? '有' : '无' }}
            </el-tag>
          </template>
        </el-table-column>
<!--        <el-table-column
          prop="scale" 
          label="规模" 
          width="80"
          align="center"
        >
          <template #default="scope">
            <el-tag 
              :type="scope.row.scale === 2 ? 'danger' : scope.row.scale === 1 ? 'warning' : 'success'" 
              size="small"
            >
              {{ ['小型', '中型', '大型'][scope.row.scale] }}
            </el-tag>
          </template>
        </el-table-column>-->
        <el-table-column 
          prop="speed" 
          label="速度" 
          width="80"
          align="center"
          resizable
        >
          <template #default="scope">
            <el-tag 
              :type="scope.row.speed >= 2 ? 'success' : scope.row.speed === 1 ? 'warning' : 'danger'" 
              size="small"
            >
              {{ ['慢速', '中速', '快速', '极快'][scope.row.speed] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          prop="intelligence" 
          label="智能程度" 
          width="100"
          align="center"
          resizable
        >
          <template #default="scope">
            <el-tag 
              :type="scope.row.intelligence >= 4 ? 'danger' : scope.row.intelligence >= 2 ? 'warning' : 'info'" 
              size="small"
            >
              {{ ['木质', '石质', '铁质', '钻石', '纳米', '量子'][scope.row.intelligence] }}
            </el-tag>
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
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="80" resizable align="center">
          <template #default="scope">
            <div style="display: flex; align-items: center; justify-content: center;">
              <el-dropdown @command="(command: string) => handleManageCommand(command, scope.row)">
                <el-button 
                  link
                  type="primary" 
                  size="small"
                  :icon="SettingIcon"
                  style="height: 24px; line-height: 24px;"
                >
                  管理<el-icon class="el-icon--right" style="margin-left: 4px;"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit" :icon="EditIcon">
                      编辑模型
                    </el-dropdown-item>
                    <el-dropdown-item command="params" :icon="SettingIcon">
                      管理参数
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" :icon="DeleteIcon" divided>
                      删除模型
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
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
          @size-change="(val: number) => {
            query.pageSize = val
            loadModelSeriesList()
          }"
          @current-change="(val: number) => {
            query.page = val
            loadModelSeriesList()
          }"
          background
        />
      </div>
    </div>

    <!-- 模型变体编辑/新增模态框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="mode === 'insert' ? '新增模型变体' : '编辑模型变体'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        v-if="dialogVisible"
        ref="formRef"
        :model="details"
        :rules="rules"
        label-width="120px"
        :validate-on-rule-change="false"
      >
        <!-- 可编辑字段 -->
        <el-form-item label="模型代码" prop="code">
          <el-input 
            v-model="details.code" 
            placeholder="请输入模型代码"
            :disabled="mode === 'update'"
          />
        </el-form-item>
        <el-form-item label="模型名称" prop="name">
          <el-input 
            v-model="details.name" 
            placeholder="请输入模型名称"
          />
        </el-form-item>
        <el-form-item label="模型类型" prop="type">
          <el-radio-group v-model="details.type">
            <el-radio :value="0">文本</el-radio>
            <el-radio :value="1">图形</el-radio>
            <el-radio :value="2">多模态</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="模型系列" prop="series">
          <el-select 
            v-model="details.series" 
            placeholder="请选择或输入模型系列/厂商"
            filterable
            allow-create
            style="width: 100%"
          >
            <el-option 
              v-for="series in modelSeriesOptions" 
              :key="series" 
              :label="series" 
              :value="series"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号" prop="seq">
          <el-input-number 
            v-model="details.seq" 
            :min="1"
            :max="9999"
            placeholder="留空则自动设置"
            style="width: 200px"
            clearable
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            排序号越小越靠前，留空则自动设置为最大值+1
          </div>
        </el-form-item>
        <el-form-item label="思考能力" prop="thinking">
          <el-radio-group v-model="details.thinking">
            <el-radio :value="0">无</el-radio>
            <el-radio :value="1">有</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="规模" prop="scale">
          <el-radio-group v-model="details.scale">
            <el-radio :value="0">小型</el-radio>
            <el-radio :value="1">中型</el-radio>
            <el-radio :value="2">大型</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="速度" prop="speed">
          <el-radio-group v-model="details.speed">
            <el-radio :value="0">慢速</el-radio>
            <el-radio :value="1">中速</el-radio>
            <el-radio :value="2">快速</el-radio>
            <el-radio :value="3">极快</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="智能程度" prop="intelligence">
          <el-radio-group v-model="details.intelligence">
            <el-radio :value="0">木质</el-radio>
            <el-radio :value="1">石质</el-radio>
            <el-radio :value="2">铁质</el-radio>
            <el-radio :value="3">钻石</el-radio>
            <el-radio :value="4">纳米</el-radio>
            <el-radio :value="5">量子</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-radio-group v-model="details.enabled">
            <el-radio :value="0">禁用</el-radio>
            <el-radio :value="1">启用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 参数模态框 -->
    <ModelVariantParamsModal
      v-model:visible="paramsModalVisible"
      :model-variant-id="currentModelVariant?.id || ''"
      :model-variant-name="currentModelVariant ? `${currentModelVariant.code} (${currentModelVariant.name})` : ''"
    />

    <!-- 应用参数模板模态框 -->
    <ApplyParamTemplateModal
      v-model:visible="applyTemplateModalVisible"
      :selected-model-variants="selectedRows"
      @applied="handleTemplateApplied"
    />
  </div>
</template>

<script setup lang="ts">

import {markRaw, onMounted, reactive, ref} from "vue";
import type {
  GetAdminModelVariantDetailsVo,
  GetAdminModelVariantListDto,
  GetAdminModelVariantListVo,
  SaveAdminModelVariantDto
} from "@/commons/api/AdminModelVariantApi.ts";
import AdminModelSeriesApi from "@/commons/api/AdminModelVariantApi.ts";
import type {FormInstance} from 'element-plus';
import {ElMessage, ElMessageBox} from 'element-plus';
import {ArrowDown, Delete, Edit, Setting} from '@element-plus/icons-vue';
import dayjs from 'dayjs';
import ModelVariantParamsModal from '@/views/admin/ModelVariantParamsModal.vue'
import ApplyParamTemplateModal from '@/views/admin/ApplyParamTemplateModal.vue'

//模态框模式
const mode = ref<"insert" | "update">("insert")

// 模型系列选项
const modelSeriesOptions = ref<string[]>([])

// 多选相关状态
const selectedRows = ref<GetAdminModelVariantListVo[]>([])
const batchLoading = ref(false)

// 参数模态框相关状态
const paramsModalVisible = ref(false)
const currentModelVariant = ref<GetAdminModelVariantListVo | null>(null)

const query = reactive<GetAdminModelVariantListDto>({
  keyword: null,
  enabled: undefined,
  page: 1,
  pageSize: 10
})

const list = ref<GetAdminModelVariantListVo[]>([])
const total = ref(0)

// 加载状态
const loading = ref(false)

// 使用markRaw包装图标组件
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const SettingIcon = markRaw(Setting);

// 模态框相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 表格引用
const tableRef = ref()

//表单数据
const details = reactive<GetAdminModelVariantDetailsVo>({
  code: "",
  createTime: "",
  enabled: 1,
  id: "",
  intelligence: 0,
  name: "",
  type: 0,
  scale: 0,
  series: "",
  speed: 0,
  thinking: 0,
  seq: null,
  updateTime: ""
})

// 表单校验规则
const rules = {
  code: [
    { required: true, message: '请输入模型代码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入模型名称', trigger: 'blur' },
    { max: 128, message: '模型名称不能超过128个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择模型类型', trigger: 'change' }
  ],
  series: [
    { required: true, message: '请输入模型系列', trigger: 'blur' },
    { max: 128, message: '模型系列不能超过128个字符', trigger: 'blur' }
  ],
  thinking: [
    { required: true, message: '请选择思考能力', trigger: 'change' }
  ],
  scale: [
    { required: true, message: '请选择规模', trigger: 'change' }
  ],
  speed: [
    { required: true, message: '请选择速度', trigger: 'change' }
  ],
  intelligence: [
    { required: true, message: '请选择智能程度', trigger: 'change' }
  ],
  enabled: [
    { required: true, message: '请选择启用状态', trigger: 'change' }
  ]
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

// 加载模型系列选项
const loadModelSeriesOptions = async () => {
  try {
    modelSeriesOptions.value = await AdminModelSeriesApi.getModelSeries()
  } catch (e) {
    ElMessage.error('加载模型系列选项失败')
    console.error("加载模型系列选项失败", e)
  }
}

const loadModelSeriesList = async () => {
  loading.value = true
  try {
    const res = await AdminModelSeriesApi.getModelVariantList(query);
    list.value = res.rows;
    total.value = res.count;
  } catch (e) {
    ElMessage.error('加载模型变体列表失败');
    console.error("加载模型变体列表失败", e);
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = null
  query.enabled = undefined
  query.page = 1
  loadModelSeriesList()
}

const resetForm = () => {
  details.id = ""
  details.code = ""
  details.name = ""
  details.type = 0
  details.series = ""
  details.thinking = 0
  details.scale = 0
  details.speed = 0
  details.intelligence = 0
  details.enabled = 1
  details.seq = null
  details.createTime = ""
  details.updateTime = ""
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

//页面加载时自动加载数据
onMounted(() => {
  loadModelSeriesOptions()
  loadModelSeriesList()
})

const openInsertModal = () => {
  mode.value = "insert"
  resetForm()
  dialogVisible.value = true
}

const openUpdateModal = async (row: GetAdminModelVariantListVo) => {
  try {
    mode.value = "update"
    resetForm()
    
    const res = await AdminModelSeriesApi.getModelVariantDetails({ id: row.id })
    Object.assign(details, res)
    
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取模型变体详情失败')
    console.error('获取模型变体详情失败', error)
  }
}

const save = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const saveDto: SaveAdminModelVariantDto = {
        id: mode.value === 'update' ? details.id : undefined,
        code: details.code,
        name: details.name,
        type: details.type,
        series: details.series,
        thinking: details.thinking,
        scale: details.scale,
        speed: details.speed,
        intelligence: details.intelligence,
        enabled: details.enabled,
        seq: details.seq || undefined // 如果为0或空则传undefined
      }
      
      await AdminModelSeriesApi.saveModelVariant(saveDto)
      
      ElMessage.success(mode.value === 'insert' ? '新增模型变体成功' : '更新模型变体成功')
      dialogVisible.value = false
      loadModelSeriesList()
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '操作失败'
      ElMessage.error(errorMsg)
    } finally {
      submitLoading.value = false
    }
  })
}

const remove = async (row: GetAdminModelVariantListVo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模型变体 ${row.name} (${row.code}) 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await AdminModelSeriesApi.removeModelVariant({ id: row.id });
    ElMessage.success('删除模型变体成功');
    loadModelSeriesList();
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error instanceof Error ? error.message : '删除失败';
      ElMessage.error(errorMsg);
    }
  }
}

const handleSelectionChange = (rows: GetAdminModelVariantListVo[]) => {
  selectedRows.value = rows
}

const batchToggle = async (enabled: number) => {
  batchLoading.value = true
  try {
    await AdminModelSeriesApi.toggleModelVariant({ ids: selectedRows.value.map(row => row.id), enabled })
    ElMessage.success('批量操作成功')
    loadModelSeriesList()
  } catch (error) {
    const errorMsg = error instanceof Error ? error.message : '操作失败'
    ElMessage.error(errorMsg)
  } finally {
    batchLoading.value = false
  }
}

const openParamsModal = (row: GetAdminModelVariantListVo) => {
  currentModelVariant.value = row
  paramsModalVisible.value = true
}

// 应用参数模板模态框相关状态
const applyTemplateModalVisible = ref(false)

const openApplyTemplateModal = () => {
  applyTemplateModalVisible.value = true
}

const handleTemplateApplied = () => {
  applyTemplateModalVisible.value = false
  selectedRows.value = []
  // 清除表格选择状态
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

const handleManageCommand = (command: string, row: GetAdminModelVariantListVo) => {
  if (command === 'edit') {
    openUpdateModal(row)
  } else if (command === 'params') {
    openParamsModal(row)
  } else if (command === 'delete') {
    remove(row)
  }
}

</script>

<style scoped>
.model-series-manager-container {
  padding: 20px;
  max-width: 100%;
  overflow-x: auto;
}

.query-form {
  margin-bottom: 20px;
}

.add-button-container {
  margin-top: 10px;
  margin-bottom: 20px;
}

.model-series-table {
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