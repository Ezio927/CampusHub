<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">认证审核</h1>
    <div v-if="list.length===0" class="empty">暂无待审核认证</div>
    <div v-for="c in list" :key="c.id" class="card cert-item">
      <div class="cert-main">
        <button v-if="c.cardImageUrl" type="button" class="cert-thumb" @click="imageViewer=c.cardImageUrl">
          <img :src="c.cardImageUrl" alt="认证图片" />
        </button>
        <div>
          <div class="text-bold">用户ID: {{ c.userId }}</div>
          <div class="text-sm text-muted">{{ c.studentId }} · {{ c.certType }} · {{ genderText(c.gender) }} · {{ c.status }}</div>
        </div>
      </div>
      <div class="flex gap-sm">
        <button class="btn-primary btn-sm" @click="review(c.id,true)">通过</button>
        <button class="btn-danger btn-sm" @click="review(c.id,false)">驳回</button>
      </div>
    </div>
    <div v-if="imageViewer" class="image-viewer" @click.self="imageViewer=''">
      <button type="button" class="viewer-close" @click="imageViewer=''">关闭</button>
      <img :src="imageViewer" alt="认证图片" />
    </div>
  </div>
</template>
<script setup>
import {ref} from 'vue';import {certAPI} from '@/api/certifications'
import { useModal } from '@/composables/useModal'
const modal = useModal()
const list=ref([])
const imageViewer=ref('')
async function load(){try{const r=await certAPI.pending();list.value=r.data||[]}catch{list.value=[]}}
async function review(id,approve){const reason=approve?null:await modal.prompt('驳回认证','请输入驳回原因','请填写驳回原因');if(reason===false)return;try{await certAPI.review(id,{approve,reason});load()}catch(e){modal.alert('操作失败',e.message)}}
function genderText(value){return{MALE:'男',FEMALE:'女',UNDISCLOSED:'不愿透露'}[value]||'未选择'}
load()
</script>
<style scoped>
.cert-item{display:flex;align-items:center;justify-content:space-between;gap:16px}
.cert-main{display:flex;align-items:center;gap:12px;min-width:0}
.cert-thumb{width:96px;height:72px;padding:0;border:2px solid black;border-radius:10px;overflow:hidden;background:var(--surface);flex-shrink:0}
.cert-thumb img{display:block;width:100%;height:100%;object-fit:cover}
.image-viewer{position:fixed;inset:0;z-index:40;display:flex;align-items:center;justify-content:center;padding:56px 20px;background:rgba(15,23,42,.82)}
.image-viewer img{max-width:min(920px,96vw);max-height:86vh;border-radius:12px;background:#fff;border:2px solid black}
.viewer-close{position:absolute;top:18px;right:18px;background:#fff;color:var(--text)}
@media (max-width: 640px){
  .cert-item{align-items:flex-start;flex-direction:column}
}
</style>
