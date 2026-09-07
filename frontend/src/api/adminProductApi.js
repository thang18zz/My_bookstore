import http from './http'

// Theme 8 - UC8.1.1-7
export default {
  list: (params) => http.get('/admin/products', { params }),
  detail: (id) => http.get(`/admin/products/${id}`),
  create: (payload) => http.post('/admin/products', payload),
  update: (id, payload) => http.put(`/admin/products/${id}`, payload),
  updatePrice: (id, price) => http.patch(`/admin/products/${id}/price`, null, { params: { price } }),
  remove: (id) => http.delete(`/admin/products/${id}`),
}
