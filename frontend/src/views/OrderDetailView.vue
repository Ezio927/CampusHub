<template>
  <div v-if="order" class="page">
    <a href="#" class="back" @click.prevent="goBack">←</a>

    <!-- 状态横幅 -->
    <div class="status-bar" :style="{ background: statusBg(order.status) }">
      <span class="status-bar-text">{{ statusLabel(order.status) }}</span>
      <span v-if="order.rewardType" class="reward-chip">{{ order.rewardType === 'MONEY' ? '¥' + order.rewardAmount : order.rewardAmount + '额度分' }}</span>
    </div>

    <!-- 主卡片 -->
    <div class="main-card">
      <h1 class="title">{{ order.demandTitle || ('需求 #' + order.demandId) }}</h1>
      <div class="time-text">创建于 {{ order.createdAt?.substring(0, 10) }}</div>

      <!-- 双方用户 -->
      <div class="users-section">
        <div class="user-line">
          <div class="user-avatar" :style="order.publisherAvatarUrl ? {} : { background: pubColor }">
            <img v-if="order.publisherAvatarUrl" :src="order.publisherAvatarUrl" alt="头像" />
            <span v-else>{{ (order.publisherName || '?')[0] }}</span>
          </div>
          <div class="user-body">
            <div class="user-name">{{ order.publisherName || ('用户 #' + order.publisherId) }}</div>
            <div class="user-tag">发布者</div>
          </div>
        </div>
        <div class="user-arrow">↓</div>
        <div class="user-line">
          <div class="user-avatar" :style="order.acceptorAvatarUrl ? {} : { background: accColor }">
            <img v-if="order.acceptorAvatarUrl" :src="order.acceptorAvatarUrl" alt="头像" />
            <span v-else>{{ (order.acceptorName || '?')[0] }}</span>
          </div>
          <div class="user-body">
            <div class="user-name">{{ order.acceptorName || ('用户 #' + order.acceptorId) }}</div>
            <div class="user-tag">接单者</div>
          </div>
        </div>
      </div>

      <div class="divider"></div>

      <!-- 进度步骤 -->
      <div class="progress-section">
        <div class="section-label">订单进度</div>
        <div class="progress-steps">
          <template v-for="(step, i) in steps" :key="step.key">
            <div class="step" :class="{ done: step.done, current: step.current }">
              <div class="step-dot">{{ step.done ? '✓' : step.current ? '●' : '○' }}</div>
              <div class="step-text">
                <div class="step-title">{{ step.label }}</div>
                <div v-if="step.time" class="step-time">{{ step.time }}</div>
              </div>
            </div>
            <div v-if="i < steps.length - 1" class="step-line" :class="{ done: step.done }"></div>
          </template>
        </div>
      </div>

      <div v-if="order.status === 'CANCELLED' && order.cancelReason" class="cancel-box">
        已取消：{{ order.cancelReason }}
      </div>
    </div>

    <!-- 评价表单 -->
    <div v-if="showReview" class="section-card">
      <div class="section-label">提交评价</div>
      <div class="star-row">
        <button v-for="i in 5" :key="i" type="button" class="star-btn" :class="{ on: reviewRating >= i }" @click="reviewRating = i">{{ reviewRating >= i ? '★' : '☆' }}</button>
      </div>
      <textarea v-model="reviewContent" class="review-input" placeholder="写下你的评价（选填）" maxlength="500" rows="2"></textarea>
      <p v-if="reviewErr" class="form-error">{{ reviewErr }}</p>
      <button class="btn-primary btn-block" :disabled="reviewSubmitting" @click="submitReviewAction">{{ reviewSubmitting ? '提交中...' : '提交评价' }}</button>
    </div>

    <!-- 已有评价 -->
    <div v-if="reviews.length" class="section-card">
      <div class="section-label">订单评价</div>
      <div v-for="item in reviews" :key="item.id" class="review-item">
        <div class="flex-between">
          <span class="review-stars">{{ '★'.repeat(item.rating) }}{{ '☆'.repeat(5 - item.rating) }}</span>
          <span class="text-sm text-muted">{{ item.createdAt?.substring(0, 10) }}</span>
        </div>
        <p v-if="item.content" class="review-content">{{ item.content }}</p>
      </div>
    </div>

    <!-- 操作 -->
    <div class="actions">
      <button v-if="targetUserId" class="btn-outline btn-block" @click="messagePeer">私信对方</button>
      <button v-if="canAppeal" class="btn-outline btn-block" @click="submitOrderAppeal">订单纠纷申诉</button>
      <button v-for="item in actions" :key="item.key" class="btn-primary btn-block" @click="doAction(item.key)">{{ item.label }}</button>
    </div>
  </div>
  <div v-else class="empty">加载中...</div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { orderAPI } from '@/api/orders'
