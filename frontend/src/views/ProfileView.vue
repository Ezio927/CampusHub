<template>
  <div class="page">
    <div class="ph">
      <div class="pav">{{ auth.user?.username?.[0]?.toUpperCase() }}</div>
      <div class="pn">{{ auth.user?.username }}</div>
      <div class="pi">学号 {{ auth.user?.studentId }}</div>
      <div class="pc">信用分 {{ auth.user?.creditScore }}</div>
      <div class="pc">额度分 {{ auth.user?.quotaAvailable || 0 }} 可用 · {{ auth.user?.quotaFrozen || 0 }} 冻结</div>

      <div v-if="creditAppeal" class="appeal-status" :class="'appeal-' + creditAppeal.status">
        <span v-if="creditAppeal.status === 'PENDING'">信用分申诉审核中</span>
        <span v-else-if="creditAppeal.status === 'APPROVED'">信用分申诉已通过</span>
        <span v-else>信用分申诉已驳回</span>
      </div>
      <button
        v-if="auth.user?.creditScore < 60 && (!creditAppeal || creditAppeal.status === 'REJECTED')"
        class="btn-appeal"
        @click="submitCreditAppeal"
      >
        提交信用分申诉
      </button>

      <div v-if="banAppeal" class="appeal-status" :class="'appeal-' + banAppeal.status">
        <span v-if="banAppeal.status === 'PENDING'">封禁申诉审核中</span>
        <span v-else-if="banAppeal.status === 'APPROVED'">封禁申诉已通过</span>
        <span v-else>封禁申诉已驳回</span>
      </div>
      <button
        v-if="auth.user?.status === 'BANNED' && (!banAppeal || banAppeal.status === 'REJECTED')"
        class="btn-appeal danger-appeal"
        @click="submitBanAppeal"
      >
        提交封禁申诉
      </button>

      <button v-if="checkedInToday" type="button" class="btn-checkin btn-checked-in" disabled>今日已签到</button>
      <button v-else type="button" class="btn-checkin" @click="checkIn">每日签到 +3 额度分</button>
    </div>

    <div class="grid-2 mb-md">
      <div class="sc"><div class="sv">{{ auth.user?.creditScore }}</div><div class="sl">信用分</div></div>
      <div class="sc"><div class="sv">{{ auth.user?.quotaAvailable || 0 }}</div><div class="sl">可用额度分</div></div>
      <div class="sc"><div class="sv">{{ auth.user?.quotaFrozen || 0 }}</div><div class="sl">冻结额度分</div></div>
      <div class="sc"><div class="sv">{{ statusText }}</div><div class="sl">账户状态</div></div>
    </div>

    <div class="card rules-card">
      <h3 class="mb-sm">信用分与额度分规则</h3>
      <div class="rules-grid">
        <section class="rule-section">
          <h4>信用分</h4>
          <ul>
            <li>初始 100 分，范围 0-100 分。</li>
            <li>低于 60 分不能接单，可提交信用分申诉，审核通过后恢复。</li>
            <li>接单后超过 10 分钟再取消会扣信用分。</li>
            <li>信用分较低时接单会冻结部分额度分作为保证金。</li>
          </ul>
        </section>
        <section class="rule-section">
          <h4>额度分获取</h4>
          <ul>
            <li>每日签到可获得额度分奖励。</li>
            <li>实名认证通过后可获得额度分奖励。</li>
            <li>完善手机号、邮箱等资料可获得奖励。</li>
            <li>完成订单并获得好评可继续提升额度分。</li>
          </ul>
        </section>
        <section class="rule-section">
          <h4>额度分使用</h4>
          <ul>
            <li>发布需求时会冻结对应额度分，订单完成后再结算。</li>
            <li>取消或删除需求会按规则退回或继续冻结额度分。</li>
            <li>逾期取消、违约等情况会扣减额度分或保证金。</li>
            <li>额度分不支持提现和转账，仅用于平台内结算。</li>
          </ul>
        </section>
      </div>
    </div>

    <div class="card">
      <h3 class="mb-sm">编辑资料</h3>
      <div class="rr">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" :placeholder="auth.user?.username" />
        </div>
        <div class="form-group">
          <label>手机</label>
          <input v-model="form.phone" :placeholder="auth.user?.phone || '未填写'" />
        </div>
      </div>
      <div class="form-group">
        <label>邮箱</label>
        <input v-model="form.email" :placeholder="auth.user?.email || '未填写'" />
      </div>
      <button class="btn-primary btn-sm" @click="update">保存修改</button>
    </div>

    <div class="card">
      <div class="flex-between mb-sm">
        <h3>信用分申诉</h3>
        <span v-if="creditAppeal" class="text-sm text-muted">{{ creditAppealStatusText }}</span>
        <span v-else class="text-sm text-muted">暂无记录</span>
      </div>
      <p class="text-sm text-muted">
        当前信用分：{{ auth.user?.creditScore ?? '-' }}。仅当信用分低于 60 分时可提交申诉。
      </p>
      <p v-if="creditAppealHint" class="text-sm text-muted mt-sm">{{ creditAppealHint }}</p>
      <button
        class="btn-primary btn-sm mt-sm"
        :disabled="!canSubmitCreditAppeal"
        @click="submitCreditAppeal"
      >
        提交信用分申诉
      </button>
    </div>

    <div class="card">
      <h3 class="mb-sm">实名认证</h3>
      <div v-if="cert" class="cert-state">
        状态：
        <span :style="{ color: cert.status === 'APPROVED' ? 'var(--success)' : cert.status === 'REJECTED' ? 'var(--danger)' : 'var(--warning)' }" class="text-bold">
          {{ cert.status }}
        </span>
        <p v-if="cert.status === 'REJECTED' && cert.rejectReason" class="text-sm text-muted">驳回原因：{{ cert.rejectReason }}</p>
      </div>
      <template v-if="canSubmitCert">
        <div class="form-group">
          <label>类型</label>
          <select v-model="certForm.certType">
            <option value="STUDENT_ID">学生证</option>
            <option value="STUDENT_CARD">校园卡</option>
          </select>
        </div>
        <div class="form-group">
          <label>学号</label>
          <input :value="auth.user?.studentId" readonly />
        </div>
        <div class="form-group">
          <label>认证图片</label>
          <div class="upload-row">
            <label class="btn-outline btn-sm file-btn">
              选择图片
              <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" @change="pickCertImage" />
            </label>
            <button v-if="certForm.cardImageUrl" type="button" class="btn-outline btn-sm" @click="clearCertImage">移除</button>
          </div>
          <button v-if="certForm.cardImageUrl" type="button" class="cert-preview" @click="certImageViewer = certForm.cardImageUrl">
            <img :src="certForm.cardImageUrl" alt="认证图片预览" />
          </button>
        </div>
        <p v-if="certErr" class="form-error">{{ certErr }}</p>
        <button class="btn-primary btn-sm" :disabled="certSubmitting" @click="submitCert">
          {{ certSubmitting ? '提交中...' : '提交认证' }}
        </button>
      </template>
      <button v-else-if="cert?.cardImageUrl" type="button" class="cert-preview compact-preview" @click="certImageViewer = cert.cardImageUrl">
        <img :src="cert.cardImageUrl" alt="认证图片" />
      </button>
    </div>

    <div class="card">
      <div class="flex-between mb-sm">
        <h3>我的评价</h3>
        <button class="btn-outline btn-sm" @click="goMyReviews">查看评价</button>
      </div>
      <p class="text-sm text-muted">查看别人对你的评价，以及可申诉的评价记录。</p>
    </div>

    <div class="card">
      <div class="flex-between mb-sm">
        <h3>黑名单</h3>
        <span class="text-sm text-muted">{{ blockedList.length }} 人</span>
      </div>
      <div v-if="blockedList.length === 0" class="text-sm text-muted">暂无拉黑用户</div>
      <div v-for="item in blockedList" :key="item.id" class="blocked-row">
        <div class="blocked-main">
          <div class="blocked-name">{{ item.targetUser?.username || ('用户 #' + item.id) }}</div>
          <div class="blocked-desc">你已拉黑该用户，对方不能继续给你发送私信。</div>
          <div v-if="item.relatedDemandTitle" class="blocked-demand text-xs text-muted">关联需求：{{ item.relatedDemandTitle }}</div>
        </div>
        <button class="btn-outline btn-sm" @click="unblockUser(item)">解除拉黑</button>
      </div>
    </div>

    <div class="card">
      <div class="flex-between mb-sm">
        <h3>我的申诉</h3>
        <span class="text-sm text-muted">{{ arbitrationList.length }} 条</span>
      </div>
      <div v-if="arbitrationList.length === 0" class="text-sm text-muted">暂无订单纠纷或评价申诉记录</div>
      <div v-for="item in arbitrationList" :key="item.id" class="appeal-row">
        <div class="appeal-main">
          <div class="blocked-name">{{ item.reviewId ? '评价申诉' : '订单纠纷申诉' }} #{{ item.id }}</div>
          <div class="blocked-desc">
            订单 #{{ item.orderId }} · 状态：{{ arbitrationStatusText(item.status) }}
          </div>
          <div v-if="item.reason" class="blocked-demand text-xs text-muted">申诉理由：{{ item.reason }}</div>
          <div v-if="item.resolution" class="blocked-demand text-xs">处理结果：{{ item.resolution }}</div>
        </div>
      </div>
    </div>

    <div class="card" v-if="creditList.length">
      <h3 class="mb-sm">信用分记录</h3>
      <div v-for="item in creditList" :key="item.id" class="cr">
        <div class="flex-between">
          <span class="text-sm">{{ item.reason }}</span>
          <span class="text-bold" :style="{ color: amountColor(item.changeAmount) }">
            {{ item.changeAmount > 0 ? '+' + item.changeAmount : item.changeAmount }}
          </span>
        </div>
        <div class="text-xs text-muted">{{ item.createdAt?.substring(0,16)?.replace('T',' ') }}</div>
      </div>
    </div>

    <div class="card" v-if="quotaList.length">
      <h3 class="mb-sm">额度分记录</h3>
      <div v-for="item in quotaList" :key="item.id" class="cr">
        <div class="flex-between">
          <span class="text-sm">{{ item.reason }}</span>
          <span class="text-bold" :style="{ color: amountColor(item.changeAmount) }">
            {{ item.changeAmount > 0 ? '+' + item.changeAmount : item.changeAmount }}
          </span>
        </div>
        <div class="text-xs text-muted">
          可用 {{ item.availableAfter }} · 冻结 {{ item.frozenAfter }} · {{ item.createdAt?.substring(0,16)?.replace('T',' ') }}
        </div>
      </div>
    </div>

    <button class="btn-block lb" @click="logout">退出登录</button>

    <div v-if="certImageViewer" class="image-viewer" @click.self="certImageViewer = ''">
      <button type="button" class="viewer-close" @click="certImageViewer = ''">关闭</button>
      <img :src="certImageViewer" alt="认证图片" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userAPI } from '@/api/users'
