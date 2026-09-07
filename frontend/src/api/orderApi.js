import http from './http'

// Theme 4 (checkout) + Epic 5.1 (customer order history)
export default {
  preview: (addressId) => http.get('/orders/preview', { params: { addressId } }),
  checkout: (addressId) => http.post('/orders/checkout', { addressId }),
  myOrders: () => http.get('/orders'),
  detail: (id) => http.get(`/orders/${id}`),
  cancel: (id) => http.post(`/orders/${id}/cancel`),
}
