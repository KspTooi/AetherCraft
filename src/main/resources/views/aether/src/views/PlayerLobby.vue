<template>
  <GlowDiv class="player-lobby-outer" border="none">
    <div class="player-lobby-container">
      <div class="lobby-header">
        <h1>选择或创建角色</h1>
        <p>从下方选择已有角色继续您的冒险，或点击加号(+)创建一位新角色。</p>
      </div>

      <div class="lobby-content">

        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>正在加载角色列表...</p>
        </div>

        <template v-else>
          <!-- 角色列表区域 -->
          <div class="player-grid" v-if="playerList.length > 0">
            <!-- 创建新角色卡片 -->
            <GlowDiv class="player-card create-player-card" @click="createNewPlayer">
              <div class="create-player-content">
                <div class="create-icon">+</div>
                <div class="create-text">创建新角色</div>
              </div>
            </GlowDiv>
            
            <!-- 现有角色卡片 -->
            <GlowDiv
                v-for="player in playerList"
                :key="player.id"
                class="player-card"
                :class="{ 'active': selectedPlayer?.id === player.id }"
                @click="selectPlayer(player)"
            >
              <div class="player-info">
                <h3>{{ player.name }}</h3>
              </div>
              <div class="avatar">
                <template v-if="getAvatarUrl(player.avatarUrl) === defaultAvatarUrl">
                  <div class="default-avatar-icon">
                    <i class="bi bi-person"></i>
                  </div>
                </template>
                <img
                  v-if="getAvatarUrl(player.avatarUrl) !== defaultAvatarUrl"
                  :src="getAvatarUrl(player.avatarUrl)"
                  :alt="player.name"
                  class="avatar-image"
                />
              </div>
              <div class="player-stats">
                <p class="balance">余额: {{ player.balance }} CU</p>
              </div>
            </GlowDiv>
          </div>

          <!-- 无角色提示 -->
          <GlowDiv v-else class="no-players">
            <p>您还没有创建任何角色</p>
            <p>创建一个新角色开始您的旅程吧！</p>
          </GlowDiv>
        </template>

        <!-- 操作按钮 - 移除创建按钮 -->
        <div class="action-buttons">
          <!-- 
          <GlowButton
              @click="createNewPlayer"
              :corners="['top-right', 'bottom-left']"
              :loading="actionLoading"
          >
            创建新角色
          </GlowButton>
          -->
        </div>
      </div>
      
      <!-- 错误提示框 -->
      <GlowAlter ref="alterRef" />
    </div>
  </GlowDiv>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowAlter from "@/components/glow-ui/GlowAlter.vue";
import PlayerApi, { type GetPlayerListVo } from "@/commons/api/PlayerApi";

const router = useRouter();

// 状态
const loading = ref(true);
const actionLoading = ref(false);
const playerList = ref<GetPlayerListVo[]>([]);
const selectedPlayer = ref<GetPlayerListVo | null>(null);
const defaultAvatarUrl = '/imgs/default-avatar.png'; // 默认头像路径
const alterRef = ref<InstanceType<typeof GlowAlter> | null>(null);

// 获取头像URL，处理null情况
const getAvatarUrl = (url: string | undefined | null): string => {
  if (!url) {
    return defaultAvatarUrl;
  }
  
  // 检查URL是否已经包含域名或协议
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/')) {
    return url;
  }
  
  // 否则添加路径前缀
  return `/res/${url}`;
};

// 获取玩家列表
const loadPlayerList = async () => {
  try {
    loading.value = true;
    const response = await PlayerApi.getPlayerList({
      page: 1,
      pageSize: 200
    });
    playerList.value = response.rows || [];
    
    // 如果有已激活角色，默认选中
    const activePlayerId = localStorage.getItem('activePlayerId');
    if (activePlayerId) {
      const activePlayer = playerList.value.find(player => player.id === activePlayerId);
      if (activePlayer) {
        selectedPlayer.value = activePlayer;
      }
    }
  } catch (error) {
    console.error('获取玩家列表失败:', error);
    alterRef.value?.showConfirm({
      title: "获取数据失败",
      content: "无法加载角色列表，请稍后重试",
      closeText: "确定",
    });
  } finally {
    loading.value = false;
  }
};

// 选择玩家并直接激活
const selectPlayer = async (player: GetPlayerListVo) => {
  // 立即高亮选中项
  selectedPlayer.value = player;

  // 执行激活逻辑
  if (actionLoading.value) {
    return;
  }
  actionLoading.value = true;

  try {
    // 存储选择的角色信息
    localStorage.setItem('activePlayerId', player.id);
    localStorage.setItem('activePlayerName', player.name);

    // 可选：添加短暂延迟或视觉反馈
    // await new Promise(resolve => setTimeout(resolve, 150));

    // 跳转到主界面
    router.push('/model/chat');
  } catch (error) {
    console.error('选择并激活角色失败:', error);
    selectedPlayer.value = null; // 激活失败时取消选中状态
    alterRef.value?.showConfirm({
      title: "操作失败",
      content: "无法选择该角色，请稍后重试",
      closeText: "确定",
    });
    actionLoading.value = false; // 确保在 catch 中重置 loading
  } 
  // finally块不再需要设置 actionLoading = false, 因为成功时已跳转，失败时在catch中处理
};

