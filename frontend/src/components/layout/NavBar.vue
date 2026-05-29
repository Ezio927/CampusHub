<template>
  <header class="nav">
    <div class="nav-inner">
      <a href="/" class="nav-logo" @click.prevent="goHome">CampusHub</a>
      <div class="nav-right">
        <template v-if="auth.isLoggedIn">
          <router-link to="/orders" class="nav-link">订单</router-link>
          <router-link v-if="auth.user?.role==='ADMIN'" to="/admin" class="nav-link">管理</router-link>
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
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
function goHome() {
  if (route.path === '/') router.go(0)
  else router.push('/')
}
const colors = ['#6366f1','#8b5cf6','#ec4899','#f43f5e','#f97316','#14b8a6']
const avatarColor = computed(() => colors[(auth.user?.username?.charCodeAt(0)||0) % colors.length])
</script>
<style scoped>
.nav { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,.92); backdrop-filter: blur(12px); border-bottom: 1px solid var(--border); }
.nav-inner { max-width: 1100px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; padding: 0 16px; height: 56px; }
.nav-logo { font-size: 20px; font-weight: 800; color: var(--primary) !important; }
.nav-right { display: flex; align-items: center; gap: 12px; }
.nav-link { color: var(--text-secondary); font-size: 14px; font-weight: 500; }
.nav-link:hover { color: var(--primary); }
.nav-user { width: 34px; height: 34px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #fff; font-size: 15px; }
</style>
