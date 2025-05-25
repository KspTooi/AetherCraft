import { createRouter, createWebHashHistory } from 'vue-router'
import { usePreferencesStore } from '@/stores/preferences'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '@/stores/player'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/ChatStandard.vue'),
      beforeEnter: async (to, from, next) => {
        const preferences = usePreferencesStore()
        await preferences.loadPreferences()
        const { clientPath } = storeToRefs(preferences)

        if (clientPath.value && clientPath.value !== '/') {
          next(clientPath.value)
          return
        }
        next('/chat')
      }
    },
    {
      path: '/chat',
      name: 'model-chat',
      component: () => import('../views/ChatStandard.vue'),
    },
    {
      path: '/rp',
      name: 'model-rp',
      component: () => import('../views/ChatNpc.vue'),
      beforeEnter: async (to, from, next) => {
        const preferences = usePreferencesStore()
        await preferences.loadPreferences()
        const {clientRpPath } = storeToRefs(preferences)
        
        // 检查上次访问的是否为角色设计器页面
        if (clientRpPath.value === '/model-role-manager') {
          next('/model-role-manager')
          return
        }
        
        // 保存当前路径到角色扮演路径
        preferences.saveClientRpPath('/rp')
        next()
      }
    },
    {
      path: '/model-role-manager',
      name: 'model-role-manager',
      component: () => import('../views/ChatNpcManager.vue'),
      beforeEnter: async (to, from, next) => {
        const preferences = usePreferencesStore()
        // 当进入角色设计器页面时，更新角色扮演路径为角色设计器
        preferences.saveClientRpPath('/model-role-manager')
        next()
      }
    },
    {
      path: '/customize',
      name: 'customize',
      component: () => import('../views/ClientCustomize.vue'),
    },
    {
      path: '/effects-test',
      name: 'effects-test',
      component: () => import('../components/EffectsTest.vue'),
    },
    {
      path: '/laser-test',
      name: 'laser-test',
      component: () => import('../views/GlowTest.vue'),
    },
    {
      path: '/side-panel-test',
      name: 'side-panel-test',
      component: () => import('../views/SidePanelTest.vue'),
    },
    {
      path: '/modelChat',
      name: 'modelChat',
      component: () => import('../views/ChatStandard.vue'),
    },
    {
      path: '/playLobby',
      name: 'playLobby',
      component: () => import('../views/PlayerLobby.vue'),
    },
    {
      path: '/player/create',
      name: 'playerCreate',
      component: () => import('../views/PlayerCreate.vue'),
    },
    {
      path: '/player/info',
      name: 'playerInfo',
      component: () => import('../views/PlayerInfo.vue'),
    }
  ],
})

// 路由白名单，这些路径不需要玩家信息
const playerCheckWhitelist = [
    '/playLobby',
    '/player/create'
];

// Define routes that should not be saved as the last accessed path
const savePathBlacklist = [
    '/playLobby',
    '/player/create'
];

// 全局前置守卫：检查玩家是否已选择
router.beforeEach(async (to, from, next) => {
  const playerStore = usePlayerStore();

  // --- 始终在导航前更新玩家信息 ---
  try {
    await playerStore.updatePlayerInfo(); // 等待玩家信息加载或刷新完成
  } catch (error) {
    // 即使更新失败，也允许导航继续，后续的检查会处理 currentPlayer 为 null 的情况
    console.warn("路由守卫：更新玩家信息失败", error);
  }
  // --- 结束更新 ---

  // 从 store 重新获取最新的状态 (经过 updatePlayerInfo 更新后)
  const { currentPlayer } = storeToRefs(playerStore);

  // 检查玩家信息是否已加载 (使用更新后的值)
  // 如果目标路径不在白名单中，并且没有当前玩家信息，则重定向到大厅
  if (!playerCheckWhitelist.includes(to.path) && !currentPlayer.value) {
    console.log(`路由守卫: 未选择玩家，目标路径 ${to.path} 不在白名单中，重定向到 /playLobby`);
    next({ path: '/playLobby' });
    return; // 确保重定向后不再执行 next()
  }

  // 允许导航
  next();
});

router.afterEach((to) => {
  const preferences = usePreferencesStore()
  // Check if the route is not the root and not in the blacklist
  if (to.fullPath !== '/' && !savePathBlacklist.includes(to.path)) {
    preferences.saveCurrentPath(to.fullPath).catch(error => {
      console.warn('保存当前路由失败:', error)
    })
  }
})

export default router