import { certAPI } from '@/api/certifications'
import { chatAPI } from '@/api/chat'
import { arbitrationAPI } from '@/api/arbitrations'
import { normalizeEmail, normalizePhone, normalizeUsername } from '@/utils/validators'
import { useModal } from '@/composables/useModal'

const modal = useModal()
const router = useRouter()
const auth = useAuthStore()

const form = reactive({ username: '', phone: '', email: '' })
const certForm = reactive({ certType: 'STUDENT_ID', studentId: '', cardImageUrl: '' })

const cert = ref(null)
const certErr = ref('')
const certSubmitting = ref(false)
const certImageViewer = ref('')
const creditList = ref([])
const quotaList = ref([])
const creditAppeal = ref(null)
const banAppeal = ref(null)
const blockedList = ref([])
const arbitrationList = ref([])

const statusText = computed(() => ({
  ACTIVE: '正常',
  BANNED: '已封禁',
  PENDING_CERT: '待认证'
}[auth.user?.status] || auth.user?.status))

const canSubmitCert = computed(() => !cert.value || cert.value.status === 'REJECTED')
const canSubmitCreditAppeal = computed(() =>
  Number(auth.user?.creditScore ?? 0) < 60 && (!creditAppeal.value || creditAppeal.value.status === 'REJECTED')
)
const creditAppealStatusText = computed(() => ({
  PENDING: '审核中',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}[creditAppeal.value?.status] || creditAppeal.value?.status || ''))
