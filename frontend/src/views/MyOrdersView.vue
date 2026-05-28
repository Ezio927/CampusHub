<template>
  <div class="page-container">
    <h2 style="margin-bottom:12px">我的订单</h2>
    <div class="flex-row gap-sm" style="margin-bottom:12px">
      <select v-model="role" @change="load"><option value="PUBLISHER">我发布的</option><option value="ACCEPTOR">我接单的</option></select>
    </div>
    <div v-if="orders.length===0" class="empty-state">暂无订单</div>
    <div v-for="o in orders" :key="o.id" class="card" style="cursor:pointer" @click="$router.push(`/orders/${o.id}`)">
      <div class="flex-between"><span>#{{ o.id }}</span><span class="badge" :style="{background:statusColor(o.status)}">{{statusText(o.status)}}</span></div>
      <p style="margin:4px 0">需求ID: {{ o.demandId }}</p>
      <p class="text-secondary">{{ o.createdAt }}</p>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { orderAPI } from '@/api/orders'
const orders = ref([]); const role = ref('PUBLISHER')
function statusText(s) { return {PENDING_ACCEPT:'待接单',ACCEPTED:'已接单',IN_PROGRESS:'进行中',PENDING_CONFIRM:'待确认',COMPLETED:'已完成',CANCELLED:'已取消'}[s]||s }
function statusColor(s) { return {PENDING_ACCEPT:'#3b82f6',ACCEPTED:'#06b6d4',IN_PROGRESS:'#f97316',PENDING_CONFIRM:'#eab308',COMPLETED:'#22c55e',CANCELLED:'#9ca3af'}[s]||'#888' }
async function load() { const r = await orderAPI.list({ role: role.value }); orders.value = r.data || [] }
onMounted(load)
</script>
