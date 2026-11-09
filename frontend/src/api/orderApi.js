import http from './http'

// Theme 4 (checkout) + Epic 5.1 (customer order history)
export default {
  preview: (addressId) => http.get('/orders/preview', { params: { addressId } }),
  checkout: (dataOrAddressId, paymentMethod = 'OFFLINE') => {
    if (typeof dataOrAddressId === 'object' && dataOrAddressId !== null) {
      return http.post('/orders/checkout', dataOrAddressId)
    }
    return http.post('/orders/checkout', { addressId: dataOrAddressId, paymentMethod })
  },
  myOrders: () => http.get('/orders'),
  detail: (id) => http.get(`/orders/${id}`),
  cancel: (id) => http.post(`/orders/${id}/cancel`),
}
