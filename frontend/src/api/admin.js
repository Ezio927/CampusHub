import c from './client'

export const adminAPI = {
  dashboard: () => c.get('/admin/dashboard'),
  users: p => c.get('/admin/users', { params: p }),
  orders: p => c.get('/admin/orders', { params: p }),
  deleteOrder: id => c.delete(`/admin/orders/${id}`),
  deleteDemand: id => c.delete(`/admin/demands/${id}`),
  ban: (id, d) => c.put(`/admin/users/${id}/ban`, d || {}),
  unban: id => c.put(`/admin/users/${id}/unban`),
  adjustCredit: (id, d) => c.put(`/admin/users/${id}/credit`, d),
  adjustQuota: (id, d) => c.put(`/admin/users/${id}/quota`, d),
  reviewCert: (id, d) => c.put(`/admin/certifications/${id}/review`, d),
  resolveArb: (id, d) => c.put(`/admin/arbitrations/${id}/resolve`, d),
  messageReports: () => c.get('/admin/message-reports'),
  reviewMessageReport: (id, d) => c.put(`/admin/message-reports/${id}/review`, d),
  creditAppeals: () => c.get('/admin/credit-appeals'),
  reviewCreditAppeal: (id, approved) => c.put(`/admin/credit-appeals/${id}/review`, { approved }),
  sendNotification: d => c.post('/admin/notifications/send', d)
}
