import c from './client'
export const userAPI = { me: () => c.get('/users/me'), update: d => c.put('/users/me',d), public: id => c.get(`/users/${id}`), creditRecords: () => c.get('/users/me/credit-records') }
