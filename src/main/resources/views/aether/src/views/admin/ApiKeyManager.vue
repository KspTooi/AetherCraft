<template>
  <div class="apikey-manager-container">
    <!-- 查询表单 -->
    <div class="query-form">
      <el-form :model="query" inline>
        <el-form-item label="密钥名称">
          <el-input 
            v-model="query.keyName" 
            placeholder="输入密钥名称" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="密钥系列">
          <el-input 
            v-model="query.keySeries" 
            placeholder="输入密钥系列" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="选择状态" clearable style="width: 200px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadApiKeyList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="add-button-container">
        <el-button type="success" @click="openInsertModal">创建密钥</el-button>
      </div>
    </div>

    <!-- API密钥列表 -->
    <div class="apikey-table">
      <el-table
        :data="list"
        stripe
        v-loading="loading"
      >
        <el-table-column 
          prop="keyName" 
          label="密钥名称" 
          min-width="150"
          show-overflow-tooltip
          :show-overflow-tooltip-props="{
            effect: 'dark',
            placement: 'top',
            enterable: false
          }"
        />
        <el-table-column 
          prop="keySeries" 
          label="密钥系列" 
          min-width="120"
        />
        <el-table-column 
          prop="isShared" 
          label="是否公开" 
          min-width="100"
        >
          <template #default="scope">
            {{ scope.row.isShared === 1 ? '公开' : '私有' }}
          </template>
        </el-table-column>
        <el-table-column 
          prop="usageCount" 
          label="使用次数" 
          min-width="100"
        />
        <el-table-column 
          prop="lastUsedTime" 
          label="最后使用时间" 
          min-width="160"
        />
        <el-table-column 
          prop="status" 
          label="状态" 
          min-width="100"
        >
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="120">
          <template #default="scope">
            <div class="action-buttons" style="display: flex; align-items: center; gap: 8px;">
              <el-dropdown trigger="hover" @command="handleCommand($event, scope.row)">
                <el-button link type="primary" size="small">
                  管理<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit" :icon="EditIcon">编辑</el-dropdown-item>
                    <el-dropdown-item command="authorization" :icon="KeyIcon">管理授权</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button 
                link
                type="danger" 
                size="small" 
                @click="removeApiKey(scope.row)"
                :icon="DeleteIcon"
              >
                删除
              </el-button>
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
            loadApiKeyList()
          }"
          @current-change="(val: number) => {
            query.page = val
            loadApiKeyList()
          }"
          background
        />
      </div>
    </div>

    <!-- API密钥编辑/新增模态框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="mode === 'insert' ? '新增API密钥' : '编辑API密钥'"
      width="500px"
      :close-on-click-modal="false"
    >
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
<!--          <el-form-item label="创建时间">
            <el-input v-model="details.createTime" disabled />
          </el-form-item>
          <el-form-item label="使用次数">
            <el-input v-model="details.usageCount" disabled />
          </el-form-item>
          <el-form-item label="最后使用">
            <el-input v-model="details.lastUsedTime" disabled />
          </el-form-item>-->
        </template>

        <!-- 可编辑字段 -->
        <el-form-item label="密钥名称" prop="keyName">
          <el-input 
            v-model="details.keyName" 
            placeholder="请输入密钥名称"
          />
        </el-form-item>
        <el-form-item label="密钥系列" prop="keySeries">
          <el-select 
            v-model="details.keySeries" 
            placeholder="选择密钥系列" 
            style="width: 200px"
            filterable
          >
            <el-option 
              v-for="series in seriesList" 
              :key="series" 
              :label="series" 
              :value="series"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="密钥值" prop="keyValue">
          <el-input 
            v-model="details.keyValue" 
            type="password"
            placeholder="请输入密钥值"
          />
          <div class="form-tip" v-show="mode == 'update'">
            <i class="bi bi-info-circle"></i>
            留空则保持原密钥值不变
          </div>
        </el-form-item>
        <el-form-item label="是否公开" prop="isShared">
          <el-radio-group v-model="details.isShared">
            <el-radio :value="0">私有</el-radio>
            <el-radio :value="1">公开</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="details.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveApiKey" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>


<script setup lang="ts">

import {markRaw, onMounted, reactive, ref} from "vue";
import type {GetApiKeyDetailsVo, GetApiKeyListDto, GetApiKeyListVo} from "@/commons/api/ApiKeyApi.ts";
import ApiKeyApi from "@/commons/api/ApiKeyApi.ts";
import type {FormInstance} from 'element-plus';
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit, Key, ArrowDown} from '@element-plus/icons-vue';


