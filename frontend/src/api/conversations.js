import client from './client'

export const chatAPI = {
  getOrCreate(data) { return client.post('/conversations', data) },
  list() { return client.get('/conversations') },
  sendMessage(id, data) { return client.post(`/conversations/${id}/messages`, data) },
  getMessages(id, params) { return client.get(`/conversations/${id}/messages`, { params }) },
  markRead(id) { return client.put(`/conversations/${id}/read`) },
}
