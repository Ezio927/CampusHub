import client from './client'

export const notifAPI = {
  list(params) { return client.get('/notifications', { params }) },
  getUnreadCount() { return client.get('/notifications/unread-count') },
  markRead(id) { return client.put(`/notifications/${id}/read`) },
  markAllRead() { return client.put('/notifications/read-all') },
}
