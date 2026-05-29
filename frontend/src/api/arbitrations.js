import c from './client'
export const arbAPI = { create: d => c.post('/arbitrations',d), pending: () => c.get('/arbitrations'), resolve: (id,d) => c.put(`/arbitrations/${id}/resolve`,d) }
