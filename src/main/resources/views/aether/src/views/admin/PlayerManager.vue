<template>
  <div class="player-manager-container">
    <!-- 查询表单 -->
    <div class="query-form">
      <el-form :model="query" inline>
        <el-form-item label="玩家名称">
          <el-input
            v-model="query.playerName"
            placeholder="输入玩家名称查询"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="所属用户">
          <el-input
            v-model="query.username"
            placeholder="输入用户名查询"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="玩家状态">
          <el-select v-model="query.status" placeholder="选择状态" clearable style="width: 150px">
            <el-option label="正在使用" :value="0" />
            <el-option label="不活跃" :value="1" />
            <el-option label="等待删除" :value="2" />
            <el-option label="已删除" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 玩家列表 -->
    <div class="player-table">
      <el-table
        :data="list"
        stripe
        v-loading="loading"
      >
        <el-table-column
          prop="name"
          label="玩家名称"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="username"
          label="所属用户"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
            prop="balance"
            label="余额(CU)"
            min-width="100"
        />
        <el-table-column label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          min-width="160"
          :formatter="(row: any, column: any, cellValue: string) => formatDate(cellValue)"
        />
        <el-table-column label="操作" fixed="right" min-width="100">
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
            <!-- 不允许删除玩家 -->
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
          @size-change="(val: number) => { query.pageSize = val; loadList() }"
          @current-change="(val: number) => { query.page = val; loadList() }"
          background
        />
      </div>
    </div>

    <!-- 玩家编辑模态框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑玩家信息"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
       v-if="dialogVisible"
        ref="formRef"
        :model="editForm"
        :rules="rules"
        label-width="120px"
      >
        <!-- 只读信息 -->
        <el-form-item label="玩家ID">
          <el-input v-model="editForm.id" disabled />
        </el-form-item>
        <!-- 后端VO包含 name 和 username, 但编辑DTO不包含, 所以也设为只读 -->
        <el-form-item label="玩家名称">
          <el-input v-model="details.name" disabled />
        </el-form-item>
        <el-form-item label="所属用户">
          <el-input v-model="details.username" disabled />
        </el-form-item>
         <el-form-item label="余额(CU)">
           <el-input v-model="details.balance" disabled />
         </el-form-item>
         <el-form-item label="创建时间">
           <el-input :value="formatDate(details.createTime)" disabled />
         </el-form-item>
         <el-form-item label="最后激活时间">
           <el-input :value="formatDate(details.lastActiveTime)" disabled />
         </el-form-item>
         <el-form-item label="移除申请时间">
           <el-input :value="formatDate(details.removalRequestTime)" disabled />
         </el-form-item>
         <el-form-item label="实际移除时间">
           <el-input :value="formatDate(details.removedTime)" disabled />
         </el-form-item>

        <!-- 可编辑字段 -->
        <el-form-item label="头像URL" prop="avatarUrl">
          <el-input v-model="editForm.avatarUrl" placeholder="请输入头像URL" clearable/>
        </el-form-item>
        <el-form-item label="公开信息" prop="publicInfo">
          <el-input v-model="editForm.publicInfo" type="textarea" :rows="3" placeholder="请输入公开信息" />
        </el-form-item>
        <el-form-item label="语言" prop="language">
          <el-input v-model="editForm.language" placeholder="例如: 中文, English, zh-CN, en-US" clearable/>
        </el-form-item>
        <el-form-item label="年代" prop="era">
          <el-input v-model="editForm.era" placeholder="例如: 古代, 现代, 未来, 80S" clearable/>
        </el-form-item>
        <el-form-item label="内容过滤等级" prop="contentFilterLevel">
          <el-select v-model="editForm.contentFilterLevel" placeholder="选择过滤等级">
            <el-option label="不过滤" :value="0" />
            <el-option label="普通" :value="1" />
            <el-option label="严格" :value="2" />
          </el-select>
        </el-form-item>
         <el-form-item label="设置状态" prop="status">
          <el-select v-model="editForm.status" placeholder="选择状态 (仅限不活跃或已删除)">
            <el-option label="不活跃" :value="1" />
            <el-option label="已删除" :value="3" />
          </el-select>
           <el-tooltip content="后台只能将状态设置为不活跃(1)或已删除(3)" placement="top">
               <el-icon style="margin-left: 5px; cursor: help;"><QuestionFilled /></el-icon>
           </el-tooltip>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="edit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">

import {reactive, ref, onMounted, markRaw} from "vue";
import type {
  EditAdminPlayerDto,
  GetAdminPlayerDetailsVo,
  GetAdminPlayerListDto,
  GetAdminPlayerListVo
} from "@/commons/api/AdminPlayerApi.ts";
import AdminPlayerApi from "@/commons/api/AdminPlayerApi.ts"; // 引入API
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, QuestionFilled } from '@element-plus/icons-vue'; // 引入图标
import type { FormInstance } from 'element-plus';
import dayjs from 'dayjs'; // 引入dayjs用于日期格式化

const mode = ref<"insert" | "update">("update") // 后台管理只有编辑模式

const query = reactive<GetAdminPlayerListDto>({
  playerName: null,
  status: null,
  username: null,
  page: 1,
  pageSize: 10
})

const list = ref<GetAdminPlayerListVo[]>([])
const total = ref(0)
const loading = ref(false) // 列表加载状态

// 用于显示在模态框中的完整详情 (从API获取)
const details = reactive<GetAdminPlayerDetailsVo>({
  avatarUrl: "",
  balance: "",
  contentFilterLevel: 0,
  createTime: "",
  era: "",
  id: "",
  language: "",
  lastActiveTime: "",
  name: "",
  publicInfo: "",
  removalRequestTime: "",
  removedTime: "",
  status: 0,
  username: ""
})

