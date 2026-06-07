<template>
  <div class="page">
    <!-- ========== 1. 账户概览 ========== -->
    <div class="ph">
      <div class="ph-top">
        <div class="pav" :class="{ 'has-avatar': auth.user?.avatarUrl }" :style="auth.user?.avatarUrl ? {} : { background: avatarColor }">
          <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" alt="头像" />
          <span v-else>{{ auth.user?.username?.[0]?.toUpperCase() }}</span>
        </div>
        <div class="ph-info">
          <div class="pn">{{ auth.user?.username }}</div>
          <div class="pi">学号 {{ auth.user?.studentId }} · {{ statusText }}</div>
        </div>
        <button v-if="checkedInToday" class="check-in-btn done" disabled>已签到</button>
        <button v-else class="check-in-btn" @click="checkIn">签到 +3</button>
      </div>

      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-val">{{ auth.user?.creditScore }}</div>
          <div class="stat-lbl">信用分</div>
        </div>
        <div class="stat-item">
          <div class="stat-val" :style="auth.user?.status === 'BANNED' ? { color: 'var(--danger)' } : {}">{{ statusText }}</div>
          <div class="stat-lbl">账户状态</div>
        </div>
      </div>
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-val">{{ auth.user?.quotaAvailable || 0 }}</div>
          <div class="stat-lbl">可用额度</div>
        </div>
        <div class="stat-item">
          <div class="stat-val">{{ auth.user?.quotaFrozen || 0 }}</div>
          <div class="stat-lbl">冻结额度</div>
        </div>
      </div>

      <div v-if="creditAppeal || banAppeal" class="appeal-row">
        <div v-if="creditAppeal" class="appeal-tag" :class="'appeal-' + creditAppeal.status">
          信用申诉 {{ {PENDING:'审核中',APPROVED:'已通过',REJECTED:'已驳回'}[creditAppeal.status] }}
        </div>
        <div v-if="banAppeal" class="appeal-tag" :class="'appeal-' + banAppeal.status">
          封禁申诉 {{ {PENDING:'审核中',APPROVED:'已通过',REJECTED:'已驳回'}[banAppeal.status] }}
        </div>
      </div>

      <div class="ph-actions">
        <button v-if="auth.user?.creditScore < 60 && (!creditAppeal || creditAppeal.status === 'REJECTED')" class="app-btn warn" @click="submitCreditAppeal">信用分申诉</button>
        <button v-if="auth.user?.status === 'BANNED' && (!banAppeal || banAppeal.status === 'REJECTED')" class="app-btn danger" @click="submitBanAppeal">封禁申诉</button>
      </div>
    </div>

    <!-- ========== 2. 积分规则（默认折叠） ========== -->
    <div class="section-card">
      <div class="section-header">
        <h3>积分规则</h3>
        <button type="button" class="btn-outline btn-sm" @click.stop="toggleSection('rules')">{{ isExpanded('rules') ? '收起' : '展开' }}</button>
      </div>
      <div v-show="isExpanded('rules')" class="rules-grid">
        <div class="rule-col">
          <h4>信用分</h4>
          <ul>
            <li>初始 100 分，范围 0-100 分。</li>
            <li>低于 60 分不能接单，可提交申诉。</li>
            <li>接单后超 10 分钟取消会扣分。</li>
            <li>信用分较低时接单会冻结保证金。</li>
          </ul>
        </div>
        <div class="rule-col">
          <h4>额度分获取</h4>
          <ul>
            <li>每日签到可获得额度分奖励。</li>
            <li>实名认证通过后可获得奖励。</li>
            <li>完善手机号、邮箱等资料可获奖励。</li>
            <li>完成订单获好评可提升额度分。</li>
          </ul>
        </div>
        <div class="rule-col">
          <h4>额度分使用</h4>
          <ul>
            <li>发布需求时冻结对应额度分。</li>
            <li>取消/删除需求按规则退回。</li>
            <li>逾期取消、违约等会扣减额度分。</li>
            <li>额度分不支持提现和转账。</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- ========== 3. 账户管理 ========== -->
    <div class="section-card">
      <div class="section-header">
        <h3>账户管理</h3>
        <button type="button" class="btn-outline btn-sm" @click.stop="toggleSection('account')">{{ isExpanded('account') ? '收起' : '展开' }}</button>
      </div>
      <div v-show="isExpanded('account')">
        <!-- 编辑资料 -->
        <div class="sub-block">
          <h4 class="sub-title">编辑资料</h4>
          <div class="form-group">
            <label>头像</label>
            <div class="avatar-upload">
              <div class="avatar-preview" :class="{ 'has-img': avatarPreview }" :style="avatarPreview ? {} : { background: avatarColor }">
                <img v-if="avatarPreview" :src="avatarPreview" alt="头像预览" />
                <span v-else>{{ auth.user?.username?.[0]?.toUpperCase() }}</span>
              </div>
              <label class="btn-outline btn-sm file-btn">
                选择图片
                <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" @change="pickAvatar" />
              </label>
              <button v-if="avatarPreview && avatarPreview !== auth.user?.avatarUrl" type="button" class="btn-outline btn-sm" @click="removeAvatar">移除</button>
            </div>
          </div>
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
          <div class="form-group">
            <label>性别</label>
            <select v-model="form.gender">
              <option value="" disabled hidden>性别</option>
              <option value="UNDISCLOSED">不愿透露</option>
              <option value="MALE">男</option>
              <option value="FEMALE">女</option>
            </select>
          </div>
          <div class="form-group">
            <label>校区</label>
            <select v-model="form.campus">
              <option value="">未选择</option>
              <option value="鼓楼校区">鼓楼校区</option>
              <option value="仙林校区">仙林校区</option>
              <option value="浦口校区">浦口校区</option>
              <option value="苏州校区">苏州校区</option>
            </select>
          </div>
          <button class="btn-primary btn-sm" @click="update">保存修改</button>
        </div>

        <hr class="divider" />

        <!-- 实名认证 -->
        <div class="sub-block">
          <h4 class="sub-title">实名认证</h4>
          <div v-if="cert" class="cert-state">
            状态：
            <span :style="{ color: cert.status === 'APPROVED' ? 'var(--success)' : cert.status === 'REJECTED' ? 'var(--danger)' : 'var(--warning)' }" class="text-bold">
              {{ cert.status === 'APPROVED' ? '已通过' : cert.status === 'REJECTED' ? '已驳回' : '审核中' }}
            </span>
            <span v-if="cert.status === 'REJECTED' && cert.rejectReason" class="text-sm text-muted"> — {{ cert.rejectReason }}</span>
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
              <label>姓名</label>
              <input :value="auth.user?.realName" readonly />
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

        <hr class="divider" />

        <button class="btn-block logout-btn" @click="logout">退出登录</button>
      </div>
    </div>

    <!-- ========== 4. 记录与申诉 ========== -->
    <div class="section-card">
      <div class="section-header">
        <h3>记录与申诉</h3>
        <button type="button" class="btn-outline btn-sm" @click.stop="toggleSection('records')">{{ isExpanded('records') ? '收起' : '展开' }}</button>
      </div>
      <div v-show="isExpanded('records')">
        <!-- 信用分申诉 -->
        <div class="sub-block">
          <div class="flex-between">
            <h4 class="sub-title">信用分申诉</h4>
            <span v-if="creditAppeal" class="text-sm text-muted">{{ creditAppealStatusText }}</span>
            <span v-else class="text-sm text-muted">暂无记录</span>
          </div>
          <p class="text-sm text-muted">当前信用分：{{ auth.user?.creditScore ?? '-' }}。低于 60 分时可提交申诉。</p>
          <p v-if="creditAppealHint" class="text-sm text-muted">{{ creditAppealHint }}</p>
          <button
            class="btn-primary btn-sm"
            style="margin-top:6px"
            :disabled="!canSubmitCreditAppeal"
            @click="submitCreditAppeal"
          >提交信用分申诉</button>
        </div>

        <hr class="divider" />

        <!-- 信用分记录 -->
        <div class="sub-block">
          <div class="flex-between">
            <h4 class="sub-title">信用分记录 <span class="text-sm text-muted">({{ creditList.length }})</span></h4>
            <button type="button" class="btn-outline btn-sm" @click="toggleSection('creditRecords')">{{ isExpanded('creditRecords') ? '收起' : '展开' }}</button>
          </div>
          <div v-show="isExpanded('creditRecords')">
            <div v-if="creditList.length === 0" class="text-sm text-muted">暂无记录</div>
            <div v-for="item in creditList" :key="item.id" class="record-row">
              <div class="flex-between">
                <span class="text-sm">{{ item.reason }}</span>
                <span class="text-bold" :style="{ color: amountColor(item.changeAmount) }">
                  {{ item.changeAmount > 0 ? '+' + item.changeAmount : item.changeAmount }}
                </span>
              </div>
              <div class="text-xs text-muted">{{ formatTime(item.createdAt) }}</div>
            </div>
          </div>
        </div>

        <hr class="divider" />

        <!-- 额度分记录 -->
        <div class="sub-block">
          <div class="flex-between">
            <h4 class="sub-title">额度分记录 <span class="text-sm text-muted">({{ quotaList.length }})</span></h4>
            <button type="button" class="btn-outline btn-sm" @click="toggleSection('quotaRecords')">{{ isExpanded('quotaRecords') ? '收起' : '展开' }}</button>
          </div>
          <div v-show="isExpanded('quotaRecords')">
            <div v-if="quotaList.length === 0" class="text-sm text-muted">暂无记录</div>
            <div v-for="item in quotaList" :key="item.id" class="record-row">
              <div class="flex-between">
                <span class="text-sm">{{ item.reason }}</span>
                <span class="text-bold" :style="{ color: amountColor(item.changeAmount) }">
                  {{ item.changeAmount > 0 ? '+' + item.changeAmount : item.changeAmount }}
                </span>
              </div>
              <div class="text-xs text-muted">
                可用 {{ item.availableAfter }} · 冻结 {{ item.frozenAfter }} · {{ formatTime(item.createdAt) }}
              </div>
            </div>
          </div>
        </div>

        <hr class="divider" />

        <!-- 我的申诉 -->
        <div class="sub-block">
          <div class="flex-between">
            <h4 class="sub-title">仲裁申诉 <span class="text-sm text-muted">({{ arbitrationList.length }})</span></h4>
            <button type="button" class="btn-outline btn-sm" @click="toggleSection('appeals')">{{ isExpanded('appeals') ? '收起' : '展开' }}</button>
          </div>
          <div v-show="isExpanded('appeals')">
            <div v-if="arbitrationList.length === 0" class="text-sm text-muted">暂无记录</div>
            <div v-for="item in arbitrationList" :key="item.id" class="record-row">
              <div class="text-sm font-bold">{{ item.reviewId ? '评价申诉' : '订单纠纷' }} #{{ item.id }}</div>
              <div class="text-xs text-muted">订单 #{{ item.orderId }} · {{ arbitrationStatusText(item.status) }}</div>
              <div v-if="item.reason" class="text-xs text-muted">理由：{{ item.reason }}</div>
              <div v-if="item.resolution" class="text-xs">结果：{{ item.resolution }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 5. 社交管理 ========== -->
    <div class="section-card">
      <div class="section-header">
        <h3>社交管理</h3>
        <button type="button" class="btn-outline btn-sm" @click.stop="toggleSection('social')">{{ isExpanded('social') ? '收起' : '展开' }}</button>
      </div>
      <div v-show="isExpanded('social')">
        <!-- 我的评价 -->
        <div class="sub-block">
          <div class="flex-between">
            <h4 class="sub-title">我的评价</h4>
            <button class="btn-outline btn-sm" @click="goMyReviews">查看评价</button>
          </div>
          <p class="text-sm text-muted">查看别人对你的评价，以及可申诉的评价记录。</p>
        </div>

        <hr class="divider" />

        <!-- 黑名单 -->
        <div class="sub-block">
          <div class="flex-between">
            <h4 class="sub-title">黑名单</h4>
            <span class="text-sm text-muted">{{ blockedList.length }} 人</span>
          </div>
          <div v-if="blockedList.length === 0" class="text-sm text-muted">暂无拉黑用户</div>
          <div v-for="item in blockedList" :key="item.id" class="blocked-row">
            <div class="blocked-main">
              <div class="blocked-name">{{ item.targetUser?.username || ('用户 #' + item.id) }}</div>
              <div class="blocked-desc">你已拉黑该用户，对方无法继续给你发送私信。</div>
              <div v-if="item.relatedDemandTitle" class="blocked-demand text-xs text-muted">关联需求：{{ item.relatedDemandTitle }}</div>
            </div>
            <button class="btn-outline btn-sm" @click="unblockUser(item)">解除拉黑</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片查看器 -->
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
import { uploadImage } from '@/api/upload'
import { normalizeEmail, normalizePhone, normalizeUsername } from '@/utils/validators'
import { useModal } from '@/composables/useModal'

const modal = useModal()
const router = useRouter()
const auth = useAuthStore()

const form = reactive({ username: '', phone: '', email: '', gender: '', campus: '' })
const avatarPreview = ref('')
const colors = ['#6366f1','#8b5cf6','#ec4899','#f43f5e','#f97316','#14b8a6']
const avatarColor = computed(() => colors[(auth.user?.username?.charCodeAt(0) || 0) % colors.length])
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
const sectionExpanded = reactive({
  rules: false,
  account: true,
  records: false,
  social: false,
  appeals: true,
  creditRecords: true,
  quotaRecords: true
})

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
  const d = new Date()
  const today = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
  return quotaList.value.some(item => item.type === 'DAILY_CHECK_IN' && item.createdAt?.substring(0, 10) === today)
})

