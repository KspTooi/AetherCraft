<template>
  <div class="api-key-authorization-container">
    <!-- 查询表单 -->
    <div class="query-form">
      <div class="back-button-container">
        <el-button @click="goBackToApiKeyManager" icon="Back">返回密钥管理</el-button>
      </div>
      <el-form :model="query" inline>
        <el-form-item label="被授权人物">
          <el-input 
            v-model="query.authorizedPlayerName" 
            placeholder="输入被授权人物名称" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="add-button-container">
        <el-button type="success" @click="openInsertModal">新增授权</el-button>
      </div>
    </div>

    <!-- 授权列表 -->
    <div class="authorization-table">
      <el-table
        :data="list"
        stripe
        v-loading="loading"
      >
        <el-table-column 
          prop="authorizedPlayerName" 
          label="被授权人物名称" 
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column 
          prop="usageCount" 
          label="已使用次数" 
          min-width="120"
        />
        <el-table-column 
          prop="usageLimit" 
          label="使用次数限制" 
          min-width="120"
        >
          <template #default="scope">
            {{ scope.row.usageLimit === null ? '无限制' : scope.row.usageLimit }}
          </template>
        </el-table-column>
        <el-table-column 
          prop="createTime" 
          label="创建时间" 
          min-width="160"
        />
        <el-table-column 
          prop="expireTime" 
          label="过期时间" 
          min-width="160"
        >
          <template #default="scope">
            {{ scope.row.expireTime === null || scope.row.expireTime === '' ? '无固定期限' : scope.row.expireTime }}
          </template>
        </el-table-column>
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
            <div class="action-buttons">
              <el-button 
                link
                type="primary" 
                size="small" 
                @click="openUpdateModal(scope.row)"
              >
                编辑
              </el-button>
              <el-button 
                link
                type="danger" 
                size="small" 
                @click="remove(scope.row)"
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
            loadList()
          }"
          @current-change="(val: number) => {
            query.page = val
            loadList()
          }"
          background
        />
      </div>
    </div>

    <!-- 授权编辑/新增模态框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="mode === 'insert' ? '新增API密钥授权' : '编辑API密钥授权'"
      width="500px"
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
        <el-form-item label="人物名称" prop="authorizedPlayerName">
          <el-input 
            v-model="details.authorizedPlayerName" 
            :disabled="mode === 'update'"
            placeholder="请输入被授权人物名称"
          />
        </el-form-item>
        <el-form-item label="使用次数限制" prop="usageLimit">
          <el-input-number 
            v-model="details.usageLimit" 
            :min="0"
            :precision="0"
            :controls="false"
            placeholder="留空表示无限制"
            clearable
          />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireTime">
          <el-date-picker
            v-model="details.expireTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="留空表示无固定期限"
            clearable
            style="width: 100%"
          />
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
          <el-button type="primary" @click="save" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRoute, useRouter } from 'vue-router';
import type { 
  GetApiKeyAuthorizationDetailsVo, 
  GetApiKeyAuthorizationListDto, 
  GetApiKeyAuthorizationListVo,
  SaveApiKeyAuthorizationDto
} from "@/commons/api/AdminApiKeyApi.ts";
import ApiKeyApi from "@/commons/api/AdminApiKeyApi.ts";
import type { FormInstance } from 'element-plus';
import { ElMessage, ElMessageBox } from "element-plus";

const router = useRouter();
const route = useRoute();

const mode = ref<"insert" | "update">("insert");

const query = reactive<GetApiKeyAuthorizationListDto>({
  apiKeyId: route.query.apiKeyId as string || "",
  authorizedPlayerName: null,
  page: 1,
  pageSize: 10
});

const list = ref<GetApiKeyAuthorizationListVo[]>([]);
const total = ref(0);
const loading = ref(false);
const dialogVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

// 表单数据
const details = reactive<GetApiKeyAuthorizationDetailsVo & { apiKeyId?: string }>({
  id: "",
  apiKeyId: query.apiKeyId,
  authorizedPlayerName: "",
  usageLimit: null,
  usageCount: "",
  expireTime: "",
  createTime: "",
  status: 1
});

