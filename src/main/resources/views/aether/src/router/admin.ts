import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'admin-home',
      component: () => import('../views/admin/Dashboard.vue'),
    },
    {
      path: '/user-manage',
      name: 'user-manage',
      component: () => import('../views/admin/UserManage.vue'),
    },
    {
      path: '/system-config',
      name: 'system-config',
      component: () => import('../views/admin/SystemConfig.vue'),
    },
    {
      path: '/log-viewer',
      name: 'log-viewer',
      component: () => import('../views/admin/LogViewer.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/admin/NotFound.vue'),
    },
  ],
})

export default router 