function arbitrationStatusText(status) {
  return { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已处理', DISMISSED: '已驳回' }[status] || status
}
function amountColor(amount) {
  if (amount > 0) return 'var(--success)'
  if (amount < 0) return 'var(--danger)'
  return 'var(--text-muted)'
}
function formatTime(t) {
  return (t || '').substring(0, 16)?.replace('T', ' ')
}
function toggleSection(key) { sectionExpanded[key] = !sectionExpanded[key] }
function isExpanded(key) { return !!sectionExpanded[key] }

async function update() {
  const payload = {}
  try {
    if (form.username) payload.username = normalizeUsername(form.username)
    if (form.phone) payload.phone = normalizePhone(form.phone)
    if (form.email) payload.email = normalizeEmail(form.email)
    payload.campus = form.campus
    if (form.gender) payload.gender = form.gender
    if (avatarPreview.value && avatarPreview.value !== auth.user?.avatarUrl) payload.avatarUrl = avatarPreview.value
    if (avatarPreview.value === 'removed') payload.avatarUrl = ''
    await userAPI.update(payload)
    await auth.fetchUser()
    await loadQuota()
    modal.alert('保存成功')
  } catch (e) { modal.alert('操作失败', e.message) }
}

async function checkIn() {
  try {
    const r = await userAPI.checkIn()
    await auth.fetchUser()
    await loadQuota()
    modal.alert('签到成功', `已获得 ${r.data?.changeAmount || 3} 点额度分`)
  } catch (e) { modal.alert('签到失败', e.message) }
}

async function submitCreditAppeal() {
  const reason = await modal.prompt('信用分申诉', '请填写申诉理由（必填）', '请说明具体情况，管理员会据此审核。')
  if (reason === false) return
  if (!reason || !reason.trim()) { await modal.alert('理由不能为空', '请填写申诉理由。'); return }
  try {
    await userAPI.creditAppeal(reason.trim())
    await auth.fetchUser()
    await loadCreditAppeal()
    modal.alert('提交成功', '信用分申诉已提交，请等待管理员审核。')
  } catch (e) { modal.alert('提交失败', e.message) }
}

async function submitBanAppeal() {
  const reason = await modal.prompt('封禁申诉', '请填写申诉理由（必填）', '请说明你认为被误封或希望解封的原因。')
  if (reason === false) return
  if (!reason || !reason.trim()) { await modal.alert('理由不能为空', '请填写申诉理由。'); return }
  try {
    await userAPI.banAppeal(reason.trim())
    await loadBanAppeal()
    modal.alert('提交成功', '封禁申诉已提交，请等待管理员审核。')
  } catch (e) { modal.alert('提交失败', e.message) }
}

async function submitCert() {
  certErr.value = ''
  certSubmitting.value = true
  try {
    if (!certForm.cardImageUrl) { certErr.value = '请先上传认证图片'; return }
    await certAPI.submit({ ...certForm, studentId: auth.user?.studentId || '' })
    clearCertImage()
    await loadCert()
  } catch (e) { modal.alert('提交失败', e.message) }
  finally { certSubmitting.value = false }
}

function pickCertImage(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) { certErr.value = '请选择图片文件'; return }
  if (file.size > 700000) { certErr.value = '图片需小于 700KB'; return }
  const reader = new FileReader()
  reader.onload = () => { certForm.cardImageUrl = String(reader.result || ''); certErr.value = '' }
  reader.readAsDataURL(file)
}
async function pickAvatar(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) { modal.alert('请选择图片文件'); return }
  if (file.size > 500000) { modal.alert('图片需小于 500KB'); return }
  try {
    const res = await uploadImage(file)
    avatarPreview.value = res.data?.url || ''
  } catch (e) {
    modal.alert('上传失败', e.message || '图片上传失败，请重试')
  }
}
function removeAvatar() { avatarPreview.value = 'removed' }
function clearCertImage() { certForm.cardImageUrl = '' }

