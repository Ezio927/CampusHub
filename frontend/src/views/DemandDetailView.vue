<template>
  <div class="page" v-if="demand">
    <a href="#" class="back" @click.prevent="$router.back()">←</a>

    <!-- 状态横幅 -->
    <div class="status-bar" :style="{ background: statusBg(demand.status) }">
      <span class="status-bar-text">{{ statusText(demand.status) }}</span>
      <span v-if="demand.urgent" class="urgent-dot">紧急</span>
    </div>

    <!-- 主卡片 -->
    <div class="main-card">
      <div class="card-head">
        <span class="cat-tag" :style="{ background: categoryColor(demand.category) }"><img :src="'/icons/cat-' + demand.category.toLowerCase() + '.png'" class="cat-icon" alt="" />{{ categoryText(demand.category) }}</span>
        <span class="time-text">{{ demand.createdAt?.substring(0, 10) }}</span>
      </div>

      <h1 class="title">{{ demand.title }}</h1>

      <div class="location-line">
        <span>{{ demand.campus || '不限校区' }}</span>
        <span class="dot">·</span>
        <span>{{ demand.locationTag || '不限位置' }}</span>
      </div>

      <!-- 报酬 — 页面焦点 -->
      <div class="reward-area">
        <div class="reward-number">{{ demand.rewardType === 'MONEY' ? '¥' + (demand.rewardAmount || 0) : (demand.rewardAmount || 0) }}</div>
        <div class="reward-unit">{{ demand.rewardType === 'MONEY' ? '现金报酬' : '额度分报酬' }}</div>
      </div>

      <!-- 分割线 -->
      <div class="divider"></div>

      <!-- 描述 -->
      <div class="desc-block">
        <div class="section-label">需求描述</div>
        <p class="desc-text">{{ demand.description }}</p>
      </div>

      <!-- 图片 -->
      <div v-if="demand.images?.length" class="image-row">
        <img v-for="img in demand.images" :key="img.id" :src="img.imageUrl" alt="需求图片" @click="imgViewer = img.imageUrl" />
      </div>

      <!-- 发布者 -->
      <div class="divider"></div>
      <div class="publisher-row">
        <button type="button" class="pub-avatar" :class="{ clickable: demand.publisher?.avatarUrl }" :style="demand.publisher?.avatarUrl ? {} : { background: avatarColor }"
          @click="openPublisherAvatar" @mouseenter="checkPopoverDir">
          <img v-if="demand.publisher?.avatarUrl" :src="demand.publisher.avatarUrl" alt="头像" />
          <span v-else>{{ userInitial(demand.publisher) }}</span>
        </button>
        <div class="pub-info">
          <div class="pub-name">{{ demand.publisher?.realName || demand.publisher?.username }}</div>
          <div class="pub-meta">信用 {{ demand.publisher?.creditScore ?? '-' }}<span class="dot">·</span>发布 {{ ratingText(demand.publisher?.publisherRating, demand.publisher?.publisherRatingCount) }}</div>
        </div>
        <!-- hover popover -->
        <div class="profile-popover" :class="{ 'popover-top': popoverAbove }">
          <div class="popover-name">{{ demand.publisher?.realName || demand.publisher?.username || '发布者' }}</div>
          <div class="popover-row"><span>信用分</span><strong>{{ demand.publisher?.creditScore ?? '-' }}</strong></div>
          <div class="popover-row"><span>性别</span><strong>{{ genderText(demand.publisher) }}</strong></div>
          <div class="popover-row"><span>发布评分</span><strong>{{ ratingText(demand.publisher?.publisherRating, demand.publisher?.publisherRatingCount) }}</strong></div>
          <div class="popover-row"><span>接单评分</span><strong>{{ ratingText(demand.publisher?.acceptorRating, demand.publisher?.acceptorRatingCount) }}</strong></div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions" v-if="auth.isLoggedIn && demand.publisherId !== auth.user?.id">
      <button class="btn-outline btn-block" @click="messagePublisher">联系发布者</button>
      <button v-if="demand.status === 'OPEN'" class="btn-primary btn-block" @click="accept">立即接单</button>
    </div>
    <div class="actions" v-if="demand.publisherId === auth.user?.id && demand.status === 'OPEN'">
      <button class="btn-danger btn-block" @click="deleteDemand">删除需求</button>
    </div>

    <!-- 图片查看器 -->
    <div v-if="imgViewer" class="image-viewer" @click.self="imgViewer = ''">
      <button type="button" class="viewer-close" @click="imgViewer = ''">✕</button>
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

