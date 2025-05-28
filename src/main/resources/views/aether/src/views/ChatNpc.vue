<template>
  <GlowMobileSupport :on-touch-move-right="() => {
    npcListRef?.toggleMobileMenu()
  }">
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
          <ModelSelector :selected="curModelCode" @select-model="onSelectMode"/>
        </div>

        <div class="message-box-container">
          <ChatMessageBox
             ref="messageBoxRef" 
             :data="messageData" 
             :isGenerating="isGenerating"
             :loading="isLoadingMessages"
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
  </GlowMobileSupport>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, nextTick } from 'vue';
import ChatMessageBox from "@/components/glow-client/ChatMessageBox.vue";
import ImMessageInput from "@/components/glow-client/ImMessageInput.vue";
import ModelSelector from "@/components/glow-client/ModelSelector.vue";
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme'
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue"
import GlowConfirmInput from "@/components/glow-ui/GlowConfirmInput.vue"
import ChatNpcList from "@/components/glow-client/ChatNpcList.vue";
import type { GetNpcListVo } from '@/commons/api/NpcApi.ts';
import ChatNpcThreadsModal from "@/components/glow-client/ChatNpcThreadsModal.vue";
import GlowAlter from "@/components/glow-ui/GlowAlter.vue";
import { useRouter } from 'vue-router';
import type { SelectThreadDto, SelectThreadVo, CreateThreadDto, CreateThreadVo } from '@/commons/api/ThreadApi';
import ThreadApi from '@/commons/api/ThreadApi';
import ConversationService from '@/views/service/ConversationService';
import type { SendMessageDto, MessageFragmentVo, RegenerateDto, AbortConversationDto } from '@/commons/api/ConversationApi';
import ConversationApi from '@/commons/api/ConversationApi';
import MessageApi, { type EditMessageDto } from '@/commons/api/MessageApi';
import type CommonIdDto from '@/entity/dto/CommonIdDto';
import GlowMobileSupport from "@/components/glow-ui/GlowMobileSupport.vue";

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)
const router = useRouter();
const messageData = ref<MessageBoxItem[]>([])
const selectThreadData = ref<SelectThreadVo | null>(null)
const selectThreadTotal = ref(0)
const selectThreadQuery = ref<SelectThreadDto>({
  npcId: "",
  modelCode:"",
  page: 1,
  pageSize: 1000
})
const curNpcId = ref<string>("")   //当前选择的NPC ID
const curThreadId = ref<string>("") //当前聊天Thread的ID
const curModelCode = ref<string>("")//当前选择的模型代码

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
// 是否正在加载消息
const isLoadingMessages = ref<boolean>(false)

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
  closeMobileMenu: () => void;
  setSelectedNpc: (npcId: string) => void;
  loadNpcList: () => Promise<void>;
  toggleMobileMenu: () => void;
}

const getNpcMessageList = async (npcId: string) => {
  isLoadingMessages.value = true; // 开始加载
  
  try {
    // 设置查询参数
    selectThreadQuery.value.npcId = npcId;
    selectThreadQuery.value.modelCode = curModelCode.value; // 添加 modelCode 参数

    // 调用ThreadApi获取消息列表，返回SelectThreadVo
    const response: SelectThreadVo = await ThreadApi.selectThread(selectThreadQuery.value);
    
    // 存储完整的SelectThreadVo响应
    selectThreadData.value = response;
    
    // 更新当前模型代码和线程ID
    curModelCode.value = response.modelCode;
    curThreadId.value = response.threadId;
    
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
  } finally {
    isLoadingMessages.value = false; // 结束加载
  }
}

