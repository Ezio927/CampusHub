<template>
  <div class="page">
    <h1 class="page-title">我的订单</h1>
    <div class="tabs">
      <div class="tab" :class="{ on: role === 'PUBLISHER' }" @click="switchRole('PUBLISHER')">我发布的</div>
      <div class="tab" :class="{ on: role === 'ACCEPTOR' }" @click="switchRole('ACCEPTOR')">我接单的</div>
    </div>

    <template v-if="role === 'PUBLISHER'">
      <div class="section-title">我发布的需求</div>
      <div v-if="demands.length===0" class="empty"><p class="mt-sm">暂无发布的需求</p></div>
      <div v-else class="card-grid">
        <div v-for="item in demands" :key="'d' + item.id" class="oc" @click="goDemand(item.id)">
          <div class="flex-between">
            <div>
              <div class="text-bold">{{ item.title }}</div>
              <div class="text-sm text-muted">{{ categoryText(item.category) }}</div>
            </div>
            <div style="text-align:right">
              <div class="dp">{{ item.rewardType === 'MONEY' ? '￥' + item.rewardAmount : item.rewardAmount + '额度分' }}</div>
              <span class="badge" :style="{ background: demandStatusColor(item.status) }">{{ demandStatusText(item.status) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="section-title" style="margin-top:20px">已生成的订单</div>
      <div v-if="orders.length===0" class="empty"><p class="mt-sm">暂无订单</p></div>
      <div v-else class="card-grid">
        <div v-for="item in orders" :key="'o' + item.id" class="oc" @click="goOrder(item.id)">
          <div class="flex-between">
            <div>
              <div class="text-bold">{{ item.demandTitle || ('需求 #' + item.demandId) }}</div>
              <div class="text-sm text-muted">{{ item.publisherName }} → {{ item.acceptorName }}</div>
            </div>
            <div style="text-align:right">
              <div class="dp">{{ item.rewardType === 'MONEY' ? '￥' + item.rewardAmount : item.rewardAmount + '额度分' }}</div>
              <span class="badge" :style="{ background: orderStatusColor(item.status) }">{{ orderStatusText(item.status) }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div v-if="orders.length===0" class="empty"><p class="mt-sm">暂无订单</p></div>
      <div v-else class="card-grid">
        <div v-for="item in orders" :key="'o' + item.id" class="oc" @click="goOrder(item.id)">
          <div class="flex-between">
            <div>
              <div class="text-bold">{{ item.demandTitle || ('需求 #' + item.demandId) }}</div>
              <div class="text-sm text-muted">{{ item.publisherName }} → {{ item.acceptorName }}</div>
            </div>
            <div style="text-align:right">
              <div class="dp">{{ item.rewardType === 'MONEY' ? '￥' + item.rewardAmount : item.rewardAmount + '额度分' }}</div>
              <span class="badge" :style="{ background: orderStatusColor(item.status) }">{{ orderStatusText(item.status) }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderAPI } from '@/api/orders'
import { demandAPI } from '@/api/demands'

const route = useRoute()
const router = useRouter()
const demands = ref([])
const orders = ref([])
const role = ref('PUBLISHER')

function orderStatusText(status) {
  return {
    PENDING_ACCEPT: '待接单',
    ACCEPTED: '已接单',
    PENDING_CONFIRM: '待确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }[status] || status
}

function orderStatusColor(status) {
  return {
    PENDING_ACCEPT: '#3b82f6',
    ACCEPTED: '#06b6d4',
    PENDING_CONFIRM: '#eab308',
    COMPLETED: '#10b981',
    CANCELLED: '#9ca3af'
  }[status] || '#888'
}

function demandStatusText(status) {
  return {
    OPEN: '待接单',
    ACCEPTED: '已接单',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }[status] || status
}

function demandStatusColor(status) {
  return {
    OPEN: '#3b82f6',
    ACCEPTED: '#06b6d4',
    IN_PROGRESS: '#f97316',
    COMPLETED: '#10b981',
    CANCELLED: '#9ca3af'
  }[status] || '#888'
}

function categoryText(category) {
  return {
    ERRAND: '跑腿代办',
    SKILL: '技能服务',
    STUDY: '学习互助',
    SECONDHAND: '二手交易',
    OTHER: '其他'
  }[category] || category
}

function goOrder(id) {
  router.push({ path: '/orders/' + id, query: { role: role.value } })
}

function goDemand(id) {
  router.push('/demands/' + id)
}

async function switchRole(nextRole) {
  if (role.value === nextRole) return
  role.value = nextRole
  await router.replace({ path: '/orders', query: { role: nextRole } })
  await load()
}

async function load() {
  if (role.value === 'PUBLISHER') {
    const [demandRes, orderRes] = await Promise.allSettled([
      demandAPI.my(),
      orderAPI.list({ role: 'PUBLISHER' })
    ])
    demands.value = demandRes.status === 'fulfilled'
      ? (demandRes.value.data?.content || []).filter(item => item.status === 'OPEN')
      : []
    orders.value = orderRes.status === 'fulfilled'
      ? (orderRes.value.data?.content || orderRes.value.data || [])
      : []
  } else {
    const res = await orderAPI.list({ role: 'ACCEPTOR' })
    orders.value = res.data?.content || res.data || []
    demands.value = []
  }
}

onMounted(async () => {
  role.value = route.query.role === 'ACCEPTOR' ? 'ACCEPTOR' : 'PUBLISHER'
  await load()
})
</script>

<style scoped>
.tabs{display:flex;background:#fff;border-radius:10px;padding:4px;margin-bottom:16px;border:2px solid black}
.tab{flex:1;text-align:center;padding:10px;border-radius:8px;font-size:14px;font-weight:700;cursor:pointer;color:var(--text-secondary);transition:all .2s ease}
.tab.on{background:var(--primary);color:#fff}
.section-title{font-size:13px;font-weight:700;color:var(--text-secondary);margin-bottom:8px;padding-left:4px}
.card-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:14px}
.oc{background:#fff;border-radius:12px;padding:18px;border:3px solid black;box-shadow:6px 6px 0 rgba(0,0,0,1);cursor:pointer;transition:all .2s ease}
.oc:hover{transform:scale(1.03);box-shadow:8px 8px 0 rgba(0,0,0,1)}
.dp{font-size:18px;font-weight:800;color:var(--danger)}
</style>
