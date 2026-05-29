import axios from 'axios'

const client = axios.create({ baseURL: '/api', timeout: 10000 })
client.interceptors.request.use(c => {
  const t = localStorage.getItem('token')
  if (t) c.headers.Authorization = `Bearer ${t}`
  return c
})
client.interceptors.response.use(r => r.data, e => {
  const d = e.response?.data
  if (e.response?.status === 401) { localStorage.removeItem('token'); window.location.href = '/login' }
  return Promise.reject(d || { code: 0, message: '网络错误' })
})
export default client
