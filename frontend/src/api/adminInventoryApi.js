import http from './http'

// Theme 11 - UC11.1.1-5
export default {
  list: (params) => http.get('/admin/inventory', { params }),
  get: (productId) => http.get(`/admin/inventory/${productId}`),
  setQuantity: (productId, quantity) => http.patch(`/admin/inventory/${productId}`, null, { params: { quantity } }),
  lowStock: (params) => http.get('/admin/inventory/low-stock', { params }),
  restock: (productId, addedQuantity) =>
    http.patch(`/admin/inventory/${productId}/restock`, null, { params: { addedQuantity } }),
}