function categoryText(c) { return { ERRAND:'跑腿代办', SKILL:'技能求助', STUDY:'学习互助', SECONDHAND:'二手交易', OTHER:'其他' }[c] || c }
function categoryColor(c) { return { ERRAND:'#fef08a', SKILL:'#c7d2fe', STUDY:'#bbf7d0', SECONDHAND:'#fecdd3', OTHER:'#e9d5ff' }[c] || '#e5e7eb' }
function statusText(s) { return { OPEN:'开放中', ACCEPTED:'已接单', IN_PROGRESS:'进行中', COMPLETED:'已完成', CANCELLED:'已取消' }[s] || s }
function statusBg(s) { return { OPEN:'#3b82f6', ACCEPTED:'#06b6d4', IN_PROGRESS:'#f97316', COMPLETED:'#10b981', CANCELLED:'#9ca3af' }[s] || '#888' }

function checkPopoverDir(e) {
  const btn = e.currentTarget; const popover = btn.nextElementSibling
  if (!popover) return
  const spaceBelow = window.innerHeight - btn.getBoundingClientRect().bottom
  popover.style.visibility = 'hidden'; popover.style.position = 'fixed'; popover.style.left = '0'
  const h = popover.offsetHeight
  popover.style.visibility = ''; popover.style.position = ''; popover.style.left = ''
  popoverAbove.value = spaceBelow < h + 12
}
function openPublisherAvatar() { if (demand.value?.publisher?.avatarUrl) imgViewer.value = demand.value.publisher.avatarUrl }

async function accept() {
  try { const res = await orderAPI.accept({ demandId: demand.value.id }); router.push('/orders/' + res.data.id) }
  catch (err) { modal.alert('接单失败', errorMessage(err)) }
}
async function deleteDemand() {
  if (!await modal.confirm('删除需求', '确定删除？不可恢复。')) return
  try { await demandAPI.delete(demand.value.id); router.push('/orders') }
  catch (err) { modal.alert('删除失败', errorMessage(err)) }
}
async function messagePublisher() {
  try { const res = await chatAPI.create({ targetUserId: demand.value.publisherId, demandId: demand.value.id }); router.push('/chat/' + res.data.id) }
  catch (err) { modal.alert('发起私信失败', errorMessage(err)) }
}
onMounted(async () => { try { demand.value = (await demandAPI.detail(route.params.id)).data } catch { demand.value = null } })
</script>

