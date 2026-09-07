import http from './http'

// UC1.1.1 / UC1.1.2 / UC1.1.3 / UC1.2.1 / UC1.2.2
export default {
  register: (payload) => http.post('/auth/register', payload),
  login: (payload) => http.post('/auth/login', payload),
  logout: () => http.post('/auth/logout'),
  me: () => http.get('/auth/me'),
  updateMe: (payload) => http.put('/auth/me', payload),
}
