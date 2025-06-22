<template>

  <GlowMobileSupport 
    :layer="1"
    :on-touch-move-right="() => {
      chatListRef?.toggleMobileMenu()
    }"
  >
    <div class="chat-layout">

      <ChatThreadList ref="chatListRef"
                      class="chat-sidebar"
                      :selected="currentThreadId"
                      @create-thread="onCreateThread"
                      @select-thread="onSelectThread"
      />

      <GlowDiv class="chat-content" border="none">

        <div class="model-selector-container">
          <ModelSelector :selected="currentModelVariantId" @select-model="onSelectMode" :allowType="[0]"/>
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

      <!-- 错误提示框组件 -->
      <GlowAlert ref="alterRef" />

      <!-- 输入框组件 -->
      <GlowConfirmInput ref="inputRef" />

    </div>

  </GlowMobileSupport>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, nextTick } from 'vue';
import ChatMessageBox from "@/components/glow-client/ChatMessageBox.vue";
import ChatThreadList from "@/components/glow-client/ChatThreadList.vue";
import ImMessageInput from "@/components/glow-client/ImMessageInput.vue";
import ModelSelector from "@/components/glow-client/ModelSelector.vue";
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme'
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue"
import GlowConfirmInput from "@/components/glow-ui/GlowConfirmInput.vue"
import GlowAlert from "@/components/glow-ui/GlowAlert.vue"
import ThreadApi from "@/commons/api/ThreadApi";
import ConversationApi from "@/commons/api/ConversationApi";
import type { SendMessageDto, QueryStreamDto, RegenerateDto, AbortConversationDto, MessageFragmentVo } from "@/commons/api/ConversationApi";
import type { SelectThreadDto } from "@/commons/api/ThreadApi";
import type CommonIdDto from "@/entity/dto/CommonIdDto";
import MessageApi from "@/commons/api/MessageApi";
import type { EditMessageDto } from "@/commons/api/MessageApi";
import GlowMobileSupport from "@/components/glow-ui/GlowMobileSupport.vue";
import type { MessageItemVo } from '@/entity/vo/MessageItemVo';

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 消息框引用
const messageBoxRef = ref<MessageBoxInstance | null>(null);
// 消息输入框引用
const messageInputRef = ref<MessageInputInstance | null>(null);
// 聊天列表引用
const chatListRef = ref<ChatListInstance | null>(null);
// 是否正在生成回复
const isGenerating = ref(false);
// 当前是否有临时消息 (移到这里)
const hasTempMessage = ref<boolean>(false)
// 是否正在加载消息
const isLoadingMessages = ref<boolean>(false)
const isCreatingThread = ref<boolean>(false)
const currentThreadId = ref<string>("")
const currentModelVariantId = ref<string>("")


const messages = ref<MessageItemVo[]>([]);

// 确认框引用
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null)
// 错误提示框引用
const alterRef = ref<InstanceType<typeof GlowAlert> | null>(null)
// 输入框引用
const inputRef = ref<InstanceType<typeof GlowConfirmInput> | null>(null)

