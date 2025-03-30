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
  ],
})

export default router
