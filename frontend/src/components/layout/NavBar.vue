<template>
  <header class="navbar">
    <div class="nav-inner">
      <router-link to="/" class="logo">🏫 CampusHub</router-link>
      <nav class="nav-links">
        <template v-if="auth.isLoggedIn">
          <router-link to="/demands/publish" class="btn-primary btn-sm">+ 发布</router-link>
          <router-link v-if="auth.currentUser?.role === 'ADMIN'" to="/admin" class="nav-link">管理</router-link>
          <router-link to="/profile" class="avatar avatar-sm" :style="{background:avatarColor}">{{ auth.currentUser?.username?.[0]?.toUpperCase() }}</router-link>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-outline btn-sm">登录</router-link>
          <router-link to="/register" class="btn-primary btn-sm">注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>
<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
const avatarColor = computed(() => {
  const colors = ['#6366f1','#8b5cf6','#ec4899','#f43f5e','#f97316']
  return colors[(auth.currentUser?.username?.charCodeAt(0)||0) % colors.length]
})
</script>
<style scoped>@import './NavBar.css';</style>
