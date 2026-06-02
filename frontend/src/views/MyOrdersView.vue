<template>
  <div class="page">
    <h1 class="page-title">我的订单</h1>
    <div class="tabs">
      <button class="tab" :class="{ on: tab === 'PUBLISHER' }" @click="switchTab('PUBLISHER')">我发布的</button>
      <button class="tab" :class="{ on: tab === 'ACCEPTOR' }" @click="switchTab('ACCEPTOR')">我接单的</button>
      <button class="tab" :class="{ on: tab === 'HISTORY' }" @click="switchTab('HISTORY')">历史记录</button>
    </div>

    <template v-if="tab === 'PUBLISHER'">
      <div class="section-title">我发布的需求</div>
      <div v-if="demands.length === 0" class="empty"><p class="mt-sm">暂无可接单需求</p></div>
      <div v-else class="card-grid">
        <div v-for="demand in demands" :key="'d' + demand.id" class="order-card" @click="goDemand(demand.id)">
          <div class="flex-between">
            <div>
              <div class="text-bold">{{ demand.title }}</div>
              <div class="text-sm text-muted">{{ categoryText(demand.category) }}</div>
            </div>
            <div class="card-side">
              <div class="reward">{{ rewardText(demand) }}</div>
              <span class="badge" :style="{ background: demandStatusColor(demand.status) }">{{ demandStatusText(demand.status) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="section-title mt-lg">已生成的订单</div>
      <OrderList :orders="orders" empty-text="暂无订单" @open="goOrder" />
    </template>

    <template v-else-if="tab === 'ACCEPTOR'">
      <OrderList :orders="orders" empty-text="暂无订单" @open="goOrder" />
    </template>

    <template v-else>
      <div v-if="history.length === 0" class="empty"><p class="mt-sm">暂无历史记录</p></div>
      <div v-else class="history-list">
        <div v-for="order in history" :key="order.id" class="history-card" @click="goOrder(order.id)">
          <div class="history-main">
            <div class="flex-between gap-sm">
              <div class="min-w-0">
                <div class="text-bold title-line">{{ order.demandTitle || '需求 #' + order.demandId }}</div>
                <div class="text-sm text-muted">{{ order.publisherName || '发布者' }} → {{ order.acceptorName || '接单者' }}</div>
              </div>
              <span class="badge" :style="{ background: orderStatusColor(order.status) }">{{ orderStatusText(order.status) }}</span>
            </div>
            <div class="history-meta">
              <span>报酬：{{ rewardText(order) }}</span>
              <span>创建：{{ formatDate(order.createdAt) }}</span>
              <span v-if="order.completedAt">完成：{{ formatDate(order.completedAt) }}</span>
              <span v-if="order.cancelledAt">取消：{{ formatDate(order.cancelledAt) }}</span>
            </div>
            <div v-if="order.cancelReason" class="history-reason">取消原因：{{ order.cancelReason }}</div>
            <div class="review-line">
              <span>发布者评价：{{ order.publisherReviewed ? '已评价' : '未评价' }}</span>
              <span>接单者评价：{{ order.acceptorReviewed ? '已评价' : '未评价' }}</span>
              <span v-if="canReview(order)" class="review-tip">待你评价</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { defineComponent, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { orderAPI } from '@/api/orders'
import { demandAPI } from '@/api/demands'
import { useAuthStore } from '@/stores/auth'

const OrderList = defineComponent({
  props: {
    orders: { type: Array, default: () => [] },
    emptyText: { type: String, default: '暂无订单' },
  },
  emits: ['open'],
  setup(props, { emit }) {
    return () =>
      props.orders.length === 0
        ? h('div', { class: 'empty' }, [h('p', { class: 'mt-sm' }, props.emptyText)])
        : h(
            'div',
            { class: 'card-grid' },
            props.orders.map(order =>
              h(
                'div',
                { key: order.id, class: 'order-card', onClick: () => emit('open', order.id) },
                h('div', { class: 'flex-between' }, [
                  h('div', { class: 'min-w-0' }, [
                    h('div', { class: 'text-bold title-line' }, order.demandTitle || `需求 #${order.demandId}`),
                    h('div', { class: 'text-sm text-muted' }, `${order.publisherName || '发布者'} → ${order.acceptorName || '接单者'}`),
                  ]),
                  h('div', { class: 'card-side' }, [
                    h('div', { class: 'reward' }, rewardText(order)),
                    h('span', { class: 'badge', style: { background: orderStatusColor(order.status) } }, orderStatusText(order.status)),
                  ]),
                ]),
              ),
            ),
          )
  },
})

const router = useRouter()
const auth = useAuthStore()
const tab = ref('PUBLISHER')
const demands = ref([])
const orders = ref([])
const history = ref([])

function switchTab(next) {
  tab.value = next
  load()
}

function orderStatusText(status) {
  return { PENDING_ACCEPT: '待接单', ACCEPTED: '已接单', PENDING_CONFIRM: '待确认', COMPLETED: '已完成', CANCELLED: '已取消' }[status] || status
}

function orderStatusColor(status) {
  return { PENDING_ACCEPT: '#3b82f6', ACCEPTED: '#06b6d4', PENDING_CONFIRM: '#eab308', COMPLETED: '#10b981', CANCELLED: '#9ca3af' }[status] || '#888'
}

function demandStatusText(status) {
  return { OPEN: '待接单', ACCEPTED: '已接单', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }[status] || status
}

function demandStatusColor(status) {
  return { OPEN: '#3b82f6', ACCEPTED: '#06b6d4', IN_PROGRESS: '#f97316', COMPLETED: '#10b981', CANCELLED: '#9ca3af' }[status] || '#888'
}

function categoryText(category) {
  return { ERRAND: '跑腿代办', SKILL: '技能求助', STUDY: '学习互助', SECONDHAND: '二手交易', OTHER: '其他' }[category] || category
}

function rewardText(item) {
  if (!item) return '-'
  return item.rewardType === 'MONEY' ? `¥${item.rewardAmount || 0}` : `${item.rewardAmount || 0}额度分`
}

function formatDate(value) {
  return value?.substring(0, 16)?.replace('T', ' ') || '-'
}

function canReview(order) {
  const uid = auth.user?.id
  if (!uid || order.status !== 'COMPLETED') return false
  if (order.publisherId === uid) return !order.publisherReviewed
  if (order.acceptorId === uid) return !order.acceptorReviewed
  return false
}

function goOrder(id) {
  router.push('/orders/' + id)
}

function goDemand(id) {
  router.push('/demands/' + id)
}

async function load() {
  if (tab.value === 'PUBLISHER') {
    const [demandRes, orderRes] = await Promise.allSettled([demandAPI.my(), orderAPI.list({ role: 'PUBLISHER' })])
    demands.value = demandRes.status === 'fulfilled' ? (demandRes.value.data?.content || []).filter(demand => demand.status === 'OPEN') : []
    orders.value = orderRes.status === 'fulfilled' ? orderRes.value.data?.content || orderRes.value.data || [] : []
    return
  }
  if (tab.value === 'ACCEPTOR') {
    const res = await orderAPI.list({ role: 'ACCEPTOR' })
    orders.value = res.data || []
    return
  }
  const res = await orderAPI.history()
  history.value = res.data || []
}

onMounted(async () => {
  await auth.fetchUser()
  load()
})
</script>

<style scoped>
.tabs { display: flex; background: #fff; border-radius: 10px; padding: 4px; margin-bottom: 16px; border: 2px solid black; gap: 4px; }
.tab { flex: 1; text-align: center; padding: 10px; border-radius: 8px; font-size: 14px; font-weight: 700; cursor: pointer; color: var(--text-secondary); transition: all .2s ease; border: 0; background: transparent; }
.tab.on { background: var(--primary); color: #fff; }
.section-title { font-size: 13px; font-weight: 700; color: var(--text-secondary); margin-bottom: 8px; padding-left: 4px; }
.card-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.order-card, .history-card { background: #fff; border-radius: 12px; padding: 18px; border: 3px solid black; box-shadow: 6px 6px 0 rgba(0,0,0,1); cursor: pointer; transition: all .2s ease; }
.order-card:hover, .history-card:hover { transform: scale(1.02); box-shadow: 8px 8px 0 rgba(0,0,0,1); }
.card-side { text-align: right; flex-shrink: 0; }
.reward { font-size: 18px; font-weight: 800; color: var(--danger); }
.history-list { display: flex; flex-direction: column; gap: 14px; }
.history-main { display: flex; flex-direction: column; gap: 10px; }
.history-meta, .review-line { display: flex; flex-wrap: wrap; gap: 8px 14px; font-size: 13px; color: var(--text-muted); }
.history-reason { padding: 8px 10px; border-radius: 8px; background: #f8fafc; border: 1px solid var(--border); color: var(--text-secondary); font-size: 13px; }
.review-tip { color: var(--danger); font-weight: 800; }
.title-line { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.min-w-0 { min-width: 0; }
.mt-lg { margin-top: 20px; }
@media (max-width: 760px) {
  .card-grid { grid-template-columns: 1fr; }
  .tabs { flex-direction: column; }
  .card-side { text-align: left; }
}
</style>
