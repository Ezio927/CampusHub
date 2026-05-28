<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">👥 用户管理</h1>
    <input v-model="kw" placeholder="搜索用户名/姓名/学号" class="mb-md" @input="search" />
    <div v-for="u in list" :key="u.id" class="card flex-between">
      <div><div class="text-bold">{{ u.username }}</div><div class="text-sm text-muted">{{ u.realName }} · {{ u.studentId }} · {{ u.status }} · 信用{{ u.creditScore }}</div></div>
      <div class="flex gap-sm">
        <button v-if="u.status!=='BANNED'" class="btn-danger btn-sm" @click="ban(u.id)">封禁</button>
        <button v-else class="btn-outline btn-sm" @click="unban(u.id)">解封</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import {ref} from 'vue';import {adminAPI} from '@/api/admin'
const list=ref([]);const kw=ref('')
async function search(){const r=await adminAPI.users({keyword:kw.value});list.value=r.data||[]}
async function ban(id){await adminAPI.ban(id);search()}
async function unban(id){await adminAPI.unban(id);search()}
search()
</script>
