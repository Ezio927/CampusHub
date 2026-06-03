<template>
  <div class="page">
    <a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">发送通知</h1>
    <div class="card">
      <div class="form-group">
        <label>通知标题</label>
        <input v-model="form.title" placeholder="请输入通知标题" />
      </div>
      <div class="form-group">
        <label>通知内容</label>
        <textarea v-model="form.content" placeholder="请输入通知内容" rows="4" class="tx"></textarea>
      </div>
      <div class="form-group">
        <label>发送范围</label>
        <div class="radio-row">
          <label class="radio-label"><input type="radio" v-model="form.targetType" value="ALL" /> 全体用户</label>
          <label class="radio-label"><input type="radio" v-model="form.targetType" value="SPECIFIC" /> 指定用户</label>
        </div>
      </div>
      <div v-if="form.targetType==='SPECIFIC'" class="form-group">
        <label>用户ID</label>
        <input v-model.number="form.targetUserId" type="number" placeholder="请输入用户ID" />
      </div>
      <p v-if="err" class="form-error">{{ err }}</p>
      <button class="btn-primary" :disabled="sending" @click="send">{{ sending?'发送中...':'发送通知' }}</button>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminAPI } from '@/api/admin'
import { useModal } from '@/composables/useModal'
const modal = useModal()
const router = useRouter()
const form = reactive({ title: '', content: '', targetType: 'ALL', targetUserId: null })
const err = ref('')
const sending = ref(false)
async function send() {
  err.value = ''
  if (!form.title.trim()) { err.value = '请输入标题'; return }
  if (!form.content.trim()) { err.value = '请输入内容'; return }
  if (form.targetType === 'SPECIFIC' && !form.targetUserId) { err.value = '请输入用户ID'; return }
  sending.value = true
  try {
    await adminAPI.sendNotification({
      title: form.title.trim(),
      content: form.content.trim(),
      targetType: form.targetType,
      targetUserId: form.targetType === 'SPECIFIC' ? form.targetUserId : null
    })
    modal.alert('发送成功', form.targetType === 'ALL' ? '已向全体用户发送通知' : '已向指定用户发送通知')
    router.push('/admin')
  } catch (e) { err.value = e.message || '发送失败' }
  finally { sending.value = false }
}
</script>
<style scoped>
.page{max-width:680px}
.tx{resize:vertical;min-height:100px}
.radio-row{display:flex;gap:20px}
.radio-label{display:inline-flex;align-items:center;gap:6px;font-size:14px;font-weight:500;cursor:pointer}
.radio-label input{width:auto}
</style>
