<template>
  <div class="no-permission-container">
    <el-result
      icon="warning"
      title="权限不足"
      :sub-title="permissionError"
    >
      <template #extra>
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const permissionError = ref<string>('您没有访问此页面的权限')

// 从路由参数中获取权限错误信息
onMounted(() => {
  if (route.query.message) {
    permissionError.value = route.query.message as string
  }
  
  // 如果有权限代码，显示在消息中
  if (route.query.permissionCode) {
    permissionError.value = `权限不足：${route.query.permissionCode}`
  }
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 返回首页
const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.no-permission-container {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}
</style> 