const mode = ref<"insert" | "update">("insert")

const query = reactive<GetApiKeyListDto>({
  keyName: null,
  keySeries: null,
  status: null,
  page: 1,
  pageSize: 10
})

const list = ref<GetApiKeyListVo[]>([])
const total = ref(0)

const seriesList = ref<string[]>([])

//表单数据
const details = reactive<GetApiKeyDetailsVo>({
  createTime: "",
  id: "",
  isShared: 0,
  keyName: "",
  keySeries: "",
  keyValue: "",
  lastUsedTime: "",
  usageCount: "",
  status: 1
})

// 加载状态
const loading = ref(false)

// 使用markRaw包装图标组件
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const KeyIcon = markRaw(Key);

// 模态框相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 表单校验规则
const rules = {
  keyName: [
    { required: true, message: '请输入密钥名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  keySeries: [
    { required: true, message: '请输入密钥系列', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  keyValue: [
    { 
      required: true, 
      message: '请输入密钥值', 
      trigger: 'blur',
      // 仅在新增模式下必填
      validator: (rule: any, value: string, callback: Function) => {
        if (mode.value === 'insert' && !value) {
          callback(new Error('请输入密钥值'))
        } else {
          callback()
        }
      }
    },
    { min: 2, max: 500, message: '长度在 2 到 500 个字符', trigger: 'blur' }
  ],
  isShared: [
    { required: true, message: '请选择是否公开', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const loadSeriesList = async () => {
  try {
    seriesList.value = await ApiKeyApi.getSeriesList();
  } catch (e) {
    ElMessage.error('加载密钥系列列表失败');
    seriesList.value = [];
  }
}

const loadApiKeyList = async () => {
  loading.value = true
  try {
    const res = await ApiKeyApi.getApiKeyList(query);
    list.value = res.rows;
    total.value = res.count;
  } catch (e) {
    ElMessage.error('加载API密钥列表失败');
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
    query.keyName = null;
    query.keySeries = null;
    query.status = null;
    query.page = 1;
    query.pageSize = 10;
    loadApiKeyList();
}

const resetForm = () => {
  details.id = ""
  details.keyName = ""
  details.keySeries = seriesList.value[0] || "" // 设置为第一个系列，如果没有则为空字符串
  details.keyValue = ""
  details.isShared = 0
  details.status = 1
  details.usageCount = ""
  details.lastUsedTime = ""
  details.createTime = ""
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const openInsertModal = () => {
  mode.value = "insert"
  resetForm()
  dialogVisible.value = true
}

const openUpdateModal = async (row: GetApiKeyListVo) => {
  try {
    mode.value = "update"
    resetForm()
    
    const res = await ApiKeyApi.getApiKeyDetails({ id: row.id })
    Object.assign(details, res)
    
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取API密钥详情失败')
  }
}

const saveApiKey = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      await ApiKeyApi.saveApiKey({
        id: mode.value === 'update' ? details.id : null,
        keyName: details.keyName,
        keySeries: details.keySeries,
        keyValue: details.keyValue,
        isShared: details.isShared,
        status: details.status
      })
      
      ElMessage.success(mode.value === 'insert' ? '新增API密钥成功' : '更新API密钥成功')
      dialogVisible.value = false
      loadApiKeyList()
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '操作失败'
      ElMessage.error(errorMsg)
    } finally {
      submitLoading.value = false
    }
  })
}

const removeApiKey = async (row: GetApiKeyListVo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除API密钥 ${row.keyName} 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await ApiKeyApi.removeApiKey({ id: row.id });
    ElMessage.success('删除API密钥成功');
    loadApiKeyList();
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error instanceof Error ? error.message : '删除失败';
      ElMessage.error(errorMsg);
    }
  }
}

// 处理下拉菜单命令
const handleCommand = (command: string, row: GetApiKeyListVo) => {
  if (command === 'edit') {
    openUpdateModal(row);
  } else if (command === 'authorization') {
    openAuthorizationModal(row);
  }
}

// 管理授权
const openAuthorizationModal = (row: GetApiKeyListVo) => {
  ElMessage.info(`暂未实现对 ${row.keyName} 的授权管理功能`);
  // 这里可以实现管理授权的功能，例如打开授权管理对话框等
}

//页面加载时自动加载数据
onMounted(() => {
  loadSeriesList()
  loadApiKeyList()
})

</script>

<style scoped>
.apikey-manager-container {
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

.apikey-table {
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-tip i {
  font-size: 14px;
}
</style>