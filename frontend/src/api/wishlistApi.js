import http from './http'

// UC2.3.1 / UC2.3.2 / UC2.3.3
export default {
  list: () => http.get('/wishlist'),
  add: (productId) => http.post(`/wishlist/${productId}`),
  remove: (productId) => http.delete(`/wishlist/${productId}`),
}
