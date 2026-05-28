import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/api/auth'
import { userAPI } from '@/api/users'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const currentUser = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await authAPI.login({ username, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    currentUser.value = res.data.user
    return res
  }

  async function register(data) {
    await authAPI.register(data)
  }

  async function fetchProfile() {
    if (!token.value) return
    try {
      const res = await userAPI.getMyProfile()
      currentUser.value = res.data
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = ''
    currentUser.value = null
    localStorage.removeItem('token')
  }

  function restoreSession() {
    const saved = localStorage.getItem('token')
    if (saved) {
      token.value = saved
      fetchProfile()
    }
  }

  return { token, currentUser, isLoggedIn, login, register, fetchProfile, logout, restoreSession }
})
