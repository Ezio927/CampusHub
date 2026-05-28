<template>
  <div class="login-wrap">
    <h1 class="login-title">登录 CampusHub</h1>
    <div class="login-card">
      <form @submit.prevent="handleLogin">
        <div class="form-group"><label>用户名</label><input v-model="username" placeholder="请输入用户名" required /></div>
        <div class="form-group"><label>密码</label><input v-model="password" type="password" placeholder="请输入密码" required /></div>
        <p v-if="error" class="form-error">{{ error }}</p>
        <button type="submit" class="btn-primary btn-block btn-lg" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
      </form>
    </div>
    <p class="login-footer">还没有账号？<router-link to="/register">立即注册</router-link></p>
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
<style scoped>@import './LoginView.css';</style>
