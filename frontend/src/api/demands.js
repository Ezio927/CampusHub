import c from './client'
export const demandAPI = { create: d => c.post('/demands',d), search: p => c.get('/demands',{params:p}), my: () => c.get('/demands',{params:{my:true}}), detail: id => c.get(`/demands/${id}`), update: (id,d) => c.put(`/demands/${id}`,d), cancel: id => c.put(`/demands/${id}/cancel`) }
