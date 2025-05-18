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

    <!-- 错误提示框组件 -->
    <GlowAlter ref="alterRef" />

    <!-- 输入框组件 -->
    <GlowConfirmInput ref="inputRef" />

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
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue"
import GlowConfirmInput from "@/components/glow-ui/GlowConfirmInput.vue"
import GlowAlter from "@/components/glow-ui/GlowAlter.vue"
import type Result from '@/entity/Result';
import type ChatSegmentVo from '@/entity/vo/ChatSegmentVo.ts';
import Http from "@/commons/Http";
import ThreadApi from "@/commons/api/ThreadApi";
import ConversationApi from "@/commons/api/ConversationApi";
import type { GetThreadListDto, GetThreadListVo } from "@/commons/api/ThreadApi";
import type { SendMessageDto, SendMessageVo, QueryStreamDto, MessageFragmentVo, RegenerateDto, AbortConversationDto } from "@/commons/api/ConversationApi";
import type RestPageableView from "@/entity/RestPageableView";
import type { SelectThreadDto, EditThreadTitleDto } from "@/commons/api/ThreadApi";
import type CommonIdDto from "@/entity/dto/CommonIdDto";

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
  active: number
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
// 错误提示框引用
const alterRef = ref<InstanceType<typeof GlowAlter> | null>(null)
// 输入框引用
const inputRef = ref<InstanceType<typeof GlowConfirmInput> | null>(null)

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
  if (isGenerating.value) return;

  isGenerating.value = true;

  try {
    // 准备发送消息的参数
    const sendMessageDto: SendMessageDto = {
      threadId: currentThreadId.value || undefined, // 为空时创建新会话
      type: 0, // 标准会话类型
      modelCode: currentModelCode.value,
      message: message
    };

    // 发送消息
    const response = await ConversationApi.sendMessage(sendMessageDto);

    // 更新当前会话ID（可能是新创建的）
    currentThreadId.value = response.threadId;

    // 添加用户消息到消息列表
    messages.value.push({
      id: response.messageId,
      name: response.senderName,
      avatarPath: response.senderAvatarUrl,
      role: 'user',
      content: response.content || message,
      createTime: response.sendTime
    });

    await nextTick();
    messageBoxRef.value?.scrollToBottom();

    // 创建临时消息并开始轮询
    await createTempMsg();
    await pollMessage(response.streamId);

  } catch (error) {
    console.error('发送消息失败:', error);
    alterRef.value?.showConfirm({
      title: "发送消息失败",
      content: `请检查网络连接或联系管理员。错误详情: ${error}`,
      closeText: "好的",
    });
    isGenerating.value = false;
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

      if (!segment || segment.type === 0) {
        await new Promise(resolve => setTimeout(resolve, 500));
        continue;
      }

      await updateTempMsg({
        id: segment.messageId,
        name: segment.senderName,
        avatarPath: segment.senderAvatarUrl
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
      break;
    }

    if (isGenerating.value) {
      await new Promise(resolve => setTimeout(resolve, 500));
    }
  }
};

// 处理中止生成
const onBatchAbort = async () => {
  if (!isGenerating.value) return;

  try {
    await Http.postEntity<void>('/model/chat/completeBatch', {
      threadId: currentThreadId.value,
      queryKind: 2
    });
    console.log('已中止AI响应');
  } catch (error) {
    console.error('中止AI响应请求失败:', error);
    alterRef.value?.showConfirm({
      title: "中止AI响应失败",
      content: `${error}`,
      closeText: "好的",
    });
  } finally {
    isGenerating.value = false;
    removeTempMsg();
    await nextTick(); 
    messageBoxRef.value?.scrollToBottom();
  }
};

//加载聊天消息列表
const reloadMessageList = async (threadId: string) => {
  try {
    // 准备请求参数
    const dto: SelectThreadDto = {
      threadId,
      page: 1,      // 页码，从PageQuery.ts定义
      pageSize: 10000  // 每页数量，从PageQuery.ts定义
    };

    const response = await ThreadApi.selectThread(dto);
    
    // 更新当前会话的模型代码和ID
    currentModelCode.value = response.modelCode;
    currentThreadId.value = response.threadId;

    // 将消息数据转换为本地格式
    messages.value = response.messages.rows.map(msg => ({
      id: msg.id,
      name: msg.senderName,
      avatarPath: msg.senderAvatarUrl,
      role: msg.senderRole === 0 ? 'user' : 'model',
      content: msg.content,
      createTime: msg.createTime
    }));
    
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
  } catch (error) {
    console.error(`恢复会话 ${threadId} 请求失败:`, error);
    // 通过alterRef向用户显示错误信息
    alterRef.value?.showConfirm({
      title: "加载会话消息失败",
      content: `请检查网络连接或联系管理员。错误详情: ${error}`,
      closeText: "好的",
    });
    messages.value = [];
  }
};

