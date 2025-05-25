<template>
  <div class="chat-layout">

    <ChatNpcList ref="npcListRef"
                   class="chat-sidebar"
                   @select-npc="onSelectNpc"
                   @create-thread="onCreateThread"
                   @edit-role="onEditRole"
                   @manageThreads="onManageThreads"
    />

    <GlowDiv class="chat-content" border="none">

      <div class="model-selector-container">
        <ModelSelector :selected="currentModelCode" @select-model="onSelectMode"/>
      </div>

      <div class="message-box-container">
        <ChatMessageBox
           ref="messageBoxRef" 
           :data="messageData" 
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

    <GlowAlter ref="alterRef" />

    <!-- 输入框组件 -->
    <GlowConfirmInput ref="inputRef" />

    <!-- 会话管理模态框 -->
    <ChatNpcThreadsModal
      ref="roleThreadsModalRef"
      @activate-thread="handleActivateThread"
    />

  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, nextTick } from 'vue';
import ChatMessageBox from "@/components/glow-client/ChatMessageBox.vue";
import ImMessageInput from "@/components/glow-client/ImMessageInput.vue";
import ModelSelector from "@/components/glow-client/ModelSelector.vue";
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme'
import http from '@/commons/Http';
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue"
import GlowConfirmInput from "@/components/glow-ui/GlowConfirmInput.vue"
import ChatNpcList from "@/components/glow-client/ChatNpcList.vue";
import type { GetNpcListVo } from '@/commons/api/NpcApi.ts';
import type RecoverRpChatVo from '@/entity/vo/RecoverRpChatVo.ts';
import type RecoverRpChatHistoryVo from '@/entity/vo/RecoverRpChatHistoryVo.ts';
import type RpSegmentVo from '@/entity/vo/RpSegmentVo.ts';
import ChatNpcThreadsModal from "@/components/glow-client/ChatNpcThreadsModal.vue";
import GlowAlter from "@/components/glow-ui/GlowAlter.vue";
import { useRouter } from 'vue-router';
import type { SelectThreadDto, SelectThreadVo } from '@/commons/api/ThreadApi';
import ThreadApi from '@/commons/api/ThreadApi';

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)
const router = useRouter();

const isCreatingThread = ref<boolean>(false)

const messageData = ref<MessageBoxItem[]>([])
const selectThreadData = ref<SelectThreadVo | null>(null)
const selectThreadTotal = ref(0)
const selectThreadQuery = ref<SelectThreadDto>({
  npcId: "",
  page: 1,
  pageSize: 1000
})


// 定义 ChatMessageBox 需要的消息项类型
interface MessageBoxItem {
  id: string; 
  name: string; 
  avatarPath: string;
  role: 'user' | 'model'; // 明确类型
  content: string; 
  createTime: string;
}

// 消息框引用
const messageBoxRef = ref<MessageBoxInstance | null>(null);
// NPC列表引用
const npcListRef = ref<NpcListInstance | null>(null);
// 是否正在生成回复
const isGenerating = ref(false);
// 当前是否有临时消息
const hasTempMessage = ref<boolean>(false)

const currentNpcId = ref<string>("")   //当前选择的NPC ID
const currentThreadId = ref<string>("") //当前聊天Thread的ID
const currentModelCode = ref<string>("")//当前选择的模型代码

const alterRef = ref<InstanceType<typeof GlowAlter> | null>(null);
// 确认框引用
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null)
// 输入框引用
const inputRef = ref<InstanceType<typeof GlowConfirmInput> | null>(null)
// 会话管理模态框引用
const roleThreadsModalRef = ref<InstanceType<typeof ChatNpcThreadsModal> | null>(null)

// 定义消息框实例类型
interface MessageBoxInstance {
  createTempMessage: (msg: any) => void;
  appendTempMessage: (content: string) => void;
  deleteTempMessage: () => void;
  scrollToBottom: () => void;
}

