<template>
  <div class="page">
    <div class="flex-between mb-md">
      <h1 class="page-title" style="margin:0">通知</h1>
      <button v-if="store.list.length" class="btn-outline btn-sm" @click="markAll">全部已读</button>
    </div>
    <div v-if="store.list.length===0" class="empty"><p class="mt-sm">暂无通知</p></div>
    <div v-for="n in store.list" :key="n.id" class="ni" :class="{unread:!n.read,expanded:expandedId===n.id}" @click="toggle(n)">
      <div class="ni-head">
        <span class="ni-title">{{ displayTitle(n) }}</span>
        <span class="ni-time">{{ fmt(n.createdAt) }}</span>
      </div>
      <p class="ni-content" :class="{clamped:expandedId!==n.id}">{{ displayContent(n) }}</p>
      <div v-if="expandedId===n.id" class="ni-actions">
        <router-link v-if="linkTo(n)" :to="linkTo(n)" class="btn-outline btn-sm">查看详情</router-link>
        <button class="btn-outline btn-sm" @click.stop="expandedId=null">收起</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useNotifStore } from '@/stores/notifications'
const store = useNotifStore()
const expandedId = ref(null)
const titleMap = {
  'Appeal created': '申诉已提交',
  'Appeal resolved': '申诉处理结果',
  'Demand accepted': '需求已被接单',
  'New review received': '收到新评价',
  'New message report': '新的私信举报',
  'Message report reviewed': '私信举报处理结果',
  'Account banned': '账号已封禁',
  'Account unbanned': '账号已解封',
  'Ban appeal approved': '封禁申诉已通过',
  'Ban appeal rejected': '封禁申诉已驳回',
  'Certification submitted': '实名认证已提交',
  'Certification approved': '实名认证已通过',
  'Certification rejected': '实名认证已驳回',
  'Order pending confirmation': '订单待确认',
  'Order completed': '订单已完成',
  'Order cancelled': '订单已取消'
}
const contentMap = {
  'Your appeal has been submitted and will be reviewed soon.': '你的申诉已提交，管理员将尽快审核。',
  'Your demand has been accepted.': '你的需求已被接单，请及时关注订单进度。',
  'You have received a new review.': '你收到了一条新的评价。',
  'A private message report is waiting for review.': '有一条私信举报等待处理。',
  'Your message report has been reviewed.': '你的私信举报已处理完成。',
  'Your account has been banned.': '你的账号已被封禁。',
  'Your account has been restored.': '你的账号已恢复正常。',
  'Your ban appeal has been approved and your account is active again.': '你的封禁申诉已通过，账号已恢复正常。',
  'Your ban appeal has been rejected.': '你的封禁申诉已被驳回。',
  'Your certification request has been submitted.': '你的实名认证申请已提交，请等待审核。',
  'Your certification has been approved.': '你的实名认证已通过。',
  'Your certification has been rejected.': '你的实名认证已被驳回。',
  'The order is delivered and waiting for your confirmation.': '订单已完成，正在等待你确认。',
  'The order has been confirmed by the publisher.': '发布方已确认订单完成。',
  'The order has been cancelled.': '订单已取消。',
  'Admin deleted demand': '管理员删除了该需求。',
  'Arbitration marked breach': '仲裁判定存在违约行为。',
  'Malicious appeal': '系统判定为恶意申诉。'
}
function fmt(t) { if (!t) return ''; return t.substring(0,16)?.replace('T',' ') }
function displayTitle(n) {
  return titleMap[n?.title] || n?.title || ''
}
function displayContent(n) {
  const raw = n?.content || ''
  return contentMap[raw] || raw
}
function toggle(n) {
  if (!n.read) store.markRead(n.id)
  expandedId.value = expandedId.value === n.id ? null : n.id
}
function linkTo(n) {
  if ((n.type === 'ORDER_ACCEPTED' || n.type === 'ORDER_COMPLETED' || n.type === 'ORDER_CANCELLED' || n.type === 'ARBITRATION_UPDATE') && n.relatedId) {
    return '/orders/' + n.relatedId
  }
  return null
}
async function markAll() { await store.markAllRead(); store.list.forEach(n => n.read = true) }
onMounted(() => store.fetchList())
</script>
<style scoped>
.page{max-width:680px}
.ni{padding:16px 20px;background:var(--card);border-radius:12px;margin-bottom:10px;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1);cursor:pointer;transition:all 0.2s ease}
.ni:hover{transform:translateY(-2px)}
.ni.expanded{box-shadow:8px 8px 0px rgba(0,0,0,1)}
.ni.unread{border-left:5px solid var(--primary)}
.ni-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:6px}
.ni-title{font-size:15px;font-weight:700;color:var(--text)}
.ni.unread .ni-title{color:var(--primary)}
.ni-time{font-size:12px;color:var(--text-muted);white-space:nowrap}
.ni-content{font-size:14px;color:var(--text-secondary);line-height:1.6;margin:0;white-space:pre-wrap}
.ni-content.clamped{display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}
.ni-actions{display:flex;gap:8px;margin-top:12px;padding-top:12px;border-top:2px solid #e5e7eb}
</style>
