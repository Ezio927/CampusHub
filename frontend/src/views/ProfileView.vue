<template>
  <div class="page">
    <div class="ph">
      <div class="pav">{{ auth.user?.username?.[0]?.toUpperCase() }}</div>
      <div class="pn">{{ auth.user?.username }}</div>
      <div class="pi">学号 {{ auth.user?.studentId }}</div>
      <div class="pc">⭐ {{ auth.user?.creditScore }} 信用分</div>
    </div>
    <div class="grid-2 mb-md">
      <div class="sc"><div class="sv">{{ auth.user?.creditScore }}</div><div class="sl">信用分</div></div>
      <div class="sc"><div class="sv">{{ st }}</div><div class="sl">账户状态</div></div>
    </div>
    <div class="card"><h3 class="mb-sm">✏️ 编辑资料</h3>
      <div class="rr"><div class="form-group"><label>姓名</label><input v-model="e.realName" :placeholder="auth.user?.realName" /></div><div class="form-group"><label>手机</label><input v-model="e.phone" :placeholder="auth.user?.phone||'未填写'" /></div></div>
      <div class="form-group"><label>邮箱</label><input v-model="e.email" :placeholder="auth.user?.email||'未填写'" /></div>
      <button class="btn-primary btn-sm" @click="update">保存修改</button>
    </div>
    <div class="card"><h3 class="mb-sm">🪪 实名认证</h3>
      <template v-if="!cert">
        <div class="form-group"><label>类型</label><select v-model="cf.certType"><option value="STUDENT_ID">学生证</option><option value="STUDENT_CARD">校园卡</option></select></div>
        <div class="form-group"><label>学号</label><input v-model="cf.studentId" /></div>
        <div class="form-group"><label>图片URL</label><input v-model="cf.cardImageUrl" placeholder="证件图片链接" /></div>
        <button class="btn-primary btn-sm" @click="submitCert">提交认证</button>
      </template>
      <p v-else>状态：<span :style="{color:cert.status==='APPROVED'?'var(--success)':'var(--warning)'}" class="text-bold">{{ cert.status }}</span></p>
    </div>
    <button class="btn-block lb" @click="logout">退出登录</button>
  </div>
</template>
<script setup>
import { ref,reactive,computed,onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userAPI } from '@/api/users'
import { certAPI } from '@/api/certifications'
import { normalizeEmail, normalizePhone, normalizeRealName } from '@/utils/validators'
const ro=useRouter();const auth=useAuthStore()
const e=reactive({realName:'',phone:'',email:''});const cert=ref(null)
const cf=reactive({certType:'STUDENT_ID',studentId:'',cardImageUrl:''})
const st=computed(()=>({ACTIVE:'正常',BANNED:'已封禁',PENDING_CERT:'待认证'}[auth.user?.status]||auth.user?.status))
async function update(){
  const d={}
  try{
    if(e.realName)d.realName=normalizeRealName(e.realName)
    if(e.phone)d.phone=normalizePhone(e.phone)
    if(e.email)d.email=normalizeEmail(e.email)
    await userAPI.update(d)
    await auth.fetchUser()
    alert('保存成功')
  }catch(e){alert(e.message)}
}
async function submitCert(){try{await certAPI.submit({...cf});await loadCert()}catch(e){alert(e.message)}}
async function loadCert(){try{const r=await certAPI.mine();cert.value=r.data}catch{cert.value=null}}
function logout(){auth.logout();ro.push('/login')}
onMounted(async()=>{await auth.fetchUser();await loadCert()})
</script>
<style scoped>
.ph{background:linear-gradient(135deg,#6366f1,#8b5cf6);border-radius:18px;padding:28px;color:#fff;text-align:center;margin-bottom:18px}
.pav{width:72px;height:72px;border-radius:50%;background:rgba(255,255,255,.25);display:flex;align-items:center;justify-content:center;font-size:30px;font-weight:800;margin:0 auto 12px;border:3px solid rgba(255,255,255,.5)}
.pn{font-size:22px;font-weight:800}.pi{font-size:13px;opacity:.75;margin-top:4px}
.pc{display:inline-flex;gap:6px;padding:6px 16px;background:rgba(255,255,255,.2);border-radius:20px;margin-top:10px;font-size:14px;font-weight:600}
.sc{background:#fff;border-radius:14px;padding:16px;text-align:center;border:1px solid var(--border)}
.sv{font-size:22px;font-weight:800;color:var(--primary)}.sl{font-size:12px;color:var(--text-muted);margin-top:2px}
.rr{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.lb{padding:14px;border-radius:12px;background:#fee2e2;color:var(--danger);font-weight:700;font-size:15px;border:1px solid #fecaca}
.lb:hover{background:#fecaca}
</style>
