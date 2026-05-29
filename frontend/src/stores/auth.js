import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/api/auth'
import { userAPI } from '@/api/users'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token')||'')
  const user = ref(null)
  const isLoggedIn = computed(() => !!token.value && !!user.value)

  async function login(username, password) {
    const res = await authAPI.login({ username, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    user.value = res.data.user
  }

  async function register(data) { await authAPI.register(data) }

  async function fetchUser() {
    if (!token.value) return
    try { const r = await userAPI.me(); user.value = r.data } catch { logout() }
  }

  async function updateProfile(data) {
    const r = await userAPI.update(data)
    user.value = r.data
  }

  function logout() { token.value=''; user.value=null; localStorage.removeItem('token') }

  async function restoreSession() {
    const t = localStorage.getItem('token')
    if (t) { token.value = t; await fetchUser() }
  }

  return { token, user, isLoggedIn, login, register, fetchUser, updateProfile, logout, restoreSession }
})