// 定义NPC列表实例类型
interface NpcListInstance {
  loadThreadList: () => Promise<any>;
  setActiveThread: (threadId: string) => void;
  getActiveThreadId: () => string;
  threads: any[];
  closeMobileMenu: () => void;
}

const getNpcMessageList = async (npcId: string) => {
  try {
    // 设置查询参数
    selectThreadQuery.value.npcId = npcId;

    console.log(selectThreadQuery.value)
    
    // 调用ThreadApi获取消息列表，返回SelectThreadVo
    const response: SelectThreadVo = await ThreadApi.selectThread(selectThreadQuery.value);
    
    // 存储完整的SelectThreadVo响应
    selectThreadData.value = response;
    
    // 更新当前模型代码和线程ID
    currentModelCode.value = response.modelCode;
    currentThreadId.value = response.threadId;
    
    // 转换消息格式
    const messageList = response.messages.rows || [];
    messageData.value = messageList.map((msg): MessageBoxItem => ({
      id: msg.id,
      name: msg.senderName,
      avatarPath: msg.senderAvatarUrl,
      role: msg.senderRole === 0 ? 'user' : 'model',
      content: msg.content,
      createTime: msg.createTime
    }));
    
    // 更新总数
    selectThreadTotal.value = response.messages.count || 0;
    
    // 滚动到底部
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
    
  } catch (error) {
    console.error(`获取NPC ${npcId} 消息列表失败:`, error);
    messageData.value = [];
    selectThreadData.value = null;
    selectThreadTotal.value = 0;
    
    alterRef.value?.showConfirm({
      title: "获取消息失败",
      content: `请检查网络连接或联系管理员。错误详情: ${error}`,
      closeText: "好的",
    });
  }
}

const sendMessage = async (message: string) => {






  
}







// 处理发送消息
const onMessageSend = async (message: string) => {

  if (isGenerating.value) return // 如果正在生成，则不处理新的发送请求

  // 3. 设置加载状态 (提前设置，因为我们要等待后端响应)
  isGenerating.value = true;

  try {
    // 4. 调用后端接口发送消息 (queryKind = 0) - 更新接口地址和响应类型
    const segment = await http.postEntity<RpSegmentVo>('/model/rp/rpCompleteBatch', { 
      threadId: currentThreadId.value, // 使用当前的聊天线程ID
      model: currentModelCode.value,
      message: message,
      queryKind: 0
    });

    // Check for successful response and ensure historyId is present and valid string
    if (segment && typeof segment.historyId === 'string' && segment.historyId) {
      // 5. Use backend historyId and original message to create the user message
      const userMessage: MessageBoxItem = {
        id: segment.historyId, // Use historyId from backend response
        name: segment.roleName, // Use a generic name for the user
        avatarPath: segment.roleAvatarPath, // Use a default/empty avatar for the user
        role: 'user', // Role is user
        content: segment.content, // Use the original message content
        createTime: new Date().toISOString() // Always use current frontend time
      };
      messageData.value.push(userMessage);
      await nextTick(); 
      messageBoxRef.value?.scrollToBottom();

      // 6. 创建AI临时消息 (调用新函数)
      await createTempMsg(); 

      // 7. 开始轮询获取AI响应
      await pollMessage();

    } else {
      // 发送失败处理 (现在没有本地用户消息需要移除了)
      console.error('发送消息失败或未收到有效的用户消息数据:', '返回数据无效');
      // 可选：显示错误提示给用户
      alterRef.value?.showConfirm({
        title: "回复消息时发生错误",
        content: "发送消息失败或未收到有效的用户消息数据",
        closeText: "好的",
      })
      isGenerating.value = false; // 重置加载状态
    }

  } catch (error) {
    console.error('发送消息请求失败:', error);
    // 网络或其他错误处理
    // 可选：显示错误提示给用户
    alterRef.value?.showConfirm({
      title: "发送消息请求失败",
      content: `${error}`,
      closeText: "好的",
    })

    isGenerating.value = false; // 重置加载状态
  }
};

