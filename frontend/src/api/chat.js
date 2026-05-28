import c from './client'
export const chatAPI = { create: d => c.post('/conversations',d), list: () => c.get('/conversations'), send: (id,d) => c.post(`/conversations/${id}/messages`,d), messages: (id,p) => c.get(`/conversations/${id}/messages`,{params:p}), read: id => c.put(`/conversations/${id}/read`) }
