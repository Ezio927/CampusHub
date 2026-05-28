import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notifAPI } from '@/api/notifications'

export const useNotifStore = defineStore('notifications', () => {
  const unreadCount = ref(0)
  const notifications = ref([])

  async function fetchUnreadCount() {
    try {
      const res = await notifAPI.getUnreadCount()
      unreadCount.value = res.data?.unreadCount || 0
    } catch { /* ignore */ }
  }

  async function fetchNotifications(page = 0) {
    const res = await notifAPI.list({ page, size: 20 })
    notifications.value = res.data || []
    return res
  }

  async function markRead(id) {
    await notifAPI.markRead(id)
    await fetchUnreadCount()
  }

  async function markAllRead() {
    await notifAPI.markAllRead()
    unreadCount.value = 0
  }

  return { unreadCount, notifications, fetchUnreadCount, fetchNotifications, markRead, markAllRead }
})
