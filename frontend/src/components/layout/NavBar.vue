<template>
  <header class="nav">
    <div class="nav-inner">
      <a href="/" class="nav-logo" @click.prevent="goHome"><img src="/logo.png" alt="" class="logo-img" />CampusHub</a>
      <nav class="nav-links">
        <router-link to="/" class="nav-link" exact-active-class="active">需求大厅</router-link>
        <router-link v-if="auth.isLoggedIn" to="/demands/publish" class="nav-link" active-class="active">发布需求</router-link>
        <router-link v-if="auth.isLoggedIn" to="/orders" class="nav-link" active-class="active">我的订单</router-link>
        <router-link v-if="auth.isLoggedIn" to="/messages" class="nav-link nav-msg" active-class="active">
          我的消息
          <span v-if="messageUnread" class="dot">{{ messageUnread }}</span>
        </router-link>
        <router-link v-if="auth.isLoggedIn" to="/notifications" class="nav-link nav-msg" active-class="active">
          系统通知
          <span v-if="notifUnread" class="dot">{{ notifUnread }}</span>
        </router-link>
        <router-link v-if="auth.isLoggedIn" to="/profile" class="nav-link" active-class="active">个人中心</router-link>
        <router-link v-if="auth.isLoggedIn && auth.user?.role==='ADMIN'" to="/admin" class="nav-link" active-class="active">管理后台</router-link>
      </nav>
      <div class="nav-right">
        <button class="theme-btn" @click="theme.toggle()" :title="theme.isDark() ? '切换亮色' : '切换暗色'">{{ theme.isDark() ? '☾' : '☀' }}</button>
        <template v-if="auth.isLoggedIn">
          <router-link to="/profile" class="nav-user" :style="{background:avatarColor}">{{ auth.user?.username?.[0]?.toUpperCase() }}</router-link>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-primary btn-sm">登录</router-link>
        </template>
      </div>
    </div>
  </header>
</template>
<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotifStore } from '@/stores/notifications'
import { chatAPI } from '@/api/chat'
import { useTheme } from '@/composables/useTheme'
const theme = useTheme()
const auth = useAuthStore()
const notif = useNotifStore()
const router = useRouter()
const route = useRoute()
const unread = ref(0)
const messageUnread = computed(() => unread.value > 99 ? '99+' : unread.value)
const notifUnread = computed(() => { const c = notif.unread; return c > 99 ? '99+' : (c || '') })
function goHome() {
  if (route.path === '/') router.go(0)
  else router.push('/')
}
const colors = ['#6366f1','#8b5cf6','#ec4899','#f43f5e','#f97316','#14b8a6']
const avatarColor = computed(() => colors[(auth.user?.username?.charCodeAt(0)||0) % colors.length])
async function fetchMessageUnread() {
  if (!auth.isLoggedIn) { unread.value = 0; return }
  try { const res = await chatAPI.unread(); unread.value = res.data || 0 } catch { unread.value = 0 }
}
onMounted(() => { fetchMessageUnread(); if (auth.isLoggedIn) notif.fetchUnread() })
watch(() => route.fullPath, () => { fetchMessageUnread(); if (auth.isLoggedIn) notif.fetchUnread() })
</script>
<style scoped>
.nav { position: sticky; top: 0; z-index: 100; background: var(--card); border-bottom: 3px solid black; }
.nav-inner { width: 80%; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; height: 56px; }
.nav-logo { font-size: 20px; font-weight: 900; color: var(--primary) !important; flex-shrink: 0; display:flex; align-items:center; gap:8px; }
.logo-img { width:28px; height:28px; object-fit:contain; }
.nav-links { display: flex; align-items: center; gap: 4px; flex: 1; justify-content: center; }
.nav-link {
  color: var(--text-secondary); font-size: 15px; font-weight: 700;
  padding: 8px 14px; border-radius: 10px; transition: all 0.2s ease;
}
.nav-link:hover { background: var(--border); color: var(--text); }
.nav-link.active { background: var(--primary); color: #fff; }
.nav-msg { position: relative; display: inline-flex; align-items: center; gap: 4px; }
.dot { min-width: 18px; height: 18px; padding: 0 5px; border-radius: 999px; background: var(--danger); color: #fff; font-size: 11px; line-height: 18px; text-align: center; font-weight: 800; }
.nav-right { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.theme-btn { width:34px; height:34px; padding:0; border-radius:50%; background:var(--surface); font-size:16px; display:flex; align-items:center; justify-content:center; box-shadow:2px 2px 0px rgba(0,0,0,1); }
.theme-btn:hover { transform:translate(1px,1px); box-shadow:none; }
.nav-user { width: 34px; height: 34px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #fff; font-size: 15px; border: 2px solid black; flex-shrink: 0; }
</style>
