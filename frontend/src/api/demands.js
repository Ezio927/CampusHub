import c from './client'
export const demandAPI = { create: d => c.post('/demands',d), search: p => c.get('/demands',{params:p}), detail: id => c.get(`/demands/${id}`), update: (id,d) => c.put(`/demands/${id}`,d), cancel: id => c.put(`/demands/${id}/cancel`) }
