import client from './client'

export const userAPI = {
  getMyProfile() { return client.get('/users/me') },
  updateProfile(data) { return client.put('/users/me', data) },
  getPublicInfo(id) { return client.get(`/users/${id}`) },
  getCreditRecords() { return client.get('/users/me/credit-records') },
}
