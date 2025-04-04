<template>
  <div class="chat-layout">
    <ModelChatList :threads="[]" class="chat-sidebar"></ModelChatList>

    <div class="chat-content">
      <ImMessageBox :messages="messages" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import ImMessageBox from "@/components/glow-client/ImMessageBox.vue";
import ModelChatList from "@/components/glow-client/ModelChatList.vue";

// 测试数据
const messages = ref([
  {
    id: '1',
    name: '用户',
    avatarPath: '',
    role: 'user',
    content: '你好，我想了解一下人工智能的基础知识',
    createTime: new Date().toISOString()
  },
  {
    id: '2',
    name: 'AI助手',
    avatarPath: '',
    role: 'assistant',
    content: '人工智能(AI)是计算机科学的一个分支，致力于创建能够模拟人类智能的系统。以下是一些AI的基础概念：\n\n## 核心领域\n* **机器学习**: 让计算机从数据中学习而不需要显式编程\n* **深度学习**: 使用神经网络处理大量数据\n* **自然语言处理**: 让计算机理解和处理人类语言\n* **计算机视觉**: 使计算机能够从图像中获取信息\n\n您有什么具体想了解的方面吗？',
    createTime: new Date().toISOString()
  },
  {
    id: '3',
    name: '用户',
    avatarPath: '',
    role: 'user',
    content: '机器学习和深度学习有什么区别？',
    createTime: new Date().toISOString()
  },
  {
    id: '4',
    name: 'AI助手',
    avatarPath: '',
    role: 'assistant',
    content: '机器学习和深度学习的主要区别：\n\n### 机器学习\n- 是AI的一个子集，专注于开发能从数据中学习的算法\n- 包括多种算法类型：决策树、SVM、随机森林等\n- 通常需要手动特征工程\n- 可以在较小的数据集上有效工作\n- 计算资源需求相对较小\n\n### 深度学习\n- 是机器学习的一个子集\n- 基于人工神经网络，特别是多层神经网络\n- 能够自动进行特征提取，减少手动特征工程\n- 通常需要大量数据和计算资源\n- 在图像识别、自然语言处理等复杂任务中表现突出\n\n简而言之，深度学习是机器学习的一种高级形式，它使用神经网络自动从数据中学习复杂模式。',
    createTime: new Date().toISOString()
  }
]);
</script>

<style scoped>
.chat-layout {
  display: flex;
  width: 100%;
  height: 100%;
}

.chat-sidebar {
  flex-shrink: 0;
}

.chat-content {
  width: 100%;
  box-sizing: border-box;
  height: 100%;
}

@media (max-width: 768px) {
  .chat-layout {
    flex-direction: column;
    height: auto;
  }
  
  .chat-sidebar {
    width: 100%;
  }
  
  .chat-content {
    width: 100%;
    height: 500px;
  }
}
</style>