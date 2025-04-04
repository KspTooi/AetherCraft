import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/model-chat.vue'),
    },
    {
      path: '/chat',
      name: 'model-chat',
      component: () => import('../views/model-chat.vue'),
    },
    {
      path: '/rp',
      name: 'model-rp',
      component: () => import('../views/model-rp.vue'),
    },
    {
      path: '/agent',
      name: 'model-agent',
      component: () => import('../views/model-agent.vue'),
    },
    {
      path: '/customize',
      name: 'customize',
      component: () => import('../views/customize.vue'),
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

export default router
