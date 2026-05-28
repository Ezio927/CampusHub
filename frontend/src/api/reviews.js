import client from './client'

export const reviewAPI = {
  submit(data) { return client.post('/reviews', data) },
  listByUser(userId) { return client.get(`/reviews/user/${userId}`) },
  listByOrder(orderId) { return client.get(`/reviews/order/${orderId}`) },
}
