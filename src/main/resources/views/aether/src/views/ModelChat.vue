<template>
  <div class="chat-layout">

    <ModelChatList ref="chatListRef"
                   class="chat-sidebar"
                   :data="threadList"
                   :selected="currentThreadId"
                   @create-thread="onCreateThread"
                   @select-thread="onSelectThread"
                   @delete-thread="onDeleteThread"
                   @update-title="onUpdateThreadTitle"
    />

    <GlowDiv class="chat-content" border="none">

      <div class="model-selector-container">
        <ModelSelector :selected="currentModelCode" @select-model="onSelectMode"/>
      </div>

      <div class="message-box-container">
        <ImMessageBox 
           ref="messageBoxRef" 
           :data="messages" 
           :isGenerating="isGenerating"
           @update-message="onMessageEdit"
           @delete-message="onMessageRemove"
           @regenerate="onMessageRegenerate"
         />
      </div>

      <div class="message-input-container">
        <ImMessageInput
            :disabled="false"
            :is-generating="isGenerating"
            @message-send="onMessageSend"
            @abort-generate="onBatchAbort"
            placeholder="为什么不问问神奇的Gemini呢?"
        />
      </div>

    </GlowDiv>

    <!-- 确认框组件 -->
    <GlowConfirm ref="confirmRef" />

    <!-- 输入框组件 -->
    <GlowInput ref="inputRef" />

  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, nextTick } from 'vue';
import ImMessageBox from "@/components/glow-client/ImMessageBox.vue";
import ModelChatList from "@/components/glow-client/ModelChatList.vue";
import ImMessageInput from "@/components/glow-client/ImMessageInput.vue";
import ModelSelector from "@/components/glow-client/ModelSelector.vue";
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme'
import axios from 'axios';
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue"
import GlowInput from "@/components/glow-ui/GlowInput.vue"
import type Result from '@/entity/Result';
import type ChatSegmentVo from '@/entity/ChatSegmentVo';

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 消息框引用
const messageBoxRef = ref<MessageBoxInstance | null>(null);
// 聊天列表引用
const chatListRef = ref<ChatListInstance | null>(null);
// 是否正在生成回复
const isGenerating = ref(false);
// 当前是否有临时消息 (移到这里)
const hasTempMessage = ref<boolean>(false)

const currentThreadId = ref<string>("")
const currentModelCode = ref<string>("")

const threadList = ref<Array<{
  id: string,
  title: string,
  modelCode: string,
  checked: number
}>>([])

const messages = ref<Array<{
  id: string, //消息记录ID(-1为临时消息)
  name: string, //发送者名称
  avatarPath: string //头像路径
  role: string //消息类型：0-用户消息，1-AI消息
  content: string //消息内容
  createTime: string //消息时间
}>>([]);

// 确认框引用
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null)
// 输入框引用
const inputRef = ref<InstanceType<typeof GlowInput> | null>(null)

onMounted(() => {
  reloadThreadList()
})

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


// 处理发送消息
const onMessageSend = async (message: string) => {

  if (isGenerating.value) return // 如果正在生成，则不处理新的发送请求

  // 3. 设置加载状态 (提前设置，因为我们要等待后端响应)
  isGenerating.value = true;

  try {
    // 4. 调用后端接口发送消息 (queryKind = 0)
    const response = await axios.post<Result<ChatSegmentVo>>('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      model: currentModelCode.value,
      message: message,
      queryKind: 0
    });

    // Check for successful response and ensure data and historyId are present and valid strings
    const backendResult = response.data; // backendResult is now Result<ChatSegmentVo>
    const backendUserMessageData = backendResult.data; // backendUserMessageData is now ChatSegmentVo

    if (backendResult.code === 0 && backendUserMessageData && typeof backendUserMessageData.historyId === 'string' && backendUserMessageData.historyId) {
      // 5. Use backend data to create the user message
      const userMessage = {
        id: backendUserMessageData.historyId, // Now definitely a string
        name: backendUserMessageData.name || 'User',
        avatarPath: backendUserMessageData.avatarPath || '',
        role: 'user' as const, // Use 'as const' for stricter type
        content: backendUserMessageData.content || message,
        createTime: new Date().toISOString() // Always use current frontend time
      };
      messages.value.push(userMessage);
      await nextTick(); 
      messageBoxRef.value?.scrollToBottom();

      // 6. 创建AI临时消息 (调用新函数)
      await createTempMsg(); 

      // 7. 开始轮询获取AI响应
      await pollMessage();

    } else {
      // 发送失败处理 (现在没有本地用户消息需要移除了)
      console.error('发送消息失败或未收到有效的用户消息数据:', backendResult.message || '返回数据无效');
      // 可选：显示错误提示给用户
      isGenerating.value = false; // 重置加载状态
    }

  } catch (error) {
    console.error('发送消息请求失败:', error);
    // 网络或其他错误处理
    // 可选：显示错误提示给用户
    isGenerating.value = false; // 重置加载状态
  }
};

