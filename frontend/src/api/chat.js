import c from './client'
export const chatAPI = {
  create: d => c.post('/conversations', d),
  list: () => c.get('/conversations'),
  detail: id => c.get(`/conversations/${id}`),
  unread: () => c.get('/conversations/unread-count'),
  send: (id, d) => c.post(`/conversations/${id}/messages`, d),
  messages: (id, p) => c.get(`/conversations/${id}/messages`, { params: p }),
  search: (id, keyword) => c.get(`/conversations/${id}/messages/search`, { params: { keyword } }),
  read: (id, lastReadMessageId) => c.put(`/conversations/${id}/read`, null, { params: { lastReadMessageId } }),
  block: id => c.put(`/conversations/${id}/block`),
  unblock: id => c.put(`/conversations/${id}/unblock`),
  recall: (id, messageId) => c.put(`/conversations/${id}/messages/${messageId}/recall`),
  report: (id, messageId, d) => c.post(`/conversations/${id}/messages/${messageId}/reports`, d),
}
