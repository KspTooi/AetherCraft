<template>
  <div class="chat-examples-container">
    <div class="chat-examples-header">
      <h3>NPC对话示例</h3>
      <p class="subtitle">管理NPC的对话示例，用于训练和指导AI模型生成更符合角色设定的回复</p>
      <p class="help-note">这些示例直接影响NPC的写作风格和对话语气，是塑造角色个性的重要环节。可使用 <code>#{npc}</code> 和 <code>#{player}</code> 占位符分别代表NPC和玩家的名称</p>
    </div>

    <div class="action-buttons">
      <GlowButton 
        @click="addChatExample"
        class="action-button add-button"
        title="添加对话示例">
        <i class="bi bi-plus-lg"></i>
        添加示例
      </GlowButton>
      <GlowButton
        :corners="['bottom-right']"
        @click="saveChatExamples" 
        :disabled="loading"
        class="action-button save-button"
        title="保存所有对话示例">
        <i class="bi bi-save"></i>
        保存全部
      </GlowButton>
    </div>

    <div v-if="loading" class="loading-container">
      <i class="bi bi-arrow-repeat spinning"></i>
      <p>加载中...</p>
    </div>

    <div v-else-if="list.length === 0" class="empty-list">
      <i class="bi bi-chat-square-text"></i>
      <p>暂无对话示例</p>
      <p class="help-text">添加对话示例可以帮助AI更好地理解NPC的对话风格和个性</p>
      <div class="example-format">
        <p>示例格式：</p>
        <pre>#{player}: 今天天气真不错，你觉得呢？
#{npc}: *望向窗外* 是啊，阳光明媚！这样的好天气适合去湖边散步，#{player}:你有空一起去吗？</pre>
      </div>
    </div>

    <div v-else class="examples-list">
      <GlowDiv 
        v-for="(item, index) in list" 
        :key="index" 
        class="example-item" 
        border="full">
        <div class="example-content">
          <div class="example-header">
            <div class="order-controls">
              <span class="order-label">排序:</span>
              <button 
                class="order-btn" 
                @click="moveItemUp(index)"
                :disabled="index === 0">
                <i class="bi bi-arrow-up"></i>
              </button>
              <button 
                class="order-btn" 
                @click="moveItemDown(index)"
                :disabled="index === list.length - 1">
                <i class="bi bi-arrow-down"></i>
              </button>
              <span class="order-number">{{ item.sortOrder }}</span>
            </div>
            <div class="example-actions">
              <button class="action-icon edit-btn" @click="editItem(index)">
                <i class="bi bi-pencil"></i>
              </button>
              <button class="action-icon delete-btn" @click="deleteItem(index)">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
          <GlowInputArea
            v-model="item.content"
            placeholder="#{player}: 今天天气真不错，你觉得呢？
#{npc}: *望向窗外* 是啊，阳光明媚！这样的好天气适合去湖边散步，
#{player}: 你有空一起去吗？"
            :maxLength="5000"
            :auto-resize="true"
            showLength
          />
        </div>
      </GlowDiv>
    </div>

    <!-- 添加提示组件 -->
    <GlowAlert ref="alterRef" />
    
    <!-- 添加确认对话框组件 -->
    <GlowConfirm ref="confirmRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, inject } from "vue";
import type { GetNpcChatExampleListVo } from "@/commons/api/NpcChatExampleApi.ts";
import NpcChatExampleApi from "@/commons/api/NpcChatExampleApi.ts";
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowInputArea from "@/components/glow-ui/GlowInputArea.vue";
import GlowAlert from "@/components/glow-ui/GlowAlert.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import { GLOW_THEME_INJECTION_KEY, defaultTheme, type GlowThemeColors } from '@/components/glow-ui/GlowTheme';

// 获取主题
const theme = inject<GlowThemeColors>(GLOW_THEME_INJECTION_KEY, defaultTheme);

const props = defineProps<{
  npcId: string // 当前选择的NPC ID
}>();

const list = ref<GetNpcChatExampleListVo[]>([]);
const loading = ref(false);
const alterRef = ref<InstanceType<typeof GlowAlert> | null>(null);
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null);
const hasChanges = ref(false);

