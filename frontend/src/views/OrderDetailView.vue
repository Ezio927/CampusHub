<template>
  <div class="page" v-if="order">
    <a href="#" class="back" @click.prevent="$router.back()">返回</a>
    <div class="detail-card">
      <div class="flex-between mb-md gap-sm">
        <h1 class="text-lg text-bold">{{ order.demandTitle || '需求 #' + order.demandId }}</h1>
        <span class="badge" :style="{ background: statusColor(order.status) }">{{ statusText(order.status) }}</span>
      </div>
      <div class="info-grid">
        <div class="info-item">
          <span class="label">发布者</span>
          <span class="value user-value">
            <button type="button" class="mini-avatar" :class="{ hasAvatar: order.publisherAvatarUrl }" @click="openAvatar(order.publisherAvatarUrl)">
              <img v-if="order.publisherAvatarUrl" :src="order.publisherAvatarUrl" alt="发布者头像" />
              <span v-else>{{ userInitial({ username: order.publisherName }) }}</span>
            </button>
            {{ order.publisherName || 'ID:' + order.publisherId }}
          </span>
        </div>
        <div class="info-item">
          <span class="label">接单者</span>
          <span class="value user-value">
            <button type="button" class="mini-avatar" :class="{ hasAvatar: order.acceptorAvatarUrl }" @click="openAvatar(order.acceptorAvatarUrl)">
              <img v-if="order.acceptorAvatarUrl" :src="order.acceptorAvatarUrl" alt="接单者头像" />
              <span v-else>{{ userInitial({ username: order.acceptorName }) }}</span>
            </button>
            {{ order.acceptorName || 'ID:' + order.acceptorId }}
          </span>
        </div>
        <div class="info-item"><span class="label">报酬</span><span class="value reward">{{ rewardText(order) }}</span></div>
        <div class="info-item"><span class="label">状态</span><span class="value">{{ statusText(order.status) }}</span></div>
        <div class="info-item"><span class="label">创建时间</span><span class="value text-sm">{{ formatTime(order.createdAt) }}</span></div>
        <div v-if="order.acceptedAt" class="info-item"><span class="label">接单时间</span><span class="value text-sm">{{ formatTime(order.acceptedAt) }}</span></div>
        <div v-if="order.completedAt" class="info-item"><span class="label">完成时间</span><span class="value text-sm">{{ formatTime(order.completedAt) }}</span></div>
        <div v-if="order.cancelledAt" class="info-item"><span class="label">取消时间</span><span class="value text-sm">{{ formatTime(order.cancelledAt) }}</span></div>
      </div>
      <div v-if="order.cancelReason" class="reason-box">取消原因：{{ order.cancelReason }}</div>
      <div class="review-status">
        <span>发布者评价：{{ order.publisherReviewed ? '已评价' : '未评价' }}</span>
        <span>接单者评价：{{ order.acceptorReviewed ? '已评价' : '未评价' }}</span>
      </div>
    </div>

    <div v-if="canReview" class="review-card">
      <h3>评价{{ reviewTargetLabel }}</h3>
      <div class="star-row">
        <button v-for="i in 5" :key="i" type="button" class="star-btn" :class="{ on: reviewRating >= i }" @click="reviewRating = i">
          {{ reviewRating >= i ? '★' : '☆' }}
        </button>
      </div>
      <textarea v-model="reviewContent" class="review-input" placeholder="写下你的评价（选填）" maxlength="500" rows="3"></textarea>
      <p v-if="reviewErr" class="form-error">{{ reviewErr }}</p>
      <button class="btn-primary btn-block" :disabled="reviewSubmitting" @click="submitReviewAction">
        {{ reviewSubmitting ? '提交中...' : '提交评价' }}
      </button>
    </div>

    <div class="actions">
      <button v-if="targetUserId" class="btn-outline btn-block" @click="messagePeer">私信对方</button>
      <button v-for="action in actions" :key="action.key" class="btn-primary btn-block" @click="doAction(action.key)">{{ action.label }}</button>
    </div>

    <div v-if="avatarViewer" class="image-viewer" @click.self="avatarViewer = ''">
      <button type="button" class="viewer-close" @click="avatarViewer = ''">关闭</button>
      <img :src="avatarViewer" alt="头像" />
    </div>
  </div>
  <div v-else class="empty">加载中...</div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { orderAPI } from '@/api/orders'
