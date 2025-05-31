import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'admin-home',
      redirect: '/admin/user',
      meta: {
        breadcrumb: {
          title: '管理台'
        }  
      }
    },
    {
      path: '/admin/user',
      name: 'user-manager',
      component: () => import('@/views/admin/UserManager.vue'),
      meta: {
        breadcrumb: {
          title: '用户管理'
        }
      }
    },
    {
      path: '/admin/player',
      name: 'player-manager',
      component: () => import('@/views/admin/PlayerManager.vue'),
      meta: {
        breadcrumb: {
          title: '玩家管理'
        }
      }
    },
    {
      path: '/admin/session',
      name: 'session-manager',
      component: () => import('@/views/admin/SessionManager.vue'),
      meta: {
        breadcrumb: {
          title: '会话管理'
        }
      }
    },
    {
      path: '/admin/player/default/group',
      name: 'player-default-group-manager',
      component: () => import('@/views/admin/PlayerDefaultGroupManager.vue'),
      meta: {
        breadcrumb: {
          title: '玩家默认访问组'
        }
      }
    },
    {
      path: '/admin/group',
      name: 'user-group-manager',
      component: () => import('@/views/admin/UserGroupManager.vue'),
      meta: {
        breadcrumb: {
          title: '访问组管理'
        }
      }
    },
    {
      path: '/admin/permission',
      name: 'permission-manager',
      component: () => import('@/views/admin/PermissionManager.vue'),
      meta: {
        breadcrumb: {
          title: '权限管理'
        }  
      }
    },
    {
      path: '/admin/config',
      name: 'config-manager',
      component: () => import('@/views/admin/ConfigManager.vue'),
      meta: {
        breadcrumb: {
          title: '配置管理'
        }
      }  
    },
    {
      path: '/admin/model',
      name: 'ai-model-manager',
      component: () => import('@/views/admin/AiModelManager.vue'),
      meta: {
        breadcrumb: {
          title: 'AI模型管理'  
        }
      }
    },
    {
      path: '/admin/model/series',
      name: 'ai-model-series-manager',
      component: () => import('@/views/admin/AiModelSeriesManager.vue'),
      meta: {
        breadcrumb: {
          title: 'AI模型系列管理'
        }
      }
    },
    {
      path: '/admin/model/apikey',
      name: 'api-key-manager',
      component: () => import('@/views/admin/ApiKeyManager.vue'),
      meta: { 
        keepAlive: true,
        breadcrumb: {
          title: 'API密钥管理'  
        }
      }
    },
    {
      path: '/admin/model/apikey/authorization',
      name: 'api-key-authorization-manager',
      component: () => import('@/views/admin/ApiKeyAuthorizationManager.vue'),
      meta: {
        breadcrumb: {
          title: 'API密钥授权'
        }
      }
    },
    {
      path: '/admin/maintain',
      name: 'application-maintain',
      component: () => import('@/views/admin/ApplicationMaintain.vue'),
      meta: {
        breadcrumb: {
          title: '应用维护'
        }
      }
    },
    {
      path: '/admin/no-permission',
      name: 'no-permission',
      component: () => import('@/views/admin/NoPermission.vue'),
      meta: {
        breadcrumb: {
          hidden: true  
        }
      }
    }
  ],
})

export default router 