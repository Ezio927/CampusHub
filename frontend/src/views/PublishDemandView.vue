<template>
  <div class="page">
    <h1 class="page-title">发布需求</h1>
    <div class="pc"><form @submit.prevent="pub">
      <div class="form-group"><label>标题 *</label><input v-model="f.title" placeholder="简单描述你要什么帮助" required /></div>
      <div class="form-group"><label>详细描述 *</label><textarea v-model="f.description" rows="4" placeholder="详细说明需求内容、要求等" required></textarea></div>
      <div class="rr"><div class="form-group"><label>分类 *</label><select v-model="f.category" required><option value="">选择分类</option><option value="ERRAND">跑腿</option><option value="SKILL">技能</option><option value="STUDY">学习</option><option value="SECONDHAND">二手</option></select></div>
      <div class="form-group"><label>报酬金额 *</label><input v-model.number="f.rewardAmount" type="number" placeholder="0" required /></div></div>
      <div class="rr"><div class="form-group"><label>报酬类型</label><select v-model="f.rewardType"><option value="MONEY">现金</option><option value="POINT">积分</option></select></div>
      <div class="form-group"><label>位置标签</label><input v-model="f.locationTag" placeholder="如：图书馆、校门口" /></div></div>
      <label class="flex gap-sm" style="cursor:pointer;margin-bottom:16px"><input type="checkbox" v-model="f.urgent" style="width:auto" /> 标记为紧急需求</label>
      <p v-if="err" class="form-error">{{ err }}</p>
      <button type="submit" class="btn-primary btn-block pbn" :disabled="ing">{{ ing?'发布中...':'发布需求' }}</button>
    </form></div>
  </div>
</template>
<script setup>
import { reactive,ref } from 'vue'
import { useRouter } from 'vue-router'
import { demandAPI } from '@/api/demands'
const ro=useRouter()
const f=reactive({title:'',description:'',category:'',rewardAmount:0,rewardType:'MONEY',locationTag:'',urgent:false,tagIds:[],imageUrls:[]})
const err=ref('');const ing=ref(false)
async function pub(){err.value='';ing.value=true;try{await demandAPI.create({...f});ro.push('/')}catch(e){err.value=e.message||'发布失败'}finally{ing.value=false}}
</script>
<style scoped>
.pc{background:#fff;border-radius:16px;padding:28px;border:1px solid var(--border)}
.rr{display:grid;grid-template-columns:1fr 1fr;gap:14px}
.pbn{padding:14px;border-radius:12px;font-size:16px;font-weight:700;margin-top:8px}
</style>
