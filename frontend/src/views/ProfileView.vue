<template>
  <div class="page">
    <div class="ph">
      <button type="button" class="pav" :class="{hasAvatar:auth.user?.avatarUrl}" @click="openAvatar(auth.user?.avatarUrl)">
        <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" alt="我的头像" />
        <span v-else>{{ userInitial(auth.user) }}</span>
      </button>
      <div class="pn">{{ auth.user?.username }}</div>
      <div class="pi">学号 {{ auth.user?.studentId }}</div>
      <div class="pc">⭐ {{ auth.user?.creditScore }} 信用分</div>
      <div class="pc">额度分 {{ auth.user?.quotaAvailable || 0 }} 可用 · {{ auth.user?.quotaFrozen || 0 }} 冻结</div>
      <div v-if="appeal" class="appeal-status" :class="'appeal-'+appeal.status">
        <span v-if="appeal.status==='PENDING'">⏳ 申诉审核中</span>
        <span v-else-if="appeal.status==='APPROVED'">✅ 申诉已通过，信用分已恢复</span>
        <span v-else-if="appeal.status==='REJECTED'">❌ 申诉已被驳回</span>
      </div>
      <button v-if="auth.user?.creditScore < 60 && (!appeal || appeal.status==='REJECTED')" class="btn-appeal" @click="submitAppeal">信用分低于60，申请恢复</button>
      <button v-if="checkedInToday" type="button" class="btn-checkin btn-checked-in" disabled>✅ 今日已签到</button>
      <button v-else type="button" class="btn-checkin" @click="checkIn">每日签到 +3 额度分</button>
    </div>
    <div class="grid-2 mb-md">
      <div class="sc"><div class="sv">{{ auth.user?.creditScore }}</div><div class="sl">信用分</div></div>
      <div class="sc"><div class="sv">{{ auth.user?.quotaAvailable || 0 }}</div><div class="sl">可用额度分</div></div>
      <div class="sc"><div class="sv">{{ auth.user?.quotaFrozen || 0 }}</div><div class="sl">冻结额度分</div></div>
      <div class="sc"><div class="sv gender-value">{{ profileGender }}</div><div class="sl">性别</div></div>
      <div class="sc"><div class="sv">{{ st }}</div><div class="sl">账户状态</div></div>
    </div>
    <div class="card rules-card">
      <h3 class="mb-sm">信用分与额度分规则</h3>
      <div class="rules-grid">
        <section class="rule-section">
          <h4>信用分</h4>
          <ul>
            <li>初始 100 分，范围 0-100 分。</li>
            <li>低于 60 分不能接单，可提交申诉，申诉通过恢复至 60 分。</li>
            <li>接单后 10 分钟内取消不扣分，超过 10 分钟取消扣 10 分。</li>
            <li>信用分低于 70 分接单时冻结 10 额度分保证金。</li>
          </ul>
        </section>
        <section class="rule-section">
          <h4>额度分获取</h4>
          <ul>
            <li>参考比例：1 元现金约等于 10 额度分，仅用于估算报酬。</li>
            <li>每日签到 +3，实名认证通过 +20。</li>
            <li>首次填写手机号 +10，首次填写邮箱 +10。</li>
            <li>收获 4-5 星好评 +5，每日首次完成订单 +10。</li>
          </ul>
        </section>
        <section class="rule-section">
          <h4>额度分使用</h4>
          <ul>
            <li>发布额度分需求时冻结对应报酬，订单完成后支付给接单者。</li>
            <li>发布者取消或删除需求会退回冻结报酬；接单者未完成前取消接单时，报酬继续冻结并等待重新接单。</li>
            <li>逾期取消会扣除 5 额度分；低信用保证金场景优先扣保证金。</li>
            <li>额度分不支持充值、提现、转账，管理员可按审核结果人工调整。</li>
          </ul>
        </section>
      </div>
    </div>
    <div class="card"><h3 class="mb-sm">✏️ 编辑资料</h3>
      <div class="rr"><div class="form-group"><label>用户名</label><input v-model="e.username" :placeholder="auth.user?.username" /></div><div class="form-group"><label>手机</label><input v-model="e.phone" :placeholder="auth.user?.phone||'未填写'" /></div></div>
      <div class="form-group"><label>邮箱</label><input v-model="e.email" :placeholder="auth.user?.email||'未填写'" /></div>
      <div class="form-group">
        <label>头像</label>
        <div class="upload-row">
          <label class="btn-outline btn-sm file-btn">
            {{ avatarUploading ? '上传中...' : '上传头像' }}
            <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" :disabled="avatarUploading" @change="pickAvatar" />
          </label>
          <button v-if="auth.user?.avatarUrl" type="button" class="btn-outline btn-sm" @click="openAvatar(auth.user.avatarUrl)">查看头像</button>
          <button v-if="auth.user?.avatarUrl" type="button" class="btn-outline btn-sm" :disabled="avatarUploading" @click="clearAvatar">移除头像</button>
        </div>
        <p v-if="avatarErr" class="form-error">{{ avatarErr }}</p>
      </div>
      <button class="btn-primary btn-sm" @click="update">保存修改</button>
    </div>
    <div class="card"><h3 class="mb-sm">🪪 实名认证</h3>
      <div v-if="cert" class="cert-state">
        状态：<span :style="{color:cert.status==='APPROVED'?'var(--success)':cert.status==='REJECTED'?'var(--danger)':'var(--warning)'}" class="text-bold">{{ cert.status }}</span>
        <p v-if="cert.status==='REJECTED' && cert.rejectReason" class="text-sm text-muted">原因：{{ cert.rejectReason }}</p>
      </div>
      <template v-if="canSubmitCert">
        <div class="form-group"><label>类型</label><select v-model="cf.certType"><option value="STUDENT_ID">学生证</option><option value="STUDENT_CARD">校园卡</option></select></div>
        <div class="form-group"><label>学号</label><input :value="auth.user?.studentId" readonly /></div>
        <div class="form-group">
          <label>性别</label>
          <select v-model="cf.gender">
            <option value="MALE">男</option>
            <option value="FEMALE">女</option>
            <option value="UNDISCLOSED">不愿透露</option>
          </select>
          <p class="text-xs text-muted mt-xs">实名认证通过后性别不再允许修改。</p>
        </div>
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
      <template v-else>
        <p class="text-sm text-muted">性别：{{ genderText(auth.user) }}</p>
        <button v-if="cert?.cardImageUrl" type="button" class="cert-preview compact-preview" @click="certImageViewer=cert.cardImageUrl">
          <img :src="cert.cardImageUrl" alt="认证图片" />
        </button>
      </template>
    </div>
    <div class="card">
      <div class="flex-between mb-sm">
        <h3>黑名单</h3>
        <span class="text-sm text-muted">{{ blockedList.length }} 人</span>
      </div>
      <div v-if="blockedList.length===0" class="text-sm text-muted">暂无拉黑用户</div>
      <div v-for="item in blockedList" :key="item.id" class="blocked-row">
        <div class="blocked-main">
          <div class="blocked-name">{{ item.targetUser?.username || ('用户 #' + item.id) }}</div>
          <div class="blocked-desc">已拉黑，ta 不能继续给你发送私信</div>
          <div v-if="item.relatedDemandTitle" class="blocked-demand text-xs text-muted">关联需求：{{ item.relatedDemandTitle }}</div>
        </div>
        <button class="btn-outline btn-sm" @click="unblockUser(item)">解除拉黑</button>
      </div>
    </div>
    <div class="card" v-if="creditList.length"><h3 class="mb-sm">馃搵 淇＄敤璁板綍</h3>
      <div v-for="r in creditList" :key="r.id" class="cr">
        <div class="flex-between">
          <span class="text-sm">{{ r.reason }}</span>
          <span class="text-bold" :style="{color:amountColor(r.changeAmount)}">{{ r.changeAmount>0?'+'+r.changeAmount:r.changeAmount }}</span>
        </div>
        <div class="text-xs text-muted">{{ r.createdAt?.substring(0,16)?.replace('T',' ') }}</div>
      </div>
    </div>
    <div class="card" v-if="quotaList.length"><h3 class="mb-sm">额度分流水</h3>
      <div v-for="r in quotaList" :key="r.id" class="cr">
        <div class="flex-between">
          <span class="text-sm">{{ r.reason }}</span>
          <span class="text-bold" :style="{color:amountColor(r.changeAmount)}">{{ r.changeAmount>0?'+'+r.changeAmount:r.changeAmount }}</span>
        </div>
        <div class="text-xs text-muted">可用 {{ r.availableAfter }} · 冻结 {{ r.frozenAfter }} · {{ r.createdAt?.substring(0,16)?.replace('T',' ') }}</div>
      </div>
    </div>
    <button class="btn-block lb" @click="logout">退出登录</button>
    <div v-if="certImageViewer" class="image-viewer" @click.self="certImageViewer=''">
      <button type="button" class="viewer-close" @click="certImageViewer=''">关闭</button>
      <img :src="certImageViewer" alt="认证图片" />
    </div>
    <div v-if="avatarViewer" class="image-viewer" @click.self="avatarViewer=''">
      <button type="button" class="viewer-close" @click="avatarViewer=''">关闭</button>
      <img :src="avatarViewer" alt="头像" />
    </div>
  </div>
