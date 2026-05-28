<template>
  <div v-if="order" class="page-container">
    <div class="card">
      <div class="flex-between"><h3>订单 #{{ order.id }}</h3><span class="badge" :style="{background:statusColor(order.status)}">{{statusText(order.status)}}</span></div>
      <p>需求ID: {{ order.demandId }}</p>
      <p>发布者ID: {{ order.publisherId }}</p>
      <p>接单者ID: {{ order.acceptorId }}</p>
      <p class="text-secondary">{{ order.createdAt }}</p>
    </div>
    <div class="card" v-if="actions.length">
      <button v-for="a in actions" :key="a.key" class="btn-primary" style="width:100%;margin-bottom:8px" @click="doAction(a.key)">{{ a.label }}</button>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { orderAPI } from '@/api/orders'
const route = useRoute(); const router = useRouter(); const auth = useAuthStore()
const order = ref(null)
function statusText(s) { return {PENDING_ACCEPT:'待接单',ACCEPTED:'已接单',IN_PROGRESS:'进行中',PENDING_CONFIRM:'待确认',COMPLETED:'已完成',CANCELLED:'已取消'}[s]||s }
function statusColor(s) { return {PENDING_ACCEPT:'#3b82f6',ACCEPTED:'#06b6d4',IN_PROGRESS:'#f97316',PENDING_CONFIRM:'#eab308',COMPLETED:'#22c55e',CANCELLED:'#9ca3af'}[s]||'#888' }
const actions = computed(() => {
  if (!order.value) return []
  const uid = auth.currentUser?.id; const s = order.value.status
  const acts = []
  if (s === 'ACCEPTED' && order.value.acceptorId === uid) acts.push({key:'deliver',label:'标记已送达'})
  if (s === 'PENDING_CONFIRM' && order.value.publisherId === uid) acts.push({key:'confirm',label:'确认完成'})
  if (['PENDING_ACCEPT','ACCEPTED','IN_PROGRESS','PENDING_CONFIRM'].includes(s)) acts.push({key:'cancel',label:'取消订单'})
  return acts
})
async function doAction(key) {
  try {
    if (key === 'deliver') await orderAPI.deliver(order.value.id)
    else if (key === 'confirm') { await orderAPI.confirm(order.value.id); router.push(`/reviews/user/${order.value.acceptorId}`) }
    else if (key === 'cancel') { const r = prompt('取消原因'); if (r) await orderAPI.cancel(order.value.id, { reason: r }) }
    router.go(0)
  } catch(e) { alert(e.message) }
}
onMounted(async () => { const r = await orderAPI.getDetail(route.params.id); order.value = r.data })
</script>
