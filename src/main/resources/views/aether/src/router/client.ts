import { createRouter, createWebHashHistory } from 'vue-router'
import { usePreferencesStore } from '@/stores/preferences'
import { storeToRefs } from 'pinia'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/ModelChat.vue'),
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
      component: () => import('../views/ModelChat.vue'),
    },
    {
      path: '/rp',
      name: 'model-rp',
      component: () => import('../views/ModelRolePlay.vue'),
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
      component: () => import('../views/ModelRoleManager.vue'),
      beforeEnter: async (to, from, next) => {
        const preferences = usePreferencesStore()
        // 当进入角色设计器页面时，更新角色扮演路径为角色设计器
        preferences.saveClientRpPath('/model-role-manager')
        next()
      }
    },
    {
      path: '/agent',
      name: 'model-agent',
      component: () => import('../views/model-agent.vue'),
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
      component: () => import('../views/ModelChat.vue'),
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

// Define routes that should not be saved as the last accessed path
const savePathBlacklist = [
    '/playLobby',
    '/player/create'
];

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
