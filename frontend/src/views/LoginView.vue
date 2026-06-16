<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-inner">
          <div class="brand-icon">CH</div>
          <h2>CampusHub</h2>
          <p class="brand-desc">校园互助平台</p>
          <div class="brand-quote">
            <p>"在校园里找到最合适的帮手"</p>
            <p>"让你的技能帮助更多同学"</p>
          </div>
        </div>
      </div>
      <!-- 右侧表单区 -->
      <div class="login-form">
        <div class="form-inner">
          <h1>欢迎回来</h1>
          <p class="form-sub">登录 CampusHub 校园互助平台</p>
          <form @submit.prevent="login">
            <div class="field">
              <label>用户名</label>
              <input v-model="username" placeholder="请输入用户名" />
            </div>
            <div class="field">
              <label>密码</label>
              <input v-model="password" type="password" placeholder="请输入密码" />
            </div>
            <p v-if="err" class="form-error">{{ err }}</p>
            <button type="submit" class="btn-submit" :disabled="ing">
              {{ ing ? '登录中...' : '登 录' }}
            </button>
          </form>
          <p class="form-footer">
            还没有账号？<a href="/register">立即注册</a>
          </p>
          <p class="form-help">无法登录？联系 <a href="mailto:admin@campushub.cn">admin@campushub.cn</a></p>
        </div>
      </div>
    </div>
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
  if (!username.value.trim() || !password.value) { err.value = '请输入用户名和密码'; ing.value = false; return }
  try { await auth.login(username.value, password.value); r.push('/') }
  catch (e) { err.value = e.message || '登录失败' }
  finally { ing.value = false }
}
</script>

<style scoped>
.login-page{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:24px;background:var(--bg)}
.login-container{display:flex;width:100%;max-width:820px;min-height:440px;border:3px solid black;border-radius:16px;overflow:hidden;box-shadow:6px 6px 0 rgba(0,0,0,1)}

/* 左侧品牌 */
.login-brand{flex:0 0 320px;background:var(--primary);display:flex;align-items:center;justify-content:center;padding:40px 32px}
.brand-inner{text-align:center;color:#fff}
.brand-icon{width:72px;height:72px;border-radius:18px;background:#fff;color:var(--primary);font-size:30px;font-weight:900;display:flex;align-items:center;justify-content:center;margin:0 auto 20px;border:3px solid rgba(255,255,255,.3)}
.brand-inner h2{font-size:26px;font-weight:900;margin-bottom:4px;letter-spacing:1px}
.brand-desc{font-size:14px;opacity:.8;margin-bottom:32px}
.brand-quote p{font-size:15px;opacity:.8;margin-bottom:8px;font-style:italic}

/* 右侧表单 */
.login-form{flex:1;background:var(--card);display:flex;align-items:center;justify-content:center;padding:48px 40px}
.form-inner{width:100%;max-width:380px}
h1{font-size:24px;font-weight:900;margin-bottom:4px}
.form-sub{font-size:14px;color:var(--text-secondary);margin-bottom:20px}

.field{margin-bottom:14px}
.field label{display:block;font-size:13px;font-weight:700;color:var(--text);margin-bottom:5px}
.field input{width:100%;padding:10px 14px;font-size:14px;font-family:inherit;border:2px solid black;border-radius:8px;background:var(--surface);color:var(--text)}
.field input:focus{outline:none;border-color:var(--primary);box-shadow:0 0 0 3px var(--primary-light)}
.field input::placeholder{color:var(--text-muted)}

.form-error{margin-bottom:12px}
.btn-submit{width:100%;padding:14px;border-radius:10px;font-size:16px;font-weight:700;margin-top:8px;background:var(--text);color:var(--bg);border:2px solid black;box-shadow:4px 4px 0 rgba(0,0,0,1);cursor:pointer;transition:all .15s ease;font-family:inherit}
.btn-submit:hover{background:var(--primary);color:#fff;transform:translate(1px,1px);box-shadow:2px 2px 0 rgba(0,0,0,1)}
.btn-submit:disabled{opacity:.6;cursor:not-allowed;transform:none;box-shadow:4px 4px 0 rgba(0,0,0,1)}
.form-footer{margin-top:20px;text-align:center;font-size:14px;color:var(--text-secondary)}
.form-footer a{color:var(--primary);font-weight:700}
.form-help{margin-top:8px;text-align:center;font-size:13px;color:var(--text-muted)}
.form-help a{color:var(--text-secondary)}

@media (max-width: 768px){
  .login-container{flex-direction:column;max-width:440px}
  .login-brand{flex:auto;padding:32px 24px}
  .brand-quote{display:none}
  .login-form{padding:32px 24px}
}
</style>
