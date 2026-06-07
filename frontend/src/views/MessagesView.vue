<template>
  <div class="page">
    <h1 class="page-title">私信</h1>
    <div v-if="list.length===0" class="empty">
      <p class="mt-sm">暂无私信</p>
    </div>
    <button v-for="c in list" :key="c.id" class="conv" @click="open(c.id)">
      <span class="avatar" :class="{hasAvatar:c.targetUser?.avatarUrl}" :style="c.targetUser?.avatarUrl ? {} : { background: avatarBg(c.targetUser) }" @click="handleAvatarClick($event,c.targetUser?.avatarUrl)">
        <img v-if="c.targetUser?.avatarUrl" :src="c.targetUser.avatarUrl" alt="对方头像" />
        <span v-else>{{ userInitial(c.targetUser) }}</span>
      </span>
      <div class="conv-main">
        <div class="flex-between">
          <span class="conv-name">{{ c.targetUser?.username || '用户 #' + c.id }}</span>
          <span class="text-sm text-muted">{{ formatTime(c.lastMessageAt) }}</span>
        </div>
        <div class="conv-context" v-if="c.relatedDemandTitle">{{ c.relatedDemandTitle }}</div>
        <div class="conv-state" v-if="c.blockedByTarget">对方已拉黑你</div>
        <div class="conv-state" v-else-if="c.blockedByMe">你已拉黑对方</div>
        <div class="conv-msg">{{ c.lastMessageContent || '还没有消息，开始沟通吧' }}</div>
      </div>
      <span v-if="c.unreadCount" class="unread">{{ c.unreadCount }}</span>
    </button>
    <div v-if="avatarViewer" class="image-viewer" @click.self="avatarViewer=''">
      <button type="button" class="viewer-close" @click="avatarViewer=''">关闭</button>
      <img :src="avatarViewer" alt="头像" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { chatAPI } from '@/api/chat'
import { userInitial } from '@/utils/userDisplay'

const router = useRouter()
const list = ref([])
const avatarViewer = ref('')
const clrs = ['#6366f1','#8b5cf6','#ec4899','#f43f5e','#f97316','#14b8a6']
function avatarBg(user) { return clrs[(user?.username?.charCodeAt(0) || 0) % clrs.length] }

async function load() {
  try { const res = await chatAPI.list(); list.value = res.data || [] } catch { list.value = [] }
}

function open(id) {
  router.push('/chat/' + id)
}

function handleAvatarClick(event, src) {
  if (!src) return
  event.stopPropagation()
  openAvatar(src)
}

function openAvatar(src) {
  if (src) avatarViewer.value = src
}

function formatTime(value) {
  if (!value) return ''
  return value.substring(0, 16).replace('T', ' ')
}

onMounted(load)
</script>

<style scoped>
.conv {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 16px;
  margin-bottom: 10px;
  background: var(--card);
  border: 2px solid black;
  border-radius: 12px;
  box-shadow: 4px 4px 0px rgba(0,0,0,1);
  text-align: left;
  transition: all 0.2s ease;
}
.conv:hover {
  transform: translate(-1px, -2px);
  box-shadow: 6px 6px 0px rgba(0,0,0,1);
}
.avatar {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  flex-shrink: 0;
  border: 2px solid black;
  overflow: hidden;
}
.avatar.hasAvatar { background: #fff; cursor: zoom-in; }
.avatar img { width: 100%; height: 100%; object-fit: contain; display: block; }
.conv-main {
  flex: 1;
  min-width: 0;
}
.conv-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
}
.conv-context {
  display: inline-flex;
  max-width: 100%;
  margin-top: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--primary-light);
  color: var(--primary);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conv-state {
  margin-top: 4px;
  color: var(--danger);
  font-size: 12px;
}
.conv-msg {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.unread {
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--danger);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-viewer{position:fixed;inset:0;z-index:40;display:flex;align-items:center;justify-content:center;padding:56px 20px;background:rgba(15,23,42,.82)}
.image-viewer img{max-width:min(920px,96vw);max-height:86vh;border-radius:12px;background:#fff;border:2px solid black}
.viewer-close{position:absolute;top:18px;right:18px;background:#fff;color:var(--text);padding:8px 16px;border-radius:8px;font-weight:700}
</style>
