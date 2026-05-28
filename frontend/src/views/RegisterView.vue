<template>
  <div class="page-container">
    <h2 style="margin-bottom:24px;text-align:center">注册账号</h2>
    <form @submit.prevent="handleRegister">
      <div class="form-group"><label>用户名 *</label><input v-model="form.username" required /></div>
      <div class="form-group"><label>密码 *</label><input v-model="form.password" type="password" required /></div>
      <div class="form-group"><label>学号 *</label><input v-model="form.studentId" required /></div>
      <div class="form-group"><label>真实姓名 *</label><input v-model="form.realName" required /></div>
      <div class="form-group"><label>手机号</label><input v-model="form.phone" /></div>
      <div class="form-group"><label>邮箱</label><input v-model="form.email" /></div>
      <p v-if="error" class="error-msg">{{ error }}</p>
      <button type="submit" class="btn-primary" style="width:100%;padding:10px" :disabled="loading">{{ loading ? '注册中...' : '注册' }}</button>
    </form>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: '', password: '', studentId: '', realName: '', phone: '', email: '' })
const error = ref('')
const loading = ref(false)
async function handleRegister() {
  error.value = ''; loading.value = true
  try {
    await auth.register({ ...form })
    router.push('/login')
  } catch (e) { error.value = e.message || '注册失败' }
  finally { loading.value = false }
}
</script>
