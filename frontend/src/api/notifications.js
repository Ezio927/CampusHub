import c from './client'
export const notifAPI = { list: p => c.get('/notifications',{params:p}), unread: () => c.get('/notifications/unread-count'), read: id => c.put(`/notifications/${id}/read`), readAll: () => c.put('/notifications/read-all') }