//加载会话列表
const reloadThreadList = async () => {
  try {
    // 准备请求参数
    const dto: GetThreadListDto = {
      type: 0, // 标准会话类型
      page: 1,    // 页码，从PageQuery.ts定义
      pageSize: 100   // 每页数量，从PageQuery.ts定义
    };

    const response: RestPageableView<GetThreadListVo> = await ThreadApi.getThreadList(dto);
    
    // 将API返回的分页数据中的rows转换为本地数据格式
    threadList.value = response.rows.map(thread => ({
      id: thread.id,
      title: thread.title,
      modelCode: thread.modelCode,
      active: thread.active // 直接使用 active
    }));
    
    const defaultThread = threadList.value.find(t => t.active === 1);
    if (defaultThread) {
      currentThreadId.value = defaultThread.id;
      // reloadMessageList 将在后续更新为使用新的API
      await reloadMessageList(defaultThread.id);
    } else {
      messages.value = [];
      currentThreadId.value = "";
      currentModelCode.value = "";
    }
  } catch (error) {
    console.error('加载会话列表失败:', error);
    // 通过alterRef向用户显示错误信息
    alterRef.value?.showConfirm({
        title: "加载会话列表失败",
        content: `请检查网络连接或联系管理员。错误详情: ${error}`,
        closeText: "好的",
    });
  }
};

//创建Thread
const onCreateThread = async () => {
  try {
    const { threadId: newThreadId } = await Http.postEntity<{ threadId: string }>('/model/chat/createEmptyThread', {
      model: currentModelCode.value,
    });

    await reloadThreadList();
    currentThreadId.value = newThreadId;
    await reloadMessageList(newThreadId);
    chatListRef.value?.closeMobileMenu();
  } catch (error) {
    console.error('创建会话请求失败:', error);
    alert('创建会话时发生网络错误');
  }
};

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
    const confirmed = await confirmRef.value?.showConfirm({
      title: '删除会话',
      content: '确定要删除这个会话吗？删除后无法恢复',
      confirmText: '删除',
      cancelText: '取消'
    });

    if (!confirmed) return;

    // 使用新的API
    const dto: CommonIdDto = { id: threadId };
    await ThreadApi.removeThread(dto);

    await reloadThreadList(); // 重新加载列表以反映删除
  } catch (error) {
    console.error('删除会话失败:', error);
    alterRef.value?.showConfirm({
      title: "删除会话失败",
      content: `请检查网络连接或联系管理员。错误详情: ${error}`,
      closeText: "好的",
    });
  }
};

//更新会话标题
const onUpdateThreadTitle = async (threadId: string, oldTitle: string) => {
  try {
    const result = await inputRef.value?.showInput({
      title: '编辑会话标题',
      defaultValue: oldTitle,
      placeholder: '请输入新的标题',
      confirmText: '保存',
      cancelText: '取消'
    });

    if (!result || !result.confirmed || !result.value || result.value === oldTitle) {
      return;
    }

    // 使用新的API
    const dto: EditThreadTitleDto = {
      threadId: threadId,
      title: result.value
    };
    await ThreadApi.editThreadTitle(dto);

    await reloadThreadList(); // 重新加载列表以反映标题更新
  } catch (error) {
    console.error('更新标题失败:', error);
    alterRef.value?.showConfirm({
      title: "更新标题失败",
      content: `请检查网络连接或联系管理员。错误详情: ${error}`,
      closeText: "好的",
    });
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
    const confirmed = await confirmRef.value.showConfirm({
      title: '删除消息',
      content: '确定要删除这条消息吗？此操作不可恢复。',
      confirmText: '确认删除',
      cancelText: '取消'
    });

    if (!confirmed) return;

    await Http.postEntity<void>('/model/chat/removeHistory', { 
      threadId: currentThreadId.value, 
      historyId: msgId 
    });

    const index = messages.value.findIndex(msg => msg.id === msgId);
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
  if (isGenerating.value) return;

  if (messages.value.length === 0) {
    console.warn('没有可用于重新生成的消息。');
    return;
  }
  
  console.log(`根据消息上下文重新生成，结束ID为 ${msgId}`);
  isGenerating.value = true;

  try {
    const lastMessageIndex = messages.value.length - 1;
    const lastMessage = messages.value[lastMessageIndex];

    if (lastMessage.role === 'model') {
      messages.value.pop();
      await nextTick();
    }

    await createTempMsg();

    // 调用 regenerate API
    const regenerateDto: RegenerateDto = {
      threadId: currentThreadId.value,
      modelCode: currentModelCode.value, 
      rootMessageId: msgId // 传递需要重新生成的消息的ID
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
  }
};

// 处理消息更新
const onMessageEdit = async (params: { msgId: string; message: string }) => {
  try {
    await Http.postEntity<void>('/model/chat/editHistory', {
      historyId: params.msgId,
      content: params.message
    });

    const messageIndex = messages.value.findIndex(msg => msg.id === params.msgId);
    if (messageIndex !== -1) {
      messages.value[messageIndex].content = params.message;
    }
    console.log(`Message ${params.msgId} updated successfully.`);
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