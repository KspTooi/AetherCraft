<script setup lang="ts">
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import GlowTheme from "@/components/glow-ui/GlowTheme.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowModal from "@/components/glow-ui/GlowModal.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import { ref } from 'vue';
import GlowColorPicker from "@/components/glow-ui/GlowColorPicker.vue";
import GlowInput from "@/components/glow-ui/GlowInput.vue";

// 控制模态框显示状态
const showModal = ref(false);
const showDangerModal = ref(false);

// 确认框引用
const confirmRef = ref();

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
</script>

<template>
  <GlowTheme>
    <div class="test-container">
      <h2>Laser UI 组件测试</h2>

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
              title="这里填入输入框的标题" 
              placeholder="请输入列表名称"
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