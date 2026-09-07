import http from './http'

// Theme 9 - UC9.1.1-5
export default {
  list: () => http.get('/admin/categories'),
  detail: (id) => http.get(`/admin/categories/${id}`),
  create: (payload) => http.post('/admin/categories', payload),
  update: (id, payload) => http.put(`/admin/categories/${id}`, payload),
  remove: (id) => http.delete(`/admin/categories/${id}`),
}
