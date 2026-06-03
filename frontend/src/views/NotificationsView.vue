<template>
  <div class="page">
    <div class="flex-between mb-md">
      <h1 class="page-title" style="margin:0">通知</h1>
      <button v-if="store.list.length" class="btn-outline btn-sm" @click="markAll">全部已读</button>
    </div>
    <div v-if="store.list.length===0" class="empty"><p class="mt-sm">暂无通知</p></div>
    <div v-for="n in store.list" :key="n.id" class="ni" :class="{unread:!n.read,expanded:expandedId===n.id}" @click="toggle(n)">
      <div class="ni-head">
        <span class="ni-title">{{ n.title }}</span>
        <span class="ni-time">{{ fmt(n.createdAt) }}</span>
      </div>
      <p class="ni-content" :class="{clamped:expandedId!==n.id}">{{ n.content }}</p>
      <div v-if="expandedId===n.id" class="ni-actions">
        <router-link v-if="linkTo(n)" :to="linkTo(n)" class="btn-outline btn-sm">查看详情</router-link>
        <button class="btn-outline btn-sm" @click.stop="expandedId=null">收起</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useNotifStore } from '@/stores/notifications'
const store = useNotifStore()
const router = useRouter()
const expandedId = ref(null)
function fmt(t) { if (!t) return ''; return t.substring(0,16)?.replace('T',' ') }
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
.ni{padding:16px 20px;background:#fff;border-radius:12px;margin-bottom:10px;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1);cursor:pointer;transition:all 0.2s ease}
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
