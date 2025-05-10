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
        <el-table-column
            prop="groupCount"
            label="访问组数"
            min-width="90"
            align="center"
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
              type="success"
              size="small"
              @click="openGroupModal(scope.row)"
              :icon="GroupIcon"
            >
              访问组
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

    <!-- 玩家编辑/管理访问组模态框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="mode === 'update' ? '编辑玩家信息' : '管理访问组'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
       v-if="dialogVisible"
        ref="formRef"
        :model="editForm"
        :rules="mode === 'update' ? rules : {}"
        label-width="120px"
      >
        <!-- --- 编辑模式 --- -->
        <template v-if="mode === 'update'">
          <!-- 只读信息 -->
          <el-form-item label="玩家ID">
            <el-input v-model="editForm.id" disabled />
          </el-form-item>
          <el-form-item label="玩家名称">
            <el-input v-model="details.name" disabled />
          </el-form-item>
          <el-form-item label="所属用户">
            <el-input v-model="details.username" disabled />
          </el-form-item>
          <el-form-item label="余额(CU)">
            <el-input v-model="details.balance" disabled />
          </el-form-item>
          <el-form-item label="玩家性别">
            <el-input :value="formatGender(details.gender)" disabled />
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
          <el-form-item label="设置性别" prop="gender">
            <el-select v-model="editForm.gender" placeholder="选择性别 (仅限男/女/不愿透露)">
              <el-option label="男" :value="0" />
              <el-option label="女" :value="1" />
              <el-option label="不愿透露" :value="2" />
            </el-select>
            <el-tooltip content="管理后台只能将性别设置为男(0), 女(1)或不愿透露(2)" placement="top">
                <el-icon style="margin-left: 5px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
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
        </template>

        <!-- --- 管理访问组模式 --- -->
        <template v-if="mode === 'group'">
            <el-form-item label="玩家名称">
                <el-input :value="details.name" disabled />
            </el-form-item>
            <el-form-item label="所属访问组" prop="groupIds">
                 <div class="group-checkbox-container">
                    <el-checkbox-group v-model="editForm.groupIds" class="group-checkbox-group">
                        <el-checkbox
                            v-for="group in allGroups"
                            :key="group.id"
                            :value="group.id"
                            class="group-checkbox-item"
                        >
                           {{ group.name }}
                        </el-checkbox>
                    </el-checkbox-group>
                     <div v-if="!allGroups || allGroups.length === 0" class="no-groups">
                        <el-text type="info">暂无访问组定义</el-text>
                    </div>
                </div>
            </el-form-item>
        </template>

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
import AdminPlayerApi from "@/commons/api/AdminPlayerApi.ts";
import AdminGroupApi, {type GetGroupDefinitionsVo} from "@/commons/api/AdminGroupApi.ts"; // 引入组API
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, QuestionFilled, User as UserIcon } from '@element-plus/icons-vue'; // 重命名导入的图标为 UserIcon
import type { FormInstance } from 'element-plus';
import dayjs from 'dayjs';

const mode = ref<"update" | "group">("update") // 修改 mode 类型

const query = reactive<GetAdminPlayerListDto>({
  playerName: null,
  status: null,
  username: null,
  page: 1,
  pageSize: 10
})

const list = ref<GetAdminPlayerListVo[]>([])
const total = ref(0)
const loading = ref(false)

// 详情数据
const details = reactive<GetAdminPlayerDetailsVo>({
  avatarUrl: "",
  balance: "",
  contentFilterLevel: 0,
  createTime: "",
  era: "",
  gender: undefined, // 添加 gender 初始化
  id: "",
  language: "",
  lastActiveTime: "",
  name: "",
  publicInfo: "",
  removalRequestTime: "",
  removedTime: "",
  status: 0,
  username: "",
  groupIds: [] // 添加 groupIds 初始化
})

// 编辑表单接口，包含 groupIds
interface PlayerEditForm extends Omit<EditAdminPlayerDto, 'status' | 'groupIds' | 'gender'> {
    status: number | undefined;
    gender: number | undefined; // 添加 gender
    groupIds: string[]; // 添加 groupIds
}

// 编辑表单数据
const editForm = reactive<PlayerEditForm>({
  id: "",
  avatarUrl: "",
  publicInfo: "",
  language: "",
  era: "",
  contentFilterLevel: 0,
  status: undefined,
  gender: undefined, // 添加 gender 初始化
  groupIds: [] // 添加 groupIds 初始化
})