const creditAppealHint = computed(() => {
  const score = Number(auth.user?.creditScore ?? 0)
  if (score >= 60) return '当前信用分不低于 60 分，暂时不能提交申诉。'
  if (creditAppeal.value?.status === 'PENDING') return '你已有一条待审核的信用分申诉，请等待管理员处理。'
  if (creditAppeal.value?.status === 'APPROVED') return '最近一条信用分申诉已通过，无需重复提交。'
  return ''
})
const checkedInToday = computed(() => {
  const today = new Date().toISOString().substring(0, 10)
  return quotaList.value.some(item => item.type === 'DAILY_CHECK_IN' && item.createdAt?.substring(0, 10) === today)
})

function arbitrationStatusText(status) {
  return {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已处理',
    DISMISSED: '已驳回'
  }[status] || status
}

function amountColor(amount) {
  if (amount > 0) return 'var(--success)'
  if (amount < 0) return 'var(--danger)'
  return 'var(--text-muted)'
}

async function update() {
  const payload = {}
  try {
    if (form.username) payload.username = normalizeUsername(form.username)
    if (form.phone) payload.phone = normalizePhone(form.phone)
    if (form.email) payload.email = normalizeEmail(form.email)
    await userAPI.update(payload)
    await auth.fetchUser()
    await loadQuota()
    modal.alert('保存成功')
  } catch (e) {
    modal.alert('操作失败', e.message)
  }
}

