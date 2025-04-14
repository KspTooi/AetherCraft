<template>
  <div class="user-manage">
    <h2>用户管理</h2>
    
    <div class="user-filter">
      <input type="text" v-model="searchTerm" placeholder="搜索用户..." />
      <button @click="searchUsers">搜索</button>
      <button class="add-btn" @click="showAddUserModal = true">添加用户</button>
    </div>
    
    <div class="user-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.status === 1 ? '启用' : '禁用' }}</td>
            <td class="actions">
              <button @click="editUser(user)">编辑</button>
              <button 
                @click="toggleUserStatus(user)"
                :class="user.status === 1 ? 'disable-btn' : 'enable-btn'"
              >
                {{ user.status === 1 ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface User {
  id: number
  username: string
  email: string
  status: number
}

const searchTerm = ref('')
const showAddUserModal = ref(false)

// 模拟用户数据
const users = ref<User[]>([
  { id: 1, username: 'admin', email: 'admin@example.com', status: 1 },
  { id: 2, username: 'user1', email: 'user1@example.com', status: 1 },
  { id: 3, username: 'user2', email: 'user2@example.com', status: 0 },
])

const searchUsers = () => {
  // 实现搜索逻辑
  console.log('Searching for:', searchTerm.value)
}

const editUser = (user: User) => {
  // 实现编辑用户逻辑
  console.log('Editing user:', user)
}

const toggleUserStatus = (user: User) => {
  // 实现切换用户状态逻辑
  user.status = user.status === 1 ? 0 : 1
  console.log('Toggled user status:', user)
}
</script>

<style scoped>
.user-manage {
  padding: 20px;
}

.user-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  flex: 1;
}

button {
  padding: 8px 15px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn {
  background-color: #2ecc71;
}

.user-table {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background-color: #f5f5f5;
  font-weight: bold;
}

tr:hover {
  background-color: #f9f9f9;
}

.actions {
  display: flex;
  gap: 5px;
}

.disable-btn {
  background-color: #e74c3c;
}

.enable-btn {
  background-color: #2ecc71;
}
</style> 