async function loadCert() {
  try { const r = await certAPI.mine(); cert.value = r.data; certForm.studentId = auth.user?.studentId || '' }
  catch { cert.value = null; certForm.studentId = auth.user?.studentId || '' }
}
async function loadCredit() {
  try { const r = await userAPI.creditRecords(); creditList.value = r.data || [] }
  catch { creditList.value = [] }
}
async function loadQuota() {
  try { const r = await userAPI.quotaRecords(); quotaList.value = r.data || [] }
  catch { quotaList.value = [] }
}
async function loadCreditAppeal() {
  try { const r = await userAPI.myAppeal(); creditAppeal.value = r.data }
  catch { creditAppeal.value = null }
}
async function loadBanAppeal() {
  try { const r = await userAPI.myBanAppeal(); banAppeal.value = r.data }
  catch { banAppeal.value = null }
}
async function loadBlockedList() {
  try { const r = await chatAPI.blockedList(); blockedList.value = r.data || [] }
  catch { blockedList.value = [] }
}
async function loadArbitrations() {
  try { const r = await arbitrationAPI.mine(); arbitrationList.value = r.data || [] }
  catch { arbitrationList.value = [] }
}
async function unblockUser(item) {
  if (!item?.id) return
  try { await chatAPI.unblock(item.id); await loadBlockedList(); modal.alert('操作成功', '已将该用户移出黑名单。') }
  catch (e) { modal.alert('操作失败', e.message) }
}
function logout() { auth.logout(); router.push('/login') }
function goMyReviews() { if (auth.user?.id) router.push('/reviews/' + auth.user.id) }