//轮询拉取响应流
const pollMessage = async () => {
  let hasReceivedData = false; // 标记是否收到过数据片段

  while (isGenerating.value) {
    try {
      // 使用 Result<ChatSegmentVo> 作为响应类型
      const response = await axios.post<Result<ChatSegmentVo>>('/model/chat/completeBatch', {
        threadId: currentThreadId.value,
        queryKind: 1 // 1: 查询响应流
      });

      // 使用 backendResult 访问 code 和 message
      const backendResult = response.data;
      if (backendResult.code === 0) {
        // 使用 segment 访问 ChatSegmentVo 数据
        const segment = backendResult.data;

        // 如果 segment 为 null/undefined 或 type 为 0，则继续
        if (!segment || segment.type === 0) {
          await new Promise(resolve => setTimeout(resolve, 500));
          continue;
        }
        
        // --- 更新临时消息元数据 --- START
        // 每次收到片段都尝试更新元数据 (名称、头像、最终ID)
        await updateTempMsg({
          id: segment.historyId, // historyId 在 type=2 时才有有效值
          name: segment.name,
          avatarPath: segment.avatarPath
        });
        // --- 更新临时消息元数据 --- END

        if (segment.type === 1) { // 数据片段
          if (!hasReceivedData) {
            // 第一次收到数据，清空 "正在思考..."
            // 直接修改临时消息的内容 (或者让 appendTempMsg 智能处理第一次追加?)
            const tempMessage = messages.value.find(msg => msg.id === '-1');
            if (tempMessage) tempMessage.content = ''; // 清空初始内容
            hasReceivedData = true;
            await appendTempMsg(segment.content || ''); // 追加第一次收到的内容
          } else {
            // 追加内容 (调用新函数)
            await appendTempMsg(segment.content || '');
          }
          // Scrolling is handled inside appendTempMsg now

        } else if (segment.type === 2) { // 结束片段
          // 在设置最终ID之前，可能需要追加最后的内容
          if (segment.content) {
            if (!hasReceivedData) {
              // 如果之前没收到数据，但结束时有内容, 直接设置最终内容
              const tempMessage = messages.value.find(msg => msg.id === '-1');
              if(tempMessage) tempMessage.content = segment.content;
            } else {
              // 如果之前有数据，结束时也有内容，追加 (调用新函数)
               await appendTempMsg(segment.content);
            }
          }
          
          // 只在type=2(结束)时才移除临时消息标记和结束加载
          // removeTempMsg(); // 清理临时消息状态 (调用新函数)
          isGenerating.value = false; // 结束加载状态
          
          // 可选: 稍后刷新会话列表 (参考 model-chat.vue)
          // setTimeout(() => reloadThreadList(), 3000); 

          break; // 结束轮询

        } else if (segment.type === 10) { // 错误片段
          console.error('AI生成错误:', segment.content);
          // 可选: 显示错误给用户
          removeTempMsg(); // 移除临时消息 (调用新函数)
          isGenerating.value = false; // 结束加载状态
          break; // 结束轮询
        }
      } else {
        // API请求失败
        console.error('轮询AI响应失败:', backendResult.message); // 使用 backendResult.message
        removeTempMsg(); // 移除临时消息 (调用新函数)
        isGenerating.value = false; // 结束加载状态
        break; // 结束轮询
      }

    } catch (error) {
      console.error('轮询AI响应请求失败:', error);
      removeTempMsg(); // 移除临时消息 (调用新函数)
      isGenerating.value = false; // 结束加载状态
      break; // 结束轮询
    }

    // 每次轮询后等待一段时间
    if (isGenerating.value) {
       await new Promise(resolve => setTimeout(resolve, 500));
    }
  }
};



