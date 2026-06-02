<template>
  <div class="page">
    <h1 class="page-title">📊 管理后台</h1>
    <div class="grid-2 mb-md">
      <div class="sc"><div class="sv">{{ d.totalUsers }}</div><div class="sl">总用户</div></div>
      <div class="sc"><div class="sv">{{ d.activeDemands }}</div><div class="sl">活跃需求</div></div>
      <div class="sc"><div class="sv">{{ d.completedOrders }}</div><div class="sl">已完成订单</div></div>
      <div class="sc"><div class="sv">{{ d.averageRating?.toFixed(1)||'-' }}</div><div class="sl">平均评分</div></div>
    </div>
    <div class="nav-cards">
      <router-link to="/admin/users" class="nc">
        <div class="nc-icon" style="background:#eef2ff">👥</div>
        <div class="nc-info"><div class="nc-title">用户管理</div><div class="nc-desc">查看、封禁、解封用户</div></div>
        <span class="nc-arrow">→</span>
      </router-link>
      <router-link to="/admin/orders" class="nc">
        <div class="nc-icon" style="background:#f0f9ff">#</div>
        <div class="nc-info"><div class="nc-title">订单管理</div><div class="nc-desc">查看订单并执行管理员删除</div></div>
        <span class="nc-arrow">→</span>
      </router-link>
      <router-link to="/admin/certifications" class="nc">
        <div class="nc-icon" style="background:#ecfdf5">🪪</div>
        <div class="nc-info"><div class="nc-title">认证审核</div><div class="nc-desc">审核学生实名认证</div></div>
        <span class="nc-arrow">→</span>
      </router-link>
      <router-link to="/admin/arbitrations" class="nc">
        <div class="nc-icon" style="background:#fffbeb">⚖️</div>
        <div class="nc-info"><div class="nc-title">仲裁处理</div><div class="nc-desc">处理纠纷与申诉工单</div></div>
        <span class="nc-arrow">→</span>
      </router-link>
      <router-link to="/admin/message-reports" class="nc">
        <div class="nc-icon" style="background:#fef2f2">!</div>
        <div class="nc-info"><div class="nc-title">私信举报</div><div class="nc-desc">审核用户提交的聊天举报</div></div>
        <span class="nc-arrow">→</span>
      </router-link>
      <router-link to="/admin/credit-appeals" class="nc">
        <div class="nc-icon" style="background:#eef2ff">⭐</div>
        <div class="nc-info"><div class="nc-title">信用申诉</div><div class="nc-desc">处理用户信用分恢复申请</div></div>
        <span class="nc-arrow">→</span>
      </router-link>
    </div>
  </div>
</template>
<script setup>
import {reactive,onMounted} from 'vue'
import {adminAPI} from '@/api/admin'
import { useModal } from '@/composables/useModal'
const modal = useModal()
const d=reactive({totalUsers:0,activeDemands:0,completedOrders:0,averageRating:0})
onMounted(async()=>{try{const r=await adminAPI.dashboard();Object.assign(d,r.data)}catch(e){modal.alert('加载失败', e.message)}})
</script>
<style scoped>
.sc{background:#fff;border-radius:12px;padding:24px;text-align:center;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1)}
.sv{font-size:34px;font-weight:900;color:var(--primary)}
.sl{font-size:13px;color:var(--text-muted);margin-top:4px;font-weight:500}
.nav-cards{display:flex;flex-direction:column;gap:10px}
.nc{display:flex;align-items:center;gap:14px;background:#fff;border-radius:12px;padding:18px 20px;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1);transition:all 0.2s ease}
.nc:hover{transform:translateY(-2px)}
.nc-icon{width:48px;height:48px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;border:2px solid black}
.nc-info{flex:1}.nc-title{font-size:15px;font-weight:700;color:var(--text)}.nc-desc{font-size:13px;color:var(--text-muted);margin-top:2px}
.nc-arrow{font-size:18px;color:var(--text-muted)}
</style>