async function checkIn() {
  try {
    const r = await userAPI.checkIn()
    await auth.fetchUser()
    await loadQuota()
    const amount = r.data?.changeAmount || 3
    modal.alert('签到成功', `已获得 ${amount} 点额度分`)
  } catch (e) {
    modal.alert('签到失败', e.message)
  }
}

async function submitCreditAppeal() {
  const reason = await modal.prompt('信用分申诉', '请填写申诉理由（必填）', '请说明具体情况，管理员会据此审核。')
  if (reason === false) return
  if (!reason || !reason.trim()) {
    await modal.alert('理由不能为空', '请填写申诉理由。')
    return
  }
  try {
    await userAPI.creditAppeal(reason.trim())
    await auth.fetchUser()
    await loadCreditAppeal()
    modal.alert('提交成功', '信用分申诉已提交，请等待管理员审核。')
  } catch (e) {
    modal.alert('提交失败', e.message)
  }
}

async function submitBanAppeal() {
  const reason = await modal.prompt('封禁申诉', '请填写申诉理由（必填）', '请说明你认为被误封或希望解封的原因。')
  if (reason === false) return
  if (!reason || !reason.trim()) {
    await modal.alert('理由不能为空', '请填写申诉理由。')
    return
  }
  try {
    await userAPI.banAppeal(reason.trim())
    await loadBanAppeal()
    modal.alert('提交成功', '封禁申诉已提交，请等待管理员审核。')
  } catch (e) {
    modal.alert('提交失败', e.message)
  }
}

