<template>
  <div class="page" v-if="d"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <div class="dc">
      <div class="flex-between mb-md"><h1>{{ d.title }}</h1><span class="cat" :style="{background:catBg(d.category)}">{{ catTxt(d.category) }}</span></div>
      <div class="grid-2 mb-md">
        <div class="mi"><span class="lbl">报酬</span><span class="val" style="color:var(--danger)">{{ d.rewardType==='MONEY'?'¥'+d.rewardAmount:d.rewardAmount+'额度分' }}</span></div>
        <div class="mi"><span class="lbl">位置</span><span class="val">{{ d.locationTag||'不限' }}</span></div>
        <div class="mi"><span class="lbl">状态</span><span class="val"><span class="badge" :style="{background:statusBg(d.status)}">{{ statusTxt(d.status) }}</span></span></div>
        <div class="mi"><span class="lbl">时间</span><span class="val text-sm">{{ d.createdAt?.substring(0,10) }}</span></div>
      </div>
      <div class="desc">{{ d.description }}</div>
      <div v-if="d.images?.length" class="detail-imgs">
        <img v-for="img in d.images" :key="img.id" :src="img.imageUrl" alt="需求图片" @click="imgViewer=img.imageUrl" />
      </div>
      <div v-if="imgViewer" class="image-viewer" @click.self="imgViewer=''">
        <button type="button" class="viewer-close" @click="imgViewer=''">关闭</button>
        <img :src="imgViewer" alt="查看大图" />
      </div>
      <div class="pub">
        <div class="pub-avatar-wrap">
          <button type="button" class="av" :class="{hasAvatar:d.publisher?.avatarUrl}" :style="d.publisher?.avatarUrl?{}:{background:avColor}" @click="openPublisherAvatar">
            <img v-if="d.publisher?.avatarUrl" :src="d.publisher.avatarUrl" alt="发布者头像" />
            <span v-else>{{ userInitial(d.publisher) }}</span>
          </button>
          <div class="profile-popover">
            <div class="popover-name">{{ d.publisher?.realName || d.publisher?.username || '发布者' }}</div>
            <div class="popover-row"><span>信用分</span><strong>{{ d.publisher?.creditScore ?? '-' }}</strong></div>
            <div class="popover-row"><span>性别</span><strong>{{ genderText(d.publisher) }}</strong></div>
          </div>
        </div>
        <div><div class="text-bold">{{ d.publisher?.username }}</div><div class="text-sm text-muted">信用 {{ d.publisher?.creditScore }}</div></div>
      </div>
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
import { genderText, userInitial } from '@/utils/userDisplay'
import { useModal } from '@/composables/useModal'
const r=useRoute(); const ro=useRouter(); const auth=useAuthStore(); const d=ref(null)
const modal = useModal()
const imgViewer = ref('')
const cols=['#2563EB','#7c3aed','#db2777','#dc2626','#ea580c']
const avColor=computed(()=>cols[(d.value?.publisher?.username?.charCodeAt(0)||0)%cols.length])
function catTxt(c){return{ERRAND:'跑腿代办',SKILL:'技能求助',STUDY:'学习互助',SECONDHAND:'二手交易',OTHER:'其他'}[c]||c}
function catBg(c){return{ERRAND:'#fef08a',SKILL:'#c7d2fe',STUDY:'#bbf7d0',SECONDHAND:'#fecdd3',OTHER:'#e9d5ff'}[c]||'#e5e7eb'}
function statusTxt(s){return{OPEN:'开放中',ACCEPTED:'已接单',IN_PROGRESS:'进行中',COMPLETED:'已完成',CANCELLED:'已取消'}[s]||s}
function statusBg(s){return{OPEN:'#3b82f6',ACCEPTED:'#06b6d4',IN_PROGRESS:'#f97316',COMPLETED:'#10b981',CANCELLED:'#9ca3af'}[s]||'#888'}
function openPublisherAvatar(){if(d.value?.publisher?.avatarUrl)imgViewer.value=d.value.publisher.avatarUrl}
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
.dc{background:#fff;border-radius:12px;padding:24px;margin-bottom:14px;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1);max-width:680px;margin-left:auto;margin-right:auto}
h1{font-size:22px;font-weight:900;flex:1;margin-right:12px}
.cat{display:inline-flex;padding:5px 14px;border-radius:10px;font-size:12px;font-weight:700;color:#111;border:2px solid black}
.mi{padding:12px;background:#f8fafc;border-radius:10px;display:flex;flex-direction:column;gap:4px;border:2px solid black}
.lbl{font-size:12px;color:var(--text-muted);font-weight:500}
.val{font-size:16px;font-weight:700}
.desc{white-space:pre-wrap;line-height:1.8;color:var(--text-secondary);padding:16px;background:#f8fafc;border-radius:10px;margin:16px 0;border:2px solid black}
.pub{display:flex;align-items:center;gap:12px;padding:16px;background:#f8fafc;border-radius:10px;border:2px solid black}
.pub-avatar-wrap{position:relative;flex-shrink:0}
.av{width:44px;height:44px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-weight:700;color:#fff;font-size:18px;border:2px solid black;padding:0;overflow:hidden}
.av.hasAvatar{background:#fff;cursor:zoom-in}
.av img{width:100%;height:100%;object-fit:cover}
.profile-popover{position:absolute;left:0;top:54px;z-index:20;width:220px;padding:12px;border:2px solid black;border-radius:10px;background:#fff;box-shadow:6px 6px 0 rgba(0,0,0,1);opacity:0;transform:translateY(-4px);pointer-events:none;transition:opacity .14s ease .45s,transform .14s ease .45s}
.pub-avatar-wrap:hover .profile-popover{opacity:1;transform:translateY(0);pointer-events:auto}
.popover-name{font-size:15px;font-weight:900;margin-bottom:8px;word-break:break-word}
.popover-row{display:flex;align-items:center;justify-content:space-between;gap:12px;font-size:13px;color:var(--text-muted);margin-top:5px}
.popover-row strong{color:var(--text)}
.act-list{display:flex;flex-direction:column;gap:8px;max-width:680px;margin-left:auto;margin-right:auto}
.act-btn{padding:14px;border-radius:10px;font-size:16px}
.btn-del{background:#dc2626;color:#fff;border:2px solid black;box-shadow:4px 4px 0px rgba(0,0,0,1)}
.btn-del:hover{background:#b91c1c;transform:translate(1px,1px);box-shadow:none}
.detail-imgs{display:flex;flex-wrap:wrap;gap:8px;margin-bottom:16px}
.detail-imgs img{width:100px;height:100px;border-radius:10px;object-fit:cover;cursor:pointer;border:2px solid black}
.detail-imgs img:hover{opacity:.85}
.image-viewer{position:fixed;inset:0;z-index:40;display:flex;align-items:center;justify-content:center;padding:56px 20px;background:rgba(15,23,42,.82)}
.image-viewer img{max-width:min(920px,96vw);max-height:86vh;border-radius:12px;background:#fff;border:2px solid black}
.viewer-close{position:absolute;top:18px;right:18px;background:#fff;color:var(--text);padding:8px 16px;border-radius:8px;font-weight:700}
</style>
