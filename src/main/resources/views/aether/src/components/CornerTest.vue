<template>
  <div class="corner-test">
    <h1>角落填充测试</h1>
    
    <div class="test-section">
      <h2>单个角落</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            corner="top-left" 
            corner-size="15px"
            width="150px">
            左上角
          </LaserButton>
          <div class="label">top-left</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            corner="top-right" 
            corner-size="15px"
            width="150px">
            右上角
          </LaserButton>
          <div class="label">top-right</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            corner="bottom-left" 
            corner-size="15px"
            width="150px">
            左下角
          </LaserButton>
          <div class="label">bottom-left</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            corner="bottom-right" 
            corner-size="15px"
            width="150px">
            右下角
          </LaserButton>
          <div class="label">bottom-right</div>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>同一侧两个角落</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            :corners="['top-left', 'top-right']" 
            corner-size="15px"
            width="150px">
            上侧两角
          </LaserButton>
          <div class="label">top-left + top-right</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :corners="['bottom-left', 'bottom-right']" 
            corner-size="15px"
            width="150px">
            下侧两角
          </LaserButton>
          <div class="label">bottom-left + bottom-right</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :corners="['top-left', 'bottom-left']" 
            corner-size="15px"
            width="150px">
            左侧两角
          </LaserButton>
          <div class="label">top-left + bottom-left</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :corners="['top-right', 'bottom-right']" 
            corner-size="15px"
            width="150px">
            右侧两角
          </LaserButton>
          <div class="label">top-right + bottom-right</div>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>对角线角落</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            :corners="['top-left', 'bottom-right']" 
            corner-size="15px"
            width="150px">
            左上/右下
          </LaserButton>
          <div class="label">top-left + bottom-right</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :corners="['top-right', 'bottom-left']" 
            corner-size="15px"
            width="150px">
            右上/左下
          </LaserButton>
          <div class="label">top-right + bottom-left</div>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>四个角落</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            :corners="['top-left', 'top-right', 'bottom-left', 'bottom-right']" 
            corner-size="15px"
            width="150px">
            四角填充
          </LaserButton>
          <div class="label">全部角落</div>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>左下角和右下角（重点测试）</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            :corners="['bottom-left', 'bottom-right']" 
            corner-size="15px"
            width="150px"
            background-color="rgba(25, 45, 88, 0.8)"
            border-color="rgba(135, 206, 250, 0.8)"
            glow-color="rgba(135, 206, 250, 0.5)">
            新建会话
          </LaserButton>
          <div class="label">底部双角填充</div>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>验证底部角落填充</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            corner="bottom-right" 
            corner-size="15px"
            width="150px"
            background-color="rgba(25, 45, 88, 0.8)"
            border-color="red">
            仅右下角
          </LaserButton>
          <div class="label">仅 bottom-right</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            corner="bottom-left" 
            corner-size="15px"
            width="150px"
            background-color="rgba(25, 45, 88, 0.8)"
            border-color="green">
            仅左下角
          </LaserButton>
          <div class="label">仅 bottom-left</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            corner="top-right" 
            corner-size="15px"
            width="150px"
            background-color="rgba(25, 45, 88, 0.8)"
            border-color="blue">
            仅右上角
          </LaserButton>
          <div class="label">仅 top-right</div>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>禁用状态 - 不显示角落填充</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            corner="bottom-right" 
            corner-size="15px"
            width="150px"
            background-color="rgba(25, 45, 88, 0.8)"
            border-color="rgba(135, 206, 250, 0.8)"
            :disabled="true">
            禁用+角落
          </LaserButton>
          <div class="label">禁用状态+右下角</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :corners="['bottom-left', 'bottom-right']" 
            corner-size="15px"
            width="150px"
            background-color="rgba(25, 45, 88, 0.8)"
            border-color="rgba(135, 206, 250, 0.8)"
            :disabled="true">
            禁用+多角落
          </LaserButton>
          <div class="label">禁用状态+双下角</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import LaserButton from './LaserButton.vue'
</script>

<style scoped>
.corner-test {
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
  margin-bottom: 15px;
  color: rgba(255, 255, 255, 0.9);
}

.test-section {
  margin-bottom: 30px;
  background: rgba(0, 0, 0, 0.2);
  padding: 20px;
  border-radius: 8px;
}

.test-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 25px;
}

.test-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}
</style> 