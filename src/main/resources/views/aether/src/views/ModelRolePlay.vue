<template>
  <div class="chat-layout">

    <ModelRoleList ref="roleListRef"
                   class="chat-sidebar"
                   :data="roleList"
                   :selected="currentRoleId"
                   @select-role="onSelectRole"
                   @create-thread="onCreateThread"
                   @edit-role="onEditRole"
                   @manageThreads="onManageThreads"
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

    <GlowAlter ref="alterRef" />

    <!-- 输入框组件 -->
    <GlowConfirmInput ref="inputRef" />

    <!-- 会话管理模态框 -->
    <RoleThreadsModal 
      ref="roleThreadsModalRef"
      @activate-thread="handleActivateThread"
    />

  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, nextTick } from 'vue';
import ImMessageBox from "@/components/glow-client/ImMessageBox.vue";
import ImMessageInput from "@/components/glow-client/ImMessageInput.vue";
import ModelSelector from "@/components/glow-client/ModelSelector.vue";
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme'
import http from '@/commons/Http';
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue"
import GlowConfirmInput from "@/components/glow-ui/GlowConfirmInput.vue"
import ModelRoleList from "@/components/glow-client/ModelRoleList.vue";
import type GetModelRoleListVo from '@/entity/vo/GetModelRoleListVo.ts';
import type PageableView from '@/entity/PageableView';
import type RecoverRpChatVo from '@/entity/vo/RecoverRpChatVo.ts';
import type RecoverRpChatHistoryVo from '@/entity/vo/RecoverRpChatHistoryVo.ts';
import type GetRpLastStatusVo from '@/entity/vo/GetRpLastStatusVo.ts';
import type RpSegmentVo from '@/entity/vo/RpSegmentVo.ts';
import RoleThreadsModal from "@/components/glow-client/RoleThreadsModal.vue";
import GlowAlter from "@/components/glow-ui/GlowAlter.vue";

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme)

// 定义 ImMessageBox 需要的消息项类型
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
// 角色列表引用
const roleListRef = ref<RoleListInstance | null>(null);
// 是否正在生成回复
const isGenerating = ref(false);
// 当前是否有临时消息
const hasTempMessage = ref<boolean>(false)

const currentRoleId = ref<string>("")   //当前选择的角色ID
const currentThreadId = ref<string>("") //当前聊天Thread的ID
const currentModelCode = ref<string>("")//当前选择的模型代码

// 更新 roleList 的类型为 GetModelRoleListVo[]
const roleList = ref<GetModelRoleListVo[]>([])

// 使用 MessageBoxItem 类型定义 messages
const messages = ref<MessageBoxItem[]>([]);

const alterRef = ref<InstanceType<typeof GlowAlter> | null>(null);
// 确认框引用
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null)
// 输入框引用
const inputRef = ref<InstanceType<typeof GlowConfirmInput> | null>(null)
// 会话管理模态框引用
const roleThreadsModalRef = ref<InstanceType<typeof RoleThreadsModal> | null>(null)

onMounted(async () => {
  await reloadRoleList();
  await resumeLastStatus();
})

// 定义消息框实例类型
interface MessageBoxInstance {
  createTempMessage: (msg: any) => void;
  appendTempMessage: (content: string) => void;
  deleteTempMessage: () => void;
  scrollToBottom: () => void;
}

