import c from './client'
export const reviewAPI = { submit: d => c.post('/reviews',d), byUser: uid => c.get(`/reviews/user/${uid}`), byOrder: oid => c.get(`/reviews/order/${oid}`) }
