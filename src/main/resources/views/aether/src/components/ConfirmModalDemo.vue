<!-- 确认模态框示例 -->
<template>
  <div class="demo-container">
    <h2>确认模态框示例</h2>
    
    <div class="buttons-container">
      <LaserButton
        class="demo-btn"
        @click="showBasicConfirm">
        基本确认框
      </LaserButton>
      
      <LaserButton
        class="demo-btn"
        @click="showDeleteConfirm">
        删除确认框
      </LaserButton>
      
      <LaserButton
        class="demo-btn"
        @click="showCustomConfirm">
        自定义确认框
      </LaserButton>
    </div>
    
    <div class="result-container" v-if="result !== null">
      操作结果: <span :class="{ 'success': result, 'cancel': !result }">{{ result ? '确认' : '取消' }}</span>
    </div>
    
    <!-- 引用确认模态框组件 -->
    <ConfirmModal ref="confirmModalRef" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LaserButton from './LaserButton.vue'
import ConfirmModal from './ConfirmModal.vue'

// 引用确认模态框
const confirmModalRef = ref<InstanceType<typeof ConfirmModal> | null>(null)
const result = ref<boolean | null>(null)

// 显示基本确认框
const showBasicConfirm = async () => {
  if (confirmModalRef.value) {
    result.value = await confirmModalRef.value.showConfirm({
      title: '确认操作',
      content: '您确定要执行此操作吗？'
    })
  }
}

// 显示删除确认框
const showDeleteConfirm = async () => {
  if (confirmModalRef.value) {
    result.value = await confirmModalRef.value.showConfirm({
      title: '移除所有职位',
      content: '您确定要清除您的全部职位和可选职位吗？如果您确认，那么您将不会获得你目前所属军团中的任何新职位，若要再次接受新职位，你必须勾选右击你的人物，然后选择"创建新职位"。'
    })
  }
}

// 显示自定义确认框
const showCustomConfirm = async () => {
  if (confirmModalRef.value) {
    result.value = await confirmModalRef.value.showConfirm({
      title: '注销会话',
      content: '当前会话尚未保存，注销会导致数据丢失。您确定要注销吗？',
      confirmText: '注销',
      cancelText: '返回'
    })
  }
}
</script>

<style scoped>
.demo-container {
  max-width: 600px;
  margin: 40px auto;
  padding: 20px;
  background: rgba(30, 30, 30, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 0px;
}

h2 {
  color: rgba(255, 255, 255, 0.9);
  margin-top: 0;
  margin-bottom: 20px;
}

.buttons-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.demo-btn {
  min-width: 120px;
}

.result-container {
  margin-top: 20px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 0px;
  color: rgba(255, 255, 255, 0.8);
}

.success {
  color: rgb(75, 181, 67);
  font-weight: bold;
}

.cancel {
  color: rgb(220, 53, 69);
  font-weight: bold;
}
</style> 