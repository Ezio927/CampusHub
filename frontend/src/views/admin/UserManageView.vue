<template>
  <div class="page-container">
    <h2>用户管理</h2>
    <div class="form-group"><input v-model="keyword" placeholder="搜索用户名/姓名/学号" @input="search" /></div>
    <div v-for="u in users" :key="u.id" class="card flex-between">
      <div><strong>{{ u.username }}</strong> ({{ u.realName }})<br /><small class="text-secondary">{{ u.studentId }} · {{ u.status }} · 信用{{ u.creditScore }}</small></div>
      <div class="flex-row gap-sm">
        <button v-if="u.status !== 'BANNED'" class="btn-danger btn-sm" @click="ban(u.id)">封禁</button>
        <button v-else class="btn-outline btn-sm" @click="unban(u.id)">解封</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { adminAPI } from '@/api/admin'
const users = ref([]); const keyword = ref('')
async function search() { const r = await adminAPI.listUsers({ keyword: keyword.value }); users.value = r.data || [] }
async function ban(id) { await adminAPI.banUser(id); search() }
async function unban(id) { await adminAPI.unbanUser(id); search() }
search()
</script>
<style scoped>@import './UserManageView.css';</style>
