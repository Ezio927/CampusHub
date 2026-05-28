<template>
  <div class="page-container">
    <h2 class="page-title">管理后台</h2>
    <div class="dash-grid">
      <div class="stat-card"><div class="stat-value">{{ d.totalUsers }}</div><div class="stat-label">总用户</div></div>
      <div class="stat-card"><div class="stat-value">{{ d.activeDemands }}</div><div class="stat-label">活跃需求</div></div>
      <div class="stat-card"><div class="stat-value">{{ d.completedOrders }}</div><div class="stat-label">已完成订单</div></div>
      <div class="stat-card"><div class="stat-value">{{ d.averageRating?.toFixed(1) || '-' }}</div><div class="stat-label">平均评分</div></div>
    </div>
    <div class="dash-nav">
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
<style scoped>@import './DashboardView.css';</style>