//轮询拉取响应流
const pollMessage = async () => {
  let hasReceivedData = false; // 标记是否收到过数据片段

  while (isGenerating.value) {
    try {
      // 使用 Result<RpSegmentVo> 作为响应类型 - 更新接口地址
      const segment = await http.postEntity<RpSegmentVo>('/model/rp/rpCompleteBatch', { 
        threadId: currentThreadId.value, // 使用当前的聊天线程ID
        queryKind: 1 // 1: 查询响应流
      });

      // 如果 segment 为 null/undefined 或 type 为 0，则继续
      if (!segment || segment.type === 0) {
        await new Promise(resolve => setTimeout(resolve, 500));
        continue;
      }
      
      // --- 更新临时消息元数据 --- START
      // 每次收到片段都尝试更新元数据 (名称、头像、最终ID)
      console.log(segment)


      await updateTempMsg({
        id: segment.historyId, // historyId 在 type=2 时才有有效值
        name: segment.roleName, // 使用 roleName
        avatarPath: segment.roleAvatarPath ? "/res/"+segment.roleAvatarPath : undefined // 使用 roleAvatarPath
      });
      // --- 更新临时消息元数据 --- END

      if (segment.type === 1) { // 数据片段
        if (!hasReceivedData) {
          // 第一次收到数据，清空 "正在思考..."
          // 直接修改临时消息的内容 (或者让 appendTempMsg 智能处理第一次追加?)
          const tempMessage = messageData.value.find(msg => msg.id === '-1');
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
            const tempMessage = messageData.value.find(msg => msg.id === '-1');
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
        // setTimeout(() => reloadRoleList(), 3000); 

        break; // 结束轮询

      } else if (segment.type === 10) { // 错误片段
        console.error('AI生成错误:', segment.content);
        // 可选: 显示错误给用户
        alterRef.value?.showConfirm({
          title: "回复消息时发生错误",
          content: segment.content,
          closeText: "好的",
        })

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
    // 调用后端接口中止 - 更新接口地址
    await http.postEntity<void>('/model/rp/rpCompleteBatch', { 
      threadId: currentThreadId.value, // 使用当前的聊天线程ID
      queryKind: 2 // 2: 终止AI响应
    });

    console.log('已中止AI响应');
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

//选择模型
const onSelectMode = (modeCode:string)=>{
  currentModelCode.value = modeCode;
  // 可以在这里添加逻辑：如果切换了模型，是否要影响当前会话？
  // 比如：如果当前有会话，提示用户切换模型会新建会话，或者只是更新下次新建会话的模型？
}

//选择NPC
const onSelectNpc = async (npc: GetNpcListVo) => {
  // When user manually selects a npc, always load the latest/new thread for that npc
  npcListRef.value?.closeMobileMenu(); 
  await getNpcMessageList(npc.id); 
};

//开始新会话
const onCreateThread = async (npc: GetNpcListVo) => {
  console.log(`开始为NPC ${npc.name} (ID: ${npc.id}) 创建新会话`);
  npcListRef.value?.closeMobileMenu(); // 关闭移动端菜单

  // 清空当前消息列表和状态
  messageData.value = []; 
  isGenerating.value = false;
  hasTempMessage.value = false; 
  currentNpcId.value = npc.id; // 设置当前NPC ID
  // currentThreadId.value 会在请求成功后被设置

  try {
    const chatData = await http.postEntity<RecoverRpChatVo>('/model/rp/recoverRpChat', { 
      modelRoleId: npc.id, 
      modelCode: currentModelCode.value, 
      newThread: 0 // 明确指示创建新线程
    });

    console.log('新会话创建成功:', chatData);

    // 更新当前线程ID和模型代码
    currentThreadId.value = chatData.threadId || ""; 
    currentModelCode.value = chatData.modelCode || currentModelCode.value; // 如果后端没返回，保持当前选择

    // 处理可能返回的初始消息 (通常新会话是空的)
    const backendMessages = chatData.messages || []; 
    messageData.value = backendMessages.map((msg: RecoverRpChatHistoryVo): MessageBoxItem => ({
      id: msg.id, 
      name: msg.name,
      avatarPath: msg.avatarPath,
      role: msg.type === 0 ? 'user' : 'model', 
      content: msg.rawContent, 
      createTime: msg.createTime 
    }));
    
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
    
  } catch (error) {
    console.error(`为NPC ${npc.name} 创建新会话请求失败:`, error);

    alterRef.value?.showConfirm({
      title: "故障",
      content: `${error}`,
      closeText: "关闭",
    })
    
    // 清空聊天相关状态
    messageData.value = [];
    currentNpcId.value = "";
    currentThreadId.value = "";
    currentModelCode.value = "";
  }
}

//编辑NPC(修改为使用Vue Router导航到ModelRoleManager)
const onEditRole = async (npc: GetNpcListVo) => {
  if (!npc.id) {
    return
  }
  // 使用Vue Router导航到ModelRoleManager.vue并携带roleId参数
  router.push({
    path: '/model-role-manager',
    query: { roleId: npc.id }
  });
}

const onManageThreads = async (npc: GetNpcListVo) => {
  if (roleThreadsModalRef.value) {
    roleThreadsModalRef.value.show(npc.id, npc.name);
  }
}

// 处理激活会话事件
const handleActivateThread = async (npcId: string, threadId: string, modelCode: string) => {
  console.log(`准备激活会话: npcId=${npcId}, threadId=${threadId}, modelCode=${modelCode}`)
  
  // 设置查询参数以获取指定的线程
  selectThreadQuery.value.npcId = undefined;
  selectThreadQuery.value.threadId = threadId;
  
  try {
    // 调用ThreadApi获取指定线程的消息列表
    const response: SelectThreadVo = await ThreadApi.selectThread(selectThreadQuery.value);
    
    // 存储完整的SelectThreadVo响应
    selectThreadData.value = response;
    
    // 更新当前状态
    currentNpcId.value = npcId;
    currentModelCode.value = response.modelCode;
    currentThreadId.value = response.threadId;
    
    // 转换消息格式
    const messageList = response.messages.rows || [];
    messageData.value = messageList.map((msg): MessageBoxItem => ({
      id: msg.id,
      name: msg.senderName,
      avatarPath: msg.senderAvatarUrl,
      role: msg.senderRole === 0 ? 'user' : 'model',
      content: msg.content,
      createTime: msg.createTime
    }));
    
    // 更新总数
    selectThreadTotal.value = response.messages.count || 0;
    
    // 滚动到底部
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
    
  } catch (error) {
    console.error(`激活会话失败: npcId=${npcId}, threadId=${threadId}`, error);
    messageData.value = [];
    selectThreadData.value = null;
    selectThreadTotal.value = 0;
    
    alterRef.value?.showConfirm({
      title: "激活会话失败",
      content: `请检查网络连接或联系管理员。错误详情: ${error}`,
      closeText: "好的",
    });
  }
}

const createTempMsg = async () => {
  if (hasTempMessage.value) return; // 防止重复创建

  // 确保临时消息符合 MessageBoxItem 类型
  const tempAiMessage: MessageBoxItem = {
    id: '-1', 
    name: '-----', // 调整名称以区分
    avatarPath: '', 
    role: 'model', // 类型已明确为 'model'
    content: '正在输入...',
    createTime: new Date().toISOString()
  };
  messageData.value.push(tempAiMessage);
  hasTempMessage.value = true;
  await nextTick();
  messageBoxRef.value?.scrollToBottom();
  console.log(messageData.value)
}

const appendTempMsg = async (content: string) => {
  if (!hasTempMessage.value) return;

  const tempMessage = messageData.value.find(msg => msg.id === '-1');
  if (tempMessage) {
    tempMessage.content += content;
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
  }
}

const removeTempMsg = () => {
  if (!hasTempMessage.value) return;

  messageData.value = messageData.value.filter(msg => msg.id !== '-1');
  hasTempMessage.value = false;
}

//更新临时消息
const updateTempMsg = async (data: {
  id?: string; // 消息ID (可选, 如果提供则表示转为永久)
  name?: string; // 名称 (可选)
  avatarPath?: string; // 头像路径 (可选)
}) => {

  if (!hasTempMessage.value) return;

  const tempMessage = messageData.value.find(msg => msg.id === '-1');

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
       messageData.value = [...messageData.value];
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

    // 调用后端 API 删除消息 - 更新接口地址和 threadId 参数
    await http.postEntity<void>('/model/rp/removeHistory', { 
      threadId: currentThreadId.value, // 使用当前的聊天线程ID
      historyId: msgId 
    });

    // API 调用成功，从本地消息列表移除
    const index = messageData.value.findIndex(msg => msg.id === msgId);
    
    // 未在本地找到消息 (理论上不应发生，除非数据不同步)
    if (index === -1) {
       console.warn(`Message ${msgId} not found locally after successful removal.`);
       return;
    }
    
    messageData.value.splice(index, 1);
    console.log(`Message ${msgId} removed successfully.`);

  } catch (error) {
    console.error(`Error removing message ${msgId}:`, error);

    alterRef.value?.showConfirm({
      title: "故障",
      content: `${error}`,
      closeText: "关闭",
    })
  }
};

// 处理消息重新生成
const onMessageRegenerate = async (msgId: string) => {
  // 1. 如果已在生成中，则阻止操作
  if (isGenerating.value) return;

  // 2. 检查是否有可用于重新生成的消息
  if (messageData.value.length === 0) {
    console.warn('没有可用于重新生成的消息。');
    return;
  }
  
  // 记录触发重新生成的消息ID
  console.log(`根据消息上下文重新生成，结束ID为 ${msgId}`); 

  // 3. 设置生成状态
  isGenerating.value = true;

  try {

    // 4. 获取最后一条消息
    const lastMessageIndex = messageData.value.length - 1;
    const lastMessage = messageData.value[lastMessageIndex];

    // 5. 如果最后一条是AI响应，则先在视觉上移除它
    if (lastMessage.role === 'model') {
      messageData.value.pop();
      // 等待UI更新后再添加临时消息
      await nextTick(); 
    }

    // 6. 为新的响应创建AI临时消息
    await createTempMsg();

    // 7. 调用后端API触发重新生成 (queryKind = 3) - 更新接口地址和响应类型
    const segment = await http.postEntity<RpSegmentVo>('/model/rp/rpCompleteBatch', { 
      threadId: currentThreadId.value, // 使用当前的聊天线程ID
      // 传递当前模型
      model: currentModelCode.value, 
      // 3: 重新生成最后一条响应
      queryKind: 3 
    });

    // 9. API调用成功，开始轮询新的响应流
    console.log('后端已确认重新生成请求，开始轮询...');
    await pollMessage();

  } catch (error) {

    console.error('消息重新生成过程中出错:', error);

    alterRef.value?.showConfirm({
      title: "故障",
      content: `${error}`,
      closeText: "关闭",
    })

    // 清理临时消息
    removeTempMsg(); 
    isGenerating.value = false;
  }
};

// 处理消息更新
const onMessageEdit = async (params: { msgId: string; message: string }) => {
  try {
    // 调用后端 API 更新消息历史 - 更新接口地址
    await http.postEntity<void>('/model/rp/editHistory', { 
      historyId: params.msgId, 
      content: params.message
    });

    // API 调用成功，更新本地消息列表
    const messageIndex = messageData.value.findIndex(msg => msg.id === params.msgId);
    if (messageIndex !== -1) {
      messageData.value[messageIndex].content = params.message;
    }
    console.log(`Message ${params.msgId} updated successfully.`);
  } catch (error) {
    console.error(`Error updating message ${params.msgId}:`, error);

    alterRef.value?.showConfirm({
      title: "故障",
      content: `${error}`,
      closeText: "关闭",
    })
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