<template>
  <aside class="sidebar">
    <div class="sb-top">
      <a href="/" class="sb-logo" @click.prevent="goHome">CampusHub</a>
    </div>
    <nav class="sb-nav">
      <router-link to="/" class="sb-link" exact-active-class="active">需求大厅</router-link>
      <router-link v-if="auth.isLoggedIn" to="/demands/publish" class="sb-link">发布需求</router-link>
      <router-link v-if="auth.isLoggedIn" to="/orders" class="sb-link">我的订单</router-link>
      <router-link v-if="auth.isLoggedIn" to="/messages" class="sb-link sb-msg">
        消息
        <span v-if="messageUnread" class="sb-dot">{{ messageUnread }}</span>
      </router-link>
      <router-link v-if="auth.isLoggedIn" to="/profile" class="sb-link">个人中心</router-link>
      <router-link v-if="auth.isLoggedIn && auth.user?.role==='ADMIN'" to="/admin" class="sb-link">管理后台</router-link>
    </nav>
    <div class="sb-bottom">
      <template v-if="auth.isLoggedIn">
        <router-link to="/profile" class="sb-user">
          <span class="sb-avatar" :style="{background:avatarColor}">{{ auth.user?.username?.[0]?.toUpperCase() }}</span>
          <span class="sb-name">{{ auth.user?.username }}</span>
        </router-link>
      </template>
      <template v-else>
        <router-link to="/login" class="btn-primary btn-block">登录</router-link>
      </template>
    </div>
  </aside>
</template>
<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { chatAPI } from '@/api/chat'
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const unread = ref(0)
const messageUnread = computed(() => unread.value > 99 ? '99+' : unread.value)
function goHome() {
  if (route.path === '/') router.go(0)
  else router.push('/')
}
const colors = ['#2563EB','#7c3aed','#db2777','#dc2626','#ea580c','#0891b2']
const avatarColor = computed(() => colors[(auth.user?.username?.charCodeAt(0)||0) % colors.length])
async function fetchMessageUnread() {
  if (!auth.isLoggedIn) { unread.value = 0; return }
  try { const res = await chatAPI.unread(); unread.value = res.data || 0 } catch { unread.value = 0 }
}
onMounted(fetchMessageUnread)
watch(() => route.fullPath, fetchMessageUnread)
</script>
<style scoped>
.sidebar {
  width: 240px;
  min-height: 100vh;
  background: #fff;
  border-right: 3px solid black;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 50;
}
.sb-top {
  padding: 20px 20px 16px;
  border-bottom: 3px solid black;
}
.sb-logo { font-size: 20px; font-weight: 900; color: var(--primary) !important; }
.sb-nav {
  flex: 1;
  padding: 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.sb-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-secondary);
  transition: all 0.2s ease;
}
.sb-link:hover { background: #f1f5f9; color: var(--text); }
.sb-link.active { background: var(--primary); color: #fff; }
.sb-msg { position: relative; }
.sb-dot {
  min-width: 20px; height: 20px; padding: 0 6px;
  border-radius: 999px; background: var(--danger); color: #fff;
  font-size: 11px; line-height: 20px; text-align: center; font-weight: 800;
}
.sb-bottom {
  padding: 16px 20px;
  border-top: 3px solid black;
}
.sb-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.2s ease;
}
.sb-user:hover { background: #f1f5f9; }
.sb-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; color: #fff; font-size: 16px;
  border: 2px solid black;
  flex-shrink: 0;
}
.sb-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
