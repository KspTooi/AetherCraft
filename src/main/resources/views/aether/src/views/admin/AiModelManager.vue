<template>
  <div class="container">
    <el-card class="box-card">
      
      <el-form :model="curModelConfig" label-position="top" :rules="rules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模型选择" prop="modelCode" required>
              <el-select v-model="curModelCode" class="w-full" placeholder="请选择模型" @change="loadModelConfig">
                <el-option 
                  v-for="model in modelList" 
                  :key="model.modelCode"
                  :label="model.modelName"
                  :value="model.modelCode"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="API Key" prop="currentApiKeyId" required>
              <el-select v-model="curModelConfig.currentApiKeyId" class="w-full" placeholder="请选择API Key">
                <el-option
                  v-for="key in curModelConfig.apiKeys"
                  :key="key.apiKeyId"
                  :label="key.ownerUsername ? `${key.keyName} (${key.keySeries}) 授权自${key.ownerUsername}` : `${key.keyName} (${key.keySeries})`"
                  :value="key.apiKeyId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="温度值" prop="temperature" required>
              <el-input-number 
                v-model="curModelConfig.temperature"
                :min="0"
                :max="2"
                :step="0.1"
                :precision="1"
                class="w-32"
              />
              <el-slider
                v-model="curModelConfig.temperature"
                :min="0"
                :max="2"
                :step="0.1"
                class="mt-2"
              />
              <div style="font-size: 0.75rem; color: #6B7280; margin-top: 0.25rem; line-height: 1.25;">
                控制响应的随机性和创造性。值越高，回答越多样化但可能偏离主题；值越低，回答越保守和确定。建议范围0.1-1.0，默认0.7
              </div>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="Top P" prop="topP" required>
              <el-input-number
                v-model="curModelConfig.topP"
                :min="0"
                :max="1"
                :step="0.1"
                :precision="1"
                class="w-32"
              />
              <el-slider
                v-model="curModelConfig.topP"
                :min="0"
                :max="1"
                :step="0.1"
                class="mt-2"
              />
              <div style="font-size: 0.75rem; color: #6B7280; margin-top: 0.25rem; line-height: 1.25;">
                核采样阈值，控制词汇选择的累积概率。较高的值(如0.9)会产生更多样的输出，较低的值(如0.1)会产生更保守的输出。建议配合温度值使用，默认1.0
              </div>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="Top K" prop="topK" required>
              <el-input-number
                v-model="curModelConfig.topK"
                :min="1"
                :max="100"
                :step="1"
                class="w-32"
              />
              <el-slider
                v-model="curModelConfig.topK"
                :min="1"
                :max="100"
                :step="1"
                class="mt-2"
              />
              <div style="font-size: 0.75rem; color: #6B7280; margin-top: 0.25rem; line-height: 1.25;">
                限制每次选词时考虑的候选词数量。较高的值会增加词汇的丰富度，较低的值会使输出更加集中。建议范围20-60，默认40
              </div>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="最大输出长度" prop="maxOutputTokens" required>
              <el-input-number
                v-model="curModelConfig.maxOutputTokens"
                :min="1"
                :max="8192000"
                :step="100"
                class="w-32"
              />
              <el-slider
                v-model="curModelConfig.maxOutputTokens"
                :min="1"
                :max="8192000"
                :step="100"
                class="mt-2"
              />
              <div style="font-size: 0.75rem; color: #6B7280; margin-top: 0.25rem; line-height: 1.25;">
                控制响应的最大长度，范围1-8192000，默认800，单次响应超过将自动截断
              </div>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="全局代理配置" prop="globalProxyConfig">
              <el-input 
                v-model="curModelConfig.globalProxyConfig"
                placeholder="http://global-proxy:7890"
              />
              <div style="font-size: 0.75rem; color: #6B7280; margin-top: 0.25rem; line-height: 1.25;">全局代理配置，适用于所有用户</div>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="用户代理配置" prop="userProxyConfig">
              <el-input
                v-model="curModelConfig.userProxyConfig"
                placeholder="http://user-proxy:7890"
              />
              <div style="font-size: 0.75rem; color: #6B7280; margin-top: 0.25rem; line-height: 1.25;">用户专属代理配置，仅对当前用户生效</div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <div class="flex justify-end gap-2 mt-4">
          <el-button type="info" @click="testConnection">
            <el-icon><Lightning /></el-icon>测试连接
          </el-button>
          <el-button type="primary" @click="handleSave">
            <el-icon><Check /></el-icon>保存配置
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import type { GetAdminModelConfigVo, GetAvailableModelVo } from "@/commons/api/ModelConfigApi.ts";
import ModelConfigApi from "@/commons/api/ModelConfigApi";
import { ElMessage, type FormInstance } from "element-plus";
import { Lightning, Check, InfoFilled } from '@element-plus/icons-vue'

