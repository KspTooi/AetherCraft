<template>
  <GlowMobileSupport 
    :layer="1"
    :on-touch-move-right="() => {
      npcListRef?.toggleMobileMenu()
    }"
  >
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
          <ModelSelector :selected="curModelVariantId" @select-model="onSelectMode" :allowType="[0]"/>
        </div>

        <div class="message-box-container">
          <ChatMessageBox
             ref="messageBoxRef" 
             :data="messages" 
             :isGenerating="isGenerating"
             :loading="isLoadingMessages"
             @update-message="onMessageEdit"
             @delete-message="onMessageRemove"
             @regenerate="onMessageRegenerate"
           />
        </div>

        <div class="message-input-container">
          <ImMessageInput
              ref="messageInputRef"
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

      <GlowAlert ref="alterRef" />

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
import GlowAlert from "@/components/glow-ui/GlowAlert.vue";
import { useRouter } from 'vue-router';
import type { SelectThreadDto, SelectThreadVo, CreateThreadDto, CreateThreadVo } from '@/commons/api/ThreadApi';
import ThreadApi from '@/commons/api/ThreadApi';
import ConversationService from '@/views/service/ConversationService';
import type { SendMessageDto, MessageFragmentVo, RegenerateDto, AbortConversationDto } from '@/commons/api/ConversationApi';
import ConversationApi from '@/commons/api/ConversationApi';
import MessageApi, { type EditMessageDto } from '@/commons/api/MessageApi';
import type CommonIdDto from '@/entity/dto/CommonIdDto';
import GlowMobileSupport from "@/components/glow-ui/GlowMobileSupport.vue";
import type { MessageItemVo } from '@/entity/vo/MessageItemVo';
import TempMessageService from './service/TempMessageService';

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)
const router = useRouter();
const messages = ref<MessageItemVo[]>([])
const selectThreadData = ref<SelectThreadVo | null>(null)
const selectThreadTotal = ref(0)
const selectThreadQuery = ref<SelectThreadDto>({
  npcId: "",
  modelVariantId:"",
  page: 1,
  pageSize: 1000
})
const curNpcId = ref<string>("")   //当前选择的NPC ID
const curThreadId = ref<string>("") //当前聊天Thread的ID
const curModelVariantId = ref<string>("")//当前选择的模型变体ID

// 消息框引用
const messageBoxRef = ref<MessageBoxInstance | null>(null);
// 消息输入框引用
const messageInputRef = ref<MessageInputInstance | null>(null);
// NPC列表引用
const npcListRef = ref<NpcListInstance | null>(null);
// 是否正在生成回复
const isGenerating = ref(false);
// 当前是否有临时消息
const hasTempMessage = ref<boolean>(false)
// 是否正在加载消息
const isLoadingMessages = ref<boolean>(false)

const alterRef = ref<InstanceType<typeof GlowAlert> | null>(null);
// 确认框引用
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null)
// 输入框引用
const inputRef = ref<InstanceType<typeof GlowConfirmInput> | null>(null)
// 会话管理模态框引用
const roleThreadsModalRef = ref<InstanceType<typeof ChatNpcThreadsModal> | null>(null)
//上一条发送的消息内容
const lastSendMessageContent = ref<string>("");

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

// 定义消息输入框实例类型
interface MessageInputInstance {
  setContent: (message: string) => void;
}