onMounted(async () => {
  await reloadMessageList();
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
  toggleMobileMenu: () => void;
}

// 定义消息输入框实例类型
interface MessageInputInstance {
  setContent: (message: string) => void;
}


//处理消息接收回调
const onMessageReceived = (fragment: MessageFragmentVo) => {

  //0:起始 1:结束 2:错误 50:思考片段 51:文本
  if(fragment.type === 0){

  }

  if(fragment.type === 1){

  }

  if(fragment.type === 2){
    
  }

  if(fragment.type === 50){
    
  }

  if(fragment.type === 51){

  }

}



// 处理发送消息
const onMessageSend = async (message: string) => {
  if (isGenerating.value) return;

  isGenerating.value = true;
  isLoadingMessages.value = true; // 启用发光条

  try {
    // 准备发送消息的参数
    const sendMessageDto: SendMessageDto = {
      // 如果是正在创建新会话，则threadId传-1，否则使用当前currentThreadId
      threadId: isCreatingThread.value ? '-1' : (currentThreadId.value || '-1'), 
      type: 0, // 标准会话类型, TODO: 后续可能需要根据 RP模式等进行区分
      modelVariantId: currentModelVariantId.value,
      message: message
    };

    // 发送消息
    const response = await ConversationApi.sendMessage(sendMessageDto);

    // 如果之前是在创建新会话模式
    if (isCreatingThread.value) {
      isCreatingThread.value = false; // 重置状态
      if (response.newThreadCreated === 1) {
        await chatListRef.value?.loadThreadList(); // 创建了新会话，刷新列表
      }
    }

    // 更新当前会话ID（可能是新创建的，也可能是旧的）
    currentThreadId.value = response.threadId;

    // 添加用户消息到消息列表
    const userMessage: MessageItemVo = {
      id: response.messageId,
      senderName: response.senderName,
      senderAvatarUrl: response.senderAvatarUrl,
      senderRole: 0,
      content: response.content || message,
      contentThoughts: null,
      createTime: response.sendTime
    };
    messages.value.push(userMessage);

    await nextTick();
    messageBoxRef.value?.scrollToBottom();

    // 创建临时消息并开始轮询
    await createTempMsg();
    await pollMessage(response.streamId);

  } catch (error) {
    console.error('发送消息失败:', error);
    
    // 发送失败时恢复输入框内容
    messageInputRef.value?.setContent(message);
    
    alterRef.value?.showConfirm({
      title: "发送消息失败",
      content: `请检查网络连接或联系管理员。错误详情: ${error}`,
      closeText: "好的",
    });
    isGenerating.value = false;
    isLoadingMessages.value = false; // 关闭发光条
  }
};

//轮询拉取响应流
const pollMessage = async (streamId: string) => {
  let hasReceivedData = false;

  while (isGenerating.value) {
    try {
      const queryStreamDto: QueryStreamDto = {
        streamId: streamId // 使用传入的 streamId
      };

      const segment = await ConversationApi.queryStream(queryStreamDto);

      await updateTempMsg({
        id: segment.messageId,
        name: segment.senderName,
        avatarPath: segment.senderAvatarUrl,
        createTime: segment.sendTime
      });

      if (segment.type === 1) { // 数据片段
        if (!hasReceivedData) {
          const tempMessage = messages.value.find(msg => msg.id === '-1');
          if (tempMessage) tempMessage.content = '';
          hasReceivedData = true;
          await appendTempMsg(segment.content || '');
        } else {
          await appendTempMsg(segment.content || '');
        }
      } else if (segment.type === 2) { // 结束
        if (segment.content) {
          if (!hasReceivedData) {
            const tempMessage = messages.value.find(msg => msg.id === '-1');
            if (tempMessage) tempMessage.content = segment.content;
          } else {
            await appendTempMsg(segment.content);
          }
        }
        isGenerating.value = false;
        isLoadingMessages.value = false; // 关闭发光条
        break;
      } else if (segment.type === 10) { // 错误
        console.error('AI生成错误:', segment.content);
        alterRef.value?.showConfirm({
          title: "回复消息时发生错误",
          content: segment.content,
          closeText: "好的",
        });
        removeTempMsg();
        isGenerating.value = false;
        isLoadingMessages.value = false; // 关闭发光条
        break;
      }
    } catch (error) {
      console.error('轮询AI响应请求失败:', error);
      alterRef.value?.showConfirm({
        title: "轮询AI响应失败",
        content: `请检查网络连接或联系管理员。错误详情: ${error}`,
        closeText: "好的",
      });
      removeTempMsg();
      isGenerating.value = false;
      isLoadingMessages.value = false; // 关闭发光条
      break;
    }

  }
};

// 处理中止生成
const onBatchAbort = async () => {
  if (!isGenerating.value) return; // 如果没有在生成，则不需要中止

  if (!currentThreadId.value) {
    console.warn('没有当前会话ID，无法中止生成');
    return;
  }

  try {
    // 调用后端接口中止会话
    const abortDto: AbortConversationDto = {
      threadId: currentThreadId.value
    };
    
    await ConversationApi.abortConversation(abortDto);
    console.log('会话中止请求已发送');
    
  } catch (error) {
    console.error('中止会话失败:', error);
    alterRef.value?.showConfirm({
      title: "中止AI响应失败",
      content: `${error}`,
      closeText: "好的",
    });
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

//加载聊天消息列表
const reloadMessageList = async (threadId?: string) => {
  if (threadId) {
    // --- Logic for when a specific threadId is provided ---
    isLoadingMessages.value = true; // 开始加载
    
    try {
      const dto: SelectThreadDto = {
        threadId,
        page: 1,
        pageSize: 100
      };
      const response = await ThreadApi.selectThread(dto);
      currentModelVariantId.value = response.modelVariantId;
      currentThreadId.value = response.threadId;
      messages.value = response.messages.rows.map((msg): MessageItemVo => ({
        id: msg.id,
        senderName: msg.senderName,
        senderAvatarUrl: msg.senderAvatarUrl,
        senderRole: msg.senderRole,
        content: msg.content,
        contentThoughts: msg.contentThoughts,
        createTime: msg.createTime
      }));
      isCreatingThread.value = false; // Loaded a specific thread, so not in create mode
      await nextTick();
      messageBoxRef.value?.scrollToBottom();
    } catch (error) {
      console.error(`加载会话 ${threadId} 的消息列表失败:`, error);
      alterRef.value?.showConfirm({
        title: "加载会话消息失败",
        content: `请检查网络连接或联系管理员。错误详情: ${error}`,
        closeText: "好的",
      });
      messages.value = [];
      // Consider resetting currentThreadId if loading specific thread fails, 
      // or revert to a known state.
      // currentThreadId.value = ""; 
      // currentModelCode.value = "";
    } finally {
      isLoadingMessages.value = false; // 结束加载
    }
  }
};


//创建Thread
const onCreateThread = async () => {
  // 1. 检查是否已选择模型，如果未选择则提示
  if (!currentModelVariantId.value) {
    alterRef.value?.showConfirm({
      title: "操作提示",
      content: "请先选择一个模型后再创建新会话。",
      closeText: "好的",
    });
    return;
  }

  //设置为"正在创建会话"状态 // 标记当前没有选中任何实际会话
  isCreatingThread.value = true;
  messages.value = [];
  currentThreadId.value = "";

  chatListRef.value?.closeMobileMenu();

  console.log("进入创建新会话模式，等待用户输入第一条消息...");
};

//选择模型
const onSelectMode = (modelVariantId:string)=>{
  currentModelVariantId.value = modelVariantId;
}

//选择会话
const onSelectThread = async (threadId: string) => {
  console.log("选择会话:", threadId);
  currentThreadId.value = threadId;
  chatListRef.value?.closeMobileMenu();
  await reloadMessageList(threadId); 
};




const createTempMsg = async () => {
  if (hasTempMessage.value) return; // 防止重复创建

  const tempAiMessage: MessageItemVo = {
    id: '-1', 
    senderName: '----',
    senderAvatarUrl: '', 
    senderRole: 1,
    content: '正在输入...',
    contentThoughts: null,
    createTime: null
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
  createTime: string
}) => {

  if (!hasTempMessage.value) return;

  const tempMessage = messages.value.find(msg => msg.id === '-1');

  if (tempMessage) {
    let updated = false;

    // 更新名称 (如果提供了有效的名称)
    if (data.name) {
      tempMessage.senderName = data.name;
      updated = true;
    }

    // 更新头像 (如果提供了有效的头像路径)
    if (data.avatarPath) {
      tempMessage.senderAvatarUrl = data.avatarPath;
      updated = true;
    }

    // 检查是否提供了有效的永久ID
    // (假设有效的永久ID不为空且不等于临时ID '-1')
    if (data.id && data.id !== '-1') {
      tempMessage.id = data.id; // 将临时消息ID更新为永久ID
      hasTempMessage.value = false; // 标记不再有临时消息
      updated = true;
    }

    if(data.createTime){
      tempMessage.createTime = data.createTime;
      updated = true;
    }
    
    // 如果有任何更新，触发Vue的响应式更新
    if (updated) {
       messages.value = [...messages.value];
    }
  }
}

const onMessageRemove = async (msgId: string) => {

  if(!confirmRef.value) return;

  try {
    const confirmed = await confirmRef.value.showConfirm({
      title: '删除消息',
      content: '确定要删除这条消息吗？此操作不可恢复。',
      confirmText: '确认删除',
      cancelText: '取消'
    });

    if (!confirmed) return;

    // 使用新的 MessageApi.removeMessage
    const dto: CommonIdDto = { id: msgId };
    await MessageApi.removeMessage(dto);

    const index = messages.value.findIndex(msg => msg.id === msgId);
    if (index === -1) {
      console.warn(`Message ${msgId} not found locally after successful removal.`);
      return;
    }
    
    messages.value.splice(index, 1);

  } catch (error) {
    console.error(`Error removing message ${msgId}:`, error);
    alterRef.value?.showConfirm({
        title: "删除消息失败",
        content: `${error}`,
        closeText: "好的",
    });
  }
};

// 处理消息重新生成
const onMessageRegenerate = async (msgId: string) => {
  if (isGenerating.value) return;

  if (messages.value.length === 0) {
    console.warn('没有可用于重新生成的消息。');
    return;
  }
  
  console.log(`根据消息上下文重新生成，结束ID为 ${msgId}`);
  isGenerating.value = true;
  isLoadingMessages.value = true; // 启用发光条

  try {
    const lastMessageIndex = messages.value.length - 1;
    const lastMessage = messages.value[lastMessageIndex];

    if (lastMessage.senderRole === 1) {
      messages.value.pop();
      await nextTick();
    }

    await createTempMsg();

    // 调用 regenerate API
    const regenerateDto: RegenerateDto = {
      threadId: currentThreadId.value,
      modelVariantId: currentModelVariantId.value, 
      rootMessageId: "-1" // 传递需要重新生成的消息的ID
    };
    const regenerateResponse = await ConversationApi.regenerate(regenerateDto);

    console.log('后端已确认重新生成请求，开始轮询...');
    // 从 regenerateResponse 中获取 streamId
    if (regenerateResponse && regenerateResponse.streamId) {
      await pollMessage(regenerateResponse.streamId);
    } else {
      console.error('重新生成消息失败: 未收到有效的 streamId');
      throw new Error('重新生成消息失败: 未收到有效的 streamId');
    }

  } catch (error) {
    console.error('消息重新生成过程中出错:', error);
    alert('重新生成消息时发生网络错误');
    removeTempMsg();
    isGenerating.value = false;
    isLoadingMessages.value = false; // 关闭发光条
  }
};

// 处理消息更新
const onMessageEdit = async (params: { msgId: string; message: string }) => {
  try {
    // 使用新的 MessageApi.editMessage
    const dto: EditMessageDto = {
      messageId: params.msgId,
      content: params.message
    };
    await MessageApi.editMessage(dto);

    const messageIndex = messages.value.findIndex(msg => msg.id === params.msgId);
    if (messageIndex !== -1) {
      messages.value[messageIndex].content = params.message;
    }
    console.log(`Message ${params.msgId} updated successfully.`);
  } catch (error) {
    console.error(`Error updating message ${params.msgId}:`, error);
    alterRef.value?.showConfirm({
        title: "消息更新失败",
        content: `请检查网络连接或联系管理员。错误详情: ${error}`,
        closeText: "好的",
    });
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