import http from './http'

// Epic 3.1 / 3.2
export default {
  view: () => http.get('/cart'),
  addItem: (payload) => http.post('/cart/items', payload),
  updateItem: (productId, quantity) => http.put(`/cart/items/${productId}`, { quantity }),
  removeItem: (productId) => http.delete(`/cart/items/${productId}`),
  clear: () => http.delete('/cart'),
}
