<template>
  <div class="page" v-if="d"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <div class="dc">
      <div class="flex-between mb-md"><h1>{{ d.title }}</h1><span class="cat" :style="{background:catBg(d.category)}">{{ catTxt(d.category) }}</span></div>
      <div class="grid-2 mb-md">
        <div class="mi"><span class="lbl">报酬</span><span class="val" style="color:var(--danger)">{{ d.rewardType==='MONEY'?'¥'+d.rewardAmount:d.rewardAmount+'积分' }}</span></div>
        <div class="mi"><span class="lbl">位置</span><span class="val">{{ d.locationTag||'不限' }}</span></div>
        <div class="mi"><span class="lbl">状态</span><span class="val"><span class="badge" :style="{background:statusBg(d.status)}">{{ statusTxt(d.status) }}</span></span></div>
        <div class="mi"><span class="lbl">时间</span><span class="val text-sm">{{ d.createdAt?.substring(0,10) }}</span></div>
      </div>
      <div class="desc">{{ d.description }}</div>
      <div class="pub"><div class="av" :style="{background:avColor}">{{ d.publisher?.username?.[0]?.toUpperCase() }}</div><div><div class="text-bold">{{ d.publisher?.username }}</div><div class="text-sm text-muted">信用 {{ d.publisher?.creditScore }}</div></div></div>
    </div>
    <div v-if="auth.isLoggedIn&&d.publisherId!==auth.user?.id" class="act-list">
      <button class="btn-outline btn-block act-btn" @click="messagePublisher">联系发布者</button>
      <button v-if="d.status==='OPEN'" class="btn-primary btn-block act-btn" @click="accept">立即接单</button>
    </div>
    <div v-if="d.publisherId===auth.user?.id&&d.status==='OPEN'" class="act-list">
      <button class="btn-del btn-block act-btn" @click="deleteDemand">删除需求</button>
    </div>
  </div>
  <div v-else class="empty">⏳ 加载中...</div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { demandAPI } from '@/api/demands'
import { orderAPI } from '@/api/orders'
import { chatAPI } from '@/api/chat'
import { errorMessage } from '@/utils/errors'
import { useModal } from '@/composables/useModal'
const r=useRoute(); const ro=useRouter(); const auth=useAuthStore(); const d=ref(null)
const modal = useModal()
const cols=['#6366f1','#8b5cf6','#ec4899','#f43f5e','#f97316']
const avColor=computed(()=>cols[(d.value?.publisher?.username?.charCodeAt(0)||0)%cols.length])
function catTxt(c){return{ERRAND:'跑腿',SKILL:'技能',STUDY:'学习',SECONDHAND:'二手'}[c]||c}
function catBg(c){return{ERRAND:'#f59e0b',SKILL:'#6366f1',STUDY:'#10b981',SECONDHAND:'#ef4444'}[c]||'#888'}
function statusTxt(s){return{OPEN:'开放中',ACCEPTED:'已接单',IN_PROGRESS:'进行中',COMPLETED:'已完成',CANCELLED:'已取消'}[s]||s}
function statusBg(s){return{OPEN:'#3b82f6',ACCEPTED:'#06b6d4',IN_PROGRESS:'#f97316',COMPLETED:'#10b981',CANCELLED:'#9ca3af'}[s]||'#888'}
async function accept(){try{await orderAPI.accept({demandId:d.value.id});ro.push('/orders')}catch(e){modal.alert('接单失败', errorMessage(e))}}
async function deleteDemand(){const ok=await modal.confirm('删除需求','确定删除该需求？删除后不可恢复。');if(!ok)return;try{await demandAPI.delete(d.value.id);ro.push('/orders')}catch(e){modal.alert('删除失败', errorMessage(e))}}
async function messagePublisher(){
  if(!d.value?.publisherId){modal.alert('发起私信失败','发布者信息缺失，暂时无法发起私信');return}
  try{
    const res=await chatAPI.create({targetUserId:d.value.publisherId,demandId:d.value.id})
    if(!res.data?.id) throw new Error('会话创建失败')
    ro.push('/chat/'+res.data.id)
  }catch(e){modal.alert('发起私信失败', errorMessage(e))}
}
onMounted(async()=>{const res=await demandAPI.detail(r.params.id);d.value=res.data})
</script>
<style scoped>
.dc{background:#fff;border-radius:16px;padding:24px;margin-bottom:14px;border:1px solid var(--border)}
h1{font-size:22px;font-weight:800;flex:1;margin-right:12px}
.cat{display:inline-flex;padding:5px 14px;border-radius:20px;font-size:12px;font-weight:700;color:#fff}
.mi{padding:12px;background:#f8fafc;border-radius:10px;display:flex;flex-direction:column;gap:4px}
.lbl{font-size:12px;color:var(--text-muted)}
.val{font-size:16px;font-weight:700}
.desc{white-space:pre-wrap;line-height:1.8;color:var(--text-secondary);padding:16px;background:#f8fafc;border-radius:12px;margin:16px 0}
.pub{display:flex;align-items:center;gap:12px;padding:16px;background:#f8fafc;border-radius:12px}
.av{width:44px;height:44px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-weight:700;color:#fff;font-size:18px}
.act-list{display:flex;flex-direction:column;gap:8px}
.act-btn{padding:14px;border-radius:12px;font-size:16px}
.btn-del{background:#dc2626;color:#fff}
.btn-del:hover{background:#b91c1c}
</style>
