<template>
  <GlowDiv class="player-lobby-outer" border="none">
    <div class="player-lobby-container">
      <div class="lobby-header">
        <h1>选择或创建人物</h1>
        <p>从下方选择已有人物继续您的冒险，或点击加号(+)创建一位新人物。</p>
      </div>

      <div class="lobby-content">

        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>正在加载人物列表...</p>
        </div>

        <template v-else>
          <!-- 人物列表区域 -->
          <div class="player-grid" v-if="playerList.length > 0">
            <!-- 创建新人物卡片 -->
            <GlowDiv class="player-card create-player-card" @click="createNewPlayer">
              <div class="create-player-content">
                <div class="create-icon">+</div>
                <div class="create-text">创建新人物</div>
              </div>
            </GlowDiv>
            
            <!-- 现有人物卡片 -->
            <GlowDiv
                v-for="player in playerList"
                :key="player.id"
                class="player-card"
                :class="{ 'active': selectedPlayer?.id === player.id, 'pending-deletion': player.status === 2 && player.remainingRemoveTime > 0 }"
                @click="selectPlayer(player)"
            >
              <!-- Confirm Delete (✕) / Request Delete (✕) Button -->
              <button
                  v-if="player.status === 1 || (player.status === 2 && player.remainingRemoveTime <= 0)"
                  class="action-button remove-confirm"
                  @click.stop="handlePrimaryAction(player)"
                  :title="player.status === 1 ? '申请删除此人物' : '确认删除此人物'"
              >
                ✕
              </button>

              <!-- Cancel Delete Button (↺) -->
              <button
                  v-if="player.status === 2"
                  class="action-button cancel-restore"
                  @click.stop="handleCancelAction(player)"
                  title="取消删除请求"
              >
                ↺
              </button>

              <div class="player-info">
                <h3>{{ player.name }}</h3>
                <p v-if="player.status === 2" class="status-text">
                  <span v-if="player.remainingRemoveTime > 0">
                    等待删除 (剩余 {{ player.remainingRemoveTime }} 小时)
                  </span>
                  <span v-else-if="player.remainingRemoveTime === 0">
                    可确认删除
                  </span>
                  <span v-else>
                    等待删除 (计算中...)
                  </span>
                </p>
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
                  :class="{'grayscale': player.status === 1 || player.status === 2}"
                />
              </div>
              <div class="player-stats">
                <p class="balance">余额: {{ player.balance }} CU</p>
              </div>
            </GlowDiv>
          </div>

          <!-- 无人物提示 -->
          <GlowDiv v-else class="no-players">
            <p>您还没有创建任何人物</p>
            <p>创建一个新人物开始您的旅程吧！</p>
            <!-- 添加创建按钮（仅在没有玩家时） -->
             <GlowButton
                  @click="createNewPlayer"
                  :corners="['top-right', 'bottom-left']"
                  :loading="actionLoading"
                  style="margin-top: 1.5rem;"
              >
                创建新人物
              </GlowButton>
          </GlowDiv>
        </template>

        <!-- 操作按钮区域 -->
        <!-- 移除这里的创建按钮，因为已移到卡片或无玩家提示中 -->
        <div class="action-buttons">
          <!-- 可以保留其他按钮 -->
        </div>
      </div>
      
      <!-- 错误提示框 -->
      <GlowAlter ref="alterRef" />
      <GlowConfirm ref="confirmRef" />
    </div>
  </GlowDiv>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import GlowDiv from "@/components/glow-ui/GlowDiv.vue";
import GlowButton from "@/components/glow-ui/GlowButton.vue";
import GlowConfirm from "@/components/glow-ui/GlowConfirm.vue";
import GlowAlter from "@/components/glow-ui/GlowAlter.vue";
import PlayerApi, { type GetPlayerListVo } from "@/commons/api/PlayerApi";
import { usePlayerStore } from "@/stores/player";

const router = useRouter();
const playerStore = usePlayerStore();

// State
const loading = ref(true);
const actionLoading = ref(false);
const buttonActionLoading = ref(false);
const playerList = ref<GetPlayerListVo[]>([]);
const selectedPlayer = ref<GetPlayerListVo | null>(null);
const defaultAvatarUrl = '/imgs/default-avatar.png';
const confirmRef = ref<InstanceType<typeof GlowConfirm> | null>(null);
const alterRef = ref<InstanceType<typeof GlowAlter> | null>(null);

// --- Avatar URL Logic ---
const getAvatarUrl = (url: string | undefined | null): string => {
    if (!url) return defaultAvatarUrl;
    if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/')) return url;
    return `/res/${url}`;
};

