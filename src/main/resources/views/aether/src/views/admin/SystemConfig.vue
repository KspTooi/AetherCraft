<template>
  <div class="system-config">
    <h2>系统配置</h2>
    
    <div class="config-form">
      <div class="config-section">
        <h3>基本设置</h3>
        
        <div class="form-group">
          <label>系统名称</label>
          <input type="text" v-model="config.systemName" />
        </div>
        
        <div class="form-group">
          <label>站点URL</label>
          <input type="text" v-model="config.siteUrl" />
        </div>
        
        <div class="form-group">
          <label>管理员邮箱</label>
          <input type="email" v-model="config.adminEmail" />
        </div>
      </div>
      
      <div class="config-section">
        <h3>API设置</h3>
        
        <div class="form-group">
          <label>API密钥</label>
          <div class="api-key-group">
            <input type="password" v-model="config.apiKey" />
            <button @click="generateApiKey">生成</button>
          </div>
        </div>
        
        <div class="form-group">
          <label>API请求限制 (每分钟)</label>
          <input type="number" v-model="config.apiRateLimit" min="1" />
        </div>
        
        <div class="form-group">
          <label>API超时 (秒)</label>
          <input type="number" v-model="config.apiTimeout" min="1" />
        </div>
      </div>
      
      <div class="config-section">
        <h3>服务器设置</h3>
        
        <div class="form-group">
          <label>日志级别</label>
          <select v-model="config.logLevel">
            <option value="debug">调试</option>
            <option value="info">信息</option>
            <option value="warn">警告</option>
            <option value="error">错误</option>
          </select>
        </div>
        
        <div class="form-group">
          <label>最大上传文件大小 (MB)</label>
          <input type="number" v-model="config.maxUploadSize" min="1" />
        </div>
      </div>
    </div>
    
    <div class="form-actions">
      <button class="save-btn" @click="saveConfig">保存配置</button>
      <button class="reset-btn" @click="resetConfig">重置</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface SystemConfig {
  systemName: string
  siteUrl: string
  adminEmail: string
  apiKey: string
  apiRateLimit: number
  apiTimeout: number
  logLevel: string
  maxUploadSize: number
}

// 模拟配置数据
const config = ref<SystemConfig>({
  systemName: 'AI管理系统',
  siteUrl: 'https://example.com',
  adminEmail: 'admin@example.com',
  apiKey: 'sk-****************************************',
  apiRateLimit: 100,
  apiTimeout: 30,
  logLevel: 'info',
  maxUploadSize: 10
})

const generateApiKey = () => {
  // 实现生成API密钥的逻辑
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let key = 'sk-'
  for (let i = 0; i < 40; i++) {
    key += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  config.value.apiKey = key
}

const saveConfig = () => {
  // 实现保存配置的逻辑
  console.log('保存配置:', config.value)
  alert('配置已保存')
}

const resetConfig = () => {
  // 实现重置配置的逻辑
  if (confirm('确定要重置所有配置吗？')) {
    config.value = {
      systemName: 'AI管理系统',
      siteUrl: 'https://example.com',
      adminEmail: 'admin@example.com',
      apiKey: 'sk-****************************************',
      apiRateLimit: 100,
      apiTimeout: 30,
      logLevel: 'info',
      maxUploadSize: 10
    }
  }
}
</script>

<style scoped>
.system-config {
  padding: 20px;
}

.config-form {
  margin-top: 20px;
}

.config-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

input, select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

select {
  background-color: white;
}

.api-key-group {
  display: flex;
  gap: 10px;
}

.api-key-group input {
  flex: 1;
}

.form-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.save-btn, .reset-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.save-btn {
  background-color: #2ecc71;
  color: white;
}

.reset-btn {
  background-color: #e74c3c;
  color: white;
}
</style> 