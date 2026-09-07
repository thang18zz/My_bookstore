import http from './http'

// UC13.1.1 / UC13.1.2
export default {
  info: () => http.get('/contact/info'),
  submit: (payload) => http.post('/contact', payload),
}
