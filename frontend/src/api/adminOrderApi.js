import http from './http'

// Epic 5.2 - UC5.2.1-5
export default {
  list: (params) => http.get('/admin/orders', { params }),
  detail: (id) => http.get(`/admin/orders/${id}`),
  updateStatus: (id, status) => http.patch(`/admin/orders/${id}/status`, { status }),
}