// --- Load Player List ---
const loadPlayerList = async () => {
  loading.value = true;
  selectedPlayer.value = null;
  try {
    const response = await PlayerApi.getPlayerList({ page: 1, pageSize: 200 });
    playerList.value = response.rows || [];

    // --- 新增逻辑：如果列表为空，直接跳转 ---
    if (playerList.value.length === 0) {
      router.push({ path: '/player/create', query: { init: 'true' } });
      return; 
    }
    // --- 结束新增逻辑 ---

  } catch (error) {
    console.error('获取人物列表失败:', error);
    alterRef.value?.showConfirm({ title: "获取数据失败", content: "无法加载人物列表，请稍后重试", closeText: "确定" });
  } finally {
    loading.value = false;
  }
};

// --- Action Button Handlers ---

// Handles '✕' button click (Request Delete for status 1, Confirm Delete for status 2)
const handlePrimaryAction = async (player: GetPlayerListVo) => {
    if (buttonActionLoading.value) return;
    buttonActionLoading.value = true;

    if (player.status === 1) {
        // Status 1: Direct send remove request
        try {
            const message = await PlayerApi.sendRemoveRequest({ id: player.id });
            alterRef.value?.showConfirm({ title: "请求已提交", content: message, closeText: "确定" });
            await loadPlayerList();
        } catch (error) {
            console.error('发送删除请求失败:', error);
            alterRef.value?.showConfirm({
                title: "操作失败",
                content: error instanceof Error ? error.message : "发送删除请求时出错",
                closeText: "确定",
            });
        } finally {
            buttonActionLoading.value = false;
        }
    } else if (player.status === 2 && player.remainingRemoveTime <= 0) {
        // Status 2, Time Expired: Confirm Deletion
        buttonActionLoading.value = false; // Turn off loading before confirmation
        const confirmed = await confirmRef.value?.showConfirm({
            title: '不可恢复',
            content: `确定要永久删除人物 "${player.name}" 吗？此操作无法撤销。`,
            confirmText: '确认删除',
            cancelText: '取消',
        });

        if (confirmed) {
            buttonActionLoading.value = true;
            try {
                await PlayerApi.removePlayer({ id: player.id });
                // Success message removed based on previous request
                await loadPlayerList();
            } catch (error) {
                 console.error('确认删除失败:', error);
                 alterRef.value?.showConfirm({
                    title: "操作失败",
                    content: error instanceof Error ? error.message : "确认删除时出错",
                    closeText: "确定",
                 });
            } finally {
                buttonActionLoading.value = false;
            }
        } else {
             // User cancelled confirmation, reset loading if it was ever set by mistake
             buttonActionLoading.value = false;
        }
    } else {
         // Should not happen based on v-if logic, reset loading just in case
         buttonActionLoading.value = false;
    }
};

// Handles '↺' button click (Cancel Delete for status 2)
const handleCancelAction = async (player: GetPlayerListVo) => {
    // This button only appears for status 2, so no need to check status again
    if (buttonActionLoading.value) return;

    // Always confirm cancel action
    const confirmed = await confirmRef.value?.showConfirm({
        title: '撤销删除',
        content: `确定要撤销对人物 "${player.name}" 的删除请求吗？`,
        confirmText: '撤销',
        cancelText: '返回', // Use '返回' for the cancel button here
    });

    if (confirmed) {
        buttonActionLoading.value = true;
        try {
            await PlayerApi.cancelRemovePlayer({ id: player.id });
            // Success message removed based on previous request
            await loadPlayerList();
        } catch (error) {
             console.error('取消删除失败:', error);
             alterRef.value?.showConfirm({
                title: "操作失败",
                content: error instanceof Error ? error.message : "取消删除时出错",
                closeText: "确定",
             });
        } finally {
            buttonActionLoading.value = false;
        }
    }
};


// --- Select Player Logic ---
const selectPlayer = async (player: GetPlayerListVo) => {
  if (player.status !== 1) {
    alterRef.value?.showConfirm({ title: "无法选择", content: `该人物正在等待删除，请在 ${player.remainingRemoveTime} 小时后确认删除，或先取消删除请求。`, closeText: "确定" });
    return;
  }

  selectedPlayer.value = player;

  if (actionLoading.value) return;
  actionLoading.value = true;

  try {
    await PlayerApi.attachPlayer({ id: player.id });
    await playerStore.updatePlayerInfo();
    router.push('/');
  } catch (error) {
    console.error('选择并激活人物失败:', error);
    selectedPlayer.value = null;
    alterRef.value?.showConfirm({ title: "操作失败", content: error instanceof Error ? error.message : "无法选择该人物，请稍后重试", closeText: "确定" });
    actionLoading.value = false;
  }
};