import { chatAPI } from '@/api/chat'
import { reviewAPI } from '@/api/reviews'
import { arbitrationAPI } from '@/api/arbitrations'
import { errorMessage } from '@/utils/errors'
import { useModal } from '@/composables/useModal'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const modal = useModal()

const order = ref(null)
const myReview = ref(null)
const reviewRating = ref(0)
const reviewContent = ref('')
const reviewErr = ref('')
const reviewSubmitting = ref(false)
const reviews = ref([])

const pubColor = computed(() => colors[(order.value?.publisherName?.charCodeAt(0) || 0) % colors.length])
const accColor = computed(() => colors[(order.value?.acceptorName?.charCodeAt(0) || 1) % colors.length])
const colors = ['#2563eb', '#7c3aed', '#db2777', '#dc2626', '#ea580c']

function statusLabel(s) { return { PENDING_ACCEPT:'待接单', ACCEPTED:'已接单', PENDING_CONFIRM:'待确认', COMPLETED:'已完成', CANCELLED:'已取消' }[s] || s }
function statusBg(s) { return { PENDING_ACCEPT:'#3b82f6', ACCEPTED:'#06b6d4', PENDING_CONFIRM:'#eab308', COMPLETED:'#10b981', CANCELLED:'#9ca3af' }[s] || '#888' }

const steps = computed(() => {
  const s = order.value?.status
  if (!s) return []
  const o = order.value
  const fmt = (d) => d ? d.substring(0, 10) : ''
  const cancelled = s === 'CANCELLED'
  return [
    { key:'created',  label:'下单',   time: fmt(o.createdAt),  done: true,                                                        current: cancelled },
    { key:'accepted', label:'接单',   time: fmt(o.acceptedAt), done: s !== 'PENDING_ACCEPT' && !cancelled,                         current: s === 'PENDING_ACCEPT' && !cancelled },
    { key:'delivered',label:'已送达', time: '',                 done: (s === 'PENDING_CONFIRM' || s === 'COMPLETED') && !cancelled, current: s === 'ACCEPTED' && !cancelled },
    { key:'completed',label:'完成',   time: fmt(o.completedAt), done: s === 'COMPLETED' && !cancelled,                             current: s === 'PENDING_CONFIRM' && !cancelled },
  ]
})

const actions = computed(() => {
  if (!order.value) return []
  const uid = auth.user?.id; const s = order.value.status; const list = []
  if (s === 'ACCEPTED' && order.value.acceptorId === uid) list.push({ key: 'deliver', label: '标记已送达' })
  if (s === 'PENDING_CONFIRM' && order.value.publisherId === uid) list.push({ key: 'confirm', label: '确认完成' })
  if (['PENDING_ACCEPT','ACCEPTED','PENDING_CONFIRM'].includes(s)) list.push({ key: 'cancel', label: '取消订单' })
  return list
})

const targetUserId = computed(() => {
  if (!order.value) return null
  const uid = auth.user?.id
  return order.value.publisherId === uid ? order.value.acceptorId : order.value.acceptorId === uid ? order.value.publisherId : null
})

const showReview = computed(() => {
  if (!order.value || !auth.user?.id) return false
  const uid = auth.user.id
  if (uid !== order.value.publisherId && uid !== order.value.acceptorId) return false
  if (!['COMPLETED','PENDING_CONFIRM'].includes(order.value.status)) return false
  return !myReview.value
})

