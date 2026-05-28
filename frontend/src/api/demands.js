import client from './client'

export const demandAPI = {
  create(data) { return client.post('/demands', data) },
  search(params) { return client.get('/demands', { params }) },
  getDetail(id) { return client.get(`/demands/${id}`) },
  update(id, data) { return client.put(`/demands/${id}`, data) },
  cancel(id) { return client.put(`/demands/${id}/cancel`) },
}
