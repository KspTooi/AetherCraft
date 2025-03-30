<template>
  <div class="effects-test">
    <h1>按钮特效测试</h1>
    
    <div class="test-section">
      <h2>内发光效果</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            :innerGlow="true"
            :particles="false"
            innerGlowSize="4px"
            width="150px">
            弱内发光
          </LaserButton>
          <div class="label">innerGlowSize="4px"</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :innerGlow="true"
            :particles="false"
            innerGlowSize="8px"
            width="150px">
            中内发光
          </LaserButton>
          <div class="label">innerGlowSize="8px"</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :innerGlow="true"
            :particles="false"
            innerGlowSize="12px"
            width="150px">
            强内发光
          </LaserButton>
          <div class="label">innerGlowSize="12px"</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :innerGlow="true"
            :particles="false"
            innerGlowColor="rgba(255, 0, 0, 0.6)"
            width="150px">
            红色内发光
          </LaserButton>
          <div class="label">自定义内发光颜色</div>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>粒子效果</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            :innerGlow="false"
            :particles="true"
            width="150px">
            默认粒子
          </LaserButton>
          <div class="label">悬停查看粒子</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :innerGlow="false"
            :particles="true"
            particleColor="rgba(255, 0, 0, 0.8)"
            width="150px">
            红色粒子
          </LaserButton>
          <div class="label">自定义粒子颜色</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :innerGlow="false"
            :particles="true"
            particleColor="rgba(0, 255, 0, 0.8)"
            width="150px">
            绿色粒子
          </LaserButton>
          <div class="label">自定义粒子颜色</div>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>结合效果</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            :innerGlow="true"
            :particles="true"
            :corners="['bottom-left', 'bottom-right']"
            background-color="rgba(25, 45, 88, 0.8)"
            border-color="rgba(135, 206, 250, 0.8)"
            glow-color="rgba(135, 206, 250, 0.5)"
            width="150px">
            全部效果
          </LaserButton>
          <div class="label">内发光+粒子+角落</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :innerGlow="true"
            :particles="true"
            corner="bottom-right"
            background-color="rgba(40, 10, 60, 0.8)"
            border-color="rgba(230, 100, 255, 0.8)"
            glow-color="rgba(230, 100, 255, 0.5)"
            innerGlowColor="rgba(230, 100, 255, 0.6)"
            particleColor="rgba(230, 100, 255, 0.8)"
            width="150px">
            紫色风格
          </LaserButton>
          <div class="label">紫色主题</div>
        </div>
        
        <div class="test-item">
          <LaserButton 
            :innerGlow="true"
            :particles="true"
            :corners="['top-left', 'top-right', 'bottom-left', 'bottom-right']"
            background-color="rgba(10, 40, 10, 0.8)"
            border-color="rgba(50, 230, 50, 0.8)"
            glow-color="rgba(50, 230, 50, 0.4)"
            innerGlowColor="rgba(50, 230, 50, 0.4)"
            width="150px">
            全角落
          </LaserButton>
          <div class="label">四角效果</div>
        </div>
      </div>
    </div>
    
    <div class="test-section">
      <h2>禁用状态 - 无特效</h2>
      <div class="test-grid">
        <div class="test-item">
          <LaserButton 
            :innerGlow="true"
            :particles="true"
            :corners="['bottom-left', 'bottom-right']"
            width="150px"
            :disabled="true">
            禁用状态
          </LaserButton>
          <div class="label">内发光和粒子均不显示</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import LaserButton from './LaserButton.vue'
</script>

<style scoped>
.effects-test {
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