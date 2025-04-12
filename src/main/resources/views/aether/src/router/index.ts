import { createRouter, createWebHashHistory } from 'vue-router'
import Http from '@/commons/Http'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/ModelChat.vue'),
      beforeEnter: async (to, from, next) => {
        try {
          const response = await Http.postEntity<{clientPath: string}>('/client/getPreferences', {})
          if (response.clientPath && response.clientPath !== '/') {
            next(response.clientPath)
            return
          }
        } catch (error) {
          console.warn('获取保存的路由失败:', error)
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
  const currentPath = to.fullPath
  Http.postEntity<string>('/client/savePreferences', {
    clientPath: currentPath
  }).catch(error => {
    console.warn('保存当前路由失败:', error)
  })
})

export default router
