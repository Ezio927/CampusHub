import client from './client'

export const orderAPI = {
  accept(data) { return client.post('/orders', data) },
  list(params) { return client.get('/orders', { params }) },
  getDetail(id) { return client.get(`/orders/${id}`) },
  deliver(id) { return client.put(`/orders/${id}/deliver`) },
  confirm(id) { return client.put(`/orders/${id}/confirm`) },
  cancel(id, data) { return client.put(`/orders/${id}/cancel`, data) },
}
