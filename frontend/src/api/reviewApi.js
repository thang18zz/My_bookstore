import http from './http'

// Epic 7.1
export default {
  forProduct: (productId, params) => http.get(`/reviews/product/${productId}`, { params }),
  add: (payload) => http.post('/reviews', payload),
  edit: (id, payload) => http.put(`/reviews/${id}`, payload),
  remove: (id) => http.delete(`/reviews/${id}`),
}
