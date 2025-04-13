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
    },
    {
      path: '/model-role-manager',
      name: 'model-role-manager',
      component: () => import('../views/ModelRoleManager.vue'),
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
    }
  ],
})

router.afterEach((to) => {
  const preferences = usePreferencesStore()
  if (to.fullPath !== '/') {
    preferences.saveCurrentPath(to.fullPath).catch(error => {
      console.warn('保存当前路由失败:', error)
    })
  }
})

export default router