// --- Create Player Logic ---
const createNewPlayer = () => {
    if (actionLoading.value) return;
    actionLoading.value = true;
    try {
        router.push('/player/create');
    } catch (error) {
        console.error('跳转到创建页面失败:', error);
        actionLoading.value = false;
    }
};

// --- Lifecycle Hook ---
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

/* Player Card - Ensure positioning context */
.player-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem 1rem;
  cursor: pointer;
  transition: background-color 0.3s ease, opacity 0.3s ease;
  position: relative; /* Crucial for absolute positioning of children */
  overflow: hidden; /* Keep button contained if it ever overflows */
}

/* 等待删除且时间未到的卡片样式 */
.player-card.pending-deletion {
  opacity: 0.6;
  cursor: not-allowed;
}
.player-card.pending-deletion:hover {
   background-color: transparent; /* 阻止 hover 效果 */
}

.player-card:hover {
  background-color: rgba(0, 196, 255, 0.05);
}

.player-info {
  text-align: center;
  margin-bottom: 1rem;
  min-height: 4em; /* 给状态文本留出空间 */
}

.player-info h3 {
  margin: 0 0 0.3rem 0; /* 增加一点底部间距 */
  font-size: 1.2rem;
  color: #ffffff;
}

.status-text {
  font-size: 0.85rem;
  color: #ffcc00; /* 醒目的黄色 */
  margin-top: 0.3rem;
}

.avatar {
  width: 90%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  margin-bottom: 1rem;
  border: 1px solid transparent;
  transition: border-color 0.3s ease;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* filter: grayscale(100%); 移除默认灰度 */
  transition: filter 0.3s ease, transform 0.3s ease;
}
/* 非活跃或等待删除时灰度 */
.avatar-image.grayscale {
    filter: grayscale(100%);
}

/* 鼠标悬停在卡片上时，头像效果 */
.player-card:not(.pending-deletion):hover .avatar-image {
  filter: grayscale(0%); /* 恢复彩色 */
  transform: scale(1.05); /* 轻微放大 */
}
.player-card:not(.pending-deletion):hover .avatar {
  border-color: #a4fff7;
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
  min-height: 240px;
  border: 1px dashed rgba(255, 255, 255, 0.3);
}

.create-player-card:hover {
  border-color: #a4fff7;
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

/* Action Button Styles (Common) */
.action-button {
  position: absolute;
  top: 8px;
  width: 24px;
  height: 24px;
  background-color: transparent;
  border: none;
  border-radius: 2px;
  font-size: 20px;
  font-weight: normal;
  line-height: 20px;
  text-align: center;
  cursor: pointer;
  z-index: 10;
  opacity: 0.65;
  transform: scale(1);
  transition: all 0.2s ease;
  pointer-events: auto;
}

/* Default Positioning (Right corner) */
.action-button.remove-confirm { right: 8px; }
.action-button.cancel-restore { right: 8px; }

/* If BOTH buttons are present inside the card, move the cancel button left */
.player-card:has(> .action-button.remove-confirm):has(> .action-button.cancel-restore) > .action-button.cancel-restore {
    right: 40px; /* Adjust this value for desired spacing */
}

/* Style for Remove/Confirm Button (✕) */
.action-button.remove-confirm {
   color: rgba(255, 100, 100, 0.8); /* Red X */
}
.action-button.remove-confirm:hover {
  color: rgba(255, 50, 50, 1);
  opacity: 1;
  transform: scale(1.05);
}
.action-button.remove-confirm:active {
  background-color: rgba(255, 100, 100, 0.2);
  transform: scale(0.95);
  color: rgba(200, 0, 0, 1);
}

/* Style for Cancel/Restore Button (↺) */
.action-button.cancel-restore {
   color: rgba(100, 180, 255, 0.8); /* Blue arrow */
   font-size: 22px; /* Adjust size if needed for the symbol */
   line-height: 22px; /* Adjust line height */
}
.action-button.cancel-restore:hover {
  color: rgba(50, 150, 255, 1);
  opacity: 1;
  transform: scale(1.05);
}
.action-button.cancel-restore:active {
  background-color: rgba(100, 180, 255, 0.2);
  transform: scale(0.95);
  color: rgba(0, 100, 200, 1);
}
</style>