// 所有访问组定义
const allGroups = ref<GetGroupDefinitionsVo[]>([])

// 模态框相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 图标
const EditIcon = markRaw(Edit);
const GroupIcon = markRaw(UserIcon); // 使用导入并重命名的 UserIcon

// 表单校验规则 (只在 update 模式下应用)
const rules = {
  avatarUrl: [ { max: 320, message: '头像URL不能超过320个字符', trigger: 'blur' } ],
  publicInfo: [ { max: 40000, message: '公开信息过长', trigger: 'blur' } ],
  language: [ { max: 24, message: '语言不能超过24个字符', trigger: 'blur' } ],
  era: [ { max: 32, message: '年代不能超过32个字符', trigger: 'blur' } ],
  gender: [], // 性别由下拉框约束
  status: [], // 状态由下拉框约束
  groupIds: [] // 访问组不需要校验规则
}

// --- 页面元素辅助函数 ---
const formatDate = (dateString: string | null | undefined): string => {
  if (!dateString) return '-';
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss');
}
const formatStatus = (status: number): string => {
  switch (status) {
    case 0: return '正在使用'; case 1: return '不活跃'; case 2: return '等待删除'; case 3: return '已删除'; default: return '未知';
  }
}
const getStatusTagType = (status: number) => {
  switch (status) {
    case 0: return 'success'; case 1: return 'info'; case 2: return 'warning'; case 3: return 'danger'; default: return 'info';
  }
}
const formatGender = (gender: number | undefined): string => {
    if (gender === undefined) return '-';
    switch (gender) {
        case 0: return '男';
        case 1: return '女';
        case 2: return '不愿透露';
        case 4: return '自定义(男性)';
        case 5: return '自定义(女性)';
        case 6: return '自定义(其他)';
        default: return `未知 (${gender})`;
    }
}

// --- 核心逻辑函数 ---

// 加载所有访问组定义
const loadAllGroups = async () => {
    try {
        allGroups.value = await AdminGroupApi.getGroupDefinitions();
        console.log("访问组定义加载成功", allGroups.value);
    } catch (e) {
        console.error("加载访问组定义失败", e);
        ElMessage.error("加载访问组定义失败");
    }
}

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
  query.playerName = null; query.status = null; query.username = null; query.page = 1;
  await loadList();
}

// 重置编辑表单
const resetEditForm = () => {
    editForm.id = ""; editForm.avatarUrl = ""; editForm.publicInfo = "";
    editForm.language = ""; editForm.era = ""; editForm.contentFilterLevel = 0;
    editForm.status = undefined; editForm.gender = undefined; editForm.groupIds = []; // 重置 groupIds

    if (formRef.value) { formRef.value.resetFields(); }
}

const openInsertModal = async () => { /* 不支持 */ ElMessage.warning("此页面不支持直接创建玩家"); }

// 打开编辑模态框
const openUpdateModal = async (row: GetAdminPlayerListVo) => {
  console.log("打开编辑模态框，玩家ID:", row.id);
  mode.value = "update";
  resetEditForm();
  try {
    const detailsRes = await AdminPlayerApi.getPlayerDetails({ id: row.id });
    Object.assign(details, detailsRes); // 填充只读详情
    console.log("获取详情成功(编辑)", details);

    // 填充可编辑表单
    editForm.id = details.id;
    editForm.avatarUrl = details.avatarUrl || ""; // 处理 undefined
    editForm.publicInfo = details.publicInfo || "";
    editForm.language = details.language || "";
    editForm.era = details.era || "";
    editForm.contentFilterLevel = details.contentFilterLevel === undefined ? 0 : details.contentFilterLevel;
    editForm.status = details.status === 1 || details.status === 3 ? details.status : undefined;
    editForm.gender = details.gender === 0 || details.gender === 1 || details.gender === 2 ? details.gender : undefined;
    editForm.groupIds = details.groupIds || []; // 填充 groupIds

    dialogVisible.value = true;
  } catch (error) {
      console.error("获取玩家详情失败", error);
      ElMessage.error(error instanceof Error ? error.message : "获取玩家详情失败");
  }
}

