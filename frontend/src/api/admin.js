import client from './client'

export const adminAPI = {
  getDashboard() { return client.get('/admin/dashboard') },
  listUsers(params) { return client.get('/admin/users', { params }) },
  banUser(id, data) { return client.put(`/admin/users/${id}/ban`, data || {}) },
  unbanUser(id) { return client.put(`/admin/users/${id}/unban`) },
  reviewCert(id, data) { return client.put(`/admin/certifications/${id}/review`, data) },
  resolveArb(id, data) { return client.put(`/admin/arbitrations/${id}/resolve`, data) },
}
