<template>
  <div class="page-container">
    <h2 style="margin-bottom:16px">发布需求</h2>
    <form @submit.prevent="publish">
      <div class="form-group"><label>标题 *</label><input v-model="form.title" required /></div>
      <div class="form-group"><label>描述 *</label><textarea v-model="form.description" rows="4" required></textarea></div>
      <div class="form-group"><label>分类 *</label><select v-model="form.category" required><option value="">请选择</option><option value="ERRAND">跑腿</option><option value="SKILL">技能</option><option value="STUDY">学习</option><option value="SECONDHAND">二手</option></select></div>
      <div class="form-group"><label>报酬金额 *</label><input v-model.number="form.rewardAmount" type="number" required /></div>
      <div class="form-group"><label>报酬类型</label><select v-model="form.rewardType"><option value="MONEY">现金</option><option value="POINT">积分</option></select></div>
      <div class="form-group"><label>位置标签</label><input v-model="form.locationTag" placeholder="如: 图书馆" /></div>
      <div class="form-group"><label><input type="checkbox" v-model="form.urgent" /> 紧急需求</label></div>
      <p v-if="error" class="error-msg">{{ error }}</p>
      <button type="submit" class="btn-primary" style="width:100%;padding:10px" :disabled="loading">发布</button>
    </form>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { demandAPI } from '@/api/demands'
const router = useRouter()
const form = reactive({ title:'', description:'', category:'', rewardAmount:0, rewardType:'MONEY', locationTag:'', urgent:false, tagIds:[], imageUrls:[] })
const error = ref(''); const loading = ref(false)
async function publish() {
  error.value = ''; loading.value = true
  try { await demandAPI.create({ ...form }); router.push('/') }
  catch (e) { error.value = e.message || '发布失败' }
  finally { loading.value = false }
}
</script>
<style scoped>@import './PublishDemandView.css';</style>