</template>
<script setup>
import { ref,reactive,computed,onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userAPI } from '@/api/users'
import { certAPI } from '@/api/certifications'
<<<<<<< HEAD
import { uploadImage } from '@/api/upload'
=======
import { chatAPI } from '@/api/chat'
>>>>>>> 205c5fc24173a26442707e40b3861e7758d89f98
import { normalizeEmail, normalizePhone, normalizeUsername } from '@/utils/validators'
import { errorMessage } from '@/utils/errors'
import { genderText, userInitial } from '@/utils/userDisplay'
import { useModal } from '@/composables/useModal'
const modal = useModal()
const ro=useRouter();const auth=useAuthStore()
<<<<<<< HEAD
const e=reactive({username:'',phone:'',email:''});const cert=ref(null);const creditList=ref([]);const quotaList=ref([]);const appeal=ref(null)
const cf=reactive({certType:'STUDENT_ID',studentId:'',gender:'UNDISCLOSED',cardImageUrl:''})
=======
const e=reactive({username:'',phone:'',email:''});const cert=ref(null);const creditList=ref([]);const quotaList=ref([]);const appeal=ref(null);const blockedList=ref([])
const cf=reactive({certType:'STUDENT_ID',studentId:'',cardImageUrl:''})
>>>>>>> 205c5fc24173a26442707e40b3861e7758d89f98
const certErr=ref('')
const certSubmitting=ref(false)
const certImageViewer=ref('')
const avatarErr=ref('')
const avatarUploading=ref(false)
const avatarViewer=ref('')
const st=computed(()=>({ACTIVE:'正常',BANNED:'已封禁',PENDING_CERT:'待认证'}[auth.user?.status]||auth.user?.status))
const canSubmitCert=computed(()=>!cert.value||cert.value.status==='REJECTED')
const checkedInToday=computed(()=>{const today=new Date().toISOString().substring(0,10);return quotaList.value.some(r=>r.type==='DAILY_CHECK_IN'&&r.createdAt?.substring(0,10)===today)})
const profileGender=computed(()=>genderText(auth.user))
async function update(){
  const d={}
  try{
    if(e.username)d.username=normalizeUsername(e.username)
    if(e.phone)d.phone=normalizePhone(e.phone)
    if(e.email)d.email=normalizeEmail(e.email)
    await userAPI.update(d)
    await auth.fetchUser()
    await loadQuota()
    modal.alert('保存成功')
  }catch(e){modal.alert('操作失败', e.message)}
}
async function submitCert(){
  certErr.value=''
  certSubmitting.value=true
  try{
    if(!cf.cardImageUrl){certErr.value='请先上传认证图片';return}
    if(!cf.gender){certErr.value='请选择性别';return}
    await certAPI.submit({...cf,studentId:auth.user?.studentId||''})
    clearCertImage()
    await loadCert()
  }catch(e){modal.alert('提交失败', e.message)}finally{certSubmitting.value=false}
}
async function loadCert(){try{const r=await certAPI.mine();cert.value=r.data;cf.studentId=auth.user?.studentId||'';cf.gender=r.data?.gender||'UNDISCLOSED'}catch{cert.value=null;cf.studentId=auth.user?.studentId||'';cf.gender='UNDISCLOSED'}}
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
async function pickAvatar(event){
  const file=event.target.files?.[0]
  event.target.value=''
  if(!file)return
  avatarErr.value=''
  if(!file.type.startsWith('image/')){avatarErr.value='请选择图片文件';return}
  if(file.size>3*1024*1024){avatarErr.value='头像需小于3MB';return}
  avatarUploading.value=true
  try{
    const res=await uploadImage(file)
    await userAPI.update({avatarUrl:res.data.url})
    await auth.fetchUser()
    await modal.alert('头像已更新')
  }catch(e){avatarErr.value=errorMessage(e,'头像上传失败')}finally{avatarUploading.value=false}
}
async function clearAvatar(){
  avatarErr.value=''
  avatarUploading.value=true
  try{
    await userAPI.update({avatarUrl:''})
    await auth.fetchUser()
  }catch(e){avatarErr.value=errorMessage(e,'移除头像失败')}finally{avatarUploading.value=false}
}
function openAvatar(src){if(src)avatarViewer.value=src}
function logout(){auth.logout();ro.push('/login')}
async function checkIn(){try{const r=await userAPI.checkIn();await auth.fetchUser();await loadQuota();const amount=r.data?.changeAmount||3;modal.alert('签到成功', `已获得 ${amount} 额度分`)}catch(e){modal.alert('签到失败', e.message)}}
async function submitAppeal(){const reason=await modal.prompt('信用分申诉','请填写申诉理由（必填）','请说明情况，管理员将根据理由审核');if(!reason||!reason.trim()){await modal.alert('理由不能为空','请填写申诉理由');return}try{await userAPI.creditAppeal(reason.trim());modal.alert('已提交','申诉已提交，请等待管理员审核。');await auth.fetchUser();loadAppeal()}catch(e){modal.alert('提交失败', e.message)}}
async function loadCredit(){try{const r=await userAPI.creditRecords();creditList.value=r.data||[]}catch{creditList.value=[]}}
async function loadQuota(){try{const r=await userAPI.quotaRecords();quotaList.value=r.data||[]}catch{quotaList.value=[]}}
async function loadAppeal(){try{const r=await userAPI.myAppeal();appeal.value=r.data}catch{appeal.value=null}}
async function loadBlockedList(){try{const r=await chatAPI.blockedList();blockedList.value=r.data||[]}catch{blockedList.value=[]}}
async function unblockUser(item){
  if(!item?.id)return
  try{
    await chatAPI.unblock(item.id)
    await loadBlockedList()
    modal.alert('操作成功','已将该用户移出黑名单')
  }catch(e){modal.alert('操作失败', e.message)}
}
function amountColor(amount){if(amount>0)return 'var(--success)';if(amount<0)return 'var(--danger)';return 'var(--text-muted)'}
onMounted(async()=>{await auth.fetchUser();await loadCert();loadCredit();loadQuota();loadAppeal();loadBlockedList()})
</script>
<style scoped>
.page{max-width:680px}
.ph{background:var(--primary);border-radius:12px;padding:28px;color:#fff;text-align:center;margin-bottom:18px;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1)}
.pav{width:72px;height:72px;border-radius:50%;background:rgba(255,255,255,.25);display:flex;align-items:center;justify-content:center;font-size:30px;font-weight:900;margin:0 auto 12px;border:3px solid rgba(255,255,255,.5);padding:0;color:#fff;overflow:hidden}
.pav.hasAvatar{background:#fff;cursor:pointer}
.pav img{width:100%;height:100%;object-fit:cover}
.pn{font-size:22px;font-weight:900}.pi{font-size:13px;opacity:.75;margin-top:4px}
.pc{display:inline-flex;gap:6px;padding:6px 16px;background:rgba(255,255,255,.2);border-radius:10px;margin-top:10px;font-size:14px;font-weight:700}
.btn-appeal{display:block;margin:10px auto 0;padding:8px 18px;border-radius:10px;background:rgba(255,255,255,.3);color:#fff;font-size:13px;font-weight:700;border:2px solid rgba(255,255,255,.5)}
.btn-appeal:hover{background:rgba(255,255,255,.45)}
.btn-checkin{display:block;margin:10px auto 0;padding:8px 18px;border-radius:10px;background:#fff;color:var(--primary);font-size:13px;font-weight:700;border:2px solid black;box-shadow:4px 4px 0px rgba(0,0,0,1)}
.btn-checkin:hover{transform:translate(1px,1px);box-shadow:none}
.btn-checked-in{background:rgba(255,255,255,.35);color:#fff;cursor:default;box-shadow:none;border:2px solid rgba(255,255,255,.3)}
.btn-checked-in:hover{transform:none;box-shadow:none}
.appeal-status{margin-top:8px;padding:6px 14px;border-radius:10px;font-size:12px;font-weight:700;text-align:center}
.appeal-PENDING{background:rgba(251,191,36,.2);color:#fbbf24}
.appeal-APPROVED{background:rgba(16,185,129,.2);color:#34d399}
.appeal-REJECTED{background:rgba(239,68,68,.2);color:#f87171}
.sc{background:#fff;border-radius:12px;padding:16px;text-align:center;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1)}
.sv{font-size:22px;font-weight:900;color:var(--primary)}.sl{font-size:12px;color:var(--text-muted);margin-top:2px;font-weight:500}
.gender-value{font-size:20px}
.rr{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.rules-card{margin-bottom:18px}
.rules-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}
.rule-section{min-width:0}
.rule-section h4{font-size:14px;font-weight:900;color:var(--text);margin:0 0 8px}
.rule-section ul{margin:0;padding-left:18px;color:var(--text-muted);font-size:13px;line-height:1.75}
.rule-section li{margin:0 0 4px}
.cert-state{margin-bottom:12px}
.blocked-row{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:12px 0;border-bottom:1px solid var(--border)}
.blocked-row:last-child{border-bottom:none;padding-bottom:0}
.blocked-main{min-width:0}
.blocked-name{font-size:15px;font-weight:800;color:var(--text)}
.blocked-desc{margin-top:4px;font-size:13px;color:var(--text-muted)}
.blocked-demand{margin-top:4px}
.upload-row{display:flex;gap:8px;align-items:center;flex-wrap:wrap}
.file-btn{position:relative;overflow:hidden}
.file-btn input{position:absolute;inset:0;opacity:0;cursor:pointer}
.cert-preview{display:block;width:180px;height:120px;margin-top:10px;padding:0;border:2px solid black;border-radius:10px;overflow:hidden;background:#fff}
.compact-preview{width:220px;height:150px}
.cert-preview img{display:block;width:100%;height:100%;object-fit:cover}
.image-viewer{position:fixed;inset:0;z-index:40;display:flex;align-items:center;justify-content:center;padding:56px 20px;background:rgba(15,23,42,.82)}
.image-viewer img{max-width:min(920px,96vw);max-height:86vh;border-radius:12px;background:#fff;border:2px solid black}
.viewer-close{position:absolute;top:18px;right:18px;background:#fff;color:var(--text)}
.lb{padding:14px;border-radius:10px;background:#fee2e2;color:var(--danger);font-weight:700;font-size:15px;border:2px solid var(--danger);box-shadow:4px 4px 0px rgba(0,0,0,1)}
.lb:hover{background:#fecaca;transform:translate(1px,1px);box-shadow:none}
.cr{padding:10px 0;border-bottom:1px solid var(--border)}
.cr:last-child{border-bottom:none}
@media(max-width:860px){
  .rules-grid{grid-template-columns:1fr}
  .blocked-row{align-items:flex-start;flex-direction:column}
  .blocked-row .btn-sm{width:100%}
}
</style>
