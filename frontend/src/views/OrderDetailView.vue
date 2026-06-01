<template>
  <div class="page" v-if="o"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <div class="dc">
      <div class="flex-between mb-md"><h1 class="text-lg text-bold">{{ o.demandTitle || '需求 #'+o.demandId }}</h1><span class="badge" :style="{background:sb(o.status)}">{{ st(o.status) }}</span></div>
      <div class="grid-2 mb-md">
        <div class="mi"><span class="lbl">发布者</span><span class="val">{{ o.publisherName || 'ID:'+o.publisherId }}</span></div>
        <div class="mi"><span class="lbl">接单者</span><span class="val">{{ o.acceptorName || 'ID:'+o.acceptorId }}</span></div>
        <div class="mi"><span class="lbl">状态</span><span class="val">{{ st(o.status) }}</span></div>
        <div class="mi"><span class="lbl">时间</span><span class="val text-sm">{{ o.createdAt?.substring(0,10) }}</span></div>
      </div>
    </div>
    <div class="acts">
      <button v-if="targetUserId" class="btn-outline btn-block" @click="messagePeer">私信对方</button>
      <button v-for="a in acts" :key="a.k" class="btn-primary btn-block" @click="doAct(a.k)">{{ a.l }}</button>
    </div>
  </div>
  <div v-else class="empty">⏳ 加载中...</div>
</template>
<script setup>
import { ref,computed,onMounted } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { orderAPI } from '@/api/orders'
import { chatAPI } from '@/api/chat'
import { errorMessage } from '@/utils/errors'
import { useModal } from '@/composables/useModal'
const r=useRoute();const ro=useRouter();const a=useAuthStore();const o=ref(null)
const modal = useModal()
function st(s){return{PENDING_ACCEPT:'待接单',ACCEPTED:'已接单',IN_PROGRESS:'进行中',PENDING_CONFIRM:'待确认',COMPLETED:'已完成',CANCELLED:'已取消'}[s]||s}
function sb(s){return{PENDING_ACCEPT:'#3b82f6',ACCEPTED:'#06b6d4',IN_PROGRESS:'#f97316',PENDING_CONFIRM:'#eab308',COMPLETED:'#10b981',CANCELLED:'#9ca3af'}[s]||'#888'}
const acts=computed(()=>{if(!o.value)return[];const uid=a.user?.id;const s=o.value.status;const as=[];if(s==='ACCEPTED'&&o.value.acceptorId===uid)as.push({k:'deliver',l:'📦 标记已送达'});if(s==='PENDING_CONFIRM'&&o.value.publisherId===uid)as.push({k:'confirm',l:'✅ 确认完成'});if(['PENDING_ACCEPT','ACCEPTED','IN_PROGRESS','PENDING_CONFIRM'].includes(s))as.push({k:'cancel',l:'❌ 取消订单'});return as})
const targetUserId=computed(()=>{if(!o.value)return null;const uid=a.user?.id;if(o.value.publisherId===uid)return o.value.acceptorId;if(o.value.acceptorId===uid)return o.value.publisherId;return null})
async function doAct(key){try{if(key==='deliver')await orderAPI.deliver(o.value.id);else if(key==='confirm'){await orderAPI.confirm(o.value.id);ro.push('/reviews/'+o.value.acceptorId)}else if(key==='cancel'){const r=await modal.prompt('取消订单','请输入取消原因（必填）','请输入取消原因');if(!r||!r.trim()){await modal.alert('原因不能为空','请填写取消原因');return}else{await orderAPI.cancel(o.value.id,{reason:r.trim()})}}ro.go(0)}catch(e){modal.alert('操作失败', errorMessage(e))}}
async function messagePeer(){
  if(!targetUserId.value){modal.alert('发起私信失败','对方用户信息缺失，暂时无法发起私信');return}
  try{
    const res=await chatAPI.create({targetUserId:targetUserId.value,demandId:o.value.demandId})
    if(!res.data?.id) throw new Error('会话创建失败')
    ro.push('/chat/'+res.data.id)
  }catch(e){modal.alert('发起私信失败', errorMessage(e))}
}
onMounted(async()=>{const res=await orderAPI.detail(r.params.id);o.value=res.data})
</script>
<style scoped>
.dc{background:#fff;border-radius:16px;padding:24px;margin-bottom:14px;border:1px solid var(--border)}
.mi{padding:12px;background:#f8fafc;border-radius:10px;display:flex;flex-direction:column;gap:4px}
.lbl{font-size:12px;color:var(--text-muted)}.val{font-size:16px;font-weight:700}
.acts{display:flex;flex-direction:column;gap:8px}
.acts button{padding:14px;border-radius:12px;font-size:15px;font-weight:700}
</style>