// 定义角色列表实例类型
interface RoleListInstance {
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
      messages.value.push(userMessage);
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
        avatarPath: "/res/"+segment.roleAvatarPath // 使用 roleAvatarPath
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

//加载聊天消息列表
const reloadMessageList = async (roleId: string, threadId?: string) => {

  // 清空当前消息列表，准备加载新的
  messages.value = []; 
  // 重置生成状态和临时消息标记（如果需要）
  isGenerating.value = false;
  hasTempMessage.value = false; 
  // 将当前角色ID设置为传入的roleId
  currentRoleId.value = roleId;

  try {
    // 构建基础请求体，始终包含 newThread: 1
    let requestBody: any = { 
      modelRoleId: roleId, 
      modelCode: currentModelCode.value, 
      newThread: 1 // 始终包含 newThread: 1
    };

    // 如果提供了 threadId，则附加它
    if (threadId) {
      requestBody.threadId = threadId;
    }

    // 发送请求
    const chatData = await http.postEntity<RecoverRpChatVo>('/model/rp/recoverRpChat', requestBody);

    // 更新当前模型代码和聊天线程ID
    currentModelCode.value = chatData.modelCode || ""; 
    currentThreadId.value = chatData.threadId || ""; // 获取后端返回的实际 threadId
    
    // currentRoleId 已经在本函数开头设置

    // 处理消息列表 - 转换格式
    const backendMessages = chatData.messages || []; 
    messages.value = backendMessages.map((msg: RecoverRpChatHistoryVo): MessageBoxItem => ({
      id: msg.id, 
      name: msg.name,
      avatarPath: msg.avatarPath,
      role: msg.type === 0 ? 'user' : 'model', 
      content: msg.rawContent, 
      createTime: msg.createTime 
    }));
    
    await nextTick();
    messageBoxRef.value?.scrollToBottom();
    return;
    
  } catch (error) {
    console.error(`恢复角色 ${roleId} ${threadId ? '的会话 ' + threadId : '的最新会话'} 请求失败:`, error);
    messages.value = []; // 确保失败时清空
    currentThreadId.value = ""; 
  }
};

//重命名函数并更新逻辑
const reloadRoleList = async () => {
  try {
    // 更新接口地址和期望的响应类型，并添加空的JSON对象作为请求体
    const data = await http.postEntity<PageableView<GetModelRoleListVo>>('/model/rp/getRoleList', {}); 
    // 从 PageableView 中提取 rows
    roleList.value = data.rows || []; 
  } catch (error) {
    console.error('加载角色列表失败:', error);
    roleList.value = []; // 清空列表
  }
}

//选择模型
const onSelectMode = (modeCode:string)=>{
  currentModelCode.value = modeCode;
  // 可以在这里添加逻辑：如果切换了模型，是否要影响当前会话？
  // 比如：如果当前有会话，提示用户切换模型会新建会话，或者只是更新下次新建会话的模型？
}

//选择会话
const onSelectRole = async (roleId: string) => {
  // When user manually selects a role, always load the latest/new thread for that role
  // Do not pass threadId here, let reloadMessageList handle the newThread=1 logic
  roleListRef.value?.closeMobileMenu(); 
  await reloadMessageList(roleId); 
};

//开始新会话
const onCreateThread = async (role:GetModelRoleListVo) => {
  console.log(`开始为角色 ${role.name} (ID: ${role.id}) 创建新会话`);
  roleListRef.value?.closeMobileMenu(); // 关闭移动端菜单

  // 清空当前消息列表和状态
  messages.value = []; 
  isGenerating.value = false;
  hasTempMessage.value = false; 
  currentRoleId.value = role.id; // 设置当前角色ID
  // currentThreadId.value 会在请求成功后被设置

  try {
    const chatData = await http.postEntity<RecoverRpChatVo>('/model/rp/recoverRpChat', { 
      modelRoleId: role.id, 
      modelCode: currentModelCode.value, 
      newThread: 0 // 明确指示创建新线程
    });

    console.log('新会话创建成功:', chatData);

    // 更新当前线程ID和模型代码
    currentThreadId.value = chatData.threadId || ""; 
    currentModelCode.value = chatData.modelCode || currentModelCode.value; // 如果后端没返回，保持当前选择

    // 处理可能返回的初始消息 (通常新会话是空的)
    const backendMessages = chatData.messages || []; 
    messages.value = backendMessages.map((msg: RecoverRpChatHistoryVo): MessageBoxItem => ({
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
    console.error(`为角色 ${role.name} 创建新会话请求失败:`, error);
    alert('创建新会话时发生网络错误');
    clearChatState();
  }
}

//编辑角色(这里桥接到旧版编辑页面)
const onEditRole = async (role:GetModelRoleListVo) => {
  if (!role.id) {
    return
  }
  window.location.href = `/dashboard?redirect=/panel/model/role/list?id=${role.id}`
}

const onManageThreads = async (role:GetModelRoleListVo) => {
  if (roleThreadsModalRef.value) {
    roleThreadsModalRef.value.show(role.id, role.name);
  }
}

// 处理激活会话事件
const handleActivateThread = async (roleId: string, threadId: string, modelCode: string) => {
  console.log(`准备激活会话: roleId=${roleId}, threadId=${threadId}, modelCode=${modelCode}`)
  // 调用现有的 reloadMessageList 函数来加载并切换到选中的会话
  // reloadMessageList 内部会处理API调用和状态更新
  await reloadMessageList(roleId, threadId)
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
  messages.value.push(tempAiMessage);
  hasTempMessage.value = true;
  await nextTick();
  messageBoxRef.value?.scrollToBottom();
  console.log(messages.value)
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

    // 调用后端 API 删除消息 - 更新接口地址和 threadId 参数
    await http.postEntity<void>('/model/rp/removeHistory', { 
      threadId: currentThreadId.value, // 使用当前的聊天线程ID
      historyId: msgId 
    });

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
    // 调用后端 API 更新消息历史 - 更新接口地址
    await http.postEntity<void>('/model/rp/editHistory', { 
      historyId: params.msgId, 
      content: params.message
    });

    // API 调用成功，更新本地消息列表
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

// 恢复最后状态函数 (移除 else)
const resumeLastStatus = async () =>{
  console.log("Attempting to resume last status...");
  try {
    const lastStatus = await http.postEntity<GetRpLastStatusVo>('/model/rp/getRpLastStatus', {});

    // 检查是否存在 lastRole
    if (lastStatus.lastRole) {
      currentRoleId.value = lastStatus.lastRole;
    }

    if(lastStatus.lastThread){
      await reloadMessageList(lastStatus.lastRole,lastStatus.lastThread)
    }

  } catch (error) {
    console.error('Error fetching last status:', error);
    // 网络错误等，不加载任何角色，清空状态
    console.log("Network error fetching status, clearing state.");
    clearChatState();
  }
}

// 辅助函数：清空聊天相关状态
const clearChatState = () => {
  messages.value = [];
  currentRoleId.value = "";
  currentThreadId.value = "";
  currentModelCode.value = "";
}

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