<style scoped>
/* 状态横幅 */
.status-bar { margin: 0 auto 16px; max-width: 720px; border-radius: 12px; padding: 12px 20px; border: 2px solid black; box-shadow: 3px 3px 0 rgba(0,0,0,1); display: flex; align-items: center; gap: 10px; }
.status-bar-text { color: #fff; font-size: 15px; font-weight: 900; letter-spacing: 1px; }
.urgent-dot { background: #fef08a; color: #111; padding: 2px 12px; border-radius: 8px; font-size: 12px; font-weight: 700; border: 1.5px solid black; }

/* 主卡片 */
.main-card { background: var(--card); border-radius: 16px; padding: 28px 24px 24px; border: 2px solid black; box-shadow: 4px 4px 0 rgba(0,0,0,1); max-width: 720px; margin: 0 auto 14px; }

.card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.cat-tag { display:inline-flex; align-items:center; gap:5px; padding: 4px 14px; border-radius: 8px; font-size: 12px; font-weight: 700; color: #111; border: 2px solid black; }
.cat-icon{width:16px;height:16px;image-rendering:pixelated}
.time-text { font-size: 13px; color: var(--text-muted); }

.title { font-size: 22px; font-weight: 900; line-height: 1.35; margin: 0 0 20px; }

/* 报酬 — 核心焦点 */
.reward-area { text-align: center; padding: 20px 0 16px; }
.reward-number { font-size: 42px; font-weight: 900; color: var(--danger); line-height: 1; letter-spacing: -1px; }
.reward-unit { font-size: 13px; color: var(--text-muted); margin-top: 4px; font-weight: 600; }

.location-line { font-size: 14px; color: var(--text-secondary); margin-bottom: 16px; }
.location-line .dot { margin: 0 6px; color: var(--text-muted); }

.divider { height: 1px; background: var(--border); margin: 18px 0; }

/* 描述 */
.section-label { font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
.desc-text { white-space: pre-wrap; line-height: 1.75; color: var(--text-secondary); font-size: 15px; margin: 0; }

/* 图片 */
.image-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 14px; }
.image-row img { width: 96px; height: 96px; border-radius: 10px; object-fit: cover; cursor: pointer; border: 2px solid black; transition: transform .12s; }
.image-row img:hover { transform: scale(1.05); }

/* 发布者 */
.publisher-row { display: flex; align-items: center; gap: 12px; position: relative; }
.pub-avatar { width: 42px; height: 42px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #fff; font-size: 17px; border: 2px solid black; padding: 0; overflow: hidden; flex-shrink: 0; background: none; cursor: default; }
.pub-avatar.clickable { cursor: zoom-in; }
.pub-avatar img { width: 100%; height: 100%; object-fit: contain; }
.pub-info { flex: 1; }
.pub-name { font-size: 15px; font-weight: 700; }
.pub-meta { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
.pub-meta .dot { margin: 0 6px; }

/* popover */
.profile-popover { position: absolute; left: 0; top: 52px; z-index: 20; width: 260px; padding: 12px; border: 2px solid black; border-radius: 10px; background: var(--card); box-shadow: 4px 4px 0 rgba(0,0,0,1); opacity: 0; transform: translateY(-4px); pointer-events: none; transition: opacity .14s ease .45s, transform .14s ease .45s; }
.publisher-row:hover .profile-popover { opacity: 1; transform: translateY(0); pointer-events: auto; }
.profile-popover.popover-top { top: auto; bottom: 52px; }
.popover-name { font-size: 15px; font-weight: 900; margin-bottom: 8px; }
.popover-row { display: flex; justify-content: space-between; font-size: 13px; color: var(--text-muted); margin-top: 5px; }
.popover-row strong { color: var(--text); }

/* 操作 */
.actions { display: flex; flex-direction: column; gap: 8px; max-width: 720px; margin: 0 auto; }
.actions :deep(.btn-outline),
.actions :deep(.btn-primary),
.actions .btn-danger { padding: 14px; border-radius: 12px; font-size: 16px; font-weight: 700; border: 2px solid black; box-shadow: 4px 4px 0 rgba(0,0,0,1); }
.actions :deep(.btn-outline):hover,
.actions :deep(.btn-primary):hover,
.actions .btn-danger:hover { transform: translate(2px,2px); box-shadow: none; }
.btn-danger { background: #dc2626; color: #fff; }
.btn-danger:hover { background: #b91c1c; }

/* 图片查看器 */
.image-viewer { position: fixed; inset: 0; z-index: 40; display: flex; align-items: center; justify-content: center; padding: 56px 20px; background: rgba(15,23,42,.85); }
.image-viewer img { max-width: min(920px,96vw); max-height: 86vh; border-radius: 12px; border: 2px solid black; }
.viewer-close { position: absolute; top: 18px; right: 18px; background: #fff; color: #111; width: 40px; height: 40px; border-radius: 50%; font-weight: 700; font-size: 18px; border: 2px solid black; cursor: pointer; display: flex; align-items: center; justify-content: center; }

@media (max-width: 480px) {
  .main-card { padding: 20px 16px; }
  .reward-number { font-size: 36px; }
  .title { font-size: 20px; }
}
</style>