// 处理中止生成
const onBatchAbort = async () => { // 改为 async
  if (!isGenerating.value) return; // 如果不在生成中，直接返回

  try {
    // 调用后端接口中止 (如果需要)
    // 注意: 根据你的 model-chat.vue 逻辑，中止操作可能也需要调用后端
    const response = await axios.post('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      queryKind: 2 // 2: 终止AI响应
    });

    if (response.data.code === 0) {
      console.log('已中止AI响应');
    } else {
      console.error('中止AI响应失败:', response.data.message);
      // 即便中止失败，前端也需要清理状态
    }
  } catch (error) {
     console.error('中止AI响应请求失败:', error);
     // 即便中止失败，前端也需要清理状态
  } finally {
     // 清理前端状态
    isGenerating.value = false;
    // 使用新函数移除临时消息
    removeTempMsg();
    
    // 可能需要滚动到底部或进行其他UI更新
    await nextTick(); 
    messageBoxRef.value?.scrollToBottom();
  }
};

//加载聊天消息列表
const reloadMessageList = async (threadId: string) => {

  try {
    // 更新类型定义以包含 threadId 和 modelCode
    const response = await axios.post<{ 
      code: number; 
      message: string; 
      data: { 
        threadId: string; // 新增
        modelCode: string; // 新增
        messages: Array<{
          id: string;
          name: string;
          avatarPath: string;
          role: number; 
          content: string;
          createTime: string;
        }> 
      } 
    }>('/model/chat/recoverChat', { 
      threadId: Number(threadId) 
    });

    if (response.data.code === 0 && response.data.data) {

      // 更新当前模型代码，直接使用响应中的 modelCode
      currentModelCode.value = response.data.data.modelCode || ""; 
      currentThreadId.value = response.data.data.threadId || "";

      // 处理消息列表
      const backendMessages = response.data.data.messages || []; // 如果 messages 不存在则为空数组
      messages.value = backendMessages.map(msg => ({
        ...msg,
        role: msg.role === 0 ? 'user' : 'model' // 保持 role 转换
      }));
      
      await nextTick();
      messageBoxRef.value?.scrollToBottom();
      return
    } 
    
    console.error(`恢复会话 ${threadId} 失败:`, response.data.message || '未知错误或无消息');
    messages.value = [];
  } catch (error) {
    console.error(`恢复会话 ${threadId} 请求失败:`, error);
    messages.value = [];
  }
};

