<template>
  <div v-if="order" class="page">
    <a href="#" class="back" @click.prevent="goBack">←</a>
    <div class="dc">
      <div class="flex-between mb-md">
        <h1 class="text-lg text-bold">{{ order.demandTitle || ('需求 #' + order.demandId) }}</h1>
        <span class="badge" :style="{ background: statusColor(order.status) }">{{ statusText(order.status) }}</span>
      </div>
      <div class="grid-2 mb-md">
        <div class="mi"><span class="lbl">发布者</span><span class="val">{{ order.publisherName || ('ID:' + order.publisherId) }}</span></div>
        <div class="mi"><span class="lbl">接单者</span><span class="val">{{ order.acceptorName || ('ID:' + order.acceptorId) }}</span></div>
        <div class="mi"><span class="lbl">状态</span><span class="val">{{ statusText(order.status) }}</span></div>
        <div class="mi"><span class="lbl">创建时间</span><span class="val text-sm">{{ order.createdAt?.substring(0,10) }}</span></div>
      </div>
    </div>

    <div v-if="showReview" class="review-card">
      <h3>提交评价</h3>
      <div class="star-row">
        <button v-for="i in 5" :key="i" type="button" class="star-btn" :class="{ on: reviewRating >= i }" @click="reviewRating = i">
          {{ reviewRating >= i ? '★' : '☆' }}
        </button>
      </div>
      <textarea v-model="reviewContent" class="review-input" :placeholder="reviewPlaceholder" maxlength="500" rows="2"></textarea>
      <p v-if="reviewErr" class="form-error">{{ reviewErr }}</p>
      <button class="btn-primary btn-block" :disabled="reviewSubmitting" @click="submitReviewAction">
        {{ reviewSubmitting ? '提交中...' : '提交评价' }}
      </button>
    </div>

    <div v-if="reviews.length" class="review-card">
      <h3>订单评价</h3>
      <div v-for="item in reviews" :key="item.id" class="review-item">
        <div class="flex-between">
          <span class="review-stars">{{ '★'.repeat(item.rating) }}{{ '☆'.repeat(5 - item.rating) }}</span>
          <span class="text-sm text-muted">{{ item.createdAt?.substring(0, 10) }}</span>
        </div>
        <p v-if="item.content" class="mt-sm">{{ item.content }}</p>
      </div>
    </div>

    <div class="acts">
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
const showReview = ref(false)
const reviewRating = ref(0)
const reviewContent = ref('')
const reviewErr = ref('')
const reviewSubmitting = ref(false)
const reviews = ref([])
const reviewPlaceholder = '\u5199\u4E0B\u4F60\u7684\u8BC4\u4EF7\uFF08\u9009\u586B\uFF09'

function statusText(status) {
  return {
    PENDING_ACCEPT: '待接单',
    ACCEPTED: '已接单',
    PENDING_CONFIRM: '待确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }[status] || status
}

function statusColor(status) {
  return {
    PENDING_ACCEPT: '#3b82f6',
    ACCEPTED: '#06b6d4',
    PENDING_CONFIRM: '#eab308',
    COMPLETED: '#10b981',
    CANCELLED: '#9ca3af'
  }[status] || '#888'
}

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

const targetUserId = computed(() => {
  if (!order.value) return null
  const uid = auth.user?.id
  if (order.value.publisherId === uid) return order.value.acceptorId
  if (order.value.acceptorId === uid) return order.value.publisherId
  return null
})

const canAppeal = computed(() => {
  if (!order.value) return false
  const uid = auth.user?.id
  return uid && [order.value.publisherId, order.value.acceptorId].includes(uid)
})

async function refresh() {
  const res = await orderAPI.detail(route.params.id)
  order.value = res.data
  await loadReviews()
}

async function loadReviews() {
  try {
    const res = await reviewAPI.byOrder(route.params.id)
    reviews.value = res.data || []
  } catch {
    reviews.value = []
  }
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
      showReview.value = true
      await refresh()
      return
    }
    if (key === 'cancel') {
      const reason = await modal.prompt('取消订单', '请输入取消原因（必填）', '请填写取消原因')
      if (!reason || !reason.trim()) {
        await modal.alert('原因不能为空', '请填写取消原因。')
        return
      }
      await orderAPI.cancel(order.value.id, { reason: reason.trim() })
      await refresh()
    }
  } catch (e) {
    modal.alert('操作失败', errorMessage(e))
  }
}

