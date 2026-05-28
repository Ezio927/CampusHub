<template>
  <div class="page-container">
    <h2>管理后台</h2>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:16px">
      <div class="card"><strong>总用户</strong><h3>{{ d.totalUsers }}</h3></div>
      <div class="card"><strong>活跃需求</strong><h3>{{ d.activeDemands }}</h3></div>
      <div class="card"><strong>已完成订单</strong><h3>{{ d.completedOrders }}</h3></div>
      <div class="card"><strong>平均评分</strong><h3>{{ d.averageRating?.toFixed(1) }}</h3></div>
    </div>
    <div class="flex-row gap-sm" style="flex-wrap:wrap">
      <router-link to="/admin/users" class="btn-outline btn-sm">用户管理</router-link>
      <router-link to="/admin/certifications" class="btn-outline btn-sm">认证审核</router-link>
      <router-link to="/admin/arbitrations" class="btn-outline btn-sm">仲裁处理</router-link>
    </div>
  </div>
</template>
<script setup>
import { reactive, onMounted } from 'vue'
import { adminAPI } from '@/api/admin'
const d = reactive({ totalUsers:0, activeDemands:0, completedOrders:0, averageRating:0, pendingArbitrations:0 })
onMounted(async () => { try { const r = await adminAPI.getDashboard(); Object.assign(d, r.data) } catch(e) { alert(e.message) } })
</script>
