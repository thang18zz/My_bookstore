import http from './http'

// Theme 10 - UC10.1.1-6
export default {
  list: (params) => http.get('/admin/customers', { params }),
  detail: (id) => http.get(`/admin/customers/${id}`),
  create: (payload) => http.post('/admin/customers', payload),
  update: (id, payload) => http.put(`/admin/customers/${id}`, payload),
  remove: (id) => http.delete(`/admin/customers/${id}`),
}
