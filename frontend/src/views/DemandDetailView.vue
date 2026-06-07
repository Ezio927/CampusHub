<template>
  <div class="page" v-if="demand">
    <a href="#" class="back" @click.prevent="$router.back()">←</a>
    <div class="detail-card">
      <div class="flex-between mb-md gap-sm">
        <h1>{{ demand.title }}</h1>
        <span class="category" :style="{ background: categoryColor(demand.category) }">{{ categoryText(demand.category) }}</span>
      </div>
      <div class="info-grid mb-md">
        <div class="info-item"><span class="label">报酬</span><span class="value reward">{{ rewardText(demand) }}</span></div>
        <div class="info-item"><span class="label">校区</span><span class="value">{{ demand.campus || '不限' }}</span></div>
        <div class="info-item"><span class="label">位置</span><span class="value">{{ demand.locationTag || '不限' }}</span></div>
        <div class="info-item"><span class="label">状态</span><span class="value"><span class="badge" :style="{ background: statusColor(demand.status) }">{{ statusText(demand.status) }}</span></span></div>
        <div class="info-item"><span class="label">时间</span><span class="value text-sm">{{ demand.createdAt?.substring(0, 10) }}</span></div>
      </div>
      <div class="description">{{ demand.description }}</div>
      <div v-if="demand.images?.length" class="detail-images">
        <img v-for="img in demand.images" :key="img.id" :src="img.imageUrl" alt="需求图片" @click="imgViewer = img.imageUrl" />
      </div>

      <div class="publisher">
        <div class="publisher-avatar-wrap">
          <button type="button" class="avatar" :class="{ hasAvatar: demand.publisher?.avatarUrl }" :style="demand.publisher?.avatarUrl ? {} : { background: avatarColor }" @click="openPublisherAvatar" @mouseenter="checkPopoverDir">
            <img v-if="demand.publisher?.avatarUrl" :src="demand.publisher.avatarUrl" alt="发布者头像" />
            <span v-else>{{ userInitial(demand.publisher) }}</span>
          </button>
          <div class="profile-popover" :class="{ 'popover-top': popoverAbove }">
            <div class="popover-name">{{ demand.publisher?.realName || demand.publisher?.username || '发布者' }}</div>
            <div class="popover-row"><span>信用分</span><strong>{{ demand.publisher?.creditScore ?? '-' }}</strong></div>
            <div class="popover-row"><span>性别</span><strong>{{ genderText(demand.publisher) }}</strong></div>
            <div class="popover-row"><span>发布评分</span><strong>{{ ratingText(demand.publisher?.publisherRating, demand.publisher?.publisherRatingCount) }}</strong></div>
            <div class="popover-row"><span>接单评分</span><strong>{{ ratingText(demand.publisher?.acceptorRating, demand.publisher?.acceptorRatingCount) }}</strong></div>
          </div>
        </div>
        <div>
          <div class="text-bold">{{ demand.publisher?.username }}</div>
          <div class="text-sm text-muted">信用 {{ demand.publisher?.creditScore }} · 发布评分 {{ ratingText(demand.publisher?.publisherRating, demand.publisher?.publisherRatingCount) }}</div>
        </div>
      </div>
    </div>

    <div v-if="auth.isLoggedIn && demand.publisherId !== auth.user?.id" class="action-list">
      <button class="btn-outline btn-block action-btn" @click="messagePublisher">联系发布者</button>
      <button v-if="demand.status === 'OPEN'" class="btn-primary btn-block action-btn" @click="accept">立即接单</button>
    </div>
    <div v-if="demand.publisherId === auth.user?.id && demand.status === 'OPEN'" class="action-list">
      <button class="delete-btn btn-block action-btn" @click="deleteDemand">删除需求</button>
    </div>

    <div v-if="imgViewer" class="image-viewer" @click.self="imgViewer = ''">
      <button type="button" class="viewer-close" @click="imgViewer = ''">关闭</button>
      <img :src="imgViewer" alt="查看大图" />
    </div>
  </div>
  <div v-else class="empty">加载中...</div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { demandAPI } from '@/api/demands'
import { orderAPI } from '@/api/orders'
import { chatAPI } from '@/api/chat'
import { errorMessage } from '@/utils/errors'
import { genderText, ratingText, userInitial } from '@/utils/userDisplay'
import { useModal } from '@/composables/useModal'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const modal = useModal()
const demand = ref(null)
const imgViewer = ref('')
const popoverAbove = ref(false)
const colors = ['#2563eb', '#7c3aed', '#db2777', '#dc2626', '#ea580c']
const avatarColor = computed(() => colors[(demand.value?.publisher?.username?.charCodeAt(0) || 0) % colors.length])

function categoryText(category) {
  return { ERRAND: '跑腿代办', SKILL: '技能求助', STUDY: '学习互助', SECONDHAND: '二手交易', OTHER: '其他' }[category] || category
}

function categoryColor(category) {
  return { ERRAND: '#fef08a', SKILL: '#c7d2fe', STUDY: '#bbf7d0', SECONDHAND: '#fecdd3', OTHER: '#e9d5ff' }[category] || '#e5e7eb'
}

function statusText(status) {
  return { OPEN: '开放中', ACCEPTED: '已接单', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }[status] || status
}

function statusColor(status) {
  return { OPEN: '#3b82f6', ACCEPTED: '#06b6d4', IN_PROGRESS: '#f97316', COMPLETED: '#10b981', CANCELLED: '#9ca3af' }[status] || '#888'
}

function rewardText(item) {
  return item.rewardType === 'MONEY' ? `¥${item.rewardAmount || 0}` : `${item.rewardAmount || 0}额度分`
}