// 创建新角色（跳转到创建页面）
const createNewPlayer = () => {
  if (actionLoading.value) {
    return;
  }
  actionLoading.value = true;
  
  try {
    router.push('/player/create');
  } catch (error) {
    console.error('跳转到创建页面失败:', error);
    actionLoading.value = false;
  }
};

// 组件挂载时加载玩家列表
onMounted(() => {
  loadPlayerList();
});
</script>

<style scoped>
.player-lobby-outer {
  height: 100%;
  user-select: none; /* 禁止文本选中 */
  -webkit-user-select: none; /* Safari 等 WebKit 浏览器 */
  -moz-user-select: none;    /* Firefox */
  -ms-user-select: none;     /* Internet Explorer/Edge */
}

.player-lobby-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  box-sizing: border-box;
  flex: 1;
  overflow-y: auto; /* 确保滚动在这里 */
}

/* 恢复滚动条样式到 container */
.player-lobby-container::-webkit-scrollbar {
  width: 8px;
}
.player-lobby-container::-webkit-scrollbar-thumb {
  background: rgba(200, 200, 200, 0.18);
  border-radius: 4px;
}
.player-lobby-container::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.04);
}

.lobby-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  /* 默认左右 padding */
  padding: 0 65px;
  box-sizing: border-box;
  /* overflow-y: auto;  移除这里的滚动 */
}

/* 响应式 Padding */
@media (max-width: 1024px) { /* 平板和较小桌面 */
  .lobby-content {
    padding: 0 30px;
  }
}

@media (max-width: 768px) { /* 较小平板和手机 */
  .lobby-content {
    padding: 0 15px;
  }

  .create-player-card {
    aspect-ratio: 1 / 1;
    min-height: unset; 
    order: 1; /* 在小屏幕上将创建卡片排到最后 */
  }
  
  /* 如果小屏幕下改为单列，可以显式设置 */
  .player-grid {
      grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) { /* 手机等最小屏幕 */
  .lobby-content {
    padding: 0; /* 左右 padding 为 0 */
  }
}

.lobby-header {
  text-align: center;
  margin-bottom: 1.5rem; /* 减小底部间距 */
  padding: 0 1rem; /* 增加左右内边距防止文本贴边 */
}

.lobby-header h1 {
  font-size: 2.2rem; /* 略微减小字体大小 */
  margin-bottom: 0.3rem; /* 减小标题和段落间距 */
  /* 尝试柔和的蓝紫渐变 */
  background: linear-gradient(to right, #8e2de2, #4a00e0);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-weight: 600; /* 加一点粗细 */
}

.lobby-header p {
  font-size: 1rem; /* 调整字体大小 */
  color: rgba(255, 255, 255, 0.65); /* 调整颜色 */
  line-height: 1.5; /* 调整行高 */
  max-width: 600px; /* 限制最大宽度防止过长 */
  margin: 0 auto; /* 居中 */
}

.player-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  width: 100%;
}

.player-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem 1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
  position: relative;
  overflow: hidden;
}

.player-card:hover {
  background-color: rgba(0, 196, 255, 0.05);
}

.player-info {
  text-align: center;
  margin-bottom: 1rem;
}

.player-info h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #ffffff;
}

.avatar {
  width: 90%; 
  aspect-ratio: 1 / 1; 
  overflow: hidden;
  margin-bottom: 1rem;
  border: 1px solid transparent; /* 默认设置透明边框占位 */
  transition: border-color 0.3s ease; 
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: grayscale(100%); 
  transition: filter 0.3s ease; 
  /* border: 1px solid #00c4ff; 移除这里的边框 */
}

/* 鼠标悬停在卡片上时，头像恢复彩色，并显示边框 */
.player-card:hover .avatar-image {
  filter: grayscale(0%);
}
.player-card:hover .avatar {
  /* border: 1px solid #00c4ff; 改为只改变颜色 */
  border-color: #a4fff7; /* Hover 时改变边框颜色 */
}

.player-stats {
  text-align: center;
}

.balance {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 1rem;
}

.no-players {
  text-align: center;
  padding: 3rem;
}

.no-players p {
  margin: 0.5rem 0;
  color: rgba(255, 255, 255, 0.7);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
}

.loading-spinner {
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  border-top: 4px solid #a4fff7;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.default-avatar-icon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 212, 212, 0.1);
  border-radius: 0;
}
.default-avatar-icon i {
  font-size: 60px;
  color: #b0b0b0;
}

.create-player-card {
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 240px; /* 恢复默认最小高度 */
  border: 1px dashed rgba(255, 255, 255, 0.3);
}

.create-player-card:hover {
  border-color: #a4fff7; /* 保持 hover 效果 */
}

.create-player-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.create-icon {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 1rem;
  line-height: 1;
}

.create-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
}

.create-player-card:hover .create-icon,
.create-player-card:hover .create-text {
  color: #a4fff7;
}
</style>