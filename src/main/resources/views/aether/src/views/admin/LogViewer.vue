<template>
  <div class="log-viewer">
    <h2>系统日志</h2>
    
    <div class="log-controls">
      <div class="log-filter">
        <select v-model="selectedLogType">
          <option value="all">所有日志</option>
          <option value="system">系统日志</option>
          <option value="api">API日志</option>
          <option value="error">错误日志</option>
          <option value="user">用户操作日志</option>
        </select>
        
        <select v-model="selectedLogLevel">
          <option value="all">所有级别</option>
          <option value="debug">Debug</option>
          <option value="info">Info</option>
          <option value="warn">Warning</option>
          <option value="error">Error</option>
        </select>
        
        <input type="text" v-model="searchTerm" placeholder="搜索日志内容..." />
      </div>
      
      <div class="log-actions">
        <button @click="refreshLogs">刷新</button>
        <button @click="downloadLogs">下载日志</button>
        <button class="clear-btn" @click="confirmClearLogs">清除日志</button>
      </div>
    </div>
    
    <div class="log-content">
      <div v-if="loading" class="loading">
        加载日志中...
      </div>
      <div v-else-if="logs.length === 0" class="empty-logs">
        没有找到符合条件的日志
      </div>
      <div v-else class="log-entries">
        <div 
          v-for="(log, index) in logs" 
          :key="index" 
          class="log-entry"
          :class="'log-level-' + log.level"
        >
          <div class="log-time">{{ log.timestamp }}</div>
          <div class="log-level">{{ log.level.toUpperCase() }}</div>
          <div class="log-source">{{ log.source }}</div>
          <div class="log-message">{{ log.message }}</div>
        </div>
      </div>
    </div>
    
    <div class="log-pagination">
      <button 
        :disabled="currentPage === 1" 
        @click="currentPage--"
      >
        上一页
      </button>
      <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
      <button 
        :disabled="currentPage === totalPages" 
        @click="currentPage++"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'

interface LogEntry {
  timestamp: string
  level: string
  source: string
  message: string
}

const loading = ref(true)
const selectedLogType = ref('all')
const selectedLogLevel = ref('all')
const searchTerm = ref('')
const currentPage = ref(1)
const itemsPerPage = 20

// 模拟日志数据
const allLogs = ref<LogEntry[]>([
  { timestamp: '2023-07-15 08:12:34', level: 'info', source: 'system', message: '系统启动' },
  { timestamp: '2023-07-15 08:14:22', level: 'debug', source: 'api', message: '接收到API请求: GET /api/users' },
  { timestamp: '2023-07-15 09:23:45', level: 'warn', source: 'system', message: '磁盘空间不足 (剩余: 512MB)' },
  { timestamp: '2023-07-15 10:04:11', level: 'error', source: 'api', message: '数据库连接错误: Connection timeout' },
  { timestamp: '2023-07-15 10:45:30', level: 'info', source: 'user', message: '用户登录: admin (127.0.0.1)' },
])

// 生成更多模拟日志
for (let i = 0; i < 50; i++) {
  const levels = ['debug', 'info', 'warn', 'error']
  const sources = ['system', 'api', 'user', 'database']
  const level = levels[Math.floor(Math.random() * levels.length)]
  const source = sources[Math.floor(Math.random() * sources.length)]
  
  let message = ''
  if (source === 'system') {
    message = ['系统资源监控', '定时任务执行', '配置更新'][Math.floor(Math.random() * 3)]
  }
  if (source === 'api') {
    message = `API请求: ${['GET', 'POST', 'PUT', 'DELETE'][Math.floor(Math.random() * 4)]} /api/${['users', 'roles', 'config'][Math.floor(Math.random() * 3)]}`
  }
  if (source === 'user') {
    message = `用户${['登录', '登出', '操作'][Math.floor(Math.random() * 3)]}: user${Math.floor(Math.random() * 10) + 1}`
  }
  if (source === 'database') {
    message = `数据库${['查询', '更新', '备份'][Math.floor(Math.random() * 3)]}`
  }
  
  const date = new Date()
  date.setMinutes(date.getMinutes() - i * 10)
  const timestamp = date.toLocaleString()
  
  allLogs.value.push({
    timestamp,
    level,
    source,
    message: message
  })
}

// 过滤日志
const filteredLogs = computed(() => {
  return allLogs.value.filter(log => {
    let matchesType = selectedLogType.value === 'all' || log.source === selectedLogType.value
    let matchesLevel = selectedLogLevel.value === 'all' || log.level === selectedLogLevel.value
    let matchesSearch = !searchTerm.value || 
      log.message.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
      log.source.toLowerCase().includes(searchTerm.value.toLowerCase())
    
    return matchesType && matchesLevel && matchesSearch
  })
})

// 分页处理
const totalPages = computed(() => {
  return Math.ceil(filteredLogs.value.length / itemsPerPage)
})

const logs = computed(() => {
  const startIndex = (currentPage.value - 1) * itemsPerPage
  const endIndex = startIndex + itemsPerPage
  return filteredLogs.value.slice(startIndex, endIndex)
})

// 当过滤条件变化时，重置页码
watch([selectedLogType, selectedLogLevel, searchTerm], () => {
  currentPage.value = 1
})

// 刷新日志
const refreshLogs = () => {
  loading.value = true
  // 模拟加载延迟
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 下载日志
const downloadLogs = () => {
  alert('日志下载功能将在后续实现')
}

// 清除日志
const confirmClearLogs = () => {
  if (confirm('确定要清除所有日志吗？此操作不可撤销！')) {
    allLogs.value = []
  }
}

onMounted(() => {
  // 模拟加载延迟
  setTimeout(() => {
    loading.value = false
  }, 800)
})
</script>

<style scoped>
.log-viewer {
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.log-controls {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.log-filter {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.log-filter select, .log-filter input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.log-actions {
  display: flex;
  gap: 10px;
}

.log-actions button {
  padding: 8px 15px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.clear-btn {
  background-color: #e74c3c;
}

.log-content {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow-y: auto;
  background-color: #f5f5f5;
  margin-bottom: 15px;
  min-height: 400px;
}

.loading, .empty-logs {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #7f8c8d;
}

.log-entries {
  padding: 10px;
}

.log-entry {
  display: flex;
  gap: 10px;
  padding: 8px;
  border-bottom: 1px solid #ddd;
  font-family: monospace;
  font-size: 0.9rem;
  white-space: pre-wrap;
}

.log-entry:hover {
  background-color: rgba(0,0,0,0.05);
}

.log-time {
  flex: 0 0 150px;
}

.log-level {
  flex: 0 0 60px;
  font-weight: bold;
}

.log-level-debug {
  color: #7f8c8d;
}

.log-level-info {
  color: #3498db;
}

.log-level-warn {
  color: #f39c12;
}

.log-level-error {
  color: #e74c3c;
  background-color: rgba(231, 76, 60, 0.1);
}

.log-source {
  flex: 0 0 100px;
  color: #16a085;
}

.log-message {
  flex: 1;
}

.log-pagination {
  display: flex;
  justify-content: center;
  gap: 15px;
  align-items: center;
}

.log-pagination button {
  padding: 5px 10px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.log-pagination button:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
}
</style> 