import { chatAPI } from '@/api/chat'
import { reviewAPI } from '@/api/reviews'
import { errorMessage } from '@/utils/errors'
import { userInitial } from '@/utils/userDisplay'
import { useModal } from '@/composables/useModal'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const modal = useModal()

const order = ref(null)
const reviewRating = ref(0)
const reviewContent = ref('')
const reviewErr = ref('')
const reviewSubmitting = ref(false)
const avatarViewer = ref('')

const targetUserId = computed(() => {
  if (!order.value) return null
  const uid = auth.user?.id
  if (order.value.publisherId === uid) return order.value.acceptorId
  if (order.value.acceptorId === uid) return order.value.publisherId
  return null
})

const reviewTargetLabel = computed(() => {
  if (!order.value) return ''
  return targetUserId.value === order.value.acceptorId ? '接单者' : '发布者'
})

const canReview = computed(() => {
  const uid = auth.user?.id
  if (!uid || !order.value || order.value.status !== 'COMPLETED') return false
  if (order.value.publisherId === uid) return !order.value.publisherReviewed
  if (order.value.acceptorId === uid) return !order.value.acceptorReviewed
  return false
})

const actions = computed(() => {
  if (!order.value) return []
  const uid = auth.user?.id
  const status = order.value.status
  const list = []
  if (status === 'ACCEPTED' && order.value.acceptorId === uid) list.push({ key: 'deliver', label: '标记已送达' })
  if (status === 'PENDING_CONFIRM' && order.value.publisherId === uid) list.push({ key: 'confirm', label: '确认完成' })
  if (['PENDING_ACCEPT', 'ACCEPTED', 'PENDING_CONFIRM'].includes(status)) list.push({ key: 'cancel', label: '取消订单' })
  return list
})

function statusText(status) {
  return { PENDING_ACCEPT: '待接单', ACCEPTED: '已接单', PENDING_CONFIRM: '待确认', COMPLETED: '已完成', CANCELLED: '已取消' }[status] || status
}

function statusColor(status) {
  return { PENDING_ACCEPT: '#3b82f6', ACCEPTED: '#06b6d4', PENDING_CONFIRM: '#eab308', COMPLETED: '#10b981', CANCELLED: '#9ca3af' }[status] || '#888'
}

function rewardText(value) {
  return value.rewardType === 'MONEY' ? `¥${value.rewardAmount || 0}` : `${value.rewardAmount || 0}额度分`
}

function formatTime(value) {
  return value?.substring(0, 16)?.replace('T', ' ') || '-'
}

function openAvatar(src) {
  if (src) avatarViewer.value = src
}

async function doAction(key) {
  try {
    if (key === 'deliver') {
      await orderAPI.deliver(order.value.id)
      await refresh()
      return
    }
    if (key === 'confirm') {
      await orderAPI.confirm(order.value.id)
      await refresh()
      return
    }
    if (key === 'cancel') {
      const reason = await modal.prompt('取消订单', '请输入取消原因', '请输入取消原因')
      if (!reason || !reason.trim()) {
        await modal.alert('原因不能为空', '请填写取消原因')
        return
      }
      await orderAPI.cancel(order.value.id, { reason: reason.trim() })
      await refresh()
    }
  } catch (err) {
    modal.alert('操作失败', errorMessage(err))
  }
}