async function submitCert() {
  certErr.value = ''
  certSubmitting.value = true
  try {
    if (!certForm.cardImageUrl) {
      certErr.value = '请先上传认证图片'
      return
    }
    await certAPI.submit({ ...certForm, studentId: auth.user?.studentId || '' })
    clearCertImage()
    await loadCert()
  } catch (e) {
    modal.alert('提交失败', e.message)
  } finally {
    certSubmitting.value = false
  }
}

function pickCertImage(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    certErr.value = '请选择图片文件'
    return
  }
  if (file.size > 700000) {
    certErr.value = '图片需小于 700KB'
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    certForm.cardImageUrl = String(reader.result || '')
    certErr.value = ''
  }
  reader.readAsDataURL(file)
}

function clearCertImage() {
  certForm.cardImageUrl = ''
}

async function loadCert() {
  try {
    const r = await certAPI.mine()
    cert.value = r.data
    certForm.studentId = auth.user?.studentId || ''
  } catch {
    cert.value = null
    certForm.studentId = auth.user?.studentId || ''
  }
}

async function loadCredit() {
  try {
    const r = await userAPI.creditRecords()
    creditList.value = r.data || []
  } catch {
    creditList.value = []
  }
}

async function loadQuota() {
  try {
    const r = await userAPI.quotaRecords()
    quotaList.value = r.data || []
  } catch {
    quotaList.value = []
  }
}

async function loadCreditAppeal() {
  try {
    const r = await userAPI.myAppeal()
    creditAppeal.value = r.data
  } catch {
    creditAppeal.value = null
  }
}

async function loadBanAppeal() {
  try {
    const r = await userAPI.myBanAppeal()
    banAppeal.value = r.data
  } catch {
    banAppeal.value = null
  }
}

async function loadBlockedList() {
  try {
    const r = await chatAPI.blockedList()
    blockedList.value = r.data || []
  } catch {
    blockedList.value = []
  }
}

async function loadArbitrations() {
  try {
    const r = await arbitrationAPI.mine()
    arbitrationList.value = r.data || []
  } catch {
    arbitrationList.value = []
  }
}

async function unblockUser(item) {
  if (!item?.id) return
  try {
    await chatAPI.unblock(item.id)
    await loadBlockedList()
    modal.alert('操作成功', '已将该用户移出黑名单。')
  } catch (e) {
    modal.alert('操作失败', e.message)
  }
}

function logout() {
  auth.logout()
  router.push('/login')
}

function goMyReviews() {
  if (!auth.user?.id) return
  router.push('/reviews/' + auth.user.id)
}

onMounted(async () => {
  await auth.fetchUser()
  await loadCert()
  await loadCredit()
  await loadQuota()
  await loadCreditAppeal()
  await loadBanAppeal()
  await loadBlockedList()
  await loadArbitrations()
})
</script>

