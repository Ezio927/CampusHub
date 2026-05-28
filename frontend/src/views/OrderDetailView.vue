<template>
  <div class="page" v-if="o"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <div class="dc">
      <div class="flex-between mb-md"><h1 class="text-lg text-bold">订单 #{{ o.id }}</h1><span class="badge" :style="{background:sb(o.status)}">{{ st(o.status) }}</span></div>
      <div class="grid-2 mb-md">
        <div class="mi"><span class="lbl">需求ID</span><span class="val">{{ o.demandId }}</span></div>
        <div class="mi"><span class="lbl">发布者ID</span><span class="val">{{ o.publisherId }}</span></div>
        <div class="mi"><span class="lbl">接单者ID</span><span class="val">{{ o.acceptorId }}</span></div>
        <div class="mi"><span class="lbl">时间</span><span class="val text-sm">{{ o.createdAt?.substring(0,10) }}</span></div>
      </div>
    </div>
    <div v-if="acts.length" class="acts"><button v-for="a in acts" :key="a.k" class="btn-primary btn-block" @click="doAct(a.k)">{{ a.l }}</button></div>
  </div>
  <div v-else class="empty">⏳ 加载中...</div>
</template>
<script setup>
import { ref,computed,onMounted } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { orderAPI } from '@/api/orders'
const r=useRoute();const ro=useRouter();const a=useAuthStore();const o=ref(null)
function st(s){return{PENDING_ACCEPT:'待接单',ACCEPTED:'已接单',IN_PROGRESS:'进行中',PENDING_CONFIRM:'待确认',COMPLETED:'已完成',CANCELLED:'已取消'}[s]||s}
function sb(s){return{PENDING_ACCEPT:'#3b82f6',ACCEPTED:'#06b6d4',IN_PROGRESS:'#f97316',PENDING_CONFIRM:'#eab308',COMPLETED:'#10b981',CANCELLED:'#9ca3af'}[s]||'#888'}
const acts=computed(()=>{if(!o.value)return[];const uid=a.user?.id;const s=o.value.status;const as=[];if(s==='ACCEPTED'&&o.value.acceptorId===uid)as.push({k:'deliver',l:'📦 标记已送达'});if(s==='PENDING_CONFIRM'&&o.value.publisherId===uid)as.push({k:'confirm',l:'✅ 确认完成'});if(['PENDING_ACCEPT','ACCEPTED','IN_PROGRESS','PENDING_CONFIRM'].includes(s))as.push({k:'cancel',l:'❌ 取消订单'});return as})
async function doAct(key){try{if(key==='deliver')await orderAPI.deliver(o.value.id);else if(key==='confirm'){await orderAPI.confirm(o.value.id);ro.push('/reviews/'+o.value.acceptorId)}else if(key==='cancel'){const r=prompt('取消原因');if(r)await orderAPI.cancel(o.value.id,{reason:r})}ro.go(0)}catch(e){alert(e.message)}}
onMounted(async()=>{const res=await orderAPI.detail(r.params.id);o.value=res.data})
</script>
<style scoped>
.dc{background:#fff;border-radius:16px;padding:24px;margin-bottom:14px;border:1px solid var(--border)}
.mi{padding:12px;background:#f8fafc;border-radius:10px;display:flex;flex-direction:column;gap:4px}
.lbl{font-size:12px;color:var(--text-muted)}.val{font-size:16px;font-weight:700}
.acts{display:flex;flex-direction:column;gap:8px}
.acts button{padding:14px;border-radius:12px;font-size:15px;font-weight:700}
</style>