const canAppeal = computed(() => {
  if (!order.value) return false
  const uid = auth.user?.id
  return uid && [order.value.publisherId, order.value.acceptorId].includes(uid)
})

async function refresh() { try { order.value = (await orderAPI.detail(route.params.id)).data } catch { order.value = null }; await loadReviews() }
async function loadReviews() {
  try { const r = await reviewAPI.byOrder(route.params.id); reviews.value = r.data || []; myReview.value = reviews.value.find(x => x.reviewerId === auth.user?.id) || null }
  catch { reviews.value = [] }
}

async function doAction(key) {
  try {
    if (key === 'deliver') { await orderAPI.deliver(order.value.id); await refresh(); return }
    if (key === 'confirm') { await orderAPI.confirm(order.value.id); await refresh(); return }
    if (key === 'cancel') {
      const reason = await modal.prompt('取消订单', '请输入取消原因', '请填写取消原因')
      if (reason === false || !reason?.trim()) { if (reason !== false) await modal.alert('原因不能为空', '请填写取消原因。'); return }
      await orderAPI.cancel(order.value.id, { reason: reason.trim() }); await refresh()
    }
  } catch (e) { modal.alert('操作失败', errorMessage(e)) }
}

async function submitReviewAction() {
  reviewErr.value = ''
  if (reviewRating.value < 1 || reviewRating.value > 5) { reviewErr.value = '请选择评分'; return }
  if (reviewRating.value <= 3 && (!reviewContent.value || reviewContent.value.trim().length < 15)) { reviewErr.value = '差评需要至少 15 个字说明原因'; return }
  reviewSubmitting.value = true
  try {
    const revieweeId = targetUserId.value; if (!revieweeId) throw new Error('无法确定评价对象')
    myReview.value = (await reviewAPI.submit({ orderId: order.value.id, revieweeId, rating: reviewRating.value, content: reviewContent.value.trim() || undefined, imageUrls: [] })).data
    await loadReviews()
  } catch (e) { reviewErr.value = errorMessage(e) }
  finally { reviewSubmitting.value = false }
}

async function messagePeer() {
  try { const r = await chatAPI.create({ targetUserId: targetUserId.value, demandId: order.value.demandId }); router.push('/chat/' + r.data.id) }
  catch (e) { modal.alert('发起私信失败', errorMessage(e)) }
}

async function submitOrderAppeal() {
  const reason = await modal.prompt('订单纠纷申诉', '请填写申诉理由', '请说明订单争议情况，管理员会进行处理。')
  if (reason === false || !reason?.trim()) { if (reason !== false) await modal.alert('理由不能为空', '请填写申诉理由。'); return }
  try { await arbitrationAPI.create({ orderId: order.value.id, reason: reason.trim(), evidenceUrls: [] }); modal.alert('提交成功', '申诉已提交，请等待管理员处理。') }
  catch (e) { modal.alert('提交失败', errorMessage(e)) }
}

function goBack() {
  const fromRole = route.query.role
  if (fromRole === 'PUBLISHER' || fromRole === 'ACCEPTOR') { router.push({ path: '/orders', query: { role: fromRole } }); return }
  router.back()
}

onMounted(refresh)
</script>

