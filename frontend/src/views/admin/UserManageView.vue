<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">用户管理</h1>
    <input v-model="kw" placeholder="搜索用户名/姓名/学号" class="mb-md" @input="search" />
    <div v-for="u in list" :key="u.id" class="card flex-between">
      <div><div class="text-bold">{{ u.username }}</div><div class="text-sm text-muted">{{ u.realName }} · {{ u.studentId }} · {{ u.status }} · 信用{{ u.creditScore }} · 额度{{ u.quotaAvailable || 0 }}/冻结{{ u.quotaFrozen || 0 }}</div></div>
      <div class="flex gap-sm">
        <button class="btn-outline btn-sm" @click="adjustCredit(u.id)">调信用</button>
        <button class="btn-outline btn-sm" @click="adjustQuota(u.id)">调额度</button>
        <button v-if="u.status!=='BANNED' && u.role!=='ADMIN'" class="btn-danger btn-sm" @click="ban(u.id)">封禁</button>
        <button v-else-if="u.status==='BANNED'" class="btn-outline btn-sm" @click="unban(u.id)">解封</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import {ref} from 'vue';import {adminAPI} from '@/api/admin'
import { useModal } from '@/composables/useModal'
const modal=useModal()
const list=ref([]);const kw=ref('')
async function search(){const r=await adminAPI.users({keyword:kw.value});list.value=r.data||[]}
async function ban(id){await adminAPI.ban(id);search()}
async function unban(id){await adminAPI.unban(id);search()}
async function askAdjust(title){
  const raw=await modal.prompt(title,'请输入调整值，正数增加，负数扣除','例如：10 或 -5')
  if(raw===null||raw===undefined||!String(raw).trim())return null
  const amount=Number.parseInt(String(raw).trim(),10)
  if(Number.isNaN(amount)||amount===0){await modal.alert('调整失败','调整值必须是非零整数');return null}
  const reason=await modal.prompt(title,'请输入调整原因','管理员手动调整')
  return {amount,reason:reason||'管理员手动调整'}
}
async function adjustCredit(id){const body=await askAdjust('调整信用分');if(!body)return;try{await adminAPI.adjustCredit(id,body);await search()}catch(e){modal.alert('调整失败', e.message)}}
async function adjustQuota(id){const body=await askAdjust('调整额度分');if(!body)return;try{await adminAPI.adjustQuota(id,body);await search()}catch(e){modal.alert('调整失败', e.message)}}
search()
</script>
