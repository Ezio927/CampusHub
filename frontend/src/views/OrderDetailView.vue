<template>
  <div class="page" v-if="o"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <div class="dc">
      <div class="flex-between mb-md"><h1 class="text-lg text-bold">{{ o.demandTitle || '需求 #'+o.demandId }}</h1><span class="badge" :style="{background:sb(o.status)}">{{ st(o.status) }}</span></div>
      <div class="grid-2 mb-md">
        <div class="mi"><span class="lbl">发布者</span><span class="val user-val"><button type="button" class="mini-avatar" :class="{hasAvatar:o.publisherAvatarUrl}" @click="openAvatar(o.publisherAvatarUrl)"><img v-if="o.publisherAvatarUrl" :src="o.publisherAvatarUrl" alt="发布者头像" /><span v-else>{{ userInitial({username:o.publisherName}) }}</span></button>{{ o.publisherName || 'ID:'+o.publisherId }}</span></div>
        <div class="mi"><span class="lbl">接单者</span><span class="val user-val"><button type="button" class="mini-avatar" :class="{hasAvatar:o.acceptorAvatarUrl}" @click="openAvatar(o.acceptorAvatarUrl)"><img v-if="o.acceptorAvatarUrl" :src="o.acceptorAvatarUrl" alt="接单者头像" /><span v-else>{{ userInitial({username:o.acceptorName}) }}</span></button>{{ o.acceptorName || 'ID:'+o.acceptorId }}</span></div>
        <div class="mi"><span class="lbl">状态</span><span class="val">{{ st(o.status) }}</span></div>
        <div class="mi"><span class="lbl">时间</span><span class="val text-sm">{{ o.createdAt?.substring(0,10) }}</span></div>
      </div>
    </div>
    <div v-if="showReview" class="review-card">
      <h3>评价 {{ targetUserId === o.acceptorId ? '接单者' : '发布者' }}</h3>
      <div class="star-row">
        <button v-for="i in 5" :key="i" type="button" class="star-btn" :class="{on:reviewRating>=i}" @click="reviewRating=i">{{ reviewRating>=i?'★':'☆' }}</button>
      </div>
      <textarea v-model="reviewContent" class="review-input" placeholder="写下你的评价（选填）" maxlength="500" rows="2"></textarea>
      <p v-if="reviewErr" class="form-error">{{ reviewErr }}</p>
      <button class="btn-primary btn-block" :disabled="reviewSubmitting" @click="submitReviewAction">{{ reviewSubmitting?'提交中...':'提交评价' }}</button>
    </div>
    <div class="acts">
      <button v-if="targetUserId" class="btn-outline btn-block" @click="messagePeer">私信对方</button>
      <button v-for="a in acts" :key="a.k" class="btn-primary btn-block" @click="doAct(a.k)">{{ a.l }}</button>
    </div>
    <div v-if="avatarViewer" class="image-viewer" @click.self="avatarViewer=''">
      <button type="button" class="viewer-close" @click="avatarViewer=''">关闭</button>
      <img :src="avatarViewer" alt="头像" />
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
import { reviewAPI } from '@/api/reviews'
import { errorMessage } from '@/utils/errors'
import { userInitial } from '@/utils/userDisplay'
import { useModal } from '@/composables/useModal'
const r=useRoute();const ro=useRouter();const a=useAuthStore();const o=ref(null)
const modal = useModal()
const showReview=ref(false)
const reviewRating=ref(0)
const reviewContent=ref('')
const reviewErr=ref('')
const reviewSubmitting=ref(false)
const avatarViewer=ref('')
function st(s){return{PENDING_ACCEPT:'待接单',ACCEPTED:'已接单',PENDING_CONFIRM:'待确认',COMPLETED:'已完成',CANCELLED:'已取消'}[s]||s}
function sb(s){return{PENDING_ACCEPT:'#3b82f6',ACCEPTED:'#06b6d4',PENDING_CONFIRM:'#eab308',COMPLETED:'#10b981',CANCELLED:'#9ca3af'}[s]||'#888'}
const acts=computed(()=>{if(!o.value)return[];const uid=a.user?.id;const s=o.value.status;const as=[];if(s==='ACCEPTED'&&o.value.acceptorId===uid)as.push({k:'deliver',l:'📦 标记已送达'});if(s==='PENDING_CONFIRM'&&o.value.publisherId===uid)as.push({k:'confirm',l:'✅ 确认完成'});if(['PENDING_ACCEPT','ACCEPTED','PENDING_CONFIRM'].includes(s))as.push({k:'cancel',l:'❌ 取消订单'});return as})
const targetUserId=computed(()=>{if(!o.value)return null;const uid=a.user?.id;if(o.value.publisherId===uid)return o.value.acceptorId;if(o.value.acceptorId===uid)return o.value.publisherId;return null})
function openAvatar(src){if(src)avatarViewer.value=src}
async function doAct(key){try{if(key==='deliver'){await orderAPI.deliver(o.value.id);await refresh()}else if(key==='confirm'){await orderAPI.confirm(o.value.id);showReview.value=true;await refresh()}else if(key==='cancel'){const r=await modal.prompt('取消订单','请输入取消原因（必填）','请输入取消原因');if(!r||!r.trim()){await modal.alert('原因不能为空','请填写取消原因');return}else{await orderAPI.cancel(o.value.id,{reason:r.trim()})};ro.go(0)}}catch(e){modal.alert('操作失败', errorMessage(e))}}
async function submitReviewAction(){
  reviewErr.value=''
  if(reviewRating.value<1||reviewRating.value>5){reviewErr.value='请选择评分';return}
  if(reviewRating.value<=3&&(!reviewContent.value||reviewContent.value.trim().length<15)){reviewErr.value='差评需要至少15字说明';return}
  reviewSubmitting.value=true
  try{
    const revieweeId=targetUserId.value
    if(!revieweeId)throw new Error('无法确定评价对象')
    await reviewAPI.submit({orderId:o.value.id,revieweeId,rating:reviewRating.value,content:reviewContent.value.trim()||undefined,imageUrls:[]})
    ro.push('/reviews/'+revieweeId)
  }catch(e){reviewErr.value=errorMessage(e)}finally{reviewSubmitting.value=false}
}
async function refresh(){const res=await orderAPI.detail(r.params.id);o.value=res.data}
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
.dc{background:#fff;border-radius:12px;padding:24px;margin-bottom:14px;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1);max-width:680px;margin-left:auto;margin-right:auto}
.mi{padding:12px;background:#f8fafc;border-radius:10px;display:flex;flex-direction:column;gap:4px;border:2px solid black}
.lbl{font-size:12px;color:var(--text-muted);font-weight:500}.val{font-size:16px;font-weight:700}
.user-val{display:flex;align-items:center;gap:8px;min-width:0}
.mini-avatar{width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;flex-shrink:0;border:2px solid black;background:var(--primary);color:#fff;font-weight:900;padding:0;overflow:hidden}
.mini-avatar.hasAvatar{background:#fff;cursor:zoom-in}
.mini-avatar img{width:100%;height:100%;object-fit:cover}
.acts{display:flex;flex-direction:column;gap:8px;max-width:680px;margin-left:auto;margin-right:auto}
.acts button{padding:14px;border-radius:10px;font-size:15px;font-weight:700}
.review-card{background:#fff;border-radius:12px;padding:20px 24px;margin-bottom:14px;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1);max-width:680px;margin-left:auto;margin-right:auto}
.review-card h3{font-size:15px;font-weight:900;margin:0 0 12px}
.star-row{display:flex;gap:4px;margin-bottom:12px}
.star-btn{background:none;border:none;font-size:28px;cursor:pointer;color:#d1d5db;padding:0;transition:color .12s}
.star-btn.on{color:#f59e0b}
.star-btn:hover{color:#f59e0b}
.review-input{width:100%;padding:10px 12px;border:2px solid black;border-radius:10px;font-size:14px;resize:vertical;margin-bottom:10px;box-sizing:border-box}
.review-input:focus{border-color:var(--primary);outline:none}
.form-error{color:var(--danger);font-size:13px;margin-bottom:8px}
.image-viewer{position:fixed;inset:0;z-index:40;display:flex;align-items:center;justify-content:center;padding:56px 20px;background:rgba(15,23,42,.82)}
.image-viewer img{max-width:min(920px,96vw);max-height:86vh;border-radius:12px;background:#fff;border:2px solid black}
.viewer-close{position:absolute;top:18px;right:18px;background:#fff;color:var(--text);padding:8px 16px;border-radius:8px;font-weight:700}
</style>
