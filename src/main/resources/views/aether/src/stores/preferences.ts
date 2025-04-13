import { defineStore } from 'pinia'
import Http from '@/commons/Http'

interface Preferences {
  clientPath: string | null
  customizePathSide: string | null
  customizePathTabWallpaper: string | null
  customizePathTabTheme: string | null
  modelRoleEditCurrentId: string | null
  modelRoleEditPathTab: string | null
  clientRpPath: string | null
}

export const usePreferencesStore = defineStore('preferences', {
  state: (): Preferences => ({
    clientPath: null,
    customizePathSide: null,
    customizePathTabWallpaper: null,
    customizePathTabTheme: null,
    modelRoleEditCurrentId: null,
    modelRoleEditPathTab: null,
    clientRpPath: null
  }),

  actions: {
    async savePreferences(preferences: Partial<Preferences>) {
      try {
        await Http.postEntity<string>('/client/savePreferences', preferences)
        // 更新本地状态
        Object.assign(this, preferences)
      } catch (error) {
        console.warn('保存用户偏好设置失败:', error)
      }
    },

    async loadPreferences() {
      try {
        const response = await Http.postEntity<Preferences>('/client/getPreferences', {})
        if (response) {
          // 更新本地状态
          Object.assign(this, response)
        }
      } catch (error) {
        console.warn('获取用户偏好设置失败:', error)
      }
    },

    // 保存当前路由路径
    async saveCurrentPath(path: string) {
      if (path === '/') return
      await this.savePreferences({ clientPath: path })
    },

    // 保存个性化边栏路径
    async saveCustomizePathSide(path: string) {
      await this.savePreferences({ customizePathSide: path })
    },

    // 保存个性化背景TAB
    async saveCustomizePathTabWallpaper(path: string) {
      await this.savePreferences({ customizePathTabWallpaper: path })
    },

    // 保存个性化主题TAB
    async saveCustomizePathTabTheme(path: string) {
      await this.savePreferences({ customizePathTabTheme: path })
    },
    
    // 保存角色设计器当前角色ID
    async saveModelRoleEditCurrentId(id: string) {
      await this.savePreferences({ modelRoleEditCurrentId: id })
    },
    
    // 保存角色设计器TAB
    async saveModelRoleEditPathTab(path: string) {
      await this.savePreferences({ modelRoleEditPathTab: path })
    },
    
    // 保存角色扮演当前路径
    async saveClientRpPath(path: string) {
      await this.savePreferences({ clientRpPath: path })
    }
  },

  getters: {
    // 获取保存的路由路径
    getSavedPath(): string | null {
      return this.clientPath
    },

    // 获取个性化边栏路径
    getCustomizePathSide(): string | null {
      return this.customizePathSide
    },

    // 获取个性化背景TAB
    getCustomizePathTabWallpaper(): string | null {
      return this.customizePathTabWallpaper
    },

    // 获取个性化主题TAB
    getCustomizePathTabTheme(): string | null {
      return this.customizePathTabTheme
    },
    
    // 获取角色设计器当前角色ID
    getModelRoleEditCurrentId(): string | null {
      return this.modelRoleEditCurrentId
    },
    
    // 获取角色设计器TAB
    getModelRoleEditPathTab(): string | null {
      return this.modelRoleEditPathTab
    },
    
    // 获取角色扮演当前路径
    getClientRpPath(): string | null {
      return this.clientRpPath
    }
  }
}) 