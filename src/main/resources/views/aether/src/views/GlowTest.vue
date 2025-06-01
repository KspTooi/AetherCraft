<script setup lang="ts">
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import GlowTheme from "@/components/glow-ui/GlowTheme.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowModal from "@/components/glow-ui/GlowModal.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import GlowAlert from "@/components/glow-ui/GlowAlert.vue";
import { ref } from 'vue';
import GlowColorPicker from "@/components/glow-ui/GlowColorPicker.vue";
import GlowInput from "@/components/glow-ui/GlowInput.vue";
import GlowInputArea from "@/components/glow-ui/GlowInputArea.vue";
import GlowCheckBox from "@/components/glow-ui/GlowCheckBox.vue";

// 控制模态框显示状态
const showModal = ref(false);
const showDangerModal = ref(false);

// 确认框引用
const confirmRef = ref();
const alterRef = ref();

// 打开模态框
const openModal = () => {
  showModal.value = true;
};

// 打开危险操作模态框
const openDangerModal = () => {
  showDangerModal.value = true;
};

// 打开确认对话框
const openConfirm = async () => {
  const result = await confirmRef.value.showConfirm({
    title: '操作确认',
    content: '您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？您确定要执行此操作吗？',
    confirmText: '确定',
    cancelText: '取消'
  });
  
  if (result) {
    alert('操作已确认');
  } else {
    alert('操作已取消');
  }
};

// 打开自定义确认对话框
const openCustomConfirm = async () => {
  const result = await confirmRef.value.showConfirm({
    title: '删除确认',
    content: '您确定要删除这些数据吗？此操作不可恢复！',
    confirmText: '删除',
    cancelText: '返回'
  });
  
  if (result) {
    alert('删除操作已确认');
  }
};

// 打开只有关闭按钮的提示框
const openAlter = async () => {
  await alterRef.value.showConfirm({
    title: '操作提示',
    content: '您的操作已完成！',
    closeText: '关闭'
  });
};

// 打开自定义提示框
const openCustomAlter = async () => {
  await alterRef.value.showConfirm({
    title: '系统通知',
    content: '系统升级完成，请刷新页面以获取最新功能。',
    closeText: '我知道了'
  });
};

// 确认操作
const confirmAction = () => {
  alert('操作已确认');
  showModal.value = false;
};

// 确认危险操作
const confirmDangerAction = () => {
  alert('危险操作已确认');
  showDangerModal.value = false;
};

// 颜色选择器测试
const selectedColor = ref('rgba(0, 196, 255, 0.8)');
const handleColorChange = (color: string) => {
  selectedColor.value = color;
  console.log('选择的颜色:', color);
};

// GlowInput测试
const inputValue = ref('');
const maxInputLength = ref(20);
const inputWithLimit = ref('');
const seriesName = ref('');
const textareaValue = ref('这是一个多行文本输入框示例，支持多行内容输入。');
const autoResizeValue = ref('这是一个自动调整高度的文本域，当内容增加时会自动扩展高度。\n尝试添加更多行来查看效果。');
const fixedTextareaValue = ref('这是一个固定大小的文本域，不允许用户手动调整大小。');
const notBlankInputValue = ref('');
const typeDoneResult = ref('');

// 当用户停止输入时触发
const handleTypeDone = (value: string) => {
  typeDoneResult.value = `用户停止输入：${value ? value : '空值'}`;
  console.log('用户停止输入:', value);
};

// GlowCheckBox测试
const checkValue1 = ref(false);
const checkValue2 = ref(true);
const checkValue3 = ref(false);
const checkValue4 = ref(false);
</script>

