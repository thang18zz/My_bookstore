import http from './http'

// Epic 6.1
export default {
  list: () => http.get('/addresses'),
  add: (payload) => http.post('/addresses', payload),
  update: (id, payload) => http.put(`/addresses/${id}`, payload),
  remove: (id) => http.delete(`/addresses/${id}`),
  setDefault: (id) => http.patch(`/addresses/${id}/default`),
}