// 加载对话示例列表
const loadChatExampleList = async () => {
  if (!props.npcId) return;

  try {
    loading.value = true;
    const data = await NpcChatExampleApi.getNpcChatExampleList({
      id: props.npcId
    });
    
    list.value = data || [];
    hasChanges.value = false;
    loading.value = false;
  } catch (error) {
    loading.value = false;
    alterRef.value?.showConfirm({
      title: '加载失败',
      content: error instanceof Error ? error.message : '无法加载对话示例，请稍后重试',
      closeText: '确定'
    });
  }
};

// 监听NPC ID变化，重新加载对话示例
watch(() => props.npcId, (newValue) => {
  if (newValue) {
    loadChatExampleList();
  } else {
    list.value = [];
  }
}, { immediate: true });

// 添加新的对话示例
const addChatExample = () => {
  const maxSortOrder = list.value.length > 0 
    ? Math.max(...list.value.map(item => Number(item.sortOrder) || 0)) 
    : 0;
  
  list.value.push({
    id: "", // 新创建的项没有ID
    content: "",
    sortOrder: String(maxSortOrder + 10) // 增加10作为新项的排序值
  });
  
  hasChanges.value = true;
};

// 编辑项
const editItem = (index: number) => {
  // 编辑功能通过直接编辑内容实现，这里不需要额外操作
  // 如果需要特殊处理可以在这里添加
};

// 删除项
const deleteItem = async (index: number) => {
  if (!confirmRef.value) return;
  
  const confirmed = await confirmRef.value.showConfirm({
    title: '删除对话示例',
    content: '确定要删除这个对话示例吗？此操作不可撤销。',
    confirmText: '确认删除',
    cancelText: '取消'
  });
  
  if (!confirmed) return;
  
  const item = list.value[index];
  
  if (item.id) {
    // 如果有ID，先从服务器删除
    try {
      loading.value = true;
      await NpcChatExampleApi.removeNpcChatExample({
        id: item.id
      });
      list.value.splice(index, 1);
      loading.value = false;
      
    } catch (error) {
      loading.value = false;
      alterRef.value?.showConfirm({
        title: '删除失败',
        content: error instanceof Error ? error.message : '删除对话示例失败，请稍后重试',
        closeText: '确定'
      });
    }
  } else {
    // 如果没有ID，直接从列表中移除
    list.value.splice(index, 1);
    hasChanges.value = true;
  }
};

// 上移项
const moveItemUp = (index: number) => {
  if (index <= 0) return;
  
  // 交换排序号
  const temp = list.value[index].sortOrder;
  list.value[index].sortOrder = list.value[index-1].sortOrder;
  list.value[index-1].sortOrder = temp;
  
  // 交换位置
  [list.value[index], list.value[index-1]] = [list.value[index-1], list.value[index]];
  
  hasChanges.value = true;
};

// 下移项
const moveItemDown = (index: number) => {
  if (index >= list.value.length - 1) return;
  
  // 交换排序号
  const temp = list.value[index].sortOrder;
  list.value[index].sortOrder = list.value[index+1].sortOrder;
  list.value[index+1].sortOrder = temp;
  
  // 交换位置
  [list.value[index], list.value[index+1]] = [list.value[index+1], list.value[index]];
  
  hasChanges.value = true;
};

// 保存所有对话示例
const saveChatExamples = async () => {
  if (!props.npcId || list.value.length === 0) return;
  
  try {
    loading.value = true;
    
    await NpcChatExampleApi.saveNpcChatExample({
      npcId: props.npcId,
      examples: list.value.map(item => ({
        id: item.id || undefined,
        content: item.content,
        sortOrder: parseInt(item.sortOrder)
      }))
    });
    
    loading.value = false;
    hasChanges.value = false;
    
    // 重新加载列表以获取最新数据
    await loadChatExampleList();
    
    alterRef.value?.showConfirm({
      title: '保存成功',
      content: '所有对话示例已保存',
      closeText: '确定'
    });
  } catch (error) {
    loading.value = false;
    alterRef.value?.showConfirm({
      title: '保存失败',
      content: error instanceof Error ? error.message : '保存对话示例失败，请稍后重试',
      closeText: '确定'
    });
  }
};

// 初始化时加载对话示例列表
onMounted(() => {
  if (props.npcId) {
    loadChatExampleList();
  }
});
</script>

<style scoped>
.chat-examples-container {
  padding: 10px 0;
}

.chat-examples-header {
  margin-bottom: 20px;
}

.chat-examples-header h3 {
  font-size: 18px;
  margin: 0 0 5px 0;
  color: v-bind('theme.boxTextColor');
}

.subtitle {
  font-size: 14px;
  margin: 0;
  color: v-bind('theme.boxTextColorNoActive');
}

.help-note {
  font-size: 13px;
  margin: 8px 0 0 0;
  color: v-bind('theme.boxTextColorNoActive');
  line-height: 1.5;
  max-width: 100%;
  text-align: left;
}

.help-note code {
  background-color: rgba(0, 0, 0, 0.2);
  padding: 2px 4px;
  border-radius: 2px;
  font-family: monospace;
  color: v-bind('theme.boxGlowColor');
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
  gap: 8px;
}

.action-button {
  min-width: 32px;
  min-height: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-button i {
  margin-right: 5px;
}

.add-button {
  background-color: v-bind('theme.boxSecondColor');
  border-color: v-bind('theme.boxBorderColor');
}

.add-button:hover {
  background-color: v-bind('theme.boxSecondColorHover');
  border-color: v-bind('theme.boxBorderColorHover');
}

.save-button {
  background-color: v-bind('theme.mainColor');
  border-color: v-bind('theme.mainBorderColor');
  color: v-bind('theme.mainTextColor');
}

.save-button:hover {
  background-color: v-bind('theme.mainColorHover');
  border-color: v-bind('theme.mainBorderColorHover');
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: v-bind('theme.boxTextColorNoActive');
}

.loading-container i {
  font-size: 32px;
  margin-bottom: 10px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: v-bind('theme.boxTextColorNoActive');
}

.empty-list i {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-list p {
  margin: 0;
  font-size: 16px;
}

.help-text {
  font-size: 14px;
  margin-top: 8px;
  max-width: 300px;
  text-align: center;
}

.example-format {
  margin-top: 15px;
  width: 95%;
  max-width: 550px;
  background-color: rgba(0, 0, 0, 0.2);
  border: 1px solid v-bind('theme.boxBorderColor');
  border-radius: 0;
  text-align: left;
}

.example-format p {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: v-bind('theme.boxTextColor');
  padding: 10px 10px 0 10px;
}

.example-format pre {
  margin: 0;
  padding: 10px;
  background-color: transparent;
  border-radius: 0;
  font-family: monospace;
  font-size: 13px;
  color: v-bind('theme.boxGlowColor');
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
}

.examples-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.example-item {
  padding: 15px;
}

.example-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.example-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-controls {
  display: flex;
  align-items: center;
  gap: 5px;
}

.order-label {
  font-size: 12px;
  color: v-bind('theme.boxTextColorNoActive');
}

.order-btn {
  background: transparent;
  border: 1px solid v-bind('theme.boxBorderColor');
  color: v-bind('theme.boxTextColor');
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 0;
}

.order-btn:hover:not(:disabled) {
  background: v-bind('theme.boxSecondColorHover');
  border-color: v-bind('theme.boxBorderColorHover');
}

.order-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.order-number {
  font-size: 12px;
  min-width: 20px;
  text-align: center;
}

.example-actions {
  display: flex;
  gap: 5px;
}

.action-icon {
  background: transparent;
  border: 1px solid v-bind('theme.boxBorderColor');
  color: v-bind('theme.boxTextColor');
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 0;
}

.edit-btn:hover {
  background: v-bind('theme.boxSecondColorHover');
  border-color: v-bind('theme.boxBorderColorHover');
}

.delete-btn:hover {
  background: v-bind('theme.dangerColor');
  border-color: v-bind('theme.dangerBorderColor');
  color: v-bind('theme.dangerTextColor');
}
</style>