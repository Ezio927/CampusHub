import c from './client'
export const orderAPI = { accept: d => c.post('/orders',d), list: p => c.get('/orders',{params:p}), detail: id => c.get(`/orders/${id}`), deliver: id => c.put(`/orders/${id}/deliver`), confirm: id => c.put(`/orders/${id}/confirm`), cancel: (id,d) => c.put(`/orders/${id}/cancel`,d) }