function checkPopoverDir(e) {
  const btn = e.currentTarget
  const popover = btn.nextElementSibling
  if (!popover) return
  const btnRect = btn.getBoundingClientRect()
  const spaceBelow = window.innerHeight - btnRect.bottom
  popover.style.visibility = 'hidden'
  popover.style.position = 'fixed'
  popover.style.left = '0'
  const popoverHeight = popover.offsetHeight
  popover.style.visibility = ''
  popover.style.position = ''
  popover.style.left = ''
  popoverAbove.value = spaceBelow < popoverHeight + 12
}

function openPublisherAvatar() {
  if (demand.value?.publisher?.avatarUrl) imgViewer.value = demand.value.publisher.avatarUrl
}

async function accept() {
  try {
    const res = await orderAPI.accept({ demandId: demand.value.id })
    router.push('/orders/' + res.data.id)
  } catch (err) {
    modal.alert('接单失败', errorMessage(err))
  }
}

async function deleteDemand() {
  const ok = await modal.confirm('删除需求', '确定删除该需求？删除后不可恢复。')
  if (!ok) return
  try {
    await demandAPI.delete(demand.value.id)
    router.push('/orders')
  } catch (err) {
    modal.alert('删除失败', errorMessage(err))
  }
}

async function messagePublisher() {
  if (!demand.value?.publisherId) {
    modal.alert('发起私信失败', '发布者信息缺失，暂时无法发起私信')
    return
  }
  try {
    const res = await chatAPI.create({ targetUserId: demand.value.publisherId, demandId: demand.value.id })
    if (!res.data?.id) throw new Error('会话创建失败')
    router.push('/chat/' + res.data.id)
  } catch (err) {
    modal.alert('发起私信失败', errorMessage(err))
  }
}

onMounted(async () => {
  try { const res = await demandAPI.detail(route.params.id); demand.value = res.data } catch { demand.value = null }
})
</script>

<style scoped>
.detail-card { background: var(--card); border-radius: 12px; padding: 24px; margin-bottom: 14px; border: 2px solid black; box-shadow: 4px 4px 0 rgba(0,0,0,1); max-width: 720px; margin-left: auto; margin-right: auto; }
h1 { font-size: 22px; font-weight: 900; flex: 1; margin-right: 12px; }
.category { display: inline-flex; padding: 5px 14px; border-radius: 10px; font-size: 12px; font-weight: 700; color: #111; border: 2px solid black; }
.info-grid { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 12px; }
.info-item { padding: 12px; background: var(--surface);border-radius: 10px; display: flex; flex-direction: column; gap: 4px; border: 2px solid black; }
.label { font-size: 12px; color: var(--text-muted); font-weight: 500; }
.value { font-size: 16px; font-weight: 700; }
.reward { color: var(--danger); }
.description { white-space: pre-wrap; line-height: 1.8; color: var(--text-secondary); padding: 16px; background: var(--surface); border-radius: 10px; margin: 16px 0; border: 2px solid black; }
.detail-images { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.detail-images img { width: 100px; height: 100px; border-radius: 10px; object-fit: contain; cursor: pointer; border: 2px solid black; }
.detail-images img:hover { opacity: .85; }
.publisher { display: flex; align-items: center; gap: 12px; padding: 16px; background: var(--surface); border-radius: 10px; border: 2px solid black; }
.publisher-avatar-wrap { position: relative; flex-shrink: 0; }
.avatar { width: 44px; height: 44px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #fff; font-size: 18px; border: 2px solid black; padding: 0; overflow: hidden; }
.avatar.hasAvatar { background: #fff; cursor: zoom-in; }
.avatar img { width: 100%; height: 100%; object-fit: contain; }
.profile-popover { position: absolute; left: 0; top: 54px; z-index: 20; width: 260px; padding: 12px; border: 2px solid black; border-radius: 10px; background: var(--card); box-shadow: 4px 4px 0 rgba(0,0,0,1); opacity: 0; transform: translateY(-4px); pointer-events: none; transition: opacity .14s ease .45s, transform .14s ease .45s; }
.publisher-avatar-wrap:hover .profile-popover { opacity: 1; transform: translateY(0); pointer-events: auto; }
.profile-popover.popover-top { top: auto; bottom: 54px; }
.popover-name { font-size: 15px; font-weight: 900; margin-bottom: 8px; word-break: break-word; }
.popover-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; font-size: 13px; color: var(--text-muted); margin-top: 5px; }
.popover-row strong { color: var(--text); text-align: right; }
.action-list { display: flex; flex-direction: column; gap: 8px; max-width: 720px; margin-left: auto; margin-right: auto; }
.action-btn { padding: 14px; border-radius: 10px; font-size: 16px; }
.delete-btn { background: #dc2626; color: #fff; border: 2px solid black; box-shadow: 4px 4px 0 rgba(0,0,0,1); }
.delete-btn:hover { background: #b91c1c; transform: translate(1px,1px); box-shadow: none; }
.image-viewer { position: fixed; inset: 0; z-index: 40; display: flex; align-items: center; justify-content: center; padding: 56px 20px; background: rgba(15,23,42,.82); }
.image-viewer img { max-width: min(920px,96vw); max-height: 86vh; border-radius: 12px; background: #fff; border: 2px solid black; }
.viewer-close { position: absolute; top: 18px; right: 18px; background: #fff; color: var(--text); padding: 8px 16px; border-radius: 8px; font-weight: 700; }
.gap-sm { gap: 12px; }
@media (max-width: 760px) {
  .info-grid { grid-template-columns: 1fr; }
  .profile-popover { left: -8px; width: 240px; }
}
</style>
