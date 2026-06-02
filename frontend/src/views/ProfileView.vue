<template>
  <div class="page">
    <div class="profile-head">
      <button type="button" class="profile-avatar" :class="{ hasAvatar: auth.user?.avatarUrl }" @click="openAvatar(auth.user?.avatarUrl)">
        <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" alt="我的头像" />
        <span v-else>{{ userInitial(auth.user) }}</span>
      </button>
      <div class="profile-name">{{ auth.user?.username }}</div>
      <div class="profile-sub">学号 {{ auth.user?.studentId }}</div>
      <div class="pill-row">
        <span class="pill">信用分 {{ auth.user?.creditScore }}</span>
        <span class="pill">发布评分 {{ ratingText(auth.user?.publisherRating, auth.user?.publisherRatingCount) }}</span>
        <span class="pill">接单评分 {{ ratingText(auth.user?.acceptorRating, auth.user?.acceptorRatingCount) }}</span>
      </div>
      <div class="pill-row">
        <span class="pill">额度分 {{ auth.user?.quotaAvailable || 0 }} 可用 · {{ auth.user?.quotaFrozen || 0 }} 冻结</span>
      </div>
      <div v-if="appeal" class="appeal-status" :class="'appeal-' + appeal.status">
        <span v-if="appeal.status === 'PENDING'">申诉审核中</span>
        <span v-else-if="appeal.status === 'APPROVED'">申诉已通过，信用分已恢复</span>
        <span v-else-if="appeal.status === 'REJECTED'">申诉已被驳回</span>
      </div>
      <button v-if="auth.user?.creditScore < 60 && (!appeal || appeal.status === 'REJECTED')" class="btn-light" @click="submitAppeal">
        申请恢复信用分
      </button>
      <button v-if="checkedInToday" type="button" class="btn-light is-disabled" disabled>今日已签到</button>
      <button v-else type="button" class="btn-light" @click="checkIn">每日签到 +3 额度分</button>
    </div>

    <div class="stat-grid">
      <div class="stat"><div class="stat-value">{{ auth.user?.creditScore }}</div><div class="stat-label">信用分</div></div>
      <div class="stat"><div class="stat-value">{{ auth.user?.quotaAvailable || 0 }}</div><div class="stat-label">可用额度分</div></div>
      <div class="stat"><div class="stat-value">{{ auth.user?.quotaFrozen || 0 }}</div><div class="stat-label">冻结额度分</div></div>
      <div class="stat"><div class="stat-value small">{{ ratingText(auth.user?.publisherRating, auth.user?.publisherRatingCount) }}</div><div class="stat-label">发布者评分</div></div>
      <div class="stat"><div class="stat-value small">{{ ratingText(auth.user?.acceptorRating, auth.user?.acceptorRatingCount) }}</div><div class="stat-label">接单者评分</div></div>
      <div class="stat"><div class="stat-value small">{{ profileGender }}</div><div class="stat-label">性别</div></div>
      <div class="stat"><div class="stat-value small">{{ statusText }}</div><div class="stat-label">账户状态</div></div>
    </div>

    <div class="card rules-card">
      <h3 class="mb-sm">信用分与额度分规则</h3>
      <div class="rules-grid">
        <section class="rule-section">
          <h4>信用分</h4>
          <ul>
            <li>初始 100 分，范围 0-100 分。</li>
            <li>低于 60 分不能接单，可提交申诉，申诉通过恢复至 60 分。</li>
            <li>接单 10 分钟内取消不扣分，超过 10 分钟取消扣 10 分。</li>
            <li>信用分低于 70 分接单时冻结 10 额度分保证金。</li>
          </ul>
        </section>
        <section class="rule-section">
          <h4>额度分获取</h4>
          <ul>
            <li>参考比例：1 元现金约等于 10 额度分，仅用于估算报酬。</li>
            <li>每日签到 +3，实名认证通过 +20。</li>
            <li>首次填写手机号 +10，首次填写邮箱 +10。</li>
            <li>收到 4-5 星好评 +5，每日首次完成订单 +10。</li>
          </ul>
        </section>
        <section class="rule-section">
          <h4>额度分使用</h4>
          <ul>
            <li>发布额度分需求时冻结对应报酬，订单完成后支付给接单者。</li>
            <li>发布者取消或删除需求会退回冻结报酬。</li>
            <li>逾期取消会扣除 5 额度分，低信用保证金场景优先扣保证金。</li>
            <li>额度分不支持充值、提现、转账，管理员可按审核结果人工调整。</li>
          </ul>
        </section>
      </div>
    </div>

    <div class="card">
      <h3 class="mb-sm">编辑资料</h3>
      <div class="form-row">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="editForm.username" :placeholder="auth.user?.username" />
        </div>
        <div class="form-group">
          <label>手机</label>
          <input v-model="editForm.phone" :placeholder="auth.user?.phone || '未填写'" />
        </div>
      </div>
      <div class="form-group">
        <label>邮箱</label>
        <input v-model="editForm.email" :placeholder="auth.user?.email || '未填写'" />
      </div>
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
      <button class="btn-primary btn-sm" @click="updateProfile">保存修改</button>
    </div>

    <div class="card">
      <h3 class="mb-sm">实名认证</h3>
      <div v-if="cert" class="cert-state">
        状态：
        <span :style="{ color: cert.status === 'APPROVED' ? 'var(--success)' : cert.status === 'REJECTED' ? 'var(--danger)' : 'var(--warning)' }" class="text-bold">
          {{ cert.status }}
        </span>
        <p v-if="cert.status === 'REJECTED' && cert.rejectReason" class="text-sm text-muted">原因：{{ cert.rejectReason }}</p>
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
          <label>性别</label>
          <select v-model="certForm.gender">
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
      <template v-else>
        <p class="text-sm text-muted">性别：{{ genderText(auth.user) }}</p>
        <button v-if="cert?.cardImageUrl" type="button" class="cert-preview compact-preview" @click="certImageViewer = cert.cardImageUrl">
          <img :src="cert.cardImageUrl" alt="认证图片" />
        </button>
      </template>
    </div>

    <div class="card">
      <div class="flex-between mb-sm">
        <h3>黑名单</h3>
        <span class="text-sm text-muted">{{ blockedList.length }} 人</span>
      </div>
      <div v-if="blockedList.length === 0" class="text-sm text-muted">暂无拉黑用户</div>
      <div v-for="item in blockedList" :key="item.id" class="blocked-row">
        <div class="blocked-main">
          <div class="blocked-name">{{ item.targetUser?.username || '用户 #' + item.id }}</div>
          <div class="blocked-desc">已拉黑，对方不能继续给你发送私信。</div>
          <div v-if="item.relatedDemandTitle" class="blocked-demand text-xs text-muted">关联需求：{{ item.relatedDemandTitle }}</div>
        </div>
        <button class="btn-outline btn-sm" @click="unblockUser(item)">解除拉黑</button>
      </div>
    </div>

    <div class="card" v-if="creditList.length">
      <h3 class="mb-sm">信用分流水</h3>
      <div v-for="record in creditList" :key="record.id" class="record-row">
        <div class="flex-between">
          <span class="text-sm">{{ record.reason }}</span>
          <span class="text-bold" :style="{ color: amountColor(record.changeAmount) }">
            {{ record.changeAmount > 0 ? '+' + record.changeAmount : record.changeAmount }}
          </span>
        </div>
        <div class="text-xs text-muted">{{ formatTime(record.createdAt) }}</div>
      </div>
    </div>

    <div class="card" v-if="quotaList.length">
      <h3 class="mb-sm">额度分流水</h3>
      <div v-for="record in quotaList" :key="record.id" class="record-row">
        <div class="flex-between">
          <span class="text-sm">{{ record.reason }}</span>
          <span class="text-bold" :style="{ color: amountColor(record.changeAmount) }">
            {{ record.changeAmount > 0 ? '+' + record.changeAmount : record.changeAmount }}
          </span>
        </div>
        <div class="text-xs text-muted">可用 {{ record.availableAfter }} · 冻结 {{ record.frozenAfter }} · {{ formatTime(record.createdAt) }}</div>
      </div>
    </div>

    <button class="logout-btn" @click="logout">退出登录</button>

    <div v-if="certImageViewer" class="image-viewer" @click.self="certImageViewer = ''">
      <button type="button" class="viewer-close" @click="certImageViewer = ''">关闭</button>
      <img :src="certImageViewer" alt="认证图片" />
    </div>
    <div v-if="avatarViewer" class="image-viewer" @click.self="avatarViewer = ''">
      <button type="button" class="viewer-close" @click="avatarViewer = ''">关闭</button>
      <img :src="avatarViewer" alt="头像" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userAPI } from '@/api/users'