async function submitReviewAction() {
  reviewErr.value = ''
  if (reviewRating.value < 1 || reviewRating.value > 5) {
    reviewErr.value = '请选择评分'
    return
  }
  if (reviewRating.value <= 3 && (!reviewContent.value || reviewContent.value.trim().length < 15)) {
    reviewErr.value = '低分评价需要至少15字说明'
    return
  }
  reviewSubmitting.value = true
  try {
    const revieweeId = targetUserId.value
    if (!revieweeId) throw new Error('无法确定评价对象')
    await reviewAPI.submit({
      orderId: order.value.id,
      revieweeId,
      rating: reviewRating.value,
      content: reviewContent.value.trim() || undefined,
      imageUrls: [],
    })
    reviewRating.value = 0
    reviewContent.value = ''
    await refresh()
    modal.alert('评价已提交')
  } catch (err) {
    reviewErr.value = errorMessage(err)
  } finally {
    reviewSubmitting.value = false
  }
}

async function refresh() {
  const res = await orderAPI.detail(route.params.id)
  order.value = res.data
}

async function messagePeer() {
  if (!targetUserId.value) {
    modal.alert('发起私信失败', '对方用户信息缺失，暂时无法发起私信')
    return
  }
  try {
    const res = await chatAPI.create({ targetUserId: targetUserId.value, demandId: order.value.demandId })
    if (!res.data?.id) throw new Error('会话创建失败')
    router.push('/chat/' + res.data.id)
  } catch (err) {
    modal.alert('发起私信失败', errorMessage(err))
  }
}

onMounted(async () => {
  await auth.fetchUser()
  await refresh()
})
</script>

<style scoped>
.detail-card, .review-card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 14px; border: 3px solid black; box-shadow: 6px 6px 0 rgba(0,0,0,1); max-width: 720px; margin-left: auto; margin-right: auto; }
.info-grid { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 12px; }
.info-item { padding: 12px; background: #f8fafc; border-radius: 10px; display: flex; flex-direction: column; gap: 4px; border: 2px solid black; }
.label { font-size: 12px; color: var(--text-muted); font-weight: 500; }
.value { font-size: 16px; font-weight: 700; }
.reward { color: var(--danger); }
.user-value { display: flex; align-items: center; gap: 8px; min-width: 0; }
.mini-avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; border: 2px solid black; background: var(--primary); color: #fff; font-weight: 900; padding: 0; overflow: hidden; }
.mini-avatar.hasAvatar { background: #fff; cursor: zoom-in; }
.mini-avatar img { width: 100%; height: 100%; object-fit: cover; }
.reason-box { margin-top: 12px; padding: 10px 12px; border-radius: 10px; border: 2px solid black; background: #f8fafc; color: var(--text-secondary); font-size: 14px; }
.review-status { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 8px 16px; font-size: 13px; color: var(--text-muted); }
.review-card h3 { font-size: 15px; font-weight: 900; margin: 0 0 12px; }
.star-row { display: flex; gap: 4px; margin-bottom: 12px; }
.star-btn { background: none; border: none; font-size: 28px; cursor: pointer; color: #d1d5db; padding: 0; transition: color .12s; }
.star-btn.on, .star-btn:hover { color: #f59e0b; }
.review-input { width: 100%; padding: 10px 12px; border: 2px solid black; border-radius: 10px; font-size: 14px; resize: vertical; margin-bottom: 10px; box-sizing: border-box; }
.review-input:focus { border-color: var(--primary); outline: none; }
.actions { display: flex; flex-direction: column; gap: 8px; max-width: 720px; margin-left: auto; margin-right: auto; }
.actions button { padding: 14px; border-radius: 10px; font-size: 15px; font-weight: 700; }
.image-viewer { position: fixed; inset: 0; z-index: 40; display: flex; align-items: center; justify-content: center; padding: 56px 20px; background: rgba(15,23,42,.82); }
.image-viewer img { max-width: min(920px,96vw); max-height: 86vh; border-radius: 12px; background: #fff; border: 2px solid black; }
.viewer-close { position: absolute; top: 18px; right: 18px; background: #fff; color: var(--text); padding: 8px 16px; border-radius: 8px; font-weight: 700; }
.gap-sm { gap: 12px; }
@media (max-width: 760px) {
  .info-grid { grid-template-columns: 1fr; }
}
</style>
