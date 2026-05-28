import client from './client'

export const arbAPI = {
  create(data) { return client.post('/arbitrations', data) },
  listPending() { return client.get('/arbitrations') },
  resolve(id, data) { return client.put(`/arbitrations/${id}/resolve`, data) },
}