const getNpcMessageList = async (npcId: string) => {
  isLoadingMessages.value = true; // 开始加载
  
  try {
    // 设置查询参数
    selectThreadQuery.value.npcId = npcId;
    selectThreadQuery.value.modelVariantId = curModelVariantId.value; // 添加 modelVariantId 参数

    // 调用ThreadApi获取消息列表，返回SelectThreadVo
    const response: SelectThreadVo = await ThreadApi.selectThread(selectThreadQuery.value);
    
    // 存储完整的SelectThreadVo响应
    selectThreadData.value = response;
    
    // 更新当前模型代码和线程ID
    curModelVariantId.value = response.modelVariantId;
    curThreadId.value = response.threadId;
    
    // 转换消息格式
    const messageList = response.messages.rows || [];
    messages.value = messageList.map((msg): MessageItemVo => ({
      id: msg.id,
      senderName: msg.senderName,
      senderAvatarUrl: msg.senderAvatarUrl,
      senderRole: msg.senderRole,
      content: msg.content,
      contentThoughts: msg.contentThoughts,
      createTime: msg.createTime,
      status: 3 //0:等待响应 1:正在计算 2:正在输入 3:结束
    }));
    
    // 更新总数
    selectThreadTotal.value = response.messages.count || 0;
    
    // 滚动到底部
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
    
  } catch (error) {
    console.error(`获取NPC ${npcId} 消息列表失败:`, error);
    messages.value = [];
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


const startGenerate = () => {
  isGenerating.value = true;
  isLoadingMessages.value = true; // 启用发光条
}
const endGenerate = () => {
  isGenerating.value = false;
  isLoadingMessages.value = false; // 关闭发光条
}


//处理消息接收回调
const onMessageReceived = (fragment: MessageFragmentVo) => {

  //0:起始 1:结束 2:错误 50:思考片段 51:文本
  if(fragment.type === 0){
    //起始片段不做任何处理
  }

  //1:结束
  if(fragment.type === 1){

    //更新临时消息状态
    TempMessageService.updateTempMessage(messages, {
      id: fragment.messageId,
      name: fragment.senderName,
      avatarPath: fragment.senderAvatarUrl,
      createTime: fragment.sendTime
    });

    endGenerate();
  }

  //2:错误
  if(fragment.type === 2){
    //删除临时消息
    TempMessageService.deleteTempMessage(messages);

    //提示用户错误
    alterRef.value?.showConfirm({
      title: "回复消息时发生错误",
      content: fragment.content,
      closeText: "好的",
    });

    //恢复上一条发送的消息内容
    messageInputRef.value?.setContent(lastSendMessageContent.value);
    endGenerate();
  }

  //50:思考片段
  if(fragment.type === 50){
    TempMessageService.appendContentThoughts(messages, fragment.content);

    //更新临时消息状态
    TempMessageService.updateTempMessage(messages, {
      id: fragment.messageId,
      name: fragment.senderName,
      avatarPath: fragment.senderAvatarUrl,
      createTime: fragment.sendTime
    });

  }

  //51:文本
  if(fragment.type === 51){
    TempMessageService.appendContent(messages, fragment.content);

    //更新临时消息状态
    TempMessageService.updateTempMessage(messages, {
      id: fragment.messageId,
      name: fragment.senderName,
      avatarPath: fragment.senderAvatarUrl,
      createTime: fragment.sendTime
    });
  }

}



const sendMessage = async (message: string) => {

  if (isGenerating.value) return // 如果正在生成，则不处理新的发送请求

  //开始生成
  startGenerate();
  lastSendMessageContent.value = message;


  if (!curThreadId.value) {
    // 发送失败时恢复输入框内容
    messageInputRef.value?.setContent(message);
    
    alterRef.value?.showConfirm({
      title: "未选择NPC",
      content: `请先选择一个NPC`,
      closeText: "好的",
    });
    return;
  }


  try {

    // 使用ConversationService发送消息
    const response = await ConversationService.sendMessage({
      threadId: curThreadId.value, 
      type: 1, 
      modelVariantId: curModelVariantId.value, 
      message: message
    }, onMessageReceived);

    
    // 添加用户消息到消息列表
    const userMessage: MessageItemVo = {
      id: response.messageId,
      status: 3, //0:等待响应 1:正在计算 2:正在输入 3:结束
      senderName: response.senderName,
      senderAvatarUrl: response.senderAvatarUrl,
      senderRole: 0,
      content: response.content,
      contentThoughts: null,
      createTime: response.sendTime
    };

    messages.value.push(userMessage);

    //创建临时消息
    TempMessageService.createTempMessage(messages);

    await nextTick();
    messageBoxRef.value?.scrollToBottom();

  } catch (error) {
    console.error('发送消息失败:', error);
    
    // 发送失败时恢复输入框内容
    messageInputRef.value?.setContent(message);
    
    alterRef.value?.showConfirm({
      title: "发送消息失败",
      content: `${error}`,
      closeText: "好的",
    });
    //删除临时消息
    TempMessageService.deleteTempMessage(messages);
    //恢复上一条发送的消息内容
    messageInputRef.value?.setContent(lastSendMessageContent.value);
    endGenerate();
  }
};

// 处理发送消息
const onMessageSend = async (message: string) => {
  await sendMessage(message);
};

//选择模型
const onSelectMode = (modelVariantId:string)=>{
  curModelVariantId.value = modelVariantId;
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
  messages.value = []; 
  isGenerating.value = false;
  hasTempMessage.value = false; 
  curNpcId.value = npc.id; // 设置当前NPC ID

  try {
    // 使用新的 ThreadApi.createThread 接口
    const createThreadDto: CreateThreadDto = {
      modelVariantId: curModelVariantId.value,
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
    messages.value = [];
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
const handleActivateThread = async (npcId: string, threadId: string, modelVariantId: string) => {
  console.log(`准备激活会话: npcId=${npcId}, threadId=${threadId}, modelVariantId=${modelVariantId}`)
  
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
    curModelVariantId.value = response.modelVariantId;
    curThreadId.value = response.threadId;
    
    // 转换消息格式
    const messageList = response.messages.rows || [];
    messages.value = messageList.map((msg): MessageItemVo => ({
      id: msg.id,
      senderName: msg.senderName,
      status: 3, //0:等待响应 1:正在计算 2:正在输入 3:结束
      senderAvatarUrl: msg.senderAvatarUrl,
      senderRole: msg.senderRole,
      content: msg.content,
      contentThoughts: msg.contentThoughts,
      createTime: msg.createTime
    }));
    
    // 更新总数
    selectThreadTotal.value = response.messages.count || 0;
    
    // 滚动到底部
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
    
  } catch (error) {
    console.error(`激活会话失败: npcId=${npcId}, threadId=${threadId}`, error);
    messages.value = [];
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

  startGenerate();

  //删除最后一条AI消息
  const lastMessageIndex = messages.value.length - 1;
  const lastMessage = messages.value[lastMessageIndex];

  if(lastMessage.senderRole === 1){
    messages.value.pop();
    await nextTick();
  }

  //创建临时消息
  TempMessageService.createTempMessage(messages);


  try {

    // 使用ConversationService重新生成消息
    const response = await ConversationService.regenerate({
      threadId: curThreadId.value,
      modelVariantId: curModelVariantId.value, 
      rootMessageId: "-1"
    }, onMessageReceived);

    console.log('重新生成请求已发送:', response);

  } catch (error) {
    console.error('重新生成消息失败:', error);
    alterRef.value?.showConfirm({
      title: "重新生成失败",
      content: `${error}`,
      closeText: "好的",
    });
    //删除临时消息
    TempMessageService.deleteTempMessage(messages);
    //恢复上一条发送的消息内容
    messageInputRef.value?.setContent(lastSendMessageContent.value);
    endGenerate();
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
    const messageIndex = messages.value.findIndex(msg => msg.id === params.msgId);
    if (messageIndex !== -1) {
      messages.value[messageIndex].content = params.message;
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
    endGenerate();
    //删除临时消息
    TempMessageService.deleteTempMessage(messages);
    //恢复上一条发送的消息内容
    messageInputRef.value?.setContent(lastSendMessageContent.value);
    
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