//全部模型列表
const modelList = reactive<GetAvailableModelVo[]>([])

//当前选择的模型代码
const curModelCode = ref<string>("");

//表单引用
const formRef = ref<FormInstance>()

//表单校验规则
const rules = {
  temperature: [
    { required: true, message: '请设置温度值', trigger: 'blur' },
    { type: 'number', min: 0, max: 2, message: '温度值必须在0-2之间', trigger: 'blur' }
  ],
  topP: [
    { required: true, message: '请设置Top P值', trigger: 'blur' },
    { type: 'number', min: 0, max: 1, message: 'Top P必须在0-1之间', trigger: 'blur' }
  ],
  topK: [
    { required: true, message: '请设置Top K值', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: 'Top K必须在1-100之间', trigger: 'blur' }
  ],
  maxOutputTokens: [
    { required: true, message: '请设置最大输出长度', trigger: 'blur' },
    { type: 'number', min: 1, max: 8192000, message: '最大输出长度必须在1-8192000之间', trigger: 'blur' }
  ]
}

//当前选择的模型配置
const curModelConfig = reactive<GetAdminModelConfigVo>({
  apiKeys: [],
  currentApiKeyId: 0,
  globalProxyConfig: "",
  maxOutputTokens: 0,
  modelCode: "",
  modelName: "",
  temperature: 0,
  topK: 0,
  topP: 0,
  userProxyConfig: ""
})

// 加载状态
const loading = ref(false)

const loadModelList = async () => {
  try {
    loading.value = true
    const res = await ModelConfigApi.getAvailableModels()
    modelList.splice(0, modelList.length, ...res)
    
    // 如果没有选中的模型，默认选择第一个
    if (modelList.length > 0) {
      curModelCode.value = modelList[0].modelCode
      await loadModelConfig(curModelCode.value)
    }
  } catch (error) {
    ElMessage.error('加载模型列表失败')
    console.error('加载模型列表失败', error)
  } finally {
    loading.value = false
  }
}

//如果为null则加载modelList第一个模型的配置
const loadModelConfig = async (modelCode: string | null = null) => {
  try {
    loading.value = true
    
    // 确定要加载的模型代码
    const targetModelCode = modelCode || (modelList.length > 0 ? modelList[0].modelCode : null)
    if (!targetModelCode) {
      ElMessage.warning('没有可用的模型')
      return
    }

    curModelCode.value = targetModelCode
    const res = await ModelConfigApi.getModelConfig({modelCode: targetModelCode})
    Object.assign(curModelConfig, res)
    
    // 手动触发表单验证
    if (formRef.value) {
      formRef.value.validateField('modelCode')
    }
  } catch (error) {
    ElMessage.error('加载模型配置失败')
    console.error('加载模型配置失败', error)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        await ModelConfigApi.saveModelConfig({
          modelCode: curModelConfig.modelCode,
          apiKeyId: curModelConfig.currentApiKeyId,
          globalProxyConfig: curModelConfig.globalProxyConfig,
          userProxyConfig: curModelConfig.userProxyConfig,
          temperature: curModelConfig.temperature,
          topP: curModelConfig.topP,
          topK: curModelConfig.topK,
          maxOutputTokens: curModelConfig.maxOutputTokens
        })
        ElMessage.success('保存成功')
        await loadModelConfig(curModelCode.value)
      } catch (error) {
        ElMessage.error('保存失败')
        console.error('保存失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const testConnection = async () => {
  try {
    loading.value = true
    await ModelConfigApi.testModelConnection({
      modelCode: curModelCode.value
    })
    ElMessage.success('连接测试成功')
  } catch (error) {
    ElMessage.error('连接测试失败')
    console.error('连接测试失败', error)
  } finally {
    loading.value = false
  }
}

// 页面加载时自动加载数据
onMounted(() => {
  loadModelList()
})
</script>

<style scoped>

</style>