// 编辑表单的类型，允许 status 为 undefined 以适配 el-select
interface PlayerEditForm extends Omit<EditAdminPlayerDto, 'status'> {
    status: number | undefined; // 覆盖 status 类型
}

// 用于提交编辑的表单数据 (只包含可编辑字段)
const editForm = reactive<PlayerEditForm>({
  id: "", // 会在打开编辑模态框时设置
  avatarUrl: "",
  publicInfo: "",
  language: "",
  era: "",
  contentFilterLevel: 0,
  status: undefined // 初始化为 undefined
})

// 模态框相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 图标
const EditIcon = markRaw(Edit);

// 表单校验规则 (根据 EditAdminPlayerDto 定义)
const rules = {
  avatarUrl: [
    { max: 320, message: '头像URL不能超过320个字符', trigger: 'blur' }
  ],
  publicInfo: [
     { max: 40000, message: '公开信息过长', trigger: 'blur' } // 与后端 PlayerPo 对应
  ],
  language: [
    { max: 24, message: '语言不能超过24个字符', trigger: 'blur' }
  ],
  era: [
    { max: 32, message: '年代不能超过32个字符', trigger: 'blur' }
  ],
   status: [
     // Service 层会校验是否为 1 或 3
   ]
}

// --- 页面元素辅助函数 ---

const formatDate = (dateString: string | null | undefined): string => {
  if (!dateString) return '-';
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss');
}

const formatStatus = (status: number): string => {
  switch (status) {
    case 0: return '正在使用';
    case 1: return '不活跃';
    case 2: return '等待删除';
    case 3: return '已删除';
    default: return '未知';
  }
}

const getStatusTagType = (status: number) => {
  switch (status) {
    case 0: return 'success'; // 正在使用
    case 1: return 'info';    // 不活跃
    case 2: return 'warning'; // 等待删除
    case 3: return 'danger';  // 已删除
    default: return 'info';
  }
}


// --- 核心逻辑函数 (真实API调用) ---

const loadList = async () => {
  console.log("尝试加载玩家列表:", query);
  loading.value = true;
  try {
    const res = await AdminPlayerApi.getPlayerList(query);
    list.value = res.rows;
    total.value = res.count;
    console.log("列表加载成功", list.value);
  } catch (e) {
     console.error("加载列表失败", e);
     ElMessage.error(e instanceof Error ? e.message : "加载玩家列表失败");
  } finally {
     loading.value = false;
  }
}

const resetQuery = async () => {
  console.log("重置查询条件");
  query.playerName = null;
  query.status = null; // 类型已修改，重置回 null
  query.username = null;
  query.page = 1;
  await loadList();
}

// 重置编辑表单 (在打开模态框时调用)
const resetEditForm = () => {
    editForm.id = "";
    editForm.avatarUrl = "";
    editForm.publicInfo = "";
    editForm.language = "";
    editForm.era = "";
    editForm.contentFilterLevel = 0;
    editForm.status = undefined; // 重置为 undefined

    if (formRef.value) {
        formRef.value.resetFields(); // 清除校验状态
    }
}

const openInsertModal = async () => {
    // 后台管理不提供新增玩家功能
    console.warn("此页面不支持新增玩家");
    ElMessage.warning("此页面不支持直接创建玩家");
}

const openUpdateModal = async (row: GetAdminPlayerListVo) => {
  console.log("打开编辑模态框，玩家ID:", row.id);
  mode.value = "update";
  resetEditForm(); // 先重置编辑表单
  try {
    // 1. 获取完整详情用于显示只读信息
    const detailsRes = await AdminPlayerApi.getPlayerDetails({ id: row.id });
    Object.assign(details, detailsRes);
    console.log("获取详情成功", details);

    // 2. 将可编辑字段填充到 editForm
    editForm.id = details.id;
    editForm.avatarUrl = details.avatarUrl;
    editForm.publicInfo = details.publicInfo;
    editForm.language = details.language;
    editForm.era = details.era;
    editForm.contentFilterLevel = details.contentFilterLevel;
    // 状态字段默认不填充，让用户选择是否修改，除非后端返回的是 1 或 3
    editForm.status = details.status === 1 || details.status === 3 ? details.status : undefined;


    dialogVisible.value = true;
  } catch (error) {
      console.error("获取玩家详情失败", error);
      ElMessage.error(error instanceof Error ? error.message : "获取玩家详情失败");
  }
}

const edit = async () => {
  console.log("尝试保存编辑:", editForm);
   if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
      if (!valid) {
          console.log("表单校验失败");
          return;
      }

      submitLoading.value = true;
      try {
           // 准备提交的数据，如果 status 是 undefined，则不应提交该字段
           const payload: EditAdminPlayerDto = { ...editForm };
           if (payload.status === undefined) {
               delete payload.status;
           }

           await AdminPlayerApi.editPlayer(payload);
           ElMessage.success("编辑玩家信息成功");
           dialogVisible.value = false;
           await loadList(); // 重新加载列表
      } catch (error) {
          console.error("编辑失败", error);
          const errorMsg = error instanceof Error ? error.message : '编辑玩家信息失败';
          ElMessage.error(errorMsg);
      } finally {
          submitLoading.value = false;
      }
  });
}

// 组件挂载后加载初始列表
onMounted(() => {
    loadList();
})

</script>

<style scoped>
.player-manager-container {
  padding: 20px;
   max-width: 100%;
   overflow-x: auto;
}

.query-form {
  margin-bottom: 20px;
}

.player-table {
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