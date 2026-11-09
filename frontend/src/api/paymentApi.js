import http from './http'

export default {
  create: (orderId) => http.post(`/payments/${orderId}/create`),
  status: (orderId) => http.get(`/payments/${orderId}/status`)
}

