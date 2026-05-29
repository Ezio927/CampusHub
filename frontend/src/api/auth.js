import c from './client'
export const authAPI = { login: d => c.post('/auth/login',d), register: d => c.post('/auth/register',d), logout: () => c.post('/auth/logout') }
