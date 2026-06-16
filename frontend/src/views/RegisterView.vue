<template>
  <div class="reg-page">
    <div class="reg-container">
      <!-- 左侧品牌区 -->
      <div class="reg-brand">
        <div class="brand-inner">
          <div class="brand-icon">CH</div>
          <h2>CampusHub</h2>
          <p class="brand-desc">校园互助平台</p>
          <div class="brand-features">
            <div class="bf-item"><span class="bf-dot"></span>快速注册，即刻体验</div>
            <div class="bf-item"><span class="bf-dot"></span>发布需求，找人帮忙</div>
            <div class="bf-item"><span class="bf-dot"></span>实名认证后解锁全部功能</div>
          </div>
        </div>
      </div>
      <!-- 右侧表单区 -->
      <div class="reg-form">
        <div class="form-inner">
          <h1>创建账号</h1>
          <p class="form-sub">加入 CampusHub，开启校园互助之旅</p>
          <form @submit.prevent="reg">
            <div class="field">
              <label>用户名</label>
              <input v-model="f.username" placeholder="2-15位中英文" />
            </div>
            <div class="field">
              <label>密码</label>
              <input v-model="f.password" type="password" placeholder="至少6位" />
            </div>
            <div class="field">
              <label>确认密码</label>
              <input v-model="password2" type="password" placeholder="再次输入密码" />
            </div>
            <div class="field">
              <label>手机号 <span class="optional">选填</span></label>
              <input v-model="f.phone" placeholder="11位手机号" />
            </div>
            <div class="field">
              <label>邮箱 <span class="optional">选填</span></label>
              <input v-model="f.email" placeholder="example@nju.edu.cn" />
            </div>
            <p v-if="err" class="form-error">{{ err }}</p>
            <button type="submit" class="btn-submit" :disabled="ing">
              {{ ing ? '注册中...' : '创建账号' }}
            </button>
          </form>
          <p class="form-footer">已有账号？<a href="/login">立即登录</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { normalizeEmail, normalizePhone, normalizeUsername } from '@/utils/validators'
const r = useRouter()
const auth = useAuthStore()
const f = reactive({ username:'', password:'', phone:'', email:'' })
const password2 = ref('')
const err = ref('')
const ing = ref(false)
async function reg() {
  err.value = ''; ing.value = true
  if (f.password !== password2.value) { err.value = '两次输入的密码不一致'; ing.value = false; return }
  try {
    await auth.register({
      ...f,
      username: normalizeUsername(f.username),
      phone: normalizePhone(f.phone),
      email: normalizeEmail(f.email),
    })
    r.push('/login')
  }
  catch (e) { err.value = e.message || '注册失败' }
  finally { ing.value = false }
}
</script>

<style scoped>
.reg-page{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:24px;background:var(--bg)}
.reg-container{display:flex;width:100%;max-width:820px;min-height:480px;border:3px solid black;border-radius:16px;overflow:hidden;box-shadow:6px 6px 0 rgba(0,0,0,1)}

/* 左侧品牌 */
.reg-brand{flex:0 0 320px;background:var(--primary);display:flex;align-items:center;justify-content:center;padding:40px 32px}
.brand-inner{text-align:center;color:#fff}
.brand-icon{width:72px;height:72px;border-radius:18px;background:#fff;color:var(--primary);font-size:30px;font-weight:900;display:flex;align-items:center;justify-content:center;margin:0 auto 20px;border:3px solid rgba(255,255,255,.3)}
.brand-inner h2{font-size:26px;font-weight:900;margin-bottom:4px;letter-spacing:1px}
.brand-desc{font-size:14px;opacity:.8;margin-bottom:32px}
.brand-features{text-align:left;display:flex;flex-direction:column;gap:14px}
.bf-item{display:flex;align-items:center;gap:10px;font-size:14px;font-weight:600;opacity:.9}
.bf-dot{width:8px;height:8px;border-radius:50%;background:#fff;flex-shrink:0;box-shadow:0 0 0 3px rgba(255,255,255,.25)}

/* 右侧表单 */
.reg-form{flex:1;background:var(--card);display:flex;align-items:center;justify-content:center;padding:48px 40px}
.form-inner{width:100%;max-width:400px}
h1{font-size:24px;font-weight:900;margin-bottom:4px}
.form-sub{font-size:14px;color:var(--text-secondary);margin-bottom:20px}

.field{margin-bottom:14px}
.field label{display:block;font-size:13px;font-weight:700;color:var(--text);margin-bottom:5px}
.field label .optional{font-weight:500;color:var(--text-muted)}
.field input{width:100%;padding:10px 14px;font-size:14px;font-family:inherit;border:2px solid black;border-radius:8px;background:var(--surface);color:var(--text)}
.field input:focus{outline:none;border-color:var(--primary);box-shadow:0 0 0 3px var(--primary-light)}
.field input::placeholder{color:var(--text-muted)}

.form-error{margin-bottom:12px}
.btn-submit{width:100%;padding:14px;border-radius:10px;font-size:16px;font-weight:700;margin-top:8px;background:var(--text);color:var(--bg);border:2px solid black;box-shadow:4px 4px 0 rgba(0,0,0,1);cursor:pointer;transition:all .15s ease;font-family:inherit}
.btn-submit:hover{background:var(--primary);color:#fff;transform:translate(1px,1px);box-shadow:2px 2px 0 rgba(0,0,0,1)}
.btn-submit:disabled{opacity:.6;cursor:not-allowed;transform:none;box-shadow:4px 4px 0 rgba(0,0,0,1)}
.form-footer{margin-top:20px;text-align:center;font-size:14px;color:var(--text-secondary)}
.form-footer a{color:var(--primary);font-weight:700}

@media (max-width: 768px){
  .reg-container{flex-direction:column;max-width:440px}
  .reg-brand{flex:auto;padding:32px 24px}
  .brand-features{display:none}
  .reg-form{padding:32px 24px}
  .field-row{grid-template-columns:1fr;gap:0}
}
</style>