onMounted(async () => {
  await auth.fetchUser()
  if (auth.user?.campus) form.campus = auth.user.campus
  if (auth.user?.gender) form.gender = auth.user.gender
  if (auth.user?.avatarUrl) avatarPreview.value = auth.user.avatarUrl
  await Promise.all([loadCert(), loadCredit(), loadQuota(), loadCreditAppeal(), loadBanAppeal(), loadBlockedList(), loadArbitrations()])
})
</script>

<style scoped>
.page{max-width:680px}

/* ---- 账户概览 ---- */
.ph{background:var(--primary-light);border-radius:14px;padding:24px 24px 20px;margin-bottom:16px;border:2px solid black;box-shadow:4px 4px 0 rgba(0,0,0,1)}

.ph-top{display:flex;align-items:center;gap:16px;margin-bottom:24px}
.pav{width:68px;height:68px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:28px;font-weight:900;flex-shrink:0;border:2px solid black;overflow:hidden;color:#fff}
.pav.has-avatar{background:#fff;padding:0}
.pav img{width:100%;height:100%;object-fit:contain}
.ph-info{flex:1;min-width:0}
.pn{font-size:19px;font-weight:900;color:var(--text);line-height:1.3}
.pi{font-size:13px;color:var(--text-muted);margin-top:4px}

.check-in-btn{flex-shrink:0;padding:8px 16px;border-radius:10px;font-size:13px;font-weight:700;border:2px solid black;background:var(--primary);color:#fff;box-shadow:2px 2px 0 rgba(0,0,0,1);cursor:pointer;transition:all .12s}
.check-in-btn:hover{transform:translate(1px,1px);box-shadow:none}
.check-in-btn.done{background:var(--surface);color:var(--text-muted);cursor:default;box-shadow:none}
.check-in-btn.done:hover{transform:none}

.stats-row{display:grid;grid-template-columns:repeat(2,1fr);gap:8px;margin-bottom:8px}
.stat-item{text-align:center;background:var(--card);border-radius:10px;padding:8px 12px;border:2px solid black;box-shadow:2px 2px 0 rgba(0,0,0,1);transition:transform .15s ease;cursor:default}
.stat-item:hover{transform:scale(1.06)}
.stat-val{font-size:20px;font-weight:900;color:var(--text)}
.stat-lbl{font-size:11px;color:var(--text-muted);margin-top:2px}

.appeal-row{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:10px}
.appeal-tag{padding:5px 12px;border-radius:8px;font-size:12px;font-weight:700;border:1.5px solid black}
.appeal-PENDING{background:#fef9c3;color:#854d0e}
.appeal-APPROVED{background:#dcfce7;color:#166534}
.appeal-REJECTED{background:#fee2e2;color:#991b1b}

.ph-actions{display:flex;gap:8px;flex-wrap:wrap}
.app-btn{padding:7px 16px;border-radius:10px;font-size:13px;font-weight:700;border:1.5px solid black;cursor:pointer;transition:all .12s}
.app-btn:hover{transform:translate(1px,1px);box-shadow:none}
.app-btn.warn{background:#fef9c3;color:#854d0e;box-shadow:2px 2px 0 rgba(0,0,0,1)}
.app-btn.danger{background:#fee2e2;color:#991b1b;box-shadow:2px 2px 0 rgba(0,0,0,1)}

/* ---- 分区卡片 ---- */
.section-card{background:var(--card);border-radius:12px;border:3px solid black;box-shadow:4px 4px 0 rgba(0,0,0,1);margin-bottom:16px;overflow:hidden}
.section-header{display:flex;align-items:center;justify-content:space-between;padding:16px 20px}
.section-header h3{margin:0;font-size:16px;font-weight:900}
.section-card > div:not(.section-header){padding:0 20px 16px}

/* ---- 子内容块 ---- */
.sub-block{padding:8px 0}
.sub-title{font-size:14px;font-weight:800;margin:0 0 6px}
.divider{border:none;border-top:1px solid var(--border);margin:0}

/* ---- 表单 ---- */
.rr{display:grid;grid-template-columns:1fr 1fr;gap:12px}

/* ---- 头像上传 ---- */
.avatar-upload{display:flex;align-items:center;gap:12px}
.avatar-preview{width:56px;height:56px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:22px;font-weight:900;color:#fff;border:2px solid black;overflow:hidden;flex-shrink:0}
.avatar-preview.has-img{background:#fff}
.avatar-preview img{width:100%;height:100%;object-fit:contain}

/* ---- 认证 ---- */
.cert-state{margin-bottom:8px}
.upload-row{display:flex;gap:8px;align-items:center;flex-wrap:wrap}
.file-btn{position:relative;overflow:hidden;cursor:pointer}
.file-btn input{position:absolute;inset:0;opacity:0;pointer-events:none}
.cert-preview{display:block;width:180px;height:120px;margin-top:10px;padding:0;border:2px solid black;border-radius:10px;overflow:hidden;background:var(--surface)}
.compact-preview{width:220px;height:150px}
.cert-preview img{display:block;width:100%;height:100%;object-fit:contain}

/* ---- 记录行 ---- */
.record-row{padding:8px 0;border-bottom:1px solid var(--border)}
.record-row:last-child{border-bottom:none}

/* ---- 黑名单 ---- */
.blocked-row{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:10px 0;border-bottom:1px solid var(--border)}
.blocked-row:last-child{border-bottom:none}
.blocked-main{min-width:0}
.blocked-name{font-size:14px;font-weight:800}
.blocked-desc{margin-top:3px;font-size:13px;color:var(--text-muted)}
.blocked-demand{margin-top:3px}

/* ---- 退出 ---- */
.logout-btn{width:100%;padding:12px;border-radius:10px;background:#fee2e2;color:var(--danger);font-weight:700;font-size:14px;border:2px solid var(--danger);box-shadow:4px 4px 0 rgba(0,0,0,1);margin-top:4px}
.logout-btn:hover{background:#fecaca;transform:translate(1px,1px);box-shadow:none}

/* ---- 规则 ---- */
.rules-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}
.rule-col h4{font-size:14px;font-weight:900;margin:0 0 8px;color:var(--text)}
.rule-col ul{margin:0;padding-left:18px;color:var(--text-muted);font-size:13px;line-height:1.75}
.rule-col li{margin:0 0 4px}

/* ---- 图片查看器 ---- */
.image-viewer{position:fixed;inset:0;z-index:40;display:flex;align-items:center;justify-content:center;padding:56px 20px;background:rgba(15,23,42,.82)}
.image-viewer img{max-width:min(920px,96vw);max-height:86vh;border-radius:12px;background:#fff;border:2px solid black}
.viewer-close{position:absolute;top:18px;right:18px;background:#fff;color:var(--text)}

@media(max-width:860px){
  .stats-row{grid-template-columns:repeat(2,1fr)}
  .rules-grid{grid-template-columns:1fr}
  .rr{grid-template-columns:1fr}
  .blocked-row{align-items:flex-start;flex-direction:column}
  .blocked-row .btn-sm{width:100%}
}
</style>
