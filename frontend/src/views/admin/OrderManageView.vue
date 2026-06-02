<template>
  <div class="page">
    <a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">订单管理</h1>
    <div class="toolbar mb-md">
      <select v-model="status" @change="load">
        <option value="">全部状态</option>
        <option value="OPEN">待接单需求</option>
        <option value="ACCEPTED">已接单</option>
        <option value="PENDING_CONFIRM">待确认</option>
        <option value="COMPLETED">已完成</option>
        <option value="CANCELLED">已取消</option>
      </select>
      <button class="btn-outline btn-sm" @click="load">刷新</button>
    </div>
    <div v-if="orders.length===0" class="card empty">暂无订单</div>
    <div v-for="o in orders" :key="rowKey(o)" class="card order-row">
      <div>
        <div class="text-bold">
          <span class="badge">{{ o.rowType==='DEMAND' ? '需求' : '订单' }}</span>
          #{{ o.orderId || o.demandId }} {{ o.demandTitle || '需求' + o.demandId }}
        </div>
        <div class="text-sm text-muted">
          需求 {{ o.demandId }} · 发布者 {{ o.publisherName || o.publisherId }} · 接单者 {{ o.acceptorName || o.acceptorId || '-' }} · {{ o.status }}
        </div>
      </div>
      <button class="btn-danger btn-sm" @click="remove(o)">删除</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminAPI } from '@/api/admin'
import { useModal } from '@/composables/useModal'

const modal = useModal()
const status = ref('')
const orders = ref([])

async function load() {
  try {
    const r = await adminAPI.orders({ status: status.value || undefined })
    orders.value = r.data || []
  } catch (e) {
    await modal.alert('加载失败', e.message)
  }
}

function rowKey(row) {
  return `${row.rowType}-${row.orderId || row.demandId}`
}

async function remove(order) {
  const label = order.rowType === 'DEMAND' ? '需求' : '订单'
  const id = order.orderId || order.demandId
  const ok = await modal.confirm(`删除${label}`, `确定删除${label} #${id}？该操作不可恢复。`)
  if (!ok) return
  try {
    if (order.rowType === 'DEMAND') {
      await adminAPI.deleteDemand(order.demandId)
    } else {
      await adminAPI.deleteOrder(order.orderId)
    }
    await load()
  } catch (e) {
    await modal.alert('删除失败', e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar{display:flex;gap:10px;align-items:center}
.toolbar select{max-width:180px}
.order-row{display:flex;align-items:center;justify-content:space-between;gap:16px}
.order-row>div{min-width:0}
.order-row .text-sm{overflow-wrap:anywhere}
.badge{display:inline-flex;align-items:center;margin-right:8px;padding:2px 8px;border-radius:999px;background:#eef2ff;color:var(--primary);font-size:12px;font-weight:700}
.empty{text-align:center;color:var(--text-muted)}
@media (max-width: 640px){.order-row{align-items:flex-start;flex-direction:column}.order-row button{width:100%}}
</style>
