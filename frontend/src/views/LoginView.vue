<template>
  <div class="lp">
    <div class="lb"><div class="lc">
      <h1>欢迎回来</h1>
      <p class="ls">登录 CampusHub 校园互助平台</p>
      <form @submit.prevent="login">
        <input class="inp" v-model="username" placeholder="用户名" required />
        <input class="inp" v-model="password" type="password" placeholder="密码" required />
        <p v-if="err" class="form-error">{{ err }}</p>
        <button type="submit" class="btn-primary btn-block lbn" :disabled="ing">{{ ing? '登录中...' : '登 录' }}</button>
      </form>
    </div>
    <p class="lf">还没有账号？<a href="/register">立即注册</a></p>
    <p class="le">无法登录？联系管理员：<a href="mailto:admin@campushub.cn">admin@campushub.cn</a></p></div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const r = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const err = ref('')
const ing = ref(false)
async function login() {
  err.value = ''; ing.value = true
  try { await auth.login(username.value, password.value); r.push('/') }
  catch (e) { err.value = e.message || '登录失败' }
  finally { ing.value = false }
}
</script>
<style scoped>
.lp{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;background:var(--bg)}
.lb{width:100%;max-width:400px}
.lc{background:#fff;border-radius:12px;padding:40px 32px;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1);text-align:center}
.li{width:56px;height:56px;background:var(--primary);border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:28px;margin:0 auto 20px;border:2px solid black}
h1{font-size:24px;font-weight:900;margin-bottom:4px}
.ls{color:var(--text-secondary);font-size:14px;margin-bottom:28px;font-weight:500}
.inp{margin-bottom:14px;padding:14px 16px;border-radius:10px;font-size:15px;text-align:left}
.lbn{padding:14px;border-radius:10px;font-size:16px;font-weight:700;margin-top:8px}
.lf{margin-top:20px;text-align:center;font-size:14px;color:var(--text-secondary)}
.lf a{color:var(--primary);font-weight:700}
.le{margin-top:10px;text-align:center;font-size:13px;color:var(--text-muted)}
.le a{color:var(--text-secondary);font-weight:500}
</style>
