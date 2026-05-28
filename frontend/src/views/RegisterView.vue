<template>
  <div class="rp"><div class="rb"><div class="rc">
    <div class="ri">📝</div><h1>创建账号</h1>
    <form @submit.prevent="reg">
      <div class="rr"><input class="inp" v-model="f.username" placeholder="用户名" required /><input class="inp" v-model="f.password" type="password" placeholder="密码" required /></div>
      <div class="rr"><input class="inp" v-model="f.studentId" placeholder="学号" required /><input class="inp" v-model="f.realName" placeholder="真实姓名" required /></div>
      <input class="inp" v-model="f.phone" placeholder="手机号（选填）" />
      <input class="inp" v-model="f.email" placeholder="邮箱（选填）" />
      <p v-if="err" class="form-error">{{ err }}</p>
      <button type="submit" class="btn-primary btn-block rbn" :disabled="ing">{{ ing? '注册中...' : '注 册' }}</button>
    </form>
  </div>
  <p class="rf">已有账号？<a href="/login">立即登录</a></p></div></div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const r = useRouter()
const a = useAuthStore()
const f = reactive({ username:'', password:'', studentId:'', realName:'', phone:'', email:'' })
const err = ref('')
const ing = ref(false)
async function reg() {
  err.value = ''; ing.value = true
  try { await a.register({...f}); r.push('/login') }
  catch (e) { err.value = e.message || '注册失败' }
  finally { ing.value = false }
}
</script>
<style scoped>
.rp{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;background:linear-gradient(135deg,#667eea,#764ba2)}
.rb{width:100%;max-width:440px}
.rc{background:#fff;border-radius:20px;padding:36px 32px;box-shadow:0 20px 60px rgba(0,0,0,.15);text-align:center}
.ri{width:52px;height:52px;background:linear-gradient(135deg,#667eea,#764ba2);border-radius:14px;display:flex;align-items:center;justify-content:center;font-size:26px;margin:0 auto 16px}
h1{font-size:22px;font-weight:800;margin-bottom:20px}
.rr{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.inp{margin-bottom:14px;padding:12px 14px;border-radius:10px;text-align:left}
.rbn{padding:14px;border-radius:12px;font-size:16px;font-weight:700;margin-top:8px}
.rf{margin-top:20px;text-align:center;font-size:14px;color:rgba(255,255,255,.85)}
.rf a{color:#fff;font-weight:700;text-decoration:underline}
</style>
