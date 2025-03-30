<template>
  <div class="laser-button-demo">
    <h1>激光按钮组件演示</h1>
    
    <div class="button-section">
      <h2>基本按钮</h2>
      <div class="button-row">
        <LaserButton @click="handleClick">默认按钮</LaserButton>
        <LaserButton 
          background-color="rgba(79, 172, 254, 0.3)"
          border-color="rgba(79, 172, 254, 0.7)"
          glow-color="rgba(79, 172, 254, 0.5)"
          @click="handleClick">
          蓝色按钮
        </LaserButton>
        <LaserButton 
          background-color="rgba(239, 68, 68, 0.3)"
          border-color="rgba(239, 68, 68, 0.7)"
          glow-color="rgba(239, 68, 68, 0.5)"
          @click="handleClick">
          红色按钮
        </LaserButton>
        <LaserButton 
          background-color="rgba(16, 185, 129, 0.3)"
          border-color="rgba(16, 185, 129, 0.7)"
          glow-color="rgba(16, 185, 129, 0.5)"
          @click="handleClick">
          绿色按钮
        </LaserButton>
      </div>
    </div>
    
    <div class="button-section">
      <h2>带图标的按钮</h2>
      <div class="button-row">
        <LaserButton @click="handleClick">
          <i class="bi bi-plus-circle"></i> 新建
        </LaserButton>
        <LaserButton @click="handleClick">
          <i class="bi bi-send"></i> 发送
        </LaserButton>
        <LaserButton @click="handleClick">
          <i class="bi bi-trash"></i> 删除
        </LaserButton>
      </div>
    </div>
    
    <div class="button-section">
      <h2>不同尺寸的按钮</h2>
      <div class="button-row">
        <LaserButton 
          width="120px" 
          height="50px" 
          font-size="16px"
          @click="handleClick">
          大按钮
        </LaserButton>
        <LaserButton @click="handleClick">
          中等按钮
        </LaserButton>
        <LaserButton 
          width="80px" 
          height="30px" 
          font-size="12px"
          @click="handleClick">
          小按钮
        </LaserButton>
      </div>
    </div>
    
    <div class="button-section">
      <h2>禁用状态</h2>
      <div class="button-row">
        <LaserButton :disabled="true">
          禁用按钮
        </LaserButton>
        <LaserButton 
          background-color="rgba(239, 68, 68, 0.3)"
          border-color="rgba(239, 68, 68, 0.7)"
          glow-color="rgba(239, 68, 68, 0.5)"
          :disabled="true">
          禁用红色按钮
        </LaserButton>
      </div>
    </div>
    
    <div class="button-section">
      <h2>虚线边框</h2>
      <div class="button-row">
        <LaserButton :dashed="true">
          虚线边框按钮
        </LaserButton>
        <LaserButton 
          background-color="rgba(16, 185, 129, 0.3)"
          border-color="rgba(16, 185, 129, 0.7)"
          glow-color="rgba(16, 185, 129, 0.5)"
          :dashed="true">
          虚线绿色按钮
        </LaserButton>
      </div>
    </div>
    
    <div class="button-section">
      <h2>角落填充效果</h2>
      <div class="button-row">
        <LaserButton 
          corner="bottom-right"
          corner-size="20px">
          右下角填充
        </LaserButton>
        <LaserButton 
          corner="bottom-left"
          corner-size="20px"
          background-color="rgba(79, 172, 254, 0.3)"
          border-color="rgba(79, 172, 254, 0.7)">
          左下角填充
        </LaserButton>
        <LaserButton 
          corner="top-right"
          corner-size="20px"
          background-color="rgba(239, 68, 68, 0.3)"
          border-color="rgba(239, 68, 68, 0.7)">
          右上角填充
        </LaserButton>
        <LaserButton 
          corner="top-left"
          corner-size="20px"
          background-color="rgba(16, 185, 129, 0.3)"
          border-color="rgba(16, 185, 129, 0.7)">
          左上角填充
        </LaserButton>
      </div>
    </div>
    
    <div class="button-section">
      <h2>不同大小的角落填充</h2>
      <div class="button-row">
        <LaserButton 
          corner="bottom-right"
          corner-size="10px">
          小角填充
        </LaserButton>
        <LaserButton 
          corner="bottom-right"
          corner-size="20px">
          中角填充
        </LaserButton>
        <LaserButton 
          corner="bottom-right"
          corner-size="30px">
          大角填充
        </LaserButton>
      </div>
    </div>
    
    <div class="button-section">
      <h2>多角落填充</h2>
      <div class="button-row">
        <LaserButton 
          :corners="['bottom-left', 'bottom-right']"
          corner-size="15px">
          双下角填充
        </LaserButton>
        <LaserButton 
          :corners="['top-left', 'top-right']"
          corner-size="15px"
          background-color="rgba(239, 68, 68, 0.3)"
          border-color="rgba(239, 68, 68, 0.7)">
          双上角填充
        </LaserButton>
        <LaserButton 
          :corners="['top-left', 'bottom-right']"
          corner-size="15px"
          background-color="rgba(16, 185, 129, 0.3)"
          border-color="rgba(16, 185, 129, 0.7)">
          对角填充
        </LaserButton>
        <LaserButton 
          :corners="['top-left', 'top-right', 'bottom-left', 'bottom-right']"
          corner-size="15px"
          background-color="rgba(79, 172, 254, 0.3)"
          border-color="rgba(79, 172, 254, 0.7)">
          四角填充
        </LaserButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LaserButton from './LaserButton.vue'

// 点击事件处理
const handleClick = () => {
  alert('按钮被点击了！')
}
</script>

<style scoped>
.laser-button-demo {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  color: white;
}

h1 {
  font-size: 24px;
  margin-bottom: 20px;
  text-align: center;
}

h2 {
  font-size: 18px;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.8);
}

.button-section {
  margin-bottom: 30px;
}

.button-row {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}
</style> 