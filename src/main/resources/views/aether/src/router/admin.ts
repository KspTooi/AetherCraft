import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'admin-home',
      redirect: '/admin/user',
    },
    {
      path: '/admin/user',
      name: 'user-manager',
      component: () => import('@/views/admin/UserManager.vue'),
    },
    {
      path: '/admin/group',
      name: 'user-group-manager',
      component: () => import('@/views/admin/UserGroupManager.vue'),
    },
    {
      path: '/admin/permission',
      name: 'permission-manager',
      component: () => import('@/views/admin/PermissionManager.vue'),
    },
    {
      path: '/admin/config',
      name: 'config-manager',
      component: () => import('@/views/admin/ConfigManager.vue'),
    },
    {
      path: '/admin/model',
      name: 'ai-model-manager',
      component: () => import('@/views/admin/AiModelManager.vue'),
    },
    {
      path: '/admin/model/apikey',
      name: 'api-key-manager',
      component: () => import('@/views/admin/ApiKeyManager.vue'),
    },
    {
      path: '/admin/maintain',
      name: 'application-maintain',
      component: () => import('@/views/admin/ApplicationMaintain.vue'),
    }
  ],
})

export default router 