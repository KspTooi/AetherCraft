<template>
  <div class="group-manager-container">
    <div class="query-form">
      <el-form :model="queryForm" inline>
        <el-form-item label="组名称" label-for="query-keyword">
          <el-input 
            v-model="queryForm.keyword" 
            placeholder="输入组名称查询" 
            clearable 
            id="query-keyword"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadGroupList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="add-button-container">
        <el-button type="success" @click="handleAdd">新增用户组</el-button>
      </div>
    </div>

    <div class="group-table">
      <el-table 
        :data="groupList" 
        border 
        stripe
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="code" label="组标识" min-width="120" />
        <el-table-column prop="name" label="组名称" min-width="120" />
        <el-table-column prop="memberCount" label="成员数量" min-width="100" />
        <el-table-column prop="permissionCount" label="权限数量" min-width="100" />
        <el-table-column label="系统组" min-width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.isSystem" type="info">是</el-tag>
            <el-tag v-else>否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEdit(scope.row)"
              :icon="EditIcon"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(scope.row)"
              :icon="DeleteIcon"
              :disabled="scope.row.isSystem"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-if="!isMobile"
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
        <el-pagination
          v-else
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.pageSize"
          layout="prev, pager, next"
          :total="total"
          @current-change="handleCurrentChange"
          :pager-count="5"
          size="small"
          background
        />
      </div>
    </div>

    <!-- 用户组编辑/新增模态框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formType === 'add' ? '新增用户组' : '编辑用户组'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="groupFormRules"
        label-width="100px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="组标识" prop="code" label-for="group-code">
          <el-input 
            v-model="groupForm.code" 
            :disabled="formType === 'edit' && isSystemGroup"
            :placeholder="formType === 'edit' && isSystemGroup ? '系统组不可修改标识' : '请输入组标识'"
            id="group-code"
          />
        </el-form-item>
        <el-form-item label="组名称" prop="name" label-for="group-name">
          <el-input 
            v-model="groupForm.name" 
            :disabled="formType === 'edit' && isSystemGroup"
            :placeholder="formType === 'edit' && isSystemGroup ? '系统组不可修改名称' : '请输入组名称'"
            id="group-name"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description" label-for="group-description">
          <el-input 
            v-model="groupForm.description" 
            type="textarea" 
            :rows="3"
            id="group-description" 
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" label-for="group-status">
          <el-radio-group v-model="groupForm.status" id="group-status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限节点" prop="permissionIds" label-for="permission-search">
          <div class="permission-container">
            <div class="permission-search">
              <el-input
                v-model="permissionSearch"
                placeholder="搜索权限节点"
                clearable
                id="permission-search"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            <div class="permission-list">
              <el-checkbox-group v-model="groupForm.permissionIds" id="permission-group">
                <div 
                  v-for="permission in filteredPermissions" 
                  :key="permission.id" 
                  class="permission-item"
                >
                  <el-checkbox 
                    :value="Number(permission.id)"
                  >
                    <div class="permission-info">
                      <span class="permission-name">{{ permission.name }}</span>
                      <span class="permission-code">{{ permission.code }}</span>
                    </div>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
              <div v-if="filteredPermissions.length === 0" class="no-permission">
                <el-empty description="未找到匹配的权限节点" />
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, markRaw, computed } from 'vue';
import { Edit, Delete, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance } from 'element-plus';
import GroupApi from "@/commons/api/GroupApi";
import type GetGroupListDto from "@/entity/dto/GetGroupListDto";
import type GetGroupListVo from "@/entity/vo/GetGroupListVo";
import type SaveGroupDto from "@/entity/dto/SaveGroupDto";
import type CommonIdDto from "@/entity/dto/CommonIdDto";
import type GroupPermissionDefinitionVo from '@/entity/vo/GroupPermissionDefinitionVo';

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 检测是否为移动设备
const isMobile = ref(false);
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768;
};

// 查询表单
const queryForm = reactive<GetGroupListDto>({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: undefined
});

// 用户组列表
const groupList = ref<GetGroupListVo[]>([]);
const total = ref(0);
const loading = ref(false);

// 模态框相关
const dialogVisible = ref(false);
const formType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const groupFormRef = ref<FormInstance>();

// 表单数据
const groupForm = reactive<SaveGroupDto>({
  name: '',
  code: '',
  description: '',
  status: 1,
  sortOrder: 0,
  permissionIds: []
});

