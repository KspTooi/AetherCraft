<template>
  <div class="admin-side-panel">
    <!-- 标题区域 -->
    <div v-if="title" class="panel-title">
      {{ title }}
    </div>
    
    <!-- 菜单区域 -->
    <el-menu
      :default-active="activeItemId"
      class="panel-menu"
      @select="handleSelect"
      :collapse="isCollapse"
      :unique-opened="true"
      :router="true"
    >
      <template v-for="item in items" :key="item.id">
        <!-- 带子菜单的项目 -->
        <el-sub-menu v-if="item.children?.length" :index="item.id">
          <template #title>
            <el-icon>
              <component :is="getIconComponent(item.icon)" v-if="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
            <el-badge v-if="item.badge" :value="item.badge" class="item-badge" />
          </template>
          <el-menu-item 
            v-for="child in item.children" 
            :key="child.id" 
            :index="child.id"
            :route="child.routerLink ? { path: child.routerLink } : undefined"
          >
            <el-icon>
              <component :is="getIconComponent(child.icon)" v-if="child.icon" />
            </el-icon>
            <span>{{ child.title }}</span>
            <el-badge v-if="child.badge" :value="child.badge" class="item-badge" />
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 无子菜单的普通项目 -->
        <el-menu-item 
          v-else 
          :index="item.id"
          :route="item.routerLink ? { path: item.routerLink } : undefined"
        >
          <el-icon>
            <component :is="getIconComponent(item.icon)" v-if="item.icon" />
          </el-icon>
          <span>{{ item.title }}</span>
          <el-badge v-if="item.badge" :value="item.badge" class="item-badge" />
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, computed, markRaw } from 'vue'
import { ElMenu, ElMenuItem, ElSubMenu, ElIcon, ElBadge } from 'element-plus'
import type { Component } from 'vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'

// 使用 markRaw 包装所有图标组件
const icons = Object.fromEntries(
  Object.entries(ElementPlusIcons).map(([key, component]) => [key, markRaw(component)])
);

// 定义组件props
const props = defineProps<{
  items: Array<{
    id: string,
    title: string,
    icon?: string | Component,
    badge?: number | string,
    routerLink?: string,
    action?: string,
    children?: Array<{
      id: string,
      title: string,
      icon?: string | Component,
      badge?: number | string,
      routerLink?: string,
      action?: string
    }>
  }>,
  activeItemId?: string,
  title?: string,
  isCollapse?: boolean
}>()

// 获取图标组件，确保使用 markRaw 版本
const getIconComponent = (iconName: string | Component | undefined) => {
  if (!iconName) return null;
  if (typeof iconName === 'string' && iconName in icons) {
    return icons[iconName];
  }
  return iconName;
}

// 定义事件
const emit = defineEmits<{
  (e: 'item-click', itemId: string): void
  (e: 'action', action: string, itemId: string): void
  (e: 'update:activeItemId', itemId: string): void
}>()

// 处理菜单选择
const handleSelect = (index: string) => {
  const findItem = (items: any[], index: string): any => {
    for (const item of items) {
      if (item.id === index) {
        return item
      }
      if (item.children?.length) {
        const found = findItem(item.children, index)
        if (found) return found
      }
    }
    return null
  }

  const selectedItem = findItem(props.items, index)
  if (!selectedItem) return
  
  if (selectedItem.action) {
    emit('action', selectedItem.action, selectedItem.id)
  } else {
    emit('item-click', selectedItem.id)
    emit('update:activeItemId', selectedItem.id)
  }
}
</script>

<style scoped>
.admin-side-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-border-color-light);
  overflow: hidden;
}

.panel-title {
  padding: 16px;
  font-size: 16px;
  font-weight: bold;
  text-align: center;
  color: var(--el-text-color-primary);
  border-bottom: 1px solid var(--el-border-color-light);
  flex-shrink: 0;
}

.panel-menu {
  flex: 1;
  overflow-y: auto;
  border-right: none;
}

.panel-menu:not(.el-menu--collapse) {
  width: 240px;
}

.item-badge {
  margin-left: 8px;
}

/* 滚动条样式 */
.panel-menu::-webkit-scrollbar {
  width: 4px;
}

.panel-menu::-webkit-scrollbar-track {
  background: transparent;
}

.panel-menu::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 2px;
}

.panel-menu::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color-darker);
}
</style>