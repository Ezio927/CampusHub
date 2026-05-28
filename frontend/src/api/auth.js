import client from './client'

export const authAPI = {
  register(data) { return client.post('/auth/register', data) },
  login(data) { return client.post('/auth/login', data) },
  logout() { return client.post('/auth/logout') },
}