import { certAPI } from '@/api/certifications'
import { uploadImage } from '@/api/upload'
import { chatAPI } from '@/api/chat'
import { normalizeEmail, normalizePhone, normalizeUsername } from '@/utils/validators'
import { errorMessage } from '@/utils/errors'
import { genderText, ratingText, userInitial } from '@/utils/userDisplay'
import { useModal } from '@/composables/useModal'

const router = useRouter()
const auth = useAuthStore()
const modal = useModal()

const editForm = reactive({ username: '', phone: '', email: '' })
const certForm = reactive({ certType: 'STUDENT_ID', studentId: '', gender: 'UNDISCLOSED', cardImageUrl: '' })
const cert = ref(null)
const certErr = ref('')
const certSubmitting = ref(false)
const certImageViewer = ref('')
const avatarErr = ref('')
const avatarUploading = ref(false)
const avatarViewer = ref('')
const creditList = ref([])
const quotaList = ref([])
const appeal = ref(null)
const blockedList = ref([])

const statusText = computed(() => ({ ACTIVE: '正常', BANNED: '已封禁', PENDING_CERT: '待认证' }[auth.user?.status] || auth.user?.status))
const canSubmitCert = computed(() => !cert.value || cert.value.status === 'REJECTED')
const checkedInToday = computed(() => {
  const today = new Date().toISOString().substring(0, 10)
  return quotaList.value.some(record => record.type === 'DAILY_CHECK_IN' && record.createdAt?.substring(0, 10) === today)
})
const profileGender = computed(() => genderText(auth.user))

