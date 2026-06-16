<template>
  <div class="rp"><div class="rb"><div class="rc">
    <h1>创建账号</h1>
    <form @submit.prevent="reg">
      <div class="rr"><input class="inp" v-model="f.username" placeholder="用户名" required /><input class="inp" v-model="f.password" type="password" placeholder="密码" required /></div>
      <div class="rr"><input class="inp" v-model="f.studentId" placeholder="学号" required /><input class="inp" v-model="f.realName" placeholder="真实姓名" required /></div>
      <div class="rr"><NeoDropdown v-model="f.gender" :options="regGenderOptions" placeholder="性别" class="reg-drop" /><input class="inp" v-model="f.phone" placeholder="手机号（选填）" /></div>
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
import NeoDropdown from '@/components/common/NeoDropdown.vue'
import {
  normalizeEmail,
  normalizePhone,
  normalizeRealName,
  normalizeStudentId,
  normalizeUsername,
} from '@/utils/validators'
const r = useRouter()
const auth = useAuthStore()
const regGenderOptions = [{ value:'UNDISCLOSED',label:'不愿透露' },{ value:'MALE',label:'男' },{ value:'FEMALE',label:'女' }]
const f = reactive({ username:'', password:'', studentId:'', realName:'', gender:'', phone:'', email:'' })
const err = ref('')
const ing = ref(false)
async function reg() {
  err.value = ''; ing.value = true
  if (!f.gender) { err.value = '请选择性别'; ing.value = false; return }
  try {
    await auth.register({
      ...f,
      username: normalizeUsername(f.username),
      realName: normalizeRealName(f.realName),
      phone: normalizePhone(f.phone),
      email: normalizeEmail(f.email),
      studentId: normalizeStudentId(f.studentId),
    })
    r.push('/login')
  }
  catch (e) { err.value = e.message || '注册失败' }
  finally { ing.value = false }
}
</script>
<style scoped>
.rp{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;background:var(--bg)}
.rb{width:100%;max-width:440px}
.rc{background:var(--card);border-radius:12px;padding:36px 32px;border:3px solid black;box-shadow:4px 4px 0px rgba(0,0,0,1);text-align:center}
.ri{width:52px;height:52px;background:var(--primary);border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:26px;margin:0 auto 16px;border:2px solid black}
h1{font-size:22px;font-weight:900;margin-bottom:20px}
.rr{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.inp{margin-bottom:14px;padding:12px 14px;border-radius:10px;text-align:left}
.reg-drop{margin-bottom:14px}
.rbn{padding:14px;border-radius:10px;font-size:16px;font-weight:700;margin-top:8px}
.rf{margin-top:20px;text-align:center;font-size:14px;color:var(--text-secondary)}
.rf a{color:var(--primary);font-weight:700}
</style>