//加载会话列表
const reloadThreadList = async () => {
  try {
    const response = await axios.post('/model/chat/getThreadList')
    if (response.data.code === 0) {
      threadList.value = response.data.data || []
      
      const defaultThread = threadList.value.find(t => t.checked === 1)
      if (defaultThread) {
        currentThreadId.value = defaultThread.id;
        // 传入 defaultThread.id
        await reloadMessageList(defaultThread.id); 
      } else {
        messages.value = [];
        currentThreadId.value = "";
        currentModelCode.value = "";
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

//创建Thread
const onCreateThread = async () => {
  try {
    // 1. 调用创建接口 (假设总是成功并返回threadId)
    const response = await axios.post<Result<{ threadId: string }>>('/model/chat/createEmptyThread', {
      model: currentModelCode.value, // 使用当前选择的模型
    });

    // 2. 直接获取 newThreadId
    const newThreadId = response.data.data.threadId;

    // 3. 重新加载会话列表 (更新UI)
    await reloadThreadList(); 

    // 4. 显式设置新创建的线程为当前活动线程
    currentThreadId.value = newThreadId;

    // 5. 加载新线程的消息 (会清空消息区)
    await reloadMessageList(newThreadId); 
    
    // 6. 关闭移动端菜单（如果打开）
    chatListRef.value?.closeMobileMenu();

  } catch (error) {
    console.error('创建会话请求失败:', error);
    alert('创建会话时发生网络错误');
  }
}

//选择模型
const onSelectMode = (modeCode:string)=>{
  currentModelCode.value = modeCode;
  // 可以在这里添加逻辑：如果切换了模型，是否要影响当前会话？
  // 比如：如果当前有会话，提示用户切换模型会新建会话，或者只是更新下次新建会话的模型？
}

//选择会话
const onSelectThread = async (threadId: string) => {
  currentThreadId.value = threadId;
  chatListRef.value?.closeMobileMenu(); 
  // 传入 threadId
  await reloadMessageList(threadId); 
};

//删除会话
const onDeleteThread = async (threadId: string) => {
  try {
    //弹出确认框
    const confirmed = await confirmRef.value?.showConfirm({
      title: '删除会话',
      content: '确定要删除这个会话吗？删除后无法恢复',
      confirmText: '删除',
      cancelText: '取消'
    })

    if (!confirmed) {
      return
    }

    //调用删除接口
    const response = await axios.post('/model/chat/removeThread', {
      threadId: threadId
    })

    if (response.data.code === 0) {
      //重新加载会话列表
      await reloadThreadList()
      return
    }

    console.error('删除会话失败:', response.data.message)
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

//更新会话标题
const onUpdateThreadTitle = async (threadId: string, oldTitle: string) => {
  try {
    //弹出输入框
    const result = await inputRef.value?.showInput({
      title: '编辑会话标题',
      defaultValue: oldTitle,
      placeholder: '请输入新的标题',
      confirmText: '保存',
      cancelText: '取消'
    });

    // 检查用户是否确认以及输入值是否有效
    if (!result || !result.confirmed || !result.value || result.value === oldTitle) {
      return; // 如果用户取消、未输入内容或内容未改变，则不进行更新
    }

    //调用更新接口
    const response = await axios.post('/model/chat/editThread', {
      threadId: threadId,
      title: result.value
    });

    if (response.data.code === 0) {
      //重新加载会话列表
      await reloadThreadList();
      return;
    }

    console.error('更新标题失败:', response.data.message);
  } catch (error) {
    console.error('更新标题失败:', error);
  }
};


const createTempMsg = async () => {
  if (hasTempMessage.value) return; // 防止重复创建

  const tempAiMessage = {
    id: '-1', 
    name: '----',
    avatarPath: '', 
    role: 'model',
    content: '正在输入...',
    createTime: new Date().toISOString()
  };
  messages.value.push(tempAiMessage);
  hasTempMessage.value = true;
  await nextTick();
  messageBoxRef.value?.scrollToBottom();
}

const appendTempMsg = async (content: string) => {
  if (!hasTempMessage.value) return;

  const tempMessage = messages.value.find(msg => msg.id === '-1');
  if (tempMessage) {
    tempMessage.content += content;
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
  }
}

const removeTempMsg = () => {
  if (!hasTempMessage.value) return;

  messages.value = messages.value.filter(msg => msg.id !== '-1');
  hasTempMessage.value = false;
}

//更新临时消息
const updateTempMsg = async (data: {
  id?: string; // 消息ID (可选, 如果提供则表示转为永久)
  name?: string; // 名称 (可选)
  avatarPath?: string; // 头像路径 (可选)
}) => {

  if (!hasTempMessage.value) return;

  const tempMessage = messages.value.find(msg => msg.id === '-1');

  if (tempMessage) {
    let updated = false;

    // 更新名称 (如果提供了有效的名称)
    if (data.name) {
      tempMessage.name = data.name;
      updated = true;
    }

    // 更新头像 (如果提供了有效的头像路径)
    if (data.avatarPath) {
      tempMessage.avatarPath = data.avatarPath;
      updated = true;
    }

    // 检查是否提供了有效的永久ID
    // (假设有效的永久ID不为空且不等于临时ID '-1')
    if (data.id && data.id !== '-1') {
      tempMessage.id = data.id; // 将临时消息ID更新为永久ID
      hasTempMessage.value = false; // 标记不再有临时消息
      updated = true;
    }
    
    // 如果有任何更新，触发Vue的响应式更新
    if (updated) {
       messages.value = [...messages.value];
       // 一般更新元数据不需要滚动，除非UI布局因此改变
       // await nextTick(); 
       // messageBoxRef.value?.scrollToBottom();
    }
  }
}


const onMessageRemove = async (msgId: string) => {

  if (!confirmRef.value) {
    console.error('GlowConfirm component reference is not available.');
    return;
  }

  try {
    // 弹出确认框
    const confirmed = await confirmRef.value.showConfirm({
      title: '删除消息',
      content: '确定要删除这条消息吗？此操作不可恢复。',
      confirmText: '确认删除',
      cancelText: '取消'
    });

    // 用户取消删除
    if (!confirmed) {
      return; 
    }

    // 调用后端 API 删除消息
    const response = await axios.post<Result<unknown>>('/model/chat/removeHistory', { 
      threadId: currentThreadId.value, 
      historyId: msgId 
    });

    // API 调用失败
    if (response.data.code !== 0) {
      console.error(`Failed to remove message ${msgId}:`, response.data.message);
      alert(`消息删除失败: ${response.data.message || '未知错误'}`);
      return;
    }

    // API 调用成功，从本地消息列表移除
    const index = messages.value.findIndex(msg => msg.id === msgId);
    
    // 未在本地找到消息 (理论上不应发生，除非数据不同步)
    if (index === -1) {
       console.warn(`Message ${msgId} not found locally after successful removal.`);
       return;
    }
    
    messages.value.splice(index, 1);
    console.log(`Message ${msgId} removed successfully.`);

  } catch (error) {
    console.error(`Error removing message ${msgId}:`, error);
    alert('删除消息时发生网络错误');
  }
};

// 处理消息重新生成
const onMessageRegenerate = async (msgId: string) => {
  // 1. 如果已在生成中，则阻止操作
  if (isGenerating.value) return;

  // 2. 检查是否有可用于重新生成的消息
  if (messages.value.length === 0) {
    console.warn('没有可用于重新生成的消息。');
    return;
  }
  
  // 记录触发重新生成的消息ID
  console.log(`根据消息上下文重新生成，结束ID为 ${msgId}`); 

  // 3. 设置生成状态
  isGenerating.value = true;

  try {
    // 4. 获取最后一条消息
    const lastMessageIndex = messages.value.length - 1;
    const lastMessage = messages.value[lastMessageIndex];

    // 5. 如果最后一条是AI响应，则先在视觉上移除它
    if (lastMessage.role === 'model') {
      messages.value.pop();
      // 等待UI更新后再添加临时消息
      await nextTick(); 
    }

    // 6. 为新的响应创建AI临时消息
    await createTempMsg();

    // 7. 调用后端API触发重新生成 (queryKind = 3)
    const response = await axios.post<Result<ChatSegmentVo>>('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      // 传递当前模型
      model: currentModelCode.value, 
      // 3: 重新生成最后一条响应
      queryKind: 3 
    });

    // 8. 处理API响应失败
    if (response.data.code !== 0) {
      console.error('启动消息重新生成失败:', response.data.message);
      alert(`重新生成失败: ${response.data.message || '未知错误'}`);
      // 清理临时消息
      removeTempMsg(); 
      isGenerating.value = false;
      // 失败时退出
      return; 
    }

    // 9. API调用成功，开始轮询新的响应流
    console.log('后端已确认重新生成请求，开始轮询...');
    await pollMessage();

  } catch (error) {
    // 10. 处理网络或其他错误
    console.error('消息重新生成过程中出错:', error);
    alert('重新生成消息时发生网络错误');
    // 清理临时消息
    removeTempMsg(); 
    isGenerating.value = false;
  }
};

// 处理消息更新
const onMessageEdit = async (params: { msgId: string; message: string }) => {
  try {
    // 调用后端 API 更新消息历史
    const response = await axios.post<Result<unknown>>('/model/chat/editHistory', { // Assuming Result<unknown> or specific type if available
      historyId: params.msgId, 
      content: params.message
    });

    if (response.data.code === 0) {
      // API 调用成功，更新本地消息列表
      const messageIndex = messages.value.findIndex(msg => msg.id === params.msgId);
      if (messageIndex !== -1) {
        messages.value[messageIndex].content = params.message;
      }
      console.log(`Message ${params.msgId} updated successfully.`);
    } else {
      console.error(`Failed to update message ${params.msgId}:`, response.data.message);
      alert(`消息更新失败: ${response.data.message || '未知错误'}`);
    }
  } catch (error) {
    console.error(`Error updating message ${params.msgId}:`, error);
    alert('消息更新时发生网络错误');
  }
};




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