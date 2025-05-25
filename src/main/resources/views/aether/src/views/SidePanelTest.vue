<script setup lang="ts">
import { ref, reactive } from 'vue'
import ClientSidePanel from '@/components/glow-client/ClientSidePanel.vue'
import GlowTheme from '@/components/glow-ui/GlowTheme.vue'
import GlowDiv from '@/components/glow-ui/GlowDiv.vue'
import ChatThreadList from "@/components/glow-client/ChatThreadList.vue";
import GlowSidePanel from "@/components/glow-ui/GlowSidePanel.vue";

// 侧边栏配置
const sideItems = reactive([
  { id: 'dashboard', title: '控制台', icon: 'bi bi-grid' },
  { id: 'users', title: '用户管理', icon: 'bi bi-people' },
  { id: 'products', title: '产品列表', icon: 'bi bi-box', badge: '9+' },
  { id: 'orders', title: '订单管理', icon: 'bi bi-cart' },
  { id: 'settings', title: '系统设置', icon: 'bi bi-gear' },
  { id: 'reports', title: '统计报表', icon: 'bi bi-graph-up' },
  { id: 'messages', title: '消息中心', icon: 'bi bi-chat-dots', badge: '3' }
])

// 当前选中的侧边栏项目ID
const activeItemId = ref('dashboard')

// 页面标题
const pageTitle = ref('控制台')

// 处理侧边栏点击
const handleSideItemClick = (itemId: string) => {
  activeItemId.value = itemId
  // 更新页面标题
  const item = sideItems.find(item => item.id === itemId)
  if (item) {
    pageTitle.value = item.title
  }
}

// 处理侧边栏动作
const handleSideAction = (action: string, itemId: string) => {
  console.log('侧边栏动作:', action, '项目ID:', itemId)
}
</script>

<template>


  <div class="main-content">
    <!-- 侧边栏 -->
    <GlowSidePanel
      :items="sideItems"
      :activeItemId="activeItemId"
      title="应用导航"
      @item-click="handleSideItemClick"
      @action="handleSideAction"
    />
    
    <!-- 内容区域 -->
    <GlowDiv border="none" class="content-area">
      <div class="page-header">
        <h2>{{ pageTitle }}</h2>
      </div>
      <div class="page-content">
        <p>这是一个使用ClientSidePanel的示例页面。</p>
        <p>当前选中的菜单项是: <strong>{{ activeItemId }}</strong></p>
        
        <div class="demo-cards">
          <GlowDiv v-for="n in 4" :key="n" class="demo-card">
            <h3>示例卡片 {{ n }}</h3>
            <p>这是一个使用GlowDiv创建的示例卡片</p>
          </GlowDiv>
        </div>
      </div>
    </GlowDiv>
  </div>
</template>

<style scoped>
.main-content {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.page-header h2 {
  font-size: 24px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.page-content {
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

.demo-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin-top: 30px;
}

.demo-card {
  padding: 20px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.demo-card h3 {
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 18px;
  font-weight: 500;
}

.demo-card p {
  margin-bottom: 0;
  font-size: 14px;
  opacity: 0.8;
}

/* 添加响应式支持 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .content-area {
    padding: 15px;
  }
  
  .demo-cards {
    grid-template-columns: 1fr;
  }
}
</style>