<style scoped>
/* 状态横幅 */
.status-bar { margin: 0 auto 16px; max-width: 680px; border-radius: 12px; padding: 12px 20px; border: 2px solid black; box-shadow: 3px 3px 0 rgba(0,0,0,1); display: flex; align-items: center; gap: 10px; }
.status-bar-text { color: #fff; font-size: 15px; font-weight: 900; letter-spacing: 1px; }
.reward-chip { background: rgba(255,255,255,.25); color: #fff; padding: 2px 12px; border-radius: 8px; font-size: 13px; font-weight: 700; }

/* 主卡片 */
.main-card { background: var(--card); border-radius: 16px; padding: 28px 24px 24px; border: 2px solid black; box-shadow: 4px 4px 0 rgba(0,0,0,1); max-width: 680px; margin: 0 auto 14px; }
.title { font-size: 20px; font-weight: 900; line-height: 1.35; margin: 0 0 4px; }
.time-text { font-size: 13px; color: var(--text-muted); margin-bottom: 20px; }

/* 用户区 */
.users-section { padding: 4px 0; }
.user-line { display: flex; align-items: center; gap: 12px; padding: 10px 12px; }
.user-avatar { width: 44px; height: 44px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #fff; font-size: 18px; border: 2px solid black; overflow: hidden; flex-shrink: 0; }
.user-avatar img { width: 100%; height: 100%; object-fit: contain; }
.user-body { flex: 1; display: flex; align-items: center; justify-content: space-between; }
.user-name { font-size: 15px; font-weight: 700; }
.user-tag { font-size: 11px; font-weight: 600; padding: 3px 10px; border-radius: 6px; border: 1.5px solid black; background: var(--surface); color: var(--text-muted); }
.user-arrow { text-align: center; font-size: 14px; font-weight: 900; color: var(--text-muted); padding: 2px 0 2px 28px; line-height: 1; }

.divider { height: 1px; background: var(--border); margin: 18px 0; }

/* 进度 */
.section-label { font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 12px; }
.progress-steps { display: flex; align-items: flex-start; }
.step { display: flex; align-items: flex-start; gap: 8px; flex: 1; min-width: 0; }
.step-dot { font-size: 16px; line-height: 1.2; flex-shrink: 0; color: var(--text-muted); }
.step.done .step-dot { color: var(--primary); font-weight: 700; }
.step.current .step-dot { color: var(--primary); font-weight: 700; }
.step-text { min-width: 0; }
.step-title { font-size: 13px; font-weight: 600; color: var(--text-muted); white-space: nowrap; }
.step.done .step-title, .step.current .step-title { color: var(--text); font-weight: 700; }
.step-time { font-size: 11px; color: var(--text-muted); margin-top: 1px; }
.step-line { width: 20px; height: 2px; background: var(--border); margin: 7px 2px 0; flex-shrink: 0; }
.step-line.done { background: var(--primary); }

.cancel-box { margin-top: 14px; padding: 12px 16px; background: #fef2f2; border: 2px solid black; border-radius: 10px; font-size: 14px; font-weight: 600; color: #991b1b; }

/* 评价卡片 */
.section-card { background: var(--card); border-radius: 16px; padding: 20px 24px; margin-bottom: 14px; border: 2px solid black; box-shadow: 4px 4px 0 rgba(0,0,0,1); max-width: 680px; margin-left: auto; margin-right: auto; }
.star-row { display: flex; gap: 4px; margin-bottom: 12px; }
.star-btn { background: none; border: none; font-size: 28px; cursor: pointer; color: #d1d5db; padding: 0; transition: color .12s; }
.star-btn.on, .star-btn:hover { color: #f59e0b; }
.review-input { width: 100%; padding: 10px 12px; border: 2px solid black; border-radius: 10px; font-size: 14px; resize: vertical; margin-bottom: 10px; box-sizing: border-box; }
.review-input:focus { border-color: var(--primary); outline: none; }
.form-error { color: var(--danger); font-size: 13px; margin-bottom: 8px; }
.review-item { padding: 10px 0; border-top: 1px solid var(--border); }
.review-item:first-of-type { border-top: none; padding-top: 0; }
.review-stars { font-size: 16px; letter-spacing: 2px; color: #f59e0b; font-weight: 700; }
.review-content { margin-top: 6px; font-size: 14px; color: var(--text-secondary); line-height: 1.6; }

/* 操作 */
.actions { display: flex; flex-direction: column; gap: 8px; max-width: 680px; margin: 0 auto; }
.actions :deep(.btn-outline),
.actions :deep(.btn-primary) { padding: 14px; border-radius: 12px; font-size: 16px; font-weight: 700; border: 2px solid black; box-shadow: 4px 4px 0 rgba(0,0,0,1); }
.actions :deep(.btn-outline):hover,
.actions :deep(.btn-primary):hover { transform: translate(2px,2px); box-shadow: none; }

@media (max-width: 480px) {
  .main-card { padding: 20px 16px; }
  .progress-steps { flex-wrap: wrap; gap: 8px; }
  .step-line { display: none; }
}
</style>
