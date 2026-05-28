<template>
  <div v-if="demand">
    <div class="card">
      <div class="flex-between"><h3>{{ demand.title }}</h3><span class="badge" :style="{ background: catColor(demand.category) }">{{ catText(demand.category) }}</span></div>
      <p style="margin:12px 0;white-space:pre-wrap">{{ demand.description }}</p>
      <p><strong>报酬:</strong> {{ demand.rewardType === 'MONEY' ? '¥' + demand.rewardAmount : demand.rewardAmount + '积分' }}</p>
      <p><strong>位置:</strong> {{ demand.locationTag || '不限' }}</p>
      <p><strong>发布者:</strong> {{ demand.publisher?.username }} (信用分: {{ demand.publisher?.creditScore }})</p>
      <p><strong>发布时间:</strong> {{ demand.createdAt }}</p>
      <span v-if="demand.urgent" class="badge" style="background:var(--danger)">紧急</span>
    </div>
    <div v-if="auth.isLoggedIn && demand.status === 'OPEN' && demand.publisherId !== auth.currentUser?.id" class="card">
      <button class="btn-primary" style="width:100%" @click="acceptDemand">接单</button>
    </div>
    <div v-if="auth.isLoggedIn && demand.publisherId === auth.currentUser?.id && demand.status === 'OPEN'" class="card">
      <button class="btn-outline btn-sm" @click="cancelDemand">取消需求</button>
    </div>
  </div>
  <div v-else class="empty-state">加载中...</div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { demandAPI } from '@/api/demands'
import { orderAPI } from '@/api/orders'
const route = useRoute(); const router = useRouter(); const auth = useAuthStore()
const demand = ref(null)
function catText(c) { return { ERRAND:'跑腿', SKILL:'技能', STUDY:'学习', SECONDHAND:'二手' }[c] || c }
function catColor(c) { return { ERRAND:'#f59e0b', SKILL:'#4f46e5', STUDY:'#22c55e', SECONDHAND:'#ef4444' }[c] || '#888' }
async function acceptDemand() { try { await orderAPI.accept({ demandId: demand.value.id }); router.push('/orders') } catch(e) { alert(e.message) } }
async function cancelDemand() { try { await demandAPI.cancel(demand.value.id); router.go(0) } catch(e) { alert(e.message) } }
onMounted(async () => { const r = await demandAPI.getDetail(route.params.id); demand.value = r.data })
</script>
<style scoped>@import './DemandDetailView.css';</style>