<style scoped>
.page{max-width:680px}
.ph{background:var(--primary);border-radius:12px;padding:28px;color:#fff;text-align:center;margin-bottom:18px;border:3px solid black;box-shadow:6px 6px 0 rgba(0,0,0,1)}
.pav{width:72px;height:72px;border-radius:50%;background:rgba(255,255,255,.25);display:flex;align-items:center;justify-content:center;font-size:30px;font-weight:900;margin:0 auto 12px;border:3px solid rgba(255,255,255,.5)}
.pn{font-size:22px;font-weight:900}
.pi{font-size:13px;opacity:.75;margin-top:4px}
.pc{display:inline-flex;gap:6px;padding:6px 16px;background:rgba(255,255,255,.2);border-radius:10px;margin-top:10px;font-size:14px;font-weight:700}
.btn-appeal{display:block;margin:10px auto 0;padding:8px 18px;border-radius:10px;background:rgba(255,255,255,.3);color:#fff;font-size:13px;font-weight:700;border:2px solid rgba(255,255,255,.5)}
.btn-appeal:hover{background:rgba(255,255,255,.45)}
.danger-appeal{background:rgba(239,68,68,.22);border-color:rgba(255,255,255,.55)}
.btn-checkin{display:block;margin:10px auto 0;padding:8px 18px;border-radius:10px;background:#fff;color:var(--primary);font-size:13px;font-weight:700;border:2px solid black;box-shadow:4px 4px 0 rgba(0,0,0,1)}
.btn-checkin:hover{transform:translate(1px,1px);box-shadow:none}
.btn-checked-in{background:rgba(255,255,255,.35);color:#fff;cursor:default;box-shadow:none;border:2px solid rgba(255,255,255,.3)}
.btn-checked-in:hover{transform:none;box-shadow:none}
.appeal-status{margin-top:8px;padding:6px 14px;border-radius:10px;font-size:12px;font-weight:700;text-align:center}
.appeal-PENDING{background:rgba(251,191,36,.2);color:#fef3c7}
.appeal-APPROVED{background:rgba(16,185,129,.2);color:#d1fae5}
.appeal-REJECTED{background:rgba(239,68,68,.2);color:#fee2e2}
.sc{background:#fff;border-radius:12px;padding:16px;text-align:center;border:3px solid black;box-shadow:6px 6px 0 rgba(0,0,0,1)}
.sv{font-size:22px;font-weight:900;color:var(--primary)}
.sl{font-size:12px;color:var(--text-muted);margin-top:2px;font-weight:500}
.rr{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.rules-card{margin-bottom:18px}
.rules-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}
.rule-section{min-width:0}
.rule-section h4{font-size:14px;font-weight:900;color:var(--text);margin:0 0 8px}
.rule-section ul{margin:0;padding-left:18px;color:var(--text-muted);font-size:13px;line-height:1.75}
.rule-section li{margin:0 0 4px}
.cert-state{margin-bottom:12px}
.blocked-row,.appeal-row{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:12px 0;border-bottom:1px solid var(--border)}
.blocked-row:last-child,.appeal-row:last-child{border-bottom:none;padding-bottom:0}
.blocked-main,.appeal-main{min-width:0}
.blocked-name{font-size:15px;font-weight:800;color:var(--text)}
.blocked-desc{margin-top:4px;font-size:13px;color:var(--text-muted)}
.blocked-demand{margin-top:4px}
.upload-row{display:flex;gap:8px;align-items:center;flex-wrap:wrap}
.file-btn{position:relative;overflow:hidden;cursor:pointer}
.file-btn input{position:absolute;inset:0;opacity:0;pointer-events:none}
.cert-preview{display:block;width:180px;height:120px;margin-top:10px;padding:0;border:2px solid black;border-radius:10px;overflow:hidden;background:#fff}
.compact-preview{width:220px;height:150px}
.cert-preview img{display:block;width:100%;height:100%;object-fit:cover}
.image-viewer{position:fixed;inset:0;z-index:40;display:flex;align-items:center;justify-content:center;padding:56px 20px;background:rgba(15,23,42,.82)}
.image-viewer img{max-width:min(920px,96vw);max-height:86vh;border-radius:12px;background:#fff;border:2px solid black}
.viewer-close{position:absolute;top:18px;right:18px;background:#fff;color:var(--text)}
.lb{padding:14px;border-radius:10px;background:#fee2e2;color:var(--danger);font-weight:700;font-size:15px;border:2px solid var(--danger);box-shadow:4px 4px 0 rgba(0,0,0,1)}
.lb:hover{background:#fecaca;transform:translate(1px,1px);box-shadow:none}
.cr{padding:10px 0;border-bottom:1px solid var(--border)}
.cr:last-child{border-bottom:none}
@media(max-width:860px){
  .rules-grid{grid-template-columns:1fr}
  .rr{grid-template-columns:1fr}
  .blocked-row{align-items:flex-start;flex-direction:column}
  .blocked-row .btn-sm{width:100%}
}
</style>