async function submitReviewAction() {
  reviewErr.value = ''
  if (reviewRating.value < 1 || reviewRating.value > 5) {
    reviewErr.value = '请选择评分'
    return
  }
  if (reviewRating.value <= 3 && (!reviewContent.value || reviewContent.value.trim().length < 15)) {
    reviewErr.value = '差评需要至少 15 个字说明原因'
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
      imageUrls: []
    })
    router.push('/reviews/' + revieweeId)
  } catch (e) {
    reviewErr.value = errorMessage(e)
  } finally {
    reviewSubmitting.value = false
  }
}

async function messagePeer() {
  if (!targetUserId.value) {
    modal.alert('发起私信失败', '对方用户信息缺失，暂时无法发起私信。')
    return
  }
  try {
    const res = await chatAPI.create({ targetUserId: targetUserId.value, demandId: order.value.demandId })
    if (!res.data?.id) throw new Error('会话创建失败')
    router.push('/chat/' + res.data.id)
  } catch (e) {
    modal.alert('发起私信失败', errorMessage(e))
  }
}

async function submitOrderAppeal() {
  const reason = await modal.prompt('订单纠纷申诉', '请填写申诉理由（必填）', '请说明订单争议情况，管理员会进行处理。')
  if (!reason || !reason.trim()) {
    await modal.alert('理由不能为空', '请填写申诉理由。')
    return
  }
  try {
    await arbitrationAPI.create({ orderId: order.value.id, reason: reason.trim(), evidenceUrls: [] })
    modal.alert('提交成功', '订单纠纷申诉已提交，请等待管理员处理。')
  } catch (e) {
    modal.alert('提交失败', errorMessage(e))
  }
}

function goBack() {
  const fromRole = route.query.role
  if (fromRole === 'PUBLISHER' || fromRole === 'ACCEPTOR') {
    router.push({ path: '/orders', query: { role: fromRole } })
    return
  }
  router.back()
}

onMounted(refresh)
</script>

<style scoped>
.dc{background:#fff;border-radius:12px;padding:24px;margin-bottom:14px;border:3px solid black;box-shadow:6px 6px 0 rgba(0,0,0,1);max-width:680px;margin-left:auto;margin-right:auto}
.mi{padding:12px;background:#f8fafc;border-radius:10px;display:flex;flex-direction:column;gap:4px;border:2px solid black}
.lbl{font-size:12px;color:var(--text-muted);font-weight:500}
.val{font-size:16px;font-weight:700}
.acts{display:flex;flex-direction:column;gap:8px;max-width:680px;margin-left:auto;margin-right:auto}
.acts button{padding:14px;border-radius:10px;font-size:15px;font-weight:700}
.review-card{background:#fff;border-radius:12px;padding:20px 24px;margin-bottom:14px;border:3px solid black;box-shadow:6px 6px 0 rgba(0,0,0,1);max-width:680px;margin-left:auto;margin-right:auto}
.review-card h3{font-size:15px;font-weight:900;margin:0 0 12px}
.review-item{padding:10px 0;border-top:1px solid var(--border)}
.review-item:first-of-type{border-top:none;padding-top:0}
.review-stars{font-size:16px;letter-spacing:2px;color:#f59e0b;font-weight:700}
.star-row{display:flex;gap:4px;margin-bottom:12px}
.star-btn{background:none;border:none;font-size:28px;cursor:pointer;color:#d1d5db;padding:0;transition:color .12s}
.star-btn.on{color:#f59e0b}
.star-btn:hover{color:#f59e0b}
.review-input{width:100%;padding:10px 12px;border:2px solid black;border-radius:10px;font-size:14px;resize:vertical;margin-bottom:10px;box-sizing:border-box}
.review-input:focus{border-color:var(--primary);outline:none}
.form-error{color:var(--danger);font-size:13px;margin-bottom:8px}
</style>
