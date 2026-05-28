<template>
  <header class="navbar">
    <router-link to="/" class="logo">CampusHub</router-link>
    <div class="nav-right">
      <template v-if="auth.isLoggedIn">
        <router-link to="/orders" class="nav-link">订单</router-link>
        <router-link v-if="auth.currentUser?.role === 'ADMIN'" to="/admin" class="nav-link">管理</router-link>
        <router-link to="/profile" class="nav-link">{{ auth.currentUser?.username }}</router-link>
      </template>
      <template v-else>
        <router-link to="/login" class="btn-primary btn-sm">登录</router-link>
      </template>
    </div>
  </header>
</template>
<script setup>
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
</script>
<style scoped>
.navbar { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: var(--card); border-bottom: 1px solid var(--border); position: sticky; top: 0; z-index: 100; }
.logo { font-size: 18px; font-weight: bold; color: var(--primary); }
.nav-right { display: flex; align-items: center; gap: 12px; }
.nav-link { color: var(--text); }
</style>
