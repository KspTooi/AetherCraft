<template>
  <div class="config-manager-container">
    <!-- 查询表单 -->
    <div class="query-form">
      <el-form :model="query" inline>
        <el-form-item label="关键字">
          <el-input 
            v-model="query.keyword" 
            placeholder="配置键/值/描述" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="所有者名称">
          <el-input 
            v-model="query.playerName" 
            placeholder="输入所有者名称查询" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadConfigList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="add-button-container">
        <el-button type="success" @click="openInsertModal">创建配置</el-button>
      </div>
    </div>

    <!-- 配置列表 -->
    <div class="config-table">
      <el-table
        :data="list"
        stripe
        v-loading="loading"
      >
        <el-table-column 
          prop="configKey" 
          label="配置键" 
          min-width="210"
          show-overflow-tooltip
          :show-overflow-tooltip-props="{
            effect: 'dark',
            placement: 'top',
            enterable: false
          }"
        />
        <el-table-column 
          prop="configValue" 
          label="配置值" 
          min-width="150" 
          show-overflow-tooltip
          :show-overflow-tooltip-props="{
            effect: 'dark',
            placement: 'top',
            enterable: false
          }"
        />
        <el-table-column 
          prop="description" 
          label="配置描述" 
          min-width="180"
          show-overflow-tooltip
          :show-overflow-tooltip-props="{
            effect: 'dark',
            placement: 'top',
            enterable: false
          }"
        />
        <el-table-column 
          prop="playerName" 
          label="所有者名称"
          min-width="120"
        />
        <el-table-column label="操作" fixed="right" min-width="140">
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
              @click="removeConfig(scope.row)"
              :icon="DeleteIcon"
            >
              删除
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
          @size-change="(val: number) => {
            query.pageSize = val
            loadConfigList()
          }"
          @current-change="(val: number) => {
            query.page = val
            loadConfigList()
          }"
          background
        />
      </div>
    </div>

    <!-- 配置编辑/新增模态框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="mode === 'insert' ? '新增配置' : '编辑配置'"
      width="500px"
      :close-on-click-modal="false"
    >
      <div v-if="mode === 'insert'" style="color: #909399; font-size: 13px; margin-bottom: 10px;">
        提示：新建配置项时，所有者默认为当前用户，无法指定为他人。
      </div>
      <el-form
        v-if="dialogVisible"
        ref="formRef"
        :model="details"
        :rules="rules"
        label-width="100px"
        :validate-on-rule-change="false"
      >
        <!-- 编辑时显示的只读信息 -->
        <template v-if="mode === 'update'">
          <el-form-item label="创建时间">
            <el-input v-model="details.createTime" disabled />
          </el-form-item>
          <el-form-item label="修改时间">
            <el-input v-model="details.updateTime" disabled />
          </el-form-item>
          <el-form-item label="所有者名称">
            <el-input v-model="details.playerName" disabled />
          </el-form-item>
        </template>

        <!-- 可编辑字段 -->
        <el-form-item label="配置键" prop="configKey">
          <el-input 
            v-model="details.configKey" 
            placeholder="请输入配置键"
            :disabled="mode === 'update'"
          />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input 
            v-model="details.configValue" 
            type="textarea"
            :rows="3"
            placeholder="请输入配置值"
          />
        </el-form-item>
        <el-form-item label="配置描述" prop="description">
          <el-input 
            v-model="details.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入配置描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveConfig" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {reactive, ref, onMounted} from "vue";
import type {GetConfigDetailsVo, GetConfigListDto, GetConfigListVo, SaveConfigDto} from "@/commons/api/AdminConfigApi.ts";
import ConfigApi from "@/commons/api/AdminConfigApi.ts";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, Delete } from '@element-plus/icons-vue';
import { markRaw } from 'vue';
import type { FormInstance } from 'element-plus';

const mode = ref<"insert" | "update">("insert")

const query = reactive<GetConfigListDto>({
  keyword: null,
  playerName: null,
  page: 1,
  pageSize: 10
})

const list = ref<GetConfigListVo[]>([])
const total = ref(0)

// 加载状态
const loading = ref(false)

// 使用markRaw包装图标组件
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 模态框相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

//表单数据
const details = reactive<GetConfigDetailsVo>({
  configKey: "",
  configValue: "",
  createTime: "",
  description: "",
  id: "",
  updateTime: "",
  userId: "",
  playerName: ""
})

// 表单校验规则
const rules = {
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_.]{2,50}$/, message: '配置键只能包含2-50位字母、数字、下划线和点', trigger: 'blur' }
  ],
  configValue: [
    { required: true, message: '请输入配置值', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

const loadConfigList = async () => {
  loading.value = true
  try {
    const res = await ConfigApi.getConfigList(query);
    list.value = res.rows;
    total.value = res.count;

    console.log(list.value)
  } catch (e) {
    ElMessage.error('加载配置列表失败');
    console.error("加载配置列表失败", e);
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = null
  query.playerName = null
  query.page = 1
  loadConfigList()
}

const resetForm = () => {
  details.id = ""
  details.configKey = ""
  details.configValue = ""
  details.description = ""
  details.createTime = ""
  details.updateTime = ""
  details.userId = ""
  details.playerName = ""
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

//页面加载时自动加载数据
onMounted(() => {
  loadConfigList()
})

const openInsertModal = () => {
  mode.value = "insert"
  resetForm()
  dialogVisible.value = true
}

const openUpdateModal = async (row: GetConfigListVo) => {
  try {
    mode.value = "update"
    resetForm()
    
    const res = await ConfigApi.getConfigDetails({ id: row.id })
    Object.assign(details, res)
    
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取配置详情失败')
    console.error('获取配置详情失败', error)
  }
}

const saveConfig = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      await ConfigApi.saveConfig({
        id: mode.value === 'update' ? details.id : undefined,
        configKey: details.configKey,
        configValue: details.configValue,
        description: details.description
      })
      
      ElMessage.success(mode.value === 'insert' ? '新增配置成功' : '更新配置成功')
      dialogVisible.value = false
      loadConfigList()
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '操作失败'
      ElMessage.error(errorMsg)
    } finally {
      submitLoading.value = false
    }
  })
}

const removeConfig = async (row: GetConfigListVo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除配置 ${row.configKey} 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await ConfigApi.removeConfig({ id: row.id });
    ElMessage.success('删除配置成功');
    loadConfigList();
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error instanceof Error ? error.message : '删除失败';
      ElMessage.error(errorMsg);
    }
  }
}
</script>

<style scoped>
.config-manager-container {
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

.config-table {
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