// 打开管理访问组模态框
const openGroupModal = async (row: GetAdminPlayerListVo) => {
    console.log("打开访问组管理模态框，玩家ID:", row.id);
    mode.value = "group";
    resetEditForm();
    try {
        // 同样需要获取详情以获得当前 groupIds 和玩家名称
        const detailsRes = await AdminPlayerApi.getPlayerDetails({ id: row.id });
        Object.assign(details, detailsRes); // 主要为了 details.name
        console.log("获取详情成功(访问组)", details);

        // 填充表单 (只需要 id 和 groupIds)
        editForm.id = details.id;
        editForm.groupIds = details.groupIds || [];

        // 确保所有组定义已加载
        if (allGroups.value.length === 0) {
            await loadAllGroups();
        }

        dialogVisible.value = true;
    } catch (error) {
        console.error("获取玩家详情失败(访问组)", error);
        ElMessage.error(error instanceof Error ? error.message : "获取玩家详情失败");
    }
}


const edit = async () => {
   if (!formRef.value) return;

   // 根据模式决定是否需要校验以及校验规则
   const needsValidation = mode.value === 'update';
   const currentRules = needsValidation ? rules : {};

   // 手动触发校验（如果需要）
   let valid = true;
   if (needsValidation) {
       valid = await formRef.value.validate().catch(() => false);
   }

  if (!valid) {
      console.log("表单校验失败");
      return;
  }

  submitLoading.value = true;
  try {
       // 构建最终提交的 payload，确保包含所有 EditAdminPlayerDto 定义的字段
       const payload: EditAdminPlayerDto = {
           id: editForm.id, // ID 始终从 editForm 获取 (在打开 modal 时已设置)
           avatarUrl: mode.value === 'update' ? editForm.avatarUrl : details.avatarUrl,
           gender: mode.value === 'update' ? editForm.gender : details.gender,
           publicInfo: mode.value === 'update' ? editForm.publicInfo : details.publicInfo,
           language: mode.value === 'update' ? editForm.language : details.language,
           era: mode.value === 'update' ? editForm.era : details.era,
           contentFilterLevel: mode.value === 'update' ? editForm.contentFilterLevel : details.contentFilterLevel,
           status: mode.value === 'update'
                     ? (editForm.status !== undefined ? editForm.status : details.status)
                     : details.status,
           groupIds: mode.value === 'update' ? details.groupIds : editForm.groupIds
       };
        // 如果 gender 是 undefined，从 payload 中删除它，避免发送 null
       if (payload.gender === undefined) {
           delete payload.gender;
       }

       console.log(`尝试保存 (${mode.value}模式):`, payload);
       await AdminPlayerApi.editPlayer(payload); // API 需要完整的 DTO
       ElMessage.success(mode.value === 'update' ? "编辑玩家信息成功" : "更新访问组成功");
       dialogVisible.value = false;
       // 刷新列表以看到更改（即使只是改了组也刷新）
       await loadList();
  } catch (error) {
      console.error("操作失败", error);
      const errorMsg = error instanceof Error ? error.message : '操作失败';
      ElMessage.error(errorMsg);
  } finally {
      submitLoading.value = false;
  }
}

// 组件挂载后加载
onMounted(() => {
    loadList();
    loadAllGroups(); // 加载所有组定义
})

</script>

<style scoped>
.player-manager-container { padding: 20px; max-width: 100%; overflow-x: auto; }
.query-form { margin-bottom: 20px; }
.player-table { margin-bottom: 20px; width: 100%; overflow-x: auto; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 20px; width: 100%; }

/* 新增样式 */
.group-checkbox-container {
  border: 1px solid var(--el-border-color);
  border-radius: var(--el-border-radius-base);
  padding: 10px;
  max-height: 250px; /* 根据需要调整最大高度 */
  overflow-y: auto;
  width: 100%;
}

.group-checkbox-group {
  display: flex;
  flex-direction: column; /* 每个 checkbox 占一行 */
}

.group-checkbox-item {
  margin-bottom: 8px; /* 增加 checkbox 之间的间距 */
   height: auto; /* 允许自动高度 */
   line-height: 1.5; /* 调整行高 */
   display: flex; /* 确保对齐 */
   align-items: center; /* 垂直居中 */
}

.no-groups {
    text-align: center;
    padding: 20px;
    color: var(--el-text-color-secondary);
}

/* 微调 checkbox 标签和内容的间距 */
:deep(.group-checkbox-item .el-checkbox__label) {
    padding-left: 8px;
}

</style>