const sendMessage = async (message: string) => {
  if (isGenerating.value) return // 如果正在生成，则不处理新的发送请求

  if (!curThreadId.value) {
    alterRef.value?.showConfirm({
      title: "未选择NPC",
      content: `请先选择一个NPC`,
      closeText: "好的",
    });
    return;
  }

  // 设置加载状态
  isGenerating.value = true;
  isLoadingMessages.value = true; // 启用发光条

  try {
    // 构建发送消息的参数
    const sendMessageDto: SendMessageDto = {
      threadId: curThreadId.value,
      type: 1, // RP会话
      modelCode: curModelCode.value,
      message: message
    };

    // 创建AI临时消息
    await createTempMsg();

    // 使用ConversationService发送消息
    const response = await ConversationService.sendMessage(sendMessageDto, (fragment: MessageFragmentVo) => {
      handleMessageFragment(fragment);
    });

    // 添加用户消息到消息列表
    const userMessage: MessageBoxItem = {
      id: response.messageId,
      name: response.senderName,
      avatarPath: response.senderAvatarUrl,
      role: 'user',
      content: response.content,
      createTime: response.sendTime
    };
    
    // 在临时消息之前插入用户消息
    const tempIndex = messageData.value.findIndex(msg => msg.id === '-1');
    if (tempIndex !== -1) {
      messageData.value.splice(tempIndex, 0, userMessage);
    } else {
      messageData.value.push(userMessage);
    }
    
    await nextTick();
    messageBoxRef.value?.scrollToBottom();

  } catch (error) {
    console.error('发送消息失败:', error);
    alterRef.value?.showConfirm({
      title: "发送消息失败",
      content: `${error}`,
      closeText: "好的",
    });
    
    // 清理临时消息和状态
    removeTempMsg();
    isGenerating.value = false;
    isLoadingMessages.value = false; // 关闭发光条
  }
};

// 处理消息片段
const handleMessageFragment = async (fragment: MessageFragmentVo) => {
  console.log('收到消息片段:', fragment);

  // 更新临时消息的元数据
  await updateTempMsg({
    id: fragment.messageId !== '-1' ? fragment.messageId : undefined,
    name: fragment.senderName,
    avatarPath: fragment.senderAvatarUrl,
    createTime: fragment.sendTime
  });

  if (fragment.type === 0) { // 起始片段
    // 起始片段，保持"正在输入..."状态
    return;
  }
  
  if (fragment.type === 1) { // 数据片段
    // 如果是第一次收到数据，清空"正在输入..."
    const tempMessage = messageData.value.find(msg => msg.id === '-1');
    if (tempMessage && tempMessage.content === '正在输入...') {
      tempMessage.content = '';
    }
    
    // 追加内容
    await appendTempMsg(fragment.content || '');
    
  } else if (fragment.type === 2) { // 结束片段
    // 如果结束时还有内容，追加它
    if (fragment.content) {
      const tempMessage = messageData.value.find(msg => msg.id === '-1');
      if (tempMessage && tempMessage.content === '正在输入...') {
        tempMessage.content = fragment.content;
      } else {
        await appendTempMsg(fragment.content);
      }
    }
    
    // 结束生成状态
    isGenerating.value = false;
    isLoadingMessages.value = false; // 关闭发光条
    
  } else if (fragment.type === 10) { // 错误片段
    console.error('AI生成错误:', fragment.content);
    alterRef.value?.showConfirm({
      title: "回复消息时发生错误",
      content: fragment.content,
      closeText: "好的",
    });
    
    // 清理临时消息和状态
    removeTempMsg();
    isGenerating.value = false;
    isLoadingMessages.value = false; // 关闭发光条
  }
};

// 处理发送消息
const onMessageSend = async (message: string) => {
  await sendMessage(message);
};

//选择模型
const onSelectMode = (modeCode:string)=>{
  curModelCode.value = modeCode;
  // 可以在这里添加逻辑：如果切换了模型，是否要影响当前会话？
  // 比如：如果当前有会话，提示用户切换模型会新建会话，或者只是更新下次新建会话的模型？
}

//选择NPC
const onSelectNpc = async (npc: GetNpcListVo) => {
  npcListRef.value?.closeMobileMenu(); 
  // 更新父组件的当前NPC ID
  curNpcId.value = npc.id;
  await getNpcMessageList(npc.id); 
};