async function updateProfile() {
  const data = {}
  try {
    if (editForm.username) data.username = normalizeUsername(editForm.username)
    if (editForm.phone) data.phone = normalizePhone(editForm.phone)
    if (editForm.email) data.email = normalizeEmail(editForm.email)
    await userAPI.update(data)
    await auth.fetchUser()
    await loadQuota()
    modal.alert('保存成功')
  } catch (err) {
    modal.alert('操作失败', errorMessage(err))
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
    if (!certForm.gender) {
      certErr.value = '请选择性别'
      return
    }
    await certAPI.submit({ ...certForm, studentId: auth.user?.studentId || '' })
    clearCertImage()
    await loadCert()
  } catch (err) {
    modal.alert('提交失败', errorMessage(err))
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
    certErr.value = '图片需小于700KB'
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

async function pickAvatar(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  avatarErr.value = ''
  if (!file.type.startsWith('image/')) {
    avatarErr.value = '请选择图片文件'
    return
  }
  if (file.size > 3 * 1024 * 1024) {
    avatarErr.value = '头像需小于3MB'
    return
  }
  avatarUploading.value = true
  try {
    const res = await uploadImage(file)
    await userAPI.update({ avatarUrl: res.data.url })
    await auth.fetchUser()
    await modal.alert('头像已更新')
  } catch (err) {
    avatarErr.value = errorMessage(err, '头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

async function clearAvatar() {
  avatarErr.value = ''
  avatarUploading.value = true
  try {
    await userAPI.update({ avatarUrl: '' })
    await auth.fetchUser()
  } catch (err) {
    avatarErr.value = errorMessage(err, '移除头像失败')
  } finally {
    avatarUploading.value = false
  }
}

function openAvatar(src) {
  if (src) avatarViewer.value = src
}

function logout() {
  auth.logout()
  router.push('/login')
}

async function checkIn() {
  try {
    const res = await userAPI.checkIn()
    await auth.fetchUser()
    await loadQuota()
    const amount = res.data?.changeAmount || 3
    modal.alert('签到成功', `已获得 ${amount} 额度分`)
  } catch (err) {
    modal.alert('签到失败', errorMessage(err))
  }
}

async function submitAppeal() {
  const reason = await modal.prompt('信用分申诉', '请填写申诉理由', '请说明情况，管理员将根据理由审核')
  if (!reason || !reason.trim()) {
    await modal.alert('理由不能为空', '请填写申诉理由')
    return
  }
  try {
    await userAPI.creditAppeal(reason.trim())
    modal.alert('已提交', '申诉已提交，请等待管理员审核。')
    await auth.fetchUser()
    loadAppeal()
  } catch (err) {
    modal.alert('提交失败', errorMessage(err))
  }
}

async function unblockUser(item) {
  if (!item?.id) return
  try {
    await chatAPI.unblock(item.id)
    await loadBlockedList()
    modal.alert('操作成功', '已将该用户移出黑名单')
  } catch (err) {
    modal.alert('操作失败', errorMessage(err))
  }
}

async function loadCert() {
  try {
    const res = await certAPI.mine()
    cert.value = res.data
    certForm.studentId = auth.user?.studentId || ''
    certForm.gender = res.data?.gender || 'UNDISCLOSED'
  } catch {
    cert.value = null
    certForm.studentId = auth.user?.studentId || ''
    certForm.gender = 'UNDISCLOSED'
  }
}

async function loadCredit() {
  try {
    const res = await userAPI.creditRecords()
    creditList.value = res.data || []
  } catch {
    creditList.value = []
  }
}

async function loadQuota() {
  try {
    const res = await userAPI.quotaRecords()
    quotaList.value = res.data || []
  } catch {
    quotaList.value = []
  }
}

async function loadAppeal() {
  try {
    const res = await userAPI.myAppeal()
    appeal.value = res.data
  } catch {
    appeal.value = null
  }
}

async function loadBlockedList() {
  try {
    const res = await chatAPI.blockedList()
    blockedList.value = res.data || []
  } catch {
    blockedList.value = []
  }
}

function amountColor(amount) {
  if (amount > 0) return 'var(--success)'
  if (amount < 0) return 'var(--danger)'
  return 'var(--text-muted)'
}

function formatTime(value) {
  return value?.substring(0, 16)?.replace('T', ' ') || '-'
}

onMounted(async () => {
  await auth.fetchUser()
  await loadCert()
  loadCredit()
  loadQuota()
  loadAppeal()
  loadBlockedList()
})
</script>

<style scoped>
.page { max-width: 760px; }
.profile-head { background: var(--primary); border-radius: 12px; padding: 28px; color: #fff; text-align: center; margin-bottom: 18px; border: 3px solid black; box-shadow: 6px 6px 0 rgba(0, 0, 0, 1); }
.profile-avatar { width: 72px; height: 72px; border-radius: 50%; background: rgba(255,255,255,.25); display: flex; align-items: center; justify-content: center; font-size: 30px; font-weight: 900; margin: 0 auto 12px; border: 3px solid rgba(255,255,255,.5); padding: 0; color: #fff; overflow: hidden; }
.profile-avatar.hasAvatar { background: #fff; cursor: zoom-in; }
.profile-avatar img { width: 100%; height: 100%; object-fit: cover; }
.profile-name { font-size: 22px; font-weight: 900; }
.profile-sub { font-size: 13px; opacity: .75; margin-top: 4px; }
.pill-row { display: flex; flex-wrap: wrap; justify-content: center; gap: 8px; margin-top: 10px; }
.pill { display: inline-flex; padding: 6px 12px; background: rgba(255,255,255,.2); border-radius: 10px; font-size: 13px; font-weight: 700; }
.btn-light { display: block; margin: 10px auto 0; padding: 8px 18px; border-radius: 10px; background: #fff; color: var(--primary); font-size: 13px; font-weight: 700; border: 2px solid black; box-shadow: 4px 4px 0 rgba(0,0,0,1); }
.btn-light:hover { transform: translate(1px,1px); box-shadow: none; }
.btn-light.is-disabled { background: rgba(255,255,255,.35); color: #fff; cursor: default; box-shadow: none; border-color: rgba(255,255,255,.3); }
.appeal-status { margin-top: 8px; padding: 6px 14px; border-radius: 10px; font-size: 12px; font-weight: 700; text-align: center; }
.appeal-PENDING { background: rgba(251,191,36,.2); color: #fbbf24; }
.appeal-APPROVED { background: rgba(16,185,129,.2); color: #34d399; }
.appeal-REJECTED { background: rgba(239,68,68,.2); color: #f87171; }
.stat-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; margin-bottom: 18px; }
.stat { background: #fff; border-radius: 12px; padding: 16px; text-align: center; border: 3px solid black; box-shadow: 6px 6px 0 rgba(0,0,0,1); }
.stat-value { font-size: 22px; font-weight: 900; color: var(--primary); }
.stat-value.small { font-size: 16px; line-height: 1.35; }
.stat-label { font-size: 12px; color: var(--text-muted); margin-top: 2px; font-weight: 500; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.rules-card { margin-bottom: 18px; }
.rules-grid { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 16px; }
.rule-section { min-width: 0; }
.rule-section h4 { font-size: 14px; font-weight: 900; color: var(--text); margin: 0 0 8px; }
.rule-section ul { margin: 0; padding-left: 18px; color: var(--text-muted); font-size: 13px; line-height: 1.75; }
.rule-section li { margin: 0 0 4px; }
.upload-row { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.file-btn { position: relative; overflow: hidden; }
.file-btn input { position: absolute; inset: 0; opacity: 0; cursor: pointer; }
.cert-state { margin-bottom: 12px; }
.cert-preview { display: block; width: 180px; height: 120px; margin-top: 10px; padding: 0; border: 2px solid black; border-radius: 10px; overflow: hidden; background: #fff; }
.compact-preview { width: 220px; height: 150px; }
.cert-preview img { display: block; width: 100%; height: 100%; object-fit: cover; }
.blocked-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border); }
.blocked-row:last-child { border-bottom: none; padding-bottom: 0; }
.blocked-main { min-width: 0; }
.blocked-name { font-size: 15px; font-weight: 800; color: var(--text); }
.blocked-desc { margin-top: 4px; font-size: 13px; color: var(--text-muted); }
.blocked-demand { margin-top: 4px; }
.record-row { padding: 10px 0; border-bottom: 1px solid var(--border); }
.record-row:last-child { border-bottom: none; }
.logout-btn { width: 100%; padding: 14px; border-radius: 10px; background: #fee2e2; color: var(--danger); font-weight: 700; font-size: 15px; border: 2px solid var(--danger); box-shadow: 4px 4px 0 rgba(0,0,0,1); }
.logout-btn:hover { background: #fecaca; transform: translate(1px,1px); box-shadow: none; }
.image-viewer { position: fixed; inset: 0; z-index: 40; display: flex; align-items: center; justify-content: center; padding: 56px 20px; background: rgba(15,23,42,.82); }
.image-viewer img { max-width: min(920px,96vw); max-height: 86vh; border-radius: 12px; background: #fff; border: 2px solid black; }
.viewer-close { position: absolute; top: 18px; right: 18px; background: #fff; color: var(--text); padding: 8px 16px; border-radius: 8px; font-weight: 700; }
@media (max-width: 860px) {
  .stat-grid, .form-row, .rules-grid { grid-template-columns: 1fr; }
  .blocked-row { align-items: flex-start; flex-direction: column; }
  .blocked-row .btn-sm { width: 100%; }
}
</style>
