<template>
  <div class="page">
    <h1 class="page-title">📊 管理后台</h1>
    <div class="grid-2 mb-md">
      <div class="sc"><div class="sv">{{ d.totalUsers }}</div><div class="sl">总用户</div></div>
      <div class="sc"><div class="sv">{{ d.activeDemands }}</div><div class="sl">活跃需求</div></div>
      <div class="sc"><div class="sv">{{ d.completedOrders }}</div><div class="sl">已完成订单</div></div>
      <div class="sc"><div class="sv">{{ d.averageRating?.toFixed(1)||'-' }}</div><div class="sl">平均评分</div></div>
    </div>
    <div class="flex gap-sm flex-wrap">
      <router-link to="/admin/users" class="btn-outline">👥 用户管理</router-link>
      <router-link to="/admin/certifications" class="btn-outline">🪪 认证审核</router-link>
      <router-link to="/admin/arbitrations" class="btn-outline">⚖️ 仲裁处理</router-link>
    </div>
  </div>
</template>
<script setup>
import {reactive,onMounted} from 'vue'
import {adminAPI} from '@/api/admin'
const d=reactive({totalUsers:0,activeDemands:0,completedOrders:0,averageRating:0})
onMounted(async()=>{try{const r=await adminAPI.dashboard();Object.assign(d,r.data)}catch(e){alert(e.message)}})
</script>
<style scoped>
.sc{background:#fff;border-radius:14px;padding:24px;text-align:center;border:1px solid var(--border)}
.sv{font-size:34px;font-weight:800;background:linear-gradient(135deg,var(--primary),#8b5cf6);-webkit-background-clip:text;-webkit-text-fill-color:transparent}
.sl{font-size:13px;color:var(--text-muted);margin-top:4px;font-weight:500}
</style>