//开始新会话
const onCreateThread = async (npc: GetNpcListVo) => {

  npcListRef.value?.closeMobileMenu(); // 关闭移动端菜单

  // 父组件主动设定子组件的选中状态
  npcListRef.value?.setSelectedNpc(npc.id);

  // 清空当前消息列表和状态
  messageData.value = []; 
  isGenerating.value = false;
  hasTempMessage.value = false; 
  curNpcId.value = npc.id; // 设置当前NPC ID

  try {
    // 使用新的 ThreadApi.createThread 接口
    const createThreadDto: CreateThreadDto = {
      modelCode: curModelCode.value,
      type: 1, // RP会话
      npcId: npc.id
    };

    const response: CreateThreadVo = await ThreadApi.createThread(createThreadDto);
    
    console.log('新会话创建成功:', response);

    // 更新当前线程ID
    curThreadId.value = response.threadId;
    
    // 会话创建完毕后获取消息列表
    await getNpcMessageList(npc.id);
    
    // 调用loadNpcList方法重新加载NPC列表
    await npcListRef.value?.loadNpcList();
    
  } catch (error) {
    console.error(`为NPC ${npc.name} 创建新会话请求失败:`, error);

    alterRef.value?.showConfirm({
      title: "故障",
      content: `${error}`,
      closeText: "关闭",
    })
    
    // 清空聊天相关状态
    messageData.value = [];
    curNpcId.value = "";
    curThreadId.value = "";
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
  
  isLoadingMessages.value = true; // 开始加载
  
  try {
    // 创建新的查询对象，只包含threadId
    const threadQuery: SelectThreadDto = {
      threadId: threadId,
      page: 1,
      pageSize: 1000
    };
    
    // 调用ThreadApi获取指定线程的消息列表
    const response: SelectThreadVo = await ThreadApi.selectThread(threadQuery);
    
    // 存储完整的SelectThreadVo响应
    selectThreadData.value = response;
    
    // 更新当前状态
    curNpcId.value = npcId;
    curModelCode.value = response.modelCode;
    curThreadId.value = response.threadId;
    
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
  } finally {
    isLoadingMessages.value = false; // 结束加载
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
    content: '',
    createTime: ""
  };
  messageData.value.push(tempAiMessage);
  hasTempMessage.value = true;
  await nextTick();
  messageBoxRef.value?.scrollToBottom();
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
  createTime?: string; // 时间 可选
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

    // 更新时间
    if (data.createTime) {
      tempMessage.createTime = data.createTime;
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

    // 使用MessageApi删除消息
    const removeDto: CommonIdDto = { id: msgId };
    await MessageApi.removeMessage(removeDto);

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
  if (isGenerating.value) return; // 如果正在生成，则不处理重新生成请求

  if (!curThreadId.value) {
    alterRef.value?.showConfirm({
      title: "未选择会话",
      content: "请先选择一个会话",
      closeText: "好的",
    });
    return;
  }

  try {
    // 设置生成状态
    isGenerating.value = true;
    isLoadingMessages.value = true; // 启用发光条

    // 检查最后一条消息是否为AI消息，如果是则删除
    if (messageData.value.length > 0) {
      const lastMessage = messageData.value[messageData.value.length - 1];
      if (lastMessage.role === 'model') {
        // 删除最后一条AI消息
        messageData.value.pop();
      }
    }

    // 创建AI临时消息
    await createTempMsg();

    // 构建重新生成的参数
    const regenerateDto: RegenerateDto = {
      threadId: curThreadId.value,
      modelCode: curModelCode.value,
      rootMessageId: "-1" // 使用-1表示最后一条用户消息
    };

    // 使用ConversationService重新生成消息
    const response = await ConversationService.regenerate(regenerateDto, (fragment: MessageFragmentVo) => {
      handleMessageFragment(fragment);
    });

    console.log('重新生成请求已发送:', response);

  } catch (error) {
    console.error('重新生成消息失败:', error);
    alterRef.value?.showConfirm({
      title: "重新生成失败",
      content: `${error}`,
      closeText: "好的",
    });
    
    // 清理临时消息和状态
    removeTempMsg();
    isGenerating.value = false;
    isLoadingMessages.value = false; // 关闭发光条
  }
};

// 处理消息更新
const onMessageEdit = async (params: { msgId: string; message: string }) => {
  try {
    // 使用MessageApi更新消息
    const editDto: EditMessageDto = {
      messageId: params.msgId,
      content: params.message
    };
    await MessageApi.editMessage(editDto);

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

// 处理中止生成
const onBatchAbort = async () => {
  if (!isGenerating.value) return; // 如果没有在生成，则不需要中止

  if (!curThreadId.value) {
    console.warn('没有当前会话ID，无法中止生成');
    return;
  }

  try {
    // 调用后端接口中止会话
    const abortDto: AbortConversationDto = {
      threadId: curThreadId.value
    };
    
    await ConversationApi.abortConversation(abortDto);
    console.log('会话中止请求已发送');
    
  } catch (error) {
    console.error('中止会话失败:', error);
    // 即使后端调用失败，也要清理前端状态
  } finally {
    // 清理前端状态
    isGenerating.value = false;
    isLoadingMessages.value = false; // 关闭发光条
    removeTempMsg();
    
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
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