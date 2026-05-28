<template>
  <div class="lp">
    <div class="lb"><div class="lc">
      <div class="li">🏫</div><h1>欢迎回来</h1>
      <p class="ls">登录 CampusHub 校园互助平台</p>
      <form @submit.prevent="login">
        <input class="inp" v-model="username" placeholder="用户名" required />
        <input class="inp" v-model="password" type="password" placeholder="密码" required />
        <p v-if="err" class="form-error">{{ err }}</p>
        <button type="submit" class="btn-primary btn-block lbn" :disabled="ing">{{ ing? '登录中...' : '登 录' }}</button>
      </form>
    </div>
    <p class="lf">还没有账号？<a href="/register">立即注册</a></p></div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const r = useRouter()
const a = useAuthStore()
const username = ref('')
const password = ref('')
const err = ref('')
const ing = ref(false)
async function login() {
  err.value = ''; ing.value = true
  try { await a.login(username.value, password.value); r.push('/') }
  catch (e) { err.value = e.message || '登录失败' }
  finally { ing.value = false }
}
</script>
<style scoped>
.lp{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;background:linear-gradient(135deg,#667eea,#764ba2)}
.lb{width:100%;max-width:400px}
.lc{background:#fff;border-radius:20px;padding:40px 32px;box-shadow:0 20px 60px rgba(0,0,0,.15);text-align:center}
.li{width:56px;height:56px;background:linear-gradient(135deg,#667eea,#764ba2);border-radius:16px;display:flex;align-items:center;justify-content:center;font-size:28px;margin:0 auto 20px}
h1{font-size:24px;font-weight:800;margin-bottom:4px}
.ls{color:var(--text-secondary);font-size:14px;margin-bottom:28px}
.inp{margin-bottom:14px;padding:14px 16px;border-radius:12px;font-size:15px;text-align:left}
.lbn{padding:14px;border-radius:12px;font-size:16px;font-weight:700;margin-top:8px}
.lf{margin-top:20px;text-align:center;font-size:14px;color:rgba(255,255,255,.85)}
.lf a{color:#fff;font-weight:700;text-decoration:underline}
</style>
