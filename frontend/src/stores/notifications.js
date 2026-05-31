import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notifAPI } from '@/api/notifications'

export const useNotifStore = defineStore('notifications', () => {
  const unread = ref(0)
  const list = ref([])

  async function fetchUnread() {
    try {
      const r = await notifAPI.unread()
      unread.value = r.data?.unreadCount || 0
    } catch {
      unread.value = 0
    }
  }

  async function fetchList() { const r = await notifAPI.list({ page:0, size:20 }); list.value = r.data || [] }

  async function markRead(id) { await notifAPI.read(id); await fetchUnread() }
  async function markAllRead() { await notifAPI.readAll(); unread.value = 0 }

  return { unread, list, fetchUnread, fetchList, markRead, markAllRead }
})