// 表单校验规则
const groupFormRules = {
  name: [
    { required: true, message: '请输入组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
};

// 在 script 部分添加
const isSystemGroup = ref(false);

// 权限列表
const permissionList = ref<GroupPermissionDefinitionVo[]>([]);

// 权限搜索
const permissionSearch = ref('');

// 过滤后的权限列表
const filteredPermissions = computed(() => {
  const search = permissionSearch.value.toLowerCase().trim();
  if (!search) {
    return permissionList.value;
  }
  return permissionList.value.filter(permission => 
    permission.name.toLowerCase().includes(search) || 
    permission.code.toLowerCase().includes(search)
  );
});

// 加载用户组列表数据
const loadGroupList = async () => {
  if (loading.value) {
    return;
  }

  try {
    loading.value = true;
    const vos = await GroupApi.getGroupList(queryForm);
    groupList.value = vos.rows;
    total.value = Number(vos.count);
  } catch (error) {
    ElMessage.error('加载用户组列表失败');
    console.error('加载用户组列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 重置查询条件
const resetQuery = () => {
  queryForm.keyword = '';
  queryForm.status = undefined;
  queryForm.page = 1;
  loadGroupList();
};

// 处理每页大小变化
const handleSizeChange = (val: number) => {
  queryForm.pageSize = val;
  loadGroupList();
};

// 处理页码变化
const handleCurrentChange = (val: number) => {
  queryForm.page = val;
  loadGroupList();
};

// 重置表单
const resetForm = () => {
  groupForm.id = undefined;
  groupForm.name = '';
  groupForm.description = '';
  groupForm.status = 1;
  groupForm.permissionIds = [];
  permissionSearch.value = '';
  
  if (groupFormRef.value) {
    groupFormRef.value.resetFields();
  }
};

// 处理新增用户组
const handleAdd = () => {
  formType.value = 'add';
  resetForm();
  isSystemGroup.value = false;
  dialogVisible.value = true;
};

// 处理编辑用户组
const handleEdit = async (row: GetGroupListVo) => {
  formType.value = 'edit';
  resetForm();
  isSystemGroup.value = row.isSystem;
  
  try {
    const details = await GroupApi.getGroupDetails({ id: row.id });
    groupForm.id = details.id;
    groupForm.code = details.code;
    groupForm.name = details.name;
    groupForm.description = details.description;
    groupForm.status = details.status;
    groupForm.sortOrder = details.sortOrder;
    
    // 设置权限列表
    permissionList.value = details.permissions;
    groupForm.permissionIds = details.permissions
      .filter(p => p.has === 0)
      .map(p => Number(p.id));
    
    dialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取用户组详情失败');
  }
};

// 提交表单
const submitForm = async () => {
  if (!groupFormRef.value) {
    return;
  }
  
  await groupFormRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    
    submitLoading.value = true;
    try {
      await GroupApi.saveGroup(groupForm);
      ElMessage.success(formType.value === 'add' ? '新增用户组成功' : '更新用户组成功');
      dialogVisible.value = false;
      loadGroupList();
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '操作失败';
      ElMessage.error(errorMsg);
    } finally {
      submitLoading.value = false;
    }
  });
};

// 处理删除用户组
const handleDelete = (row: GetGroupListVo) => {
  ElMessageBox.confirm(
    `确定要删除用户组 ${row.name} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const params: CommonIdDto = { id: row.id };
      await GroupApi.removeGroup(params);
      ElMessage.success('删除用户组成功');
      loadGroupList();
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '删除失败';
      ElMessage.error(errorMsg);
    }
  }).catch(() => {
    // 用户取消删除操作
  });
};

// 页面加载和窗口大小变化时检测设备类型
onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
  loadGroupList();
});
</script>

<style scoped>
.group-manager-container {
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

@media (max-width: 768px) {
  .query-form :deep(.el-form) {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .query-form :deep(.el-form-item) {
    margin-bottom: 10px;
    width: 100%;
  }
  
  .group-manager-container {
    padding: 10px;
  }
}

.group-table {
  margin-bottom: 20px;
  width: 100%;
  overflow-x: auto;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  width: 100%;
}

@media (min-width: 768px) {
  .pagination-container {
    justify-content: flex-end;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  width: 100%;
}

.permission-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.permission-search {
  padding: 0 0 10px 0;
}

.permission-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 10px;
}

.permission-item {
  margin: 8px 0;
  display: flex;
  align-items: center;
}

.permission-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.permission-name {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.permission-code {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-family: monospace;
}

.no-permission {
  padding: 20px 0;
}

:deep(.el-checkbox__label) {
  display: flex;
  align-items: flex-start;
}
</style>