// 表单校验规则
const rules = {
  authorizedPlayerName: [
    { 
      validator: (rule: any, value: string, callback: Function) => {
        if (mode.value === 'update') {
          callback(); // 编辑模式下跳过验证
          return;
        }
        
        if (!value) {
          callback(new Error('请输入授权人物名'));
          return;
        }
        
        if (value.length < 2 || value.length > 50) {
          callback(new Error('长度在 2 到 50 个字符'));
          return;
        }
        
        callback();
      }, 
      trigger: 'blur'
    }
  ],
  usageLimit: [
    { 
      validator: (rule: any, value: number | null, callback: Function) => {
        if (value === null) {
          callback(); // 允许为 null
          return;
        }
        
        if (typeof value !== 'number' || value < 0) {
          callback(new Error('使用次数限制必须为非负数'));
          return;
        }
        
        callback();
      }, 
      trigger: 'blur'
    }
  ],
  expireTime: [
    { 
      validator: (rule: any, value: string, callback: Function) => {
        // 编辑模式下允许留空
        if ((mode.value === 'update' || mode.value === "insert") && (value === '' || value === null)) {
          callback();
          return;
        }
        
        // 有值时进行格式验证
        if (value === '') {
          callback(new Error('请选择过期时间'));
          return;
        }
        
        // 正则表达式验证 yyyy-MM-dd HH:mm:ss 格式
        const dateRegex = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
        if (!dateRegex.test(value)) {
          callback(new Error('请使用 yyyy-MM-dd HH:mm:ss 格式'));
        } else {
          callback();
        }
      }, 
      trigger: 'blur'
    }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

// 加载授权列表
const loadList = async () => {
  loading.value = true;
  try {
    const res = await ApiKeyApi.getAuthorizationList(query);
    list.value = res.rows;
    total.value = res.count;
  } catch (e) {
    ElMessage.error("加载授权列表失败");
  } finally {
    loading.value = false;
  }
};

// 重置查询条件
const resetQuery = () => {
  query.authorizedPlayerName = null;
  query.page = 1;
  query.pageSize = 10;
  loadList();
};

// 重置表单
const resetForm = () => {
  details.id = "";
  details.authorizedPlayerName = "";
  details.usageLimit = null;
  details.expireTime = "";
  details.status = 1;
  details.usageCount = "";
  details.createTime = "";
  
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 打开新增模态框
const openInsertModal = () => {
  mode.value = "insert";
  resetForm();
  dialogVisible.value = true;
};

// 打开编辑模态框
const openUpdateModal = async (row: GetApiKeyAuthorizationListVo) => {
  try {
    mode.value = "update";
    resetForm();
    
    const res = await ApiKeyApi.getApiKeyAuthorizationDetails({ id: row.id });
    Object.assign(details, res);
    
    dialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取授权详情失败');
  }
};

// 保存（新增/编辑）
const save = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitLoading.value = true;
    try {
      const dto: SaveApiKeyAuthorizationDto = {
        id: mode.value === 'update' ? details.id : null,
        apiKeyId: query.apiKeyId,
        authorizedPlayerName: details.authorizedPlayerName,
        usageLimit: details.usageLimit,
        expireTime: details.expireTime,
        status: details.status
      };
      
      await ApiKeyApi.saveAuth(dto);
      
      ElMessage.success(mode.value === 'insert' ? '新增授权成功' : '更新授权成功');
      dialogVisible.value = false;
      loadList();
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '操作失败';
      ElMessage.error(errorMsg);
    } finally {
      submitLoading.value = false;
    }
  });
};

// 删除授权
const remove = async (row: GetApiKeyAuthorizationListVo) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除对 ${row.authorizedPlayerName} 的授权吗？人物授权将被删除且无法恢复。`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await ApiKeyApi.removeAuthorization({ id: row.id });
    ElMessage.success('移除授权成功');
    loadList();
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error instanceof Error ? error.message : '删除失败';
      ElMessage.error(errorMsg);
    }
  }
};

// 返回 API 密钥管理页面
const goBackToApiKeyManager = () => {
  // 获取路由中的所有原始查询参数
  const { apiKeyId, ...originalQuery } = route.query;
  
  router.replace({ 
    name: 'api-key-manager',
    query: originalQuery
  });
};

// 页面加载时自动校验参数并加载数据
onMounted(() => {
  // 从路由参数中获取 apiKeyId
  const routeQuery = route.query;
  
  // 必须有 apiKeyId
  if (!routeQuery.apiKeyId || typeof routeQuery.apiKeyId !== "string" || routeQuery.apiKeyId.trim() === "") {
    ElMessage.warning("参数无效，已返回密钥管理页");
    router.replace({ name: "api-key-manager" });
    return;
  }
  
  // 仅设置 apiKeyId
  query.apiKeyId = routeQuery.apiKeyId as string;
  query.authorizedPlayerName = null;
  query.page = 1;
  query.pageSize = 10;
  
  loadList();
});
</script>

<style scoped>
.api-key-authorization-container {
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

.authorization-table {
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

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-button-container {
  margin-bottom: 20px;
}
</style>