<template>
  <GlowTheme>
    <div class="test-container">
      <h2>Laser UI 组件测试</h2>

      <div class="component-section">
        <h3>GlowCheckBox 复选框组件</h3>
        <div style="display: flex; flex-direction: column; gap: 15px; margin-bottom: 20px;">
          <div>
            <GlowCheckBox v-model="checkValue1">未选中状态的复选框</GlowCheckBox>
            <p style="margin-top: 5px; font-size: 14px;">当前值: {{ checkValue1 }}</p>
          </div>
          
          <div>
            <GlowCheckBox v-model="checkValue2">已选中状态的复选框</GlowCheckBox>
            <p style="margin-top: 5px; font-size: 14px;">当前值: {{ checkValue2 }}</p>
          </div>
          
          <div>
            <GlowCheckBox v-model="checkValue3">点击切换选中状态</GlowCheckBox>
            <p style="margin-top: 5px; font-size: 14px;">当前值: {{ checkValue3 }}</p>
          </div>
          
          <div>
            <GlowCheckBox v-model="checkValue4" tip="这是一个带有提示信息的复选框，鼠标悬停时会显示此提示">带提示信息的复选框</GlowCheckBox>
            <p style="margin-top: 5px; font-size: 14px;">当前值: {{ checkValue4 }}</p>
          </div>
        </div>
      </div>

      <div class="component-section">
        <h3>GlowInput 输入框组件</h3>
        <div style="display: flex; flex-direction: column; gap: 20px; margin-bottom: 20px;">
          <div>
            <p>基本输入框：{{ inputValue }}</p>
            <GlowInput v-model="inputValue" placeholder="请输入内容" />
          </div>
          
          <div>
            <p>带字数限制的输入框：{{ inputWithLimit.length }}/{{ maxInputLength }}</p>
            <GlowInput 
              v-model="inputWithLimit" 
              :max-length="maxInputLength" 
              :show-length="true"
              placeholder="最多输入20个字符" 
            />
          </div>
          
          <div>
            <p>禁用状态的输入框</p>
            <GlowInput disabled placeholder="禁用状态" />
          </div>
          
          <div>
            <p>带标题的输入框</p>
            <GlowInput 
              v-model="seriesName" 
              title="访问列表名称" 
              placeholder="请输入列表名称"
            />
          </div>
          
          <div>
            <p>当输入停止时触发事件的输入框</p>
            <GlowInput 
              v-model="inputValue" 
              title="停止输入触发事件" 
              placeholder="输入后停顿0.5秒将触发事件"
              @typeDone="handleTypeDone"
            />
            <p v-if="typeDoneResult" style="color: #4caf50; font-size: 12px; margin-top: 4px;">
              {{ typeDoneResult }}
            </p>
          </div>
          
          <div>
            <p>必填输入框（为空时显示错误边框）</p>
            <GlowInput 
              v-model="notBlankInputValue" 
              :notBlank="true"
              title="必填字段" 
              placeholder="此字段不能为空"
            />
          </div>
        </div>

        <h3>GlowInputArea 文本域组件</h3>
        <div style="display: flex; flex-direction: column; gap: 20px; margin-bottom: 20px;">
          <div>
            <p>基本文本域：</p>
            <GlowInputArea
              v-model="textareaValue"
              :rows="4"
              placeholder="请输入多行文本"
            />
          </div>
          
          <div>
            <p>带字数限制的文本域：</p>
            <GlowInputArea
              v-model="inputWithLimit"
              :max-length="maxInputLength"
              :show-length="true"
              title="限制字数的文本域"
              placeholder="最多输入20个字符"
            />
          </div>
          
          <div>
            <p>自动调整高度的文本域：</p>
            <GlowInputArea
              v-model="autoResizeValue"
              :auto-resize="true"
              title="自动调整高度"
              placeholder="输入文本时会自动调整高度"
            />
          </div>
          
          <div>
            <p>禁用调整大小的文本域：</p>
            <GlowInputArea
              v-model="fixedTextareaValue"
              :no-resize="true"
              title="禁用调整大小"
              placeholder="不允许用户调整大小的文本域"
            />
          </div>
          
          <div>
            <p>必填文本域（为空时显示错误边框）</p>
            <GlowInputArea
              v-model="notBlankInputValue"
              :notBlank="true"
              title="必填文本域"
              placeholder="此文本域不能为空"
              :rows="3"
            />
          </div>
        </div>

        <h3>LaserButton 按钮组件</h3>
        <div style="display: flex; gap: 10px; margin-bottom: 20px;">
          <GlowButton :corners="['top-right','top-left']">普通按钮</GlowButton>
          <GlowButton :corners="['top-right','top-left']" :disabled="true">禁用按钮</GlowButton>
          <GlowButton theme="danger">危险按钮</GlowButton>
        </div>
      </div>

      <div class="component-section">
        <h3>LaserModal 模态框组件</h3>
        <div style="display: flex; gap: 10px; margin-bottom: 20px;">
          <GlowButton @click="openModal" :corners="['top-right']">打开普通模态框</GlowButton>
          <GlowButton @click="openDangerModal" theme="danger" :corners="['top-left']">打开危险模态框</GlowButton>
        </div>
      </div>
      
      <div class="component-section">
        <h3>LaserConfirm 确认框组件</h3>
        <div style="display: flex; gap: 10px; margin-bottom: 20px;">
          <GlowButton @click="openConfirm">打开确认对话框</GlowButton>
          <GlowButton @click="openCustomConfirm" theme="danger">自定义确认对话框</GlowButton>
        </div>
      </div>
      
      <div class="component-section">
        <h3>GlowAlter 提示框组件</h3>
        <div style="display: flex; gap: 10px; margin-bottom: 20px;">
          <GlowButton @click="openAlter">打开提示框</GlowButton>
          <GlowButton @click="openCustomAlter" :corners="['bottom-left']">自定义提示框</GlowButton>
        </div>
      </div>
      
      <div class="component-section">
        <h3>LaserDiv 容器组件</h3>
        <GlowDiv style="padding: 20px; margin-bottom: 10px;">
          <p>这是一个基本的LaserDiv容器</p>
        </GlowDiv>
        
        <GlowDiv border="top" style="padding: 20px; margin-bottom: 10px;">
          <p>这是一个顶部有边框的LaserDiv</p>
        </GlowDiv>
        
        <GlowDiv border="left" style="padding: 20px;">
          <p>这是一个左侧有边框的LaserDiv</p>
        </GlowDiv>
      </div>

      <div class="component-section">
        <h3>GlowColorPicker 颜色选择器组件</h3>
        <div style="display: flex; gap: 20px; align-items: center;">
          <GlowColorPicker color=""/>

        </div>
      </div>

      <!-- 普通模态框 -->
      <GlowModal v-model="showModal" title="提示">
        <div>
          <p>这是一个普通的模态框示例，用于展示信息或进行简单的交互。</p>
          <p>模态框支持自定义内容，可以包含任何Vue组件或HTML元素。</p>
        </div>
        
        <template #footer>
          <GlowButton @click="showModal = false">取消</GlowButton>
          <GlowButton @click="confirmAction" :corners="['top-right', 'bottom-left']">确认</GlowButton>
        </template>
      </GlowModal>

      <!-- 危险操作模态框 -->
      <GlowModal v-model="showDangerModal" title="危险操作警告">
        <div>
          <p style="color: #ff6b6b; font-weight: bold;">警告：此操作不可逆！</p>
          <p>您即将执行一个危险操作，该操作将会删除所有数据且无法恢复。</p>
          <p>请确认您已了解操作的风险并希望继续。</p>
        </div>
        
        <template #footer>
          <GlowButton @click="showDangerModal = false">取消</GlowButton>
          <GlowButton @click="confirmDangerAction" theme="danger" :corners="['top-right', 'bottom-left']">
            确认删除
          </GlowButton>
        </template>
      </GlowModal>
      
      <!-- LaserConfirm组件 -->
      <GlowConfirm ref="confirmRef" />
      
      <!-- GlowAlter组件 -->
      <GlowAlert ref="alterRef" />
    </div>
  </GlowTheme>
</template>

<style scoped>
h2 {
  margin-bottom: 20px;
  color: #fff;
}

h3 {
  margin-top: 30px;
  margin-bottom: 15px;
  color: #fff;
}

p {
  margin-bottom: 15px;
  line-height: 1.5;
}

.component-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.test-container {
  padding: 20px;
}

.color-preview-box {
  width: 50px;
  height: 50px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.color-value {
  color: #fff;
  font-size: 14px;
}
</style>