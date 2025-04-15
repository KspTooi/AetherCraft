<template>
  <div class="user-manager-container">
    <div class="query-form">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="输入用户名查询" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUserList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="add-button-container">
        <el-button type="success" @click="handleAdd">新增用户</el-button>
      </div>
    </div>

    <div class="user-table">
      <el-table 
        :data="userList" 
        border 
        stripe
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="nickname" label="昵称" min-width="150" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column prop="lastLoginTime" label="最后登录时间" min-width="180" />
        <el-table-column label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
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
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <!-- 桌面端分页 -->
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
        <!-- 移动端分页 -->
        <el-pagination
          v-else
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.pageSize"
          layout="prev, pager, next"
          :total="total"
          @current-change="handleCurrentChange"
          :pager-count="5"
          small
          background
        />
      </div>
    </div>

    <!-- 用户编辑/新增模态框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="formType === 'edit'" />
        </el-form-item>
        <el-form-item 
          label="密码" 
          prop="password"
          :rules="formType === 'add' ? userFormRules.password : []"
        >
          <el-input v-model="userForm.password" type="password" show-password />
          <div v-if="formType === 'edit'" class="form-tip">不修改密码请留空</div>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属用户组" prop="groupIds">
          <el-select
            v-model="userForm.groupIds"
            multiple
            placeholder="请选择用户组"
            style="width: 100%"
          >
            <el-option
              v-for="group in groupOptions"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
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
import { Edit, Delete } from '@element-plus/icons-vue';
import Http from '@/commons/Http';
import type GetUserListDto from '@/entity/dto/GetUserListDto.ts';
import type GetUserListVo from '@/entity/vo/GetUserListVo.ts';
import type SaveUserDto from '@/entity/dto/SaveUserDto.ts';
import type CommonIdDto from '@/entity/dto/CommonIdDto';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance } from 'element-plus';
import UserApi from "@/commons/api/UserApi.ts";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 检测是否为移动设备
const isMobile = ref(false);
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768;
};

// 查询表单
const queryForm = reactive<GetUserListDto>({
  page: 1,
  pageSize: 10,
  username: ''
});

// 用户列表
const userList = ref<GetUserListVo[]>([]);
const total = ref(0);
const loading = ref(false);

// 模态框相关
const dialogVisible = ref(false);
const formType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const userFormRef = ref<FormInstance>();

// 表单数据
const userForm = reactive<SaveUserDto>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  status: 1,
  groupIds: []
});

// 表单校验规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '用户名只能包含4-20位字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
};

// 模拟的用户组数据，实际应该从接口获取
const groupOptions = ref([
  { id: '1', name: '管理员组' },
  { id: '2', name: '普通用户组' },
  { id: '3', name: '访客组' }
]);

// 加载用户列表数据
const loadUserList = async () => {

  try{

    loading.value = true;
    let vos = await UserApi.getUserList(queryForm);
    userList.value = vos.rows;
    total.value = Number(vos.count);

  } catch (error) {
    ElMessage.error('加载用户列表失败');
    console.error('加载用户列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 重置查询条件
const resetQuery = () => {
  queryForm.username = '';
  queryForm.page = 1;
  loadUserList();
};

// 处理每页大小变化
const handleSizeChange = (val: number) => {
  queryForm.pageSize = val;
  loadUserList();
};

// 处理页码变化
const handleCurrentChange = (val: number) => {
  queryForm.page = val;
  loadUserList();
};

// 重置表单
const resetForm = () => {
  userForm.id = undefined;
  userForm.username = '';
  userForm.password = '';
  userForm.nickname = '';
  userForm.email = '';
  userForm.status = 1;
  userForm.groupIds = [];
  
  if (userFormRef.value) {
    userFormRef.value.resetFields();
  }
};

// 处理新增用户
const handleAdd = () => {
  formType.value = 'add';
  resetForm();
  dialogVisible.value = true;
};

// 处理编辑用户
const handleEdit = (row: GetUserListVo) => {
  formType.value = 'edit';
  resetForm();
  
  // 填充表单
  userForm.id = row.id;
  userForm.username = row.username;
  userForm.nickname = row.nickname || '';
  userForm.email = row.email || '';
  userForm.status = row.status || 1;
  
  // 这里应该从后端获取用户的分组信息
  // 模拟数据
  userForm.groupIds = ['1'];
  
  dialogVisible.value = true;
};

// 提交表单
const submitForm = async () => {
  if (!userFormRef.value) return;
  
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitLoading.value = true;
    try {
      await Http.postEntity<string>('/admin/user/saveUser', userForm);
      ElMessage.success(formType.value === 'add' ? '新增用户成功' : '更新用户成功');
      dialogVisible.value = false;
      loadUserList(); // 重新加载列表
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '操作失败';
      ElMessage.error(errorMsg);
    } finally {
      submitLoading.value = false;
    }
  });
};

// 处理删除用户
const handleDelete = (row: GetUserListVo) => {
  ElMessageBox.confirm(
    `确定要删除用户 ${row.username} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const params: CommonIdDto = { id: row.id };
      await Http.postEntity<string>('/admin/user/removeUser', params);
      ElMessage.success('删除用户成功');
      loadUserList(); // 重新加载列表
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
  loadUserList();
  
  // TODO: 加载用户组数据
  // loadGroupOptions();
});
</script>

<style scoped>
.user-manager-container {
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
  
  .user-manager-container {
    padding: 10px;
  }
}

.user-table {
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>