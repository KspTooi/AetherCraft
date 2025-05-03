<template>
  <div class="permission-manager-container">
    <!-- 查询表单 -->
    <div class="query-form">
      <el-form :model="query" inline>
        <el-form-item label="权限代码">
          <el-input 
            v-model="query.code" 
            placeholder="输入权限代码查询" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="权限名称">
          <el-input 
            v-model="query.name" 
            placeholder="输入权限名称查询" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPermissionList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="add-button-container">
        <el-button type="success" @click="openInsertModal">创建权限</el-button>
      </div>
    </div>

    <!-- 权限列表 -->
    <div class="permission-table">
      <el-table
        :data="list"
        stripe
        v-loading="loading"
      >
        <el-table-column 
          prop="code" 
          label="权限代码" 
          min-width="150" 
          show-overflow-tooltip
          :show-overflow-tooltip-props="{
            effect: 'dark',
            placement: 'top',
            enterable: false
          }"
        />
        <el-table-column 
          prop="name" 
          label="权限名称" 
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
          label="权限描述" 
          min-width="200" 
          show-overflow-tooltip
          :show-overflow-tooltip-props="{
            effect: 'dark',
            placement: 'top',
            enterable: false
          }"
        />
        <el-table-column label="系统权限" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isSystem === 1 ? 'warning' : 'info'">
              {{ scope.row.isSystem === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="180">
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
              @click="removePermission(scope.row)"
              :icon="DeleteIcon"
              :disabled="scope.row.isSystem === 1"
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
            loadPermissionList()
          }"
          @current-change="(val: number) => {
            query.page = val
            loadPermissionList()
          }"
          background
        />
      </div>
    </div>

    <!-- 权限编辑/新增模态框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="mode === 'insert' ? '新增权限' : '编辑权限'"
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
          <el-form-item label="创建时间">
            <el-input v-model="details.createTime" disabled />
          </el-form-item>
          <el-form-item label="修改时间">
            <el-input v-model="details.updateTime" disabled />
          </el-form-item>
          <el-form-item label="系统权限">
            <el-tag :type="details.isSystem === 1 ? 'warning' : 'info'">
              {{ details.isSystem === 1 ? '是' : '否' }}
            </el-tag>
          </el-form-item>
        </template>

        <!-- 可编辑字段 -->
        <el-form-item label="权限代码" prop="code">
          <el-input 
            v-model="details.code" 
            :disabled="mode === 'update' && details.isSystem === 1"
            :placeholder="mode === 'update' && details.isSystem === 1 ? '系统权限不可修改代码' : '请输入权限代码'"
          />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input 
            v-model="details.name" 
            :disabled="mode === 'update' && details.isSystem === 1"
            :placeholder="mode === 'update' && details.isSystem === 1 ? '系统权限不可修改名称' : '请输入权限名称'"
          />
        </el-form-item>
        <el-form-item label="权限描述" prop="description">
          <el-input 
            v-model="details.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入权限描述"
          />
        </el-form-item>
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number 
            v-model="details.sortOrder" 
            :min="0"
            :max="9999"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermission" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">

import {reactive, ref, onMounted} from "vue";
import type {GetPermissionDetailsVo, GetPermissionListDto, GetPermissionListVo} from "@/commons/api/PermissionApi.ts";
import PermissionApi from "@/commons/api/PermissionApi";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, Delete } from '@element-plus/icons-vue';
import { markRaw } from 'vue';
import type { FormInstance } from 'element-plus';

const mode = ref<"insert" | "update">("insert")

const query = reactive<GetPermissionListDto>({
  code: null,
  name: null,
  page: 1,
  pageSize: 10
})
const list = ref<GetPermissionListVo[]>([])
const total = ref(0)

//用户表单数据
const details = reactive<GetPermissionDetailsVo>({
  code: "",
  createTime: "",
  description: "",
  id: "",
  isSystem: 0,
  name: "",
  sortOrder: 0,
  updateTime: ""
})

// 加载状态
const loading = ref(false)

// 使用markRaw包装图标组件
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 模态框相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 表单校验规则
const rules = {
  code: [
    { required: true, message: '请输入权限代码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_:]{2,50}$/, message: '权限代码只能包含2-50位字母、数字、下划线和冒号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

const loadPermissionList = async () => {
  try {
    const res = await PermissionApi.getPermissionList(query);
    list.value = res.rows;
    total.value = res.count;
  } catch (e) {
    ElMessage.error('加载权限列表失败');
    console.error("加载权限列表失败", e);
  }
}

// 重置查询条件
const resetQuery = () => {
  query.code = null
  query.name = null
  query.page = 1
  loadPermissionList()
}

// 重置表单
const resetForm = () => {
  details.id = ""
  details.code = ""
  details.name = ""
  details.description = ""
  details.sortOrder = 0
  details.isSystem = 0
  details.createTime = ""
  details.updateTime = ""
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

//页面加载时自动加载数据
onMounted(() => {
  loadPermissionList()
})

//打开新增模态框
const openInsertModal = () => {
  mode.value = "insert"
  resetForm()
  dialogVisible.value = true
}

//打开修改模态框
const openUpdateModal = async (row: GetPermissionListVo) => {
  try {
    mode.value = "update"
    resetForm()
    
    const res = await PermissionApi.getPermissionDetails({ id: row.id })
    Object.assign(details, res)
    
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取权限详情失败')
    console.error('获取权限详情失败', error)
  }
}

//新增或修改权限
const savePermission = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      await PermissionApi.savePermission({
        id: mode.value === 'update' ? details.id : undefined,
        code: details.code,
        name: details.name,
        description: details.description,
        sortOrder: details.sortOrder
      })
      
      ElMessage.success(mode.value === 'insert' ? '新增权限成功' : '更新权限成功')
      dialogVisible.value = false
      loadPermissionList()
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '操作失败'
      ElMessage.error(errorMsg)
    } finally {
      submitLoading.value = false
    }
  })
}

//删除权限
const removePermission = async (row: GetPermissionListVo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限 ${row.name} 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await PermissionApi.removePermission({ id: row.id });
    ElMessage.success('删除权限成功');
    loadPermissionList();
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error instanceof Error ? error.message : '删除失败';
      ElMessage.error(errorMsg);
    }
  }
}

</script>

<style scoped>
.permission-manager-container {
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

.permission-table {
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