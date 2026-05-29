import c from './client'
export const certAPI = { submit: d => c.post('/certifications',d), mine: () => c.get('/certifications/me'), pending: () => c.get('/certifications/pending'), review: (id,d) => c.put(`/certifications/${id}/review`,d) }
