<template>
  <div class="page">
    <h1 class="page-title">我的订单</h1>
    <div class="tabs"><div class="tab" :class="{on:role==='PUBLISHER'}" @click="role='PUBLISHER';load()">我发布的</div><div class="tab" :class="{on:role==='ACCEPTOR'}" @click="role='ACCEPTOR';load()">我接单的</div></div>
    <div v-if="list.length===0" class="empty">📋<p class="mt-sm">暂无订单</p></div>
    <div v-for="o in list" :key="o.id" class="oc" @click="goDetail(o.id)">
      <div class="flex-between">
        <div><div class="text-bold">{{ o.demandTitle || '需求 #'+o.demandId }}</div><div class="text-sm text-muted">{{ o.publisherName }} → {{ o.acceptorName }}</div></div>
        <div style="text-align:right"><div class="dp">{{ o.rewardType==='MONEY'?'¥'+o.rewardAmount:o.rewardAmount+'积分' }}</div><span class="badge" :style="{background:sb(o.status)}">{{ st(o.status) }}</span></div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref,onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderAPI } from '@/api/orders'
const ro=useRouter();const list=ref([]);const role=ref('PUBLISHER')
function st(s){return{PENDING_ACCEPT:'待接单',ACCEPTED:'已接单',IN_PROGRESS:'进行中',PENDING_CONFIRM:'待确认',COMPLETED:'已完成',CANCELLED:'已取消'}[s]||s}
function sb(s){return{PENDING_ACCEPT:'#3b82f6',ACCEPTED:'#06b6d4',IN_PROGRESS:'#f97316',PENDING_CONFIRM:'#eab308',COMPLETED:'#10b981',CANCELLED:'#9ca3af'}[s]||'#888'}
function goDetail(id){ro.push('/orders/'+id)}
async function load(){const r=await orderAPI.list({role:role.value});list.value=r.data||[]}
onMounted(load)
</script>
<style scoped>
.tabs{display:flex;background:#e2e8f0;border-radius:12px;padding:3px;margin-bottom:16px}
.tab{flex:1;text-align:center;padding:10px;border-radius:10px;font-size:14px;font-weight:600;cursor:pointer;color:var(--text-secondary);transition:all .2s}
.tab.on{background:#fff;color:var(--primary);box-shadow:0 1px 3px rgba(0,0,0,.1)}
.oc{background:#fff;border-radius:14px;padding:18px;margin-bottom:10px;border:1px solid var(--border);cursor:pointer;transition:all .2s}
.oc:hover{box-shadow:0 4px 16px rgba(0,0,0,.06);transform:translateY(-1px)}
.dp{font-size:18px;font-weight:800;color:var(--danger)}
</style>
