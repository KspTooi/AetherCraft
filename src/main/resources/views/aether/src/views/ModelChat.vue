<template>
  <div class="chat-layout">

    <ModelChatList ref="chatListRef" :threads="[]" class="chat-sidebar"/>

    <GlowDiv class="chat-content" border="none">

      <div class="model-selector-container">
        <ModelSelector selected=""/>
      </div>

      <div class="message-box-container">
        <ImMessageBox ref="messageBoxRef" :messages="messages" />
      </div>

      <div class="message-input-container">
        <ImMessageInput
            :disabled="false"
            :is-generating="false"
            @message-send="handleMessageSend"
            @abort-generate="handleAbortGenerate"
            placeholder="为什么不问问神奇的Gemini呢?"
        />
      </div>

    </GlowDiv>

  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted } from 'vue';
import ImMessageBox from "@/components/glow-client/ImMessageBox.vue";
import ModelChatList from "@/components/glow-client/ModelChatList.vue";
import ImMessageInput from "@/components/glow-client/ImMessageInput.vue";
import ModelSelector from "@/components/glow-client/ModelSelector.vue";
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme'

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 定义消息框实例类型
interface MessageBoxInstance {
  createTempMessage: (msg: any) => void;
  appendTempMessage: (content: string) => void;
  deleteTempMessage: () => void;
  scrollToBottom: () => void;
}

// 定义聊天列表实例类型
interface ChatListInstance {
  loadThreadList: () => Promise<any>;
  setActiveThread: (threadId: string) => void;
  getActiveThreadId: () => string;
  threads: any[];
  closeMobileMenu: () => void;
}

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

// 消息框引用
const messageBoxRef = ref<MessageBoxInstance | null>(null);
// 聊天列表引用
const chatListRef = ref<ChatListInstance | null>(null);
// 是否正在生成回复
const isGenerating = ref(false);

// 处理发送消息
const handleMessageSend = (params: { message: string }) => {
  // 创建用户消息
  const userMessage = {
    id: Date.now().toString(),
    name: '用户',
    avatarPath: '',
    role: 'user',
    content: params.message,
    createTime: new Date().toISOString()
  };
  
  // 添加到消息列表
  messages.value.push(userMessage);
  
  // 模拟AI回复过程
  isGenerating.value = true;
  
  // 创建临时消息
  if (messageBoxRef.value) {
    messageBoxRef.value.createTempMessage({
      id: '-1',
      name: 'AI助手',
      avatarPath: '',
      role: 'assistant',
      content: '',
      createTime: new Date().toISOString()
    });
    
    // 模拟流式回复
    let response = '感谢您的提问！我正在思考中...';
    let index = 0;
    
    const appendInterval = setInterval(() => {
      if (index < response.length) {
        // 追加内容
        if (messageBoxRef.value) {
          messageBoxRef.value.appendTempMessage(response[index]);
        }
        index++;
      } else {
        // 完成回复
        clearInterval(appendInterval);
        
        // 删除临时消息
        if (messageBoxRef.value) {
          messageBoxRef.value.deleteTempMessage();
        }
        
        // 添加完整回复
        messages.value.push({
          id: Date.now().toString(),
          name: 'AI助手',
          avatarPath: '',
          role: 'assistant',
          content: '我收到了您的消息：' + params.message + '\n\n这是一个模拟的AI回复，实际应用中会调用AI接口获取真实回复。',
          createTime: new Date().toISOString()
        });
        
        isGenerating.value = false;
        
        // 滚动到底部
        if (messageBoxRef.value) {
          messageBoxRef.value.scrollToBottom();
        }
      }
    }, 50);
  }
};

// 处理中止生成
const handleAbortGenerate = () => {
  isGenerating.value = false;
  
  // 删除临时消息
  if (messageBoxRef.value) {
    messageBoxRef.value.deleteTempMessage();
  }
};

// 处理移动端菜单关闭
const handleMenuStateChange = () => {
  // 如果有消息框，滚动到底部
  if (messageBoxRef.value) {
    setTimeout(() => {
      messageBoxRef.value?.scrollToBottom();
    }, 300);
  }
};

// 组件挂载时设置
onMounted(() => {
  // 监听点击内容区域时关闭移动端菜单
  const contentEl = document.querySelector('.chat-content');
  if (contentEl && chatListRef.value) {
    contentEl.addEventListener('click', () => {
      if (chatListRef.value?.closeMobileMenu) {
        chatListRef.value.closeMobileMenu();
      }
    });
  }
});
</script>

<style scoped>

.model-selector-container{
  padding: 0 25px 0 15px;
  border-bottom: 1px solid v-bind('theme.boxBorderColorHover');
}

.chat-layout {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.chat-sidebar {
  flex-shrink: 0;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.message-box-container {
  flex: 1;
  overflow: hidden;
}

.message-input-container {
  flex-shrink: 0;
  padding-right: 25px
}

@media (max-width: 768px) {
  .chat-layout {
    flex-direction: column;
    height: 100%;
    position: relative;
  }
  
  .chat-content {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
  }
  
  .message-box-container {
    height: calc(100% - 140px);
  }
  

}
</style>