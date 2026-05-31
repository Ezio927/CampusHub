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
      <div v-if="cert" class="cert-state">
        状态：<span :style="{color:cert.status==='APPROVED'?'var(--success)':cert.status==='REJECTED'?'var(--danger)':'var(--warning)'}" class="text-bold">{{ cert.status }}</span>
        <p v-if="cert.rejectReason" class="text-sm text-muted">原因：{{ cert.rejectReason }}</p>
      </div>
      <template v-if="canSubmitCert">
        <div class="form-group"><label>类型</label><select v-model="cf.certType"><option value="STUDENT_ID">学生证</option><option value="STUDENT_CARD">校园卡</option></select></div>
        <div class="form-group"><label>学号</label><input :value="auth.user?.studentId" readonly /></div>
        <div class="form-group">
          <label>认证图片</label>
          <div class="upload-row">
            <label class="btn-outline btn-sm file-btn">
              选择图片
              <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" @change="pickCertImage" />
            </label>
            <button v-if="cf.cardImageUrl" type="button" class="btn-outline btn-sm" @click="clearCertImage">移除</button>
          </div>
          <button v-if="cf.cardImageUrl" type="button" class="cert-preview" @click="certImageViewer=cf.cardImageUrl">
            <img :src="cf.cardImageUrl" alt="认证图片预览" />
          </button>
        </div>
        <p v-if="certErr" class="form-error">{{ certErr }}</p>
        <button class="btn-primary btn-sm" :disabled="certSubmitting" @click="submitCert">{{ certSubmitting ? '提交中...' : '提交认证' }}</button>
      </template>
      <button v-else-if="cert?.cardImageUrl" type="button" class="cert-preview compact-preview" @click="certImageViewer=cert.cardImageUrl">
        <img :src="cert.cardImageUrl" alt="认证图片" />
      </button>
    </div>
    <button class="btn-block lb" @click="logout">退出登录</button>
    <div v-if="certImageViewer" class="image-viewer" @click.self="certImageViewer=''">
      <button type="button" class="viewer-close" @click="certImageViewer=''">关闭</button>
      <img :src="certImageViewer" alt="认证图片" />
    </div>
  </div>
</template>
<script setup>
import { ref,reactive,computed,onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userAPI } from '@/api/users'
import { certAPI } from '@/api/certifications'
import { normalizeEmail, normalizePhone, normalizeRealName } from '@/utils/validators'
import { useModal } from '@/composables/useModal'
const modal = useModal()
const ro=useRouter();const auth=useAuthStore()
const e=reactive({realName:'',phone:'',email:''});const cert=ref(null)
const cf=reactive({certType:'STUDENT_ID',studentId:'',cardImageUrl:''})
const certErr=ref('')
const certSubmitting=ref(false)
const certImageViewer=ref('')
const st=computed(()=>({ACTIVE:'正常',BANNED:'已封禁',PENDING_CERT:'待认证'}[auth.user?.status]||auth.user?.status))
const canSubmitCert=computed(()=>!cert.value||cert.value.status==='REJECTED')
async function update(){
  const d={}
  try{
    if(e.realName)d.realName=normalizeRealName(e.realName)
    if(e.phone)d.phone=normalizePhone(e.phone)
    if(e.email)d.email=normalizeEmail(e.email)
    await userAPI.update(d)
    await auth.fetchUser()
    modal.alert('保存成功')
  }catch(e){modal.alert('操作失败', e.message)}
}
async function submitCert(){
  certErr.value=''
  certSubmitting.value=true
  try{
    if(!cf.cardImageUrl){certErr.value='请先上传认证图片';return}
    await certAPI.submit({...cf,studentId:auth.user?.studentId||''})
    clearCertImage()
    await loadCert()
  }catch(e){modal.alert('提交失败', e.message)}finally{certSubmitting.value=false}
}
async function loadCert(){try{const r=await certAPI.mine();cert.value=r.data;cf.studentId=auth.user?.studentId||''}catch{cert.value=null;cf.studentId=auth.user?.studentId||''}}
function pickCertImage(event){
  const file=event.target.files?.[0]
  event.target.value=''
  if(!file)return
  if(!file.type.startsWith('image/')){certErr.value='请选择图片文件';return}
  if(file.size>700000){certErr.value='图片需小于700KB';return}
  const reader=new FileReader()
  reader.onload=()=>{cf.cardImageUrl=String(reader.result||'');certErr.value=''}
  reader.readAsDataURL(file)
}
function clearCertImage(){cf.cardImageUrl=''}
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
.cert-state{margin-bottom:12px}
.upload-row{display:flex;gap:8px;align-items:center;flex-wrap:wrap}
.file-btn{position:relative;overflow:hidden}
.file-btn input{position:absolute;inset:0;opacity:0;cursor:pointer}
.cert-preview{display:block;width:180px;height:120px;margin-top:10px;padding:0;border:1px solid var(--border);border-radius:8px;overflow:hidden;background:#fff}
.compact-preview{width:220px;height:150px}
.cert-preview img{display:block;width:100%;height:100%;object-fit:cover}
.image-viewer{position:fixed;inset:0;z-index:40;display:flex;align-items:center;justify-content:center;padding:56px 20px;background:rgba(15,23,42,.82)}
.image-viewer img{max-width:min(920px,96vw);max-height:86vh;border-radius:8px;background:#fff}
.viewer-close{position:absolute;top:18px;right:18px;background:#fff;color:var(--text)}
.lb{padding:14px;border-radius:12px;background:#fee2e2;color:var(--danger);font-weight:700;font-size:15px;border:1px solid #fecaca}
.lb:hover{background:#fecaca}
</style>
