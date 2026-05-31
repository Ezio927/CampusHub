import c from './client'
export function uploadImage(file) {
  const fd = new FormData()
  fd.append('file', file)
  return c.post('/upload/image', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000
  })
}
