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
      path: '/effects-test',
      name: 'effects-test',
      component: () => import('../components/EffectsTest.vue'),
    },
  ],
})

export default router
