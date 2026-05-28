<template>
  <div class="page-container">
    <h2 style="margin-bottom:24px;text-align:center">登录 CampusHub</h2>
    <form @submit.prevent="handleLogin">
      <div class="form-group"><label>用户名</label><input v-model="username" placeholder="请输入用户名" required /></div>
      <div class="form-group"><label>密码</label><input v-model="password" type="password" placeholder="请输入密码" required /></div>
      <p v-if="error" class="error-msg">{{ error }}</p>
      <button type="submit" class="btn-primary" style="width:100%;padding:10px" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
    </form>
    <p style="text-align:center;margin-top:16px">还没有账号？<router-link to="/register">立即注册</router-link></p>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
async function handleLogin() {
  error.value = ''; loading.value = true
  try {
    await auth.login(username.value, password.value)
    router.push('/')
  } catch (e) { error.value = e.message || '登录失败' }
  finally { loading.value = false }
}
</script>
