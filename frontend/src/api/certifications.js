import client from './client'

export const certAPI = {
  submit(data) { return client.post('/certifications', data) },
  getMine() { return client.get('/certifications/me') },
  review(id, data) { return client.put(`/certifications/${id}/review`, data) },
}
