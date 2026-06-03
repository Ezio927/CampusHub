import c from './client'

export const userAPI = {
  me: () => c.get('/users/me'),
  update: d => c.put('/users/me', d),
  public: id => c.get(`/users/${id}`),
  creditRecords: () => c.get('/users/me/credit-records'),
  quota: () => c.get('/users/me/quota'),
  quotaRecords: () => c.get('/users/me/quota-records'),
  checkIn: () => c.post('/users/me/quota/check-in'),
  creditAppeal: reason => c.post('/users/me/credit-appeal', { reason }),
  myAppeal: () => c.get('/users/me/credit-appeal'),
  banAppeal: reason => c.post('/users/me/ban-appeal', { reason }),
  myBanAppeal: () => c.get('/users